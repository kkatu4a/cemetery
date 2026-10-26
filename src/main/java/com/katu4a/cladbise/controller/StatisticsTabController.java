package com.katu4a.cladbise.controller;
import com.katu4a.cladbise.util.BackendProvider;

import com.katu4a.cladbise.model.StatisticsData;
import com.katu4a.cladbise.service.RequestService;
import com.katu4a.cladbise.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.Map;

public class StatisticsTabController {

    @FXML private Label lblTotalRequests;
    @FXML private Label lblNewRequests;
    @FXML private Label lblCompletedRequests;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalPlots;
    @FXML private Label lblFreePlots;

    @FXML private PieChart pieChartRequests;
    @FXML private PieChart pieChartPlots;

    private final RequestService requestService = BackendProvider.getRequestService();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    @FXML
    public void loadStatistics() {
        try {
            StatisticsData stats = requestService.getStatistics();

            lblTotalRequests.setText(String.valueOf(stats.getTotalRequests()));
            lblNewRequests.setText(String.valueOf(stats.getNewRequests()));
            lblCompletedRequests.setText(String.valueOf(stats.getCompletedRequests()));
            lblTotalRevenue.setText(String.format("%,.2f ₽", stats.getTotalRevenue()));
            lblTotalPlots.setText(String.valueOf(stats.getTotalPlots()));
            lblFreePlots.setText(String.valueOf(stats.getFreePlots()));

            ObservableList<PieChart.Data> reqChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : stats.getRequestStatusCounts().entrySet()) {
                if (entry.getValue() > 0) {
                    reqChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            pieChartRequests.setData(reqChartData);

            ObservableList<PieChart.Data> plotChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : stats.getPlotStatusCounts().entrySet()) {
                if (entry.getValue() > 0) {
                    plotChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            pieChartPlots.setData(plotChartData);

        } catch (Exception e) {
            AlertHelper.showError("Ошибка аналитики", e.getMessage());
        }
    }
}