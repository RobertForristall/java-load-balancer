package com.rforristall.java.load.balancer.strategy;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.rforristall.java.load.balancer.config.Backend;

public class RoundRobinSelector extends AbstractBalancingStrategy{
  
  private final AtomicInteger currentIndex = new AtomicInteger(0);
  private final String name = BalancingStrategies.ROUND_ROBIN.getName();

  protected RoundRobinSelector(List<Backend> backends) {
    super(backends);
  }

  @Override
  public Backend selectBackend() {
    return selectBackend(Collections.emptyList());
  }

  @Override
  public Backend selectBackend(List<Backend> excludedBackends) {
    List<Backend> backendOptions = filterBackends(excludedBackends);
    if (backendOptions.isEmpty()) {
      return null;
    }
    int index = Math.abs(currentIndex.getAndIncrement()) % backendOptions.size();
    return backendOptions.get(index);
  }

}
