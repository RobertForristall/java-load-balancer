package com.rforristall.java.load.balancer.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rforristall.java.load.balancer.backend.Backend;
import com.rforristall.java.load.balancer.strategy.BalancingStrategies;
import com.rforristall.java.load.balancer.strategy.BalancingStrategy;
import com.rforristall.java.load.balancer.strategy.BalancingStrategyBuilder;

public class ImmutableLoadBalancerServerConfig implements LoadBalancerServerConfig{
  
  private final int port;
  private final List<Backend> backends;
  private final BalancingStrategy balancingStrategy;
  private final int healthCheckInterval;
  private final int requestTimeout;
  private final int maxRetries;
  
  private ImmutableLoadBalancerServerConfig(ImmutableLoadBalancerServerConfigBuilder builder) {
    this.port = builder.port;
    this.backends = Collections.unmodifiableList(new ArrayList<>(builder.backends));
    this.balancingStrategy = BalancingStrategyBuilder.createStrategy(builder.strategy, backends);
    this.healthCheckInterval = builder.healthCheckInterval;
    this.requestTimeout = builder.requestTimeout;
    this.maxRetries = builder.maxRetries;
  }
  
  @Override
  public int getPort() {
    return port;
  }

  @Override
  public List<Backend> getBackends() {
    return backends;
  }

  @Override
  public BalancingStrategy getStrategy() {
    return balancingStrategy;
  }

  @Override
  public int getHealthCheckInterval() {
    return healthCheckInterval;
  }

  @Override
  public int getRequestTimeout() {
    return requestTimeout;
  }

  @Override
  public int getMaxRetries() {
    return maxRetries;
  }
  
  public static class ImmutableLoadBalancerServerConfigBuilder {
    private int port = 8080;
    private List<Backend> backends = new ArrayList<>();
    private BalancingStrategies strategy = BalancingStrategies.ROUND_ROBIN;
    private int healthCheckInterval = 10000;
    private int requestTimeout = 30000;
    private int maxRetries = 2;
    
    public ImmutableLoadBalancerServerConfigBuilder port(int port) { this.port = port; return this; }
    public ImmutableLoadBalancerServerConfigBuilder backends(List<Backend> backends) { 
        this.backends = backends; 
        return this; 
    }
    public ImmutableLoadBalancerServerConfigBuilder strategy(BalancingStrategies strategy) { 
        this.strategy = strategy; 
        return this; 
    }
    public ImmutableLoadBalancerServerConfigBuilder healthCheckInterval(int ms) { 
        this.healthCheckInterval = ms; 
        return this; 
    }
    public ImmutableLoadBalancerServerConfigBuilder requestTimeout(int ms) { 
        this.requestTimeout = ms; 
        return this; 
    }
    public ImmutableLoadBalancerServerConfigBuilder maxRetries(int retries) { 
        this.maxRetries = retries; 
        return this; 
    }
    
    public LoadBalancerServerConfig build() {
        if (backends.isEmpty()) {
            throw new IllegalArgumentException("At least one backend required");
        }
        return new ImmutableLoadBalancerServerConfig(this);
    }
  }

}
