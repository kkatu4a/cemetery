package com.katu4a.cladbise.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.katu4a.cladbise.util.Role;
import com.katu4a.cladbise.util.PlotStatus;
import com.katu4a.cladbise.util.RequestStatus;

/** Wire contracts match the backend DTOs; archive status is separate from business status. */
public final class ApiModels {
    private ApiModels() {}
    public enum Status { ACTIVE, ARCHIVE }
    public record Customer(UUID id, Status status, String fullName, String phone,
                           String email, LocalDateTime createdAt) {}
    public record CustomerInput(String fullName, String phone, String email) {}
    public record Employee(UUID id, Status status, String fullName, Role role,
                           String login, boolean active, LocalDateTime createdAt) {}
    public record EmployeeInput(String fullName, Role role, String login,
                                String passwordHash, boolean active) {}
    public record Sector(UUID id, Status status, String name, LocalDateTime createdAt) {}
    public record Plot(UUID id, Status status, UUID sectorId, int rowNumber, int plotNumber,
                       PlotStatus plotStatus, int lengthCm, int widthCm, String coordinates) {}
    public record PlotInput(UUID sectorId, int rowNumber, int plotNumber, PlotStatus plotStatus,
                            int lengthCm, int widthCm, String coordinates) {}
    public record Request(UUID id, Status status, UUID customerId, UUID plotId, UUID employeeId,
                          String deceasedFullName, LocalDate deceasedBirthday, LocalDate deceasedDeathday,
                          String deceasedCertificate, RequestStatus requestStatus, LocalDateTime requestDate,
                          LocalDateTime processedDate, BigDecimal totalCost, String note, LocalDateTime createdAt) {}
    public record RequestInput(UUID customerId, UUID plotId, UUID employeeId,
                               String deceasedFullName, LocalDate deceasedBirthday, LocalDate deceasedDeathday,
                               String deceasedCertificate, RequestStatus requestStatus, LocalDateTime requestDate,
                               LocalDateTime processedDate, BigDecimal totalCost, String note) {}
}
