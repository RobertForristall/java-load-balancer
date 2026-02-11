package com.rforristall.java.load.balancer.health;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.rforristall.java.load.balancer.backend.Backend;

public class HealthChecker {
  
  private final List<Backend> backends;
  private final HttpClient httpClient;
  private final int intervalMs;
  private final ScheduledExecutorService scheduler;
  private volatile boolean running = false;
  
  public HealthChecker(List<Backend> backends, HttpClient httpClient, int intervalMs) {
      this.backends = backends;
      this.httpClient = httpClient;
      this.intervalMs = intervalMs;
      this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
          Thread t = new Thread(r, "HealthChecker");
          t.setDaemon(true);
          return t;
      });
  }
  
  public void start() {
      running = true;
      scheduler.scheduleAtFixedRate(
          this::checkAllBackends,
          0,
          intervalMs,
          TimeUnit.MILLISECONDS
      );
  }
  
  public void stop() {
      running = false;
      scheduler.shutdown();
      try {
          if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
              scheduler.shutdownNow();
          }
      } catch (InterruptedException e) {
          scheduler.shutdownNow();
      }
  }
  
  private void checkAllBackends() {
      for (Backend backend : backends) {
          backend.runHealthCheck(httpClient);
      }
  }

}
