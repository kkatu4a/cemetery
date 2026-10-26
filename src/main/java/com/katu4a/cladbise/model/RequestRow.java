package com.katu4a.cladbise.model;
import java.util.UUID;
import java.math.BigDecimal;

import com.katu4a.cladbise.util.RequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestRow {
    private UUID id;
    private UUID customerId;
    private UUID employeeId;
    private UUID plotId;

    private String customerFullName;
    private String employeeFullName;
    private String plotAddress;

    private String deceasedFullName;
    private LocalDate deceasedBirthday;
    private LocalDate deceasedDeathday;
    private String deceasedCertificate;

    private RequestStatus status;
    private String statusRussian;
    private BigDecimal totalCost;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime value) { requestDate = value; }
    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime value) { processedDate = value; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    public UUID getPlotId() { return plotId; }
    public void setPlotId(UUID plotId) { this.plotId = plotId; }

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

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}