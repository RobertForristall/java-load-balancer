package com.rforristall.java.load.balancer.strategy;

import java.util.List;

import com.rforristall.java.load.balancer.backend.Backend;

public class BalancingStrategyBuilder {
  
  public static BalancingStrategy createStrategy(BalancingStrategies strategy, List<Backend> backends) {
    switch(strategy) {
      case LEAST_CONNECTIONS:
        return new LeastConnectionsSelector(backends);
      case ROUND_ROBIN:
        return new RoundRobinSelector(backends);
      default:
        return null; 
    }
  }

}
