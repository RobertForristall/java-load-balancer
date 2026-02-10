package com.rforristall.java.load.balancer.config;

import java.util.List;

import com.rforristall.java.load.balancer.backend.Backend;
import com.rforristall.java.load.balancer.strategy.BalancingStrategy;

public interface LoadBalancerServerConfig {
  
  /**
   * Get the port that the load balancer will be listening for requests on
   * @return the port number for the load balancer
   */
  public int getPort();
  
  /**
   * Get the backends that the server will be forwarding requests to
   * @return {@link List}<{@link Backend}> that the server will select from to forward to
   */
  public List<Backend> getBackends();
  
  /**
   * Get the balancing strategy used to determine which backend server will be selected
   * @return {@link BalancingStrategy} that will be used for selecting the server that each request will be sent to
   */
  public BalancingStrategy getStrategy();
  
  /**
   * Get the interval in milliseconds that the load balancing server will check the health of servers
   * @return milliseconds between each set of health checks
   */
  public int getHealthCheckInterval();
  
  /**
   * Get the interval in milliseconds that the load balancing server will wait between requests for timeouts
   * @return milliseconds the server will wait for a response to a request
   */
  public int getRequestTimeout();
  
  /**
   * Get the number of retries that the server will attempt to make in order to serve a received request with a single backend server
   * @return the number of times the server will try to make requests to a backend before marking it unhealthy and switching to a different one
   */
  public int getMaxRetries();

}
