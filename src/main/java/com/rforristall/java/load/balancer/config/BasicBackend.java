package com.rforristall.java.load.balancer.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class BasicBackend extends AbstractBackend{
  
  private static final int MAX_FAILURES = 3;

  protected BasicBackend(String url, String healthCheckPath) {
    super(url, healthCheckPath);
  }

  @Override
  public void runHealthCheck(HttpClient httpClient) {
    try {
      String healthUrl = getFullHealthCheckUrl();
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(healthUrl))
              .timeout(Duration.ofSeconds(3))
              .GET()
              .build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        if (!isHealthy()) {
          //TODO: Log backend has recovered
        }
        setHealthy(true);
        resetConsecutiveFailures();
      } else {
        markUnhealthy();
      }
    } catch (Exception ex) {
      
    }
  }
  
  private void markUnhealthy() {
    int failures = incrementConsecutiveFailures();
    if (failures >= MAX_FAILURES && isHealthy()) {
        setHealthy(false);
    }
}

}
