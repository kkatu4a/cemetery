package com.katu4a.cladbise.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsData {
    private int totalRequests;
    private int newRequests;
    private int processingRequests;
    private int completedRequests;
    private double totalRevenue;

    private int totalPlots;
    private int freePlots;
    private int occupiedPlots;

    private final Map<String, Integer> requestStatusCounts = new LinkedHashMap<>();
    private final Map<String, Integer> plotStatusCounts = new LinkedHashMap<>();

    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

    public int getNewRequests() { return newRequests; }
    public void setNewRequests(int newRequests) { this.newRequests = newRequests; }

    public int getProcessingRequests() { return processingRequests; }
    public void setProcessingRequests(int processingRequests) { this.processingRequests = processingRequests; }

    public int getCompletedRequests() { return completedRequests; }
    public void setCompletedRequests(int completedRequests) { this.completedRequests = completedRequests; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalPlots() { return totalPlots; }
    public void setTotalPlots(int totalPlots) { this.totalPlots = totalPlots; }

    public int getFreePlots() { return freePlots; }
    public void setFreePlots(int freePlots) { this.freePlots = freePlots; }

    public int getOccupiedPlots() { return occupiedPlots; }
    public void setOccupiedPlots(int occupiedPlots) { this.occupiedPlots = occupiedPlots; }

    public Map<String, Integer> getRequestStatusCounts() { return requestStatusCounts; }
    public Map<String, Integer> getPlotStatusCounts() { return plotStatusCounts; }
}