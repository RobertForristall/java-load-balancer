package com.rforristall.java.load.balancer.strategy;

import java.util.List;

import com.rforristall.java.load.balancer.backend.Backend;

public abstract class AbstractBalancingStrategy implements BalancingStrategy{
  
  private final List<Backend> backends;
  
  protected AbstractBalancingStrategy(List<Backend> backends) {
    this.backends = backends;
  }
  
  protected List<Backend> filterBackends(List<Backend> excludedBackends) {
    return backends.stream().filter(b -> b.isHealthy() && !excludedBackends.contains(b)).toList();
  }

}
