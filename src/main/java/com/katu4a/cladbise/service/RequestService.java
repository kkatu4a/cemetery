package com.katu4a.cladbise.service;

import com.arnor4eck.model.*;
import com.arnor4eck.repository.*;
import com.arnor4eck.util.enums.PlotStatus;
import com.arnor4eck.util.enums.RequestStatus;
import com.arnor4eck.util.exceptions.CreateValueException;
import com.katu4a.cladbise.model.ComboItem;
import com.katu4a.cladbise.model.RequestRow;
import com.katu4a.cladbise.model.StatisticsData;
import com.katu4a.cladbise.util.BackendProvider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RequestService {

    private final RequestRepository requestRepo = BackendProvider.getRequestRepository();
    private final CustomerRepository customerRepo = BackendProvider.getCustomerRepository();
    private final EmployeeRepository employeeRepo = BackendProvider.getEmployeeRepository();
    private final PlotRepository plotRepo = BackendProvider.getPlotRepository();
    private final SectorRepository sectorRepo = BackendProvider.getSectorRepository();

    public List<RequestRow> findAll() throws SQLException {
        Map<Integer, Customer> customers = customerRepo.getAll().stream()
                .collect(Collectors.toMap(Customer::id, c -> c));
        Map<Integer, Employee> employees = employeeRepo.getAll().stream()
                .collect(Collectors.toMap(Employee::id, e -> e));
        Map<Integer, Sector> sectors = sectorRepo.getAll().stream()
                .collect(Collectors.toMap(Sector::id, s -> s));
        Map<Integer, Plot> plots = plotRepo.getAll().stream()
                .collect(Collectors.toMap(Plot::id, p -> p));

        List<RequestRow> result = new ArrayList<>();
        for (Request req : requestRepo.getAll()) {
            RequestRow row = new RequestRow();
            row.setId(req.id());
            row.setCustomerId(req.costumerId());
            row.setEmployeeId(req.employeeId());
            row.setPlotId(req.plotId());

            Customer c = customers.get(req.costumerId());
            row.setCustomerFullName(c != null ? c.fullName() : "ID: " + req.costumerId());

            Employee e = employees.get(req.employeeId());
            row.setEmployeeFullName(e != null ? e.fullName() : "Не назначен");

            Plot p = plots.get(req.plotId());
            if (p != null) {
                Sector s = sectors.get(p.sectorId());
                String sectorName = s != null ? s.name() : String.valueOf(p.sectorId());
                row.setPlotAddress(String.format("Сектор %s, Ряд %d, № %d", sectorName, p.rowNumber(), p.plotNumber()));
            } else {
                row.setPlotAddress("Участок ID: " + req.plotId());
            }

            row.setDeceasedFullName(req.deceasedFullName() != null ? req.deceasedFullName() : "— (Бронь)");
            row.setDeceasedBirthday(req.deceasedBirthday());
            row.setDeceasedDeathday(req.deceasedDeathday());
            row.setDeceasedCertificate(req.deceasedCertificate());
            row.setStatus(req.status());

            double cost = 0.0;
            if (req.totalCost() != null && !req.totalCost().isBlank()) {
                try {
                    cost = Double.parseDouble(req.totalCost().replace(',', '.'));
                } catch (NumberFormatException ignored) {}
            }
            row.setTotalCost(cost);
            row.setNote(req.note());
            row.setCreatedAt(req.createdAt());

            result.add(row);
        }
        result.sort(Comparator.comparingInt(RequestRow::getId).reversed());
        return result;
    }

    public void validate(RequestRow row) {
        if (row.getCustomerId() <= 0) {
            throw new CreateValueException("Выберите заявителя из списка!");
        }
        if (row.getPlotId() <= 0) {
            throw new CreateValueException("Выберите место захоронения!");
        }
        if (row.getEmployeeId() <= 0) {
            throw new CreateValueException("Выберите ответственного сотрудника!");
        }
        if (row.getDeceasedFullName() == null || row.getDeceasedFullName().isBlank()) {
            throw new CreateValueException("Введите ФИО умершего!");
        }
        if (row.getDeceasedBirthday() == null || row.getDeceasedDeathday() == null) {
            throw new CreateValueException("Укажите дату рождения и дату смерти!");
        }
        if (row.getDeceasedDeathday().isBefore(row.getDeceasedBirthday())) {
            throw new CreateValueException("Дата смерти не может быть раньше даты рождения!");
        }
        if (row.getTotalCost() == null || row.getTotalCost() < 0) {
            throw new CreateValueException("Стоимость не может быть отрицательной!");
        }
        if (row.getStatus() == null) {
            throw new CreateValueException("Выберите статус заявки!");
        }
    }

    public void create(RequestRow row) throws SQLException {
        validate(row);
        Request req = new Request(
                0,
                row.getCustomerId(),
                row.getEmployeeId(),
                row.getPlotId(),
                row.getDeceasedFullName(),
                row.getDeceasedBirthday(),
                row.getDeceasedDeathday(),
                row.getDeceasedCertificate(),
                row.getStatus(),
                String.format(Locale.US, "%.2f", row.getTotalCost()),
                row.getNote(),
                LocalDateTime.now()
        );
        requestRepo.save(req);
    }

    public void update(RequestRow row) throws SQLException {
        validate(row);
        Request req = new Request(
                row.getId(),
                row.getCustomerId(),
                row.getEmployeeId(),
                row.getPlotId(),
                row.getDeceasedFullName(),
                row.getDeceasedBirthday(),
                row.getDeceasedDeathday(),
                row.getDeceasedCertificate(),
                row.getStatus(),
                String.format(Locale.US, "%.2f", row.getTotalCost()),
                row.getNote(),
                row.getCreatedAt() != null ? row.getCreatedAt() : LocalDateTime.now()
        );
        requestRepo.update(req);
    }

    public void delete(int id) throws SQLException {
        requestRepo.delete(id);
    }

    public List<ComboItem> getCustomersForCombo() throws SQLException {
        return customerRepo.getAll().stream()
                .map(c -> new ComboItem(c.id(), c.fullName() + " (" + c.phone() + ")"))
                .toList();
    }

    public List<ComboItem> getEmployeesForCombo() throws SQLException {
        return employeeRepo.getAll().stream()
                .filter(Employee::isActive)
                .map(e -> new ComboItem(e.id(), e.fullName() + " [" + e.role().name() + "]"))
                .toList();
    }

    public List<ComboItem> getPlotsForCombo(Integer currentPlotId) throws SQLException {
        Map<Integer, String> sectorNames = sectorRepo.getAll().stream()
                .collect(Collectors.toMap(Sector::id, Sector::name));

        return plotRepo.getAll().stream()
                .filter(p -> p.status() == PlotStatus.FREE || (currentPlotId != null && p.id() == currentPlotId))
                .map(p -> {
                    String sName = sectorNames.getOrDefault(p.sectorId(), String.valueOf(p.sectorId()));
                    String title = String.format("Сектор %s, Ряд %d, № %d (%s)",
                            sName, p.rowNumber(), p.plotNumber(), p.status().getValue());
                    return new ComboItem(p.id(), title);
                })
                .toList();
    }

    public StatisticsData getStatistics() throws SQLException {
        StatisticsData stats = new StatisticsData();
        Collection<Request> allRequests = requestRepo.getAll();
        Collection<Plot> allPlots = plotRepo.getAll();

        // Статистика заявок (как в StatisticsOutputStrategy)
        for (RequestStatus status : RequestStatus.values()) {
            long count = allRequests.stream().filter(r -> r.status() == status).count();
            stats.getRequestStatusCounts().put(status.getValue(), (int) count);
        }

        double revenue = 0.0;
        for (Request r : allRequests) {
            if (r.totalCost() != null) {
                try {
                    revenue += Double.parseDouble(r.totalCost().replace(',', '.'));
                } catch (NumberFormatException ignored) {}
            }
        }

        stats.setTotalRequests(allRequests.size());
        stats.setNewRequests(stats.getRequestStatusCounts().getOrDefault(RequestStatus.NEW.getValue(), 0));
        stats.setProcessingRequests(stats.getRequestStatusCounts().getOrDefault(RequestStatus.PROCESSING.getValue(), 0));
        stats.setCompletedRequests(stats.getRequestStatusCounts().getOrDefault(RequestStatus.COMPLETED.getValue(), 0));
        stats.setTotalRevenue(revenue);

        // Статистика мест (как в StatisticsOutputStrategy)
        for (PlotStatus status : PlotStatus.values()) {
            long count = allPlots.stream().filter(p -> p.status() == status).count();
            stats.getPlotStatusCounts().put(status.getValue(), (int) count);
        }

        stats.setTotalPlots(allPlots.size());
        stats.setFreePlots(stats.getPlotStatusCounts().getOrDefault(PlotStatus.FREE.getValue(), 0));
        stats.setOccupiedPlots(stats.getPlotStatusCounts().getOrDefault(PlotStatus.OCCUPIED.getValue(), 0));

        return stats;
    }

    public void exportFile(File file, List<RequestRow> rows) throws Exception {
        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            exportToXlsx(file, rows);
        } else {
            exportToCsv(file, rows);
        }
    }

    private void exportToXlsx(File file, List<RequestRow> rows) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Заявки");
            String[] headers = {"ID", "ФИО Умершего", "Заявитель", "Сотрудник", "Адрес места", "Статус", "Стоимость (₽)", "Дата создания", "Примечание"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (RequestRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getDeceasedFullName() != null ? r.getDeceasedFullName() : "");
                row.createCell(2).setCellValue(r.getCustomerFullName() != null ? r.getCustomerFullName() : "");
                row.createCell(3).setCellValue(r.getEmployeeFullName() != null ? r.getEmployeeFullName() : "");
                row.createCell(4).setCellValue(r.getPlotAddress() != null ? r.getPlotAddress() : "");
                row.createCell(5).setCellValue(r.getStatusRussian() != null ? r.getStatusRussian() : "");
                row.createCell(6).setCellValue(r.getTotalCost() != null ? r.getTotalCost() : 0.0);
                row.createCell(7).setCellValue(r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "");
                row.createCell(8).setCellValue(r.getNote() != null ? r.getNote() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
        }
    }

    private void exportToCsv(File file, List<RequestRow> rows) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            writer.println("ID;ФИО Умершего;Заявитель;Сотрудник;Адрес места;Статус;Стоимость (₽);Дата создания;Примечание");
            for (RequestRow r : rows) {
                String createdStr = r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "";
                writer.printf("%d;\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";%.2f;\"%s\";\"%s\"%n",
                        r.getId(),
                        r.getDeceasedFullName() != null ? r.getDeceasedFullName() : "",
                        r.getCustomerFullName() != null ? r.getCustomerFullName() : "",
                        r.getEmployeeFullName() != null ? r.getEmployeeFullName() : "",
                        r.getPlotAddress() != null ? r.getPlotAddress() : "",
                        r.getStatusRussian() != null ? r.getStatusRussian() : "",
                        r.getTotalCost() != null ? r.getTotalCost() : 0.0,
                        createdStr,
                        r.getNote() != null ? r.getNote().replace("\"", "\"\"") : ""
                );
            }
        }
    }
}
