package com.rforristall.java.load.balancer.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/**
 * Basic backend abstraction that the load balancer can use to forward requests
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
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
  
  /**
   * Helper function for marking a backend as unhealthy if it surpasses its max failure limit
   */
  private void markUnhealthy() {
    int failures = incrementConsecutiveFailures();
    if (failures >= MAX_FAILURES && isHealthy()) {
        setHealthy(false);
    }
}

}
