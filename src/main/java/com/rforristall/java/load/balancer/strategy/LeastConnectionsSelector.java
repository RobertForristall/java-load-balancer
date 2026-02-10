package com.rforristall.java.load.balancer.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.rforristall.java.load.balancer.config.Backend;

public class LeastConnectionsSelector extends AbstractBalancingStrategy{

  private final String name = BalancingStrategies.LEAST_CONNECTIONS.getName();
  
  protected LeastConnectionsSelector(List<Backend> backends) {
    super(backends);
  }

  @Override
  public Backend selectBackend() {
    return selectBackend(Collections.emptyList());
  }

  @Override
  public Backend selectBackend(List<Backend> excludedBackends) {
    List<Backend> backendOptions = filterBackends(excludedBackends);
    return backendOptions.stream().min(Comparator.comparingInt(Backend::getActiveConnections)).orElse(null);
  }

  
  
}
