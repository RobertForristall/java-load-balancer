package com.rforristall.java.load.balancer.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rforristall.java.load.balancer.backend.Backend;
import com.rforristall.java.load.balancer.config.LoadBalancerServerConfig;
import com.rforristall.java.load.balancer.health.HealthChecker;
import com.rforristall.java.load.balancer.metrics.MetricsCollector;
import com.rforristall.java.load.balancer.strategy.BalancingStrategy;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LoadBalancerServer {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerServer.class);
  
  private final LoadBalancerServerConfig config;
  private final HttpServer httpServer;
  private final HttpClient httpClient;
  private final BalancingStrategy balancingStrategy;
  private final HealthChecker healthChecker;
  private final MetricsCollector metricsCollector;
  private final ScheduledExecutorService metricsExecutor;
  
  public LoadBalancerServer(LoadBalancerServerConfig config) throws IOException {
    super();
    this.config = config;
    this.httpServer = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
    httpServer.createContext("/", new LoadBalancerHandler());
    httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    this.httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofMillis(5000))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    this.balancingStrategy = config.getStrategy();
    this.healthChecker = new HealthChecker(config.getBackends(), httpClient, config.getHealthCheckInterval());
    this.metricsCollector = new MetricsCollector();
    this.metricsExecutor = Executors.newSingleThreadScheduledExecutor();
  }
  
  public void start() {
    httpServer.start();
    healthChecker.start();
    
    metricsExecutor.scheduleAtFixedRate(this::logMetrics, 60, 60, TimeUnit.SECONDS);
  }
  
  public void stop() {
    httpServer.stop(5);
    healthChecker.stop();
    metricsExecutor.shutdown();
    
    try {
      if (!metricsExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
          metricsExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
        metricsExecutor.shutdownNow();
    }
      
    logMetrics();
  }
  
  private void logMetrics() {
    LOGGER.info(metricsCollector.toString());
    for (Backend backend : config.getBackends()) {
      LOGGER.info(backend.toString());
    }
  }
  
  private class LoadBalancerHandler implements HttpHandler{
    
    private final static String ERROR_MSG_TEMPLATE = "<html><body><h1>%d Error</h1><p>%s</p></body></html>";
    private final static String ERROR_MSG_CONTENT_TYPE_HEADER = "Content-Type";
    private final static String ERROR_MSG_CONTENT_TYPE_VALUE = "text/html; charset=UTF-8";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String clientAddress = exchange.getRemoteAddress().getAddress().getHostAddress();
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().toString();
      
      Backend backend = balancingStrategy.selectBackend();
      
      if (backend == null) {
        sendErrorResponse(exchange, 503, "No healthy backends available");
        metricsCollector.incrementNoBackendErrors();
        return;
      }
      
      boolean success = forwardRequestWithRetry(exchange, backend, clientAddress);
      
      if (success) {
        metricsCollector.incrementSuccessfulRequests();
      } else {
        metricsCollector.incrementFailedRequests();
      }
      
    }
    
    private boolean forwardRequestWithRetry(HttpExchange httpExchange, Backend initialBackend, String clientAddress) {
      Set<Backend> triedBackends = new HashSet<>();
      Backend backend = initialBackend;
      int attempts = 0;
      
      while(attempts <= config.getMaxRetries() && backend != null) {
        triedBackends.add(backend);
        attempts++;
        
        try {
          if (forwardRequest(httpExchange, backend)) return true;
        } catch (Exception ex) {
          backend.incrementTotalErrors();
          backend.incrementConsecutiveFailures();
        }
        
        if (attempts <= config.getMaxRetries()) {
          backend = balancingStrategy.selectBackend(triedBackends.stream().collect(Collectors.toList()));
        }
      }
      
      try {
        sendErrorResponse(httpExchange, 502, "All backends failed");
      } catch (IOException ex) {
        
      }
      
      return false;
    }
    
    private boolean forwardRequest(HttpExchange httpExchange, Backend backend) throws IOException, InterruptedException {
      backend.incrementActiveConnections();
      backend.incrementTotalRequests();
      long startTime = System.currentTimeMillis();
      
      try {
        String backendUrl = backend.getUrl() + httpExchange.getRequestURI().toString();
        byte[] requestBody = httpExchange.getRequestBody().readAllBytes();
        
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(backendUrl)).timeout(Duration.ofMillis(config.getRequestTimeout()));
        Headers requestHeaders = httpExchange.getRequestHeaders();
        for (Map.Entry<String, List<String>> entry: requestHeaders.entrySet()) {
          String headerName = entry.getKey();
          if (!headerName.equalsIgnoreCase("Host") && !headerName.equalsIgnoreCase("Connection")) {
            for (String value : entry.getValue()) {
              httpRequestBuilder.header(headerName, value);
            }
          }
        }
        HttpRequest.BodyPublisher bodyPublisher = requestBody.length > 0 ? BodyPublishers.ofByteArray(requestBody) : BodyPublishers.noBody();
        httpRequestBuilder.method(httpExchange.getRequestMethod(), bodyPublisher);
        HttpRequest httpRequest = httpRequestBuilder.build();
        
        HttpResponse<byte[]> response = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
        byte[] responseBody = response.body();
        httpExchange.sendResponseHeaders(response.statusCode(), responseBody.length);
        Headers exchangeResponseHeaders = httpExchange.getResponseHeaders();
        for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
          String headerName = entry.getKey();
          if (!headerName.equalsIgnoreCase("Transfer-Encoding") && !headerName.equalsIgnoreCase("Connection")) {
            exchangeResponseHeaders.put(headerName, entry.getValue());
          }
        }
        try (OutputStream os = httpExchange.getResponseBody()) {
          if (responseBody.length > 0) {
            os.write(responseBody);
          }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        metricsCollector.recordRequestDuration(duration);
        backend.resetConsecutiveFailures();
        return true;
      } finally {
        backend.decrementActiveConnections();
      }
    }

    private void sendErrorResponse(HttpExchange httpExchange, int statusCode, String message) throws IOException {
      String body = String.format(ERROR_MSG_TEMPLATE, statusCode, message);
      byte[] responseBytes = body.getBytes();
      httpExchange.getResponseHeaders().set(ERROR_MSG_CONTENT_TYPE_HEADER, ERROR_MSG_CONTENT_TYPE_VALUE);
      httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
      try(OutputStream os = httpExchange.getResponseBody()) {
        os.write(responseBytes);
      }
    }

  }

}
