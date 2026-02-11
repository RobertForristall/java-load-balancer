package com.rforristall.java.load.balancer.metrics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

  private final AtomicLong successfulRequests = new AtomicLong(0);
  private final AtomicLong failedRequests = new AtomicLong(0);
  private final AtomicLong noBackendErrors = new AtomicLong(0);
  private final ConcurrentLinkedQueue<Long> requestDurations = new ConcurrentLinkedQueue<>();
  private static final int MAX_DURATION_SAMPLES = 1000;
  
  public MetricsCollector() {
    
  }
  
  public void incrementSuccessfulRequests() {
      successfulRequests.incrementAndGet();
  }
  
  public void incrementFailedRequests() {
      failedRequests.incrementAndGet();
  }
  
  public void incrementNoBackendErrors() {
      noBackendErrors.incrementAndGet();
  }
  
  public void recordRequestDuration(long durationMs) {
      requestDurations.offer(durationMs);
      while (requestDurations.size() > MAX_DURATION_SAMPLES) {
          requestDurations.poll();
      }
  }
  
  public long getSuccessfulRequests() {
      return successfulRequests.get();
  }
  
  public long getFailedRequests() {
      return failedRequests.get();
  }
  
  public long getTotalRequests() {
      return successfulRequests.get() + failedRequests.get();
  }
  
  public double getSuccessRate() {
      long total = getTotalRequests();
      return total > 0 ? (double) successfulRequests.get() / total * 100 : 0;
  }
  
  public double getAverageDuration() {
      if (requestDurations.isEmpty()) return 0;
      return requestDurations.stream()
          .mapToLong(Long::longValue)
          .average()
          .orElse(0);
  }
  
  @Override
  public String toString() {
      return String.format(
          "Total: %d, Success: %d (%.1f%%), Failed: %d, No Backend: %d, Avg Duration: %.1fms",
          getTotalRequests(), successfulRequests.get(), getSuccessRate(),
          failedRequests.get(), noBackendErrors.get(), getAverageDuration()
      );
  }
  
}
