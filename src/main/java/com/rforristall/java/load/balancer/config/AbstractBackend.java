package com.rforristall.java.load.balancer.config;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Backend server representation for managing if the connection is healthy and what its active
 * connection load currently is.
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public abstract class AbstractBackend implements Backend {
  
  private static final String DEFAULT_HEALTH_CHECK_PATH = "health";
  
  /**
   * URL of the backend server that the load balancer will use
   */
  private final String url;
  
  /**
   * Path of the backend server that hosts the health check
   */
  private final String healthCheckPath;
  
  /**
   * Boolean if the server is healthy, managed in an Atomic state to make it thread safe
   */
  private final AtomicBoolean healthy = new AtomicBoolean(true);

  /**
   * Number of currently active connections that the backend is handling from the load balancer
   */
  private final AtomicInteger activeConnections = new AtomicInteger(0);
  
  /**
   * Number of total requests that the backend has served since starting
   */
  private final AtomicLong totalRequests = new AtomicLong(0);
  
  /**
   * Number of total errors that the backend has returned since starting
   */
  private final AtomicLong totalErrors = new AtomicLong(0);
  
  /**
   * Number of consecutive failures that have occured making requests to the server
   */
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  
  /**
   * Instant on when the last health check of the server has been run, is set to volatile to make sure that the value is consistent across all threads
   */
  private volatile Instant lastHealthCheck;
  
  protected AbstractBackend(String url, String healthCheckPath) {
    this.url = url.endsWith("/") ? url.substring(0, url.length()-1) : url;
    this.healthCheckPath = healthCheckPath != null ? (healthCheckPath.startsWith("/") ? healthCheckPath.substring(1, healthCheckPath.length()) : healthCheckPath) : DEFAULT_HEALTH_CHECK_PATH;
  }

  @Override
  public String getUrl() {
    return url;
  }
  
  @Override
  public String getHealthCheckPath() {
    return healthCheckPath;
  }

  @Override
  public String getFullHealthCheckUrl() {
    return url + "/" + healthCheckPath;
  }

  @Override
  public boolean isHealthy() {
    return healthy.get();
  }
  
  @Override
  public void setHealthy(boolean healthStatus) {
    healthy.set(healthStatus);
  }

  @Override
  public int getActiveConnections() {
    return activeConnections.get();
  }

  @Override
  public void incrementActiveConnections() {
    activeConnections.incrementAndGet();
  }

  @Override
  public void decrementActiveConnections() {
    activeConnections.decrementAndGet();
  }

  @Override
  public long getTotalRequests() {
    return totalRequests.get();
  }

  @Override
  public void incrementTotalRequests() {
    totalRequests.get();
  }

  @Override
  public long getTotalErrors() {
    return totalErrors.get();
  }

  @Override
  public void incrementTotalErrors() {
    totalErrors.incrementAndGet();
  }

  @Override
  public int getConsecutiveFailures() {
    return consecutiveFailures.get();
  }

  @Override
  public void resetConsecutiveFailures() {
    consecutiveFailures.set(0);
  }

  @Override
  public int incrementConsecutiveFailures() {
    return consecutiveFailures.incrementAndGet();
  }

  @Override
  public Instant getLastHealthCheck() {
    return lastHealthCheck;
  }
  
}
