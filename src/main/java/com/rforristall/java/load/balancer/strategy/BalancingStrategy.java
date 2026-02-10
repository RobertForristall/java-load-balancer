package com.rforristall.java.load.balancer.strategy;

import java.util.List;

import com.rforristall.java.load.balancer.backend.Backend;

public interface BalancingStrategy {

  /**
   * Select a backend using the balancing strategy and no excluded backends
   * @return {@link Backend} selected to serve the next request
   */
  Backend selectBackend();
  
  /**
   * Select a backend using the balancing strategy while excluding a subset of the available backends
   * @param excludedBackends {@link List}<{@link Backend}> to exclude from the selection
   * @return {@link Backend} selected to serve the next request
   */
  Backend selectBackend(List<Backend> excludedBackends);
  
}
