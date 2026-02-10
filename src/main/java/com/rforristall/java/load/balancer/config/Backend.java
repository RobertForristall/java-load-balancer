package com.rforristall.java.load.balancer.config;

import java.net.http.HttpClient;
import java.time.Instant;

/**
 * Interface for backend servers that the load balancer will support
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public interface Backend {
  
  /**
   * Get the URL of the Backend server
   * @return root URL of the server
   */
  String getUrl();
  
  /**
   * Get the health check path of the backend server
   * @return path of the server that handles health checks
   */
  String getHealthCheckPath();
  
  /**
   * Get the joined url and path to perform a health check
   * @return joined url for the health check
   */
  String getFullHealthCheckUrl();
  
  /**
   * Check if the Backend is healthy based on the last health check
   * @return True if the backend is healthy and false otherwise
   */
  boolean isHealthy();
  
  /**
   * Set if the backend is healthy after running a health check
   * @param healthStatus true if the backend is healthy and false otherwise
   */
  void setHealthy(boolean healthStatus);
  
  /**
   * Run a health check on the server and set if its healthy or not
   * @param httpClient {@link HttpClient} to run the health check
   */
  void runHealthCheck(HttpClient httpClient);
  
  /**
   * Get the number of currently active connections the server is serving
   * @return The number of active connections
   */
  int getActiveConnections();
  
  /**
   * Increment the number of active connections atomically
   */
  void incrementActiveConnections();
  
  /**
   * Decrement the number of active connections atomically
   */
  void decrementActiveConnections();
  
  /**
   * Get the number of total requests that the backend has serviced since being added to the load balancer
   * @return Number of total requests served
   */
  long getTotalRequests();
  
  /**
   * Increment the number of requests served atomically
   */
  void incrementTotalRequests();
  
  /**
   * Get the number of total errors returned from the backend since being added to the load balancer
   * @return Number of total errors returned
   */
  long getTotalErrors();
  
  /**
   * Increment the number of total errors returned atomically
   */
  void incrementTotalErrors();
  
  /**
   * Get the number of consecutive failures that the server has returned to be used with retry counts
   * @return Number of consecutive failures returned by the server
   */
  int getConsecutiveFailures();
  
  /**
   * Reset the number of consecutive failures returned by the server atomically
   */
  void resetConsecutiveFailures();
  
  /**
   * Increment and return the number of consecutive failures returned by the server atomically
   * @return Number of consecutive failures returned by the server after incrementing
   */
  int incrementConsecutiveFailures();
  
  /**
   * Get the time of when the last health check was issued to the server
   * @return When the backend had its last health check
   */
  Instant getLastHealthCheck();
  
}
