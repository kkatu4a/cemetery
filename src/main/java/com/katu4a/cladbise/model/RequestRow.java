package com.katu4a.cladbise.model;

import com.arnor4eck.util.enums.RequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestRow {
    private int id;
    private int customerId;
    private int employeeId;
    private int plotId;

    private String customerFullName;
    private String employeeFullName;
    private String plotAddress;

    private String deceasedFullName;
    private LocalDate deceasedBirthday;
    private LocalDate deceasedDeathday;
    private String deceasedCertificate;

    private RequestStatus status;
    private String statusRussian;
    private Double totalCost;
    private String note;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getPlotId() { return plotId; }
    public void setPlotId(int plotId) { this.plotId = plotId; }

    public String getCustomerFullName() { return customerFullName; }
    public void setCustomerFullName(String customerFullName) { this.customerFullName = customerFullName; }

    public String getEmployeeFullName() { return employeeFullName; }
    public void setEmployeeFullName(String employeeFullName) { this.employeeFullName = employeeFullName; }

    public String getPlotAddress() { return plotAddress; }
    public void setPlotAddress(String plotAddress) { this.plotAddress = plotAddress; }

    public String getDeceasedFullName() { return deceasedFullName; }
    public void setDeceasedFullName(String deceasedFullName) { this.deceasedFullName = deceasedFullName; }

    public LocalDate getDeceasedBirthday() { return deceasedBirthday; }
    public void setDeceasedBirthday(LocalDate deceasedBirthday) { this.deceasedBirthday = deceasedBirthday; }

    public LocalDate getDeceasedDeathday() { return deceasedDeathday; }
    public void setDeceasedDeathday(LocalDate deceasedDeathday) { this.deceasedDeathday = deceasedDeathday; }

    public String getDeceasedCertificate() { return deceasedCertificate; }
    public void setDeceasedCertificate(String deceasedCertificate) { this.deceasedCertificate = deceasedCertificate; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) {
        this.status = status;
        this.statusRussian = status != null ? status.getValue() : "";
    }

    public String getStatusRussian() { return statusRussian; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}