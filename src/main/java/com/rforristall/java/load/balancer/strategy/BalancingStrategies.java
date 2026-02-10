package com.rforristall.java.load.balancer.strategy;

public enum BalancingStrategies {
  
  ROUND_ROBIN("Round Robin"),
  LEAST_CONNECTIONS("Least Connections");
  
  private String name;
  
  private BalancingStrategies(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

}
