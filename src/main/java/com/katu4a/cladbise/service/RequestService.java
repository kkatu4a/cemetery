package com.katu4a.cladbise.service;

import com.katu4a.cladbise.api.*;
import com.katu4a.cladbise.model.*;
import com.katu4a.cladbise.util.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RequestService {
    private final ApiClient api;
    public RequestService(ApiClient api) { this.api = api; }
    private <T> Map<UUID,T> index(List<T> values, Function<T,UUID> id) {
        return values.stream().collect(Collectors.toMap(id, Function.identity()));
    }
    public List<RequestRow> findAll() {
        var customers = index(api.list("api/customers", ApiModels.Customer.class), ApiModels.Customer::id);
        var employees = index(api.list("api/employees", ApiModels.Employee.class), ApiModels.Employee::id);
        var sectors = index(api.list("api/sectors", ApiModels.Sector.class), ApiModels.Sector::id);
        var plots = index(api.list("api/plots", ApiModels.Plot.class), ApiModels.Plot::id);
        return api.list("api/requests", ApiModels.Request.class).stream()
                .filter(r -> r.status() == ApiModels.Status.ACTIVE)
                .sorted(Comparator.comparing(ApiModels.Request::createdAt).reversed()).map(r -> {
            RequestRow row = new RequestRow();
            row.setId(r.id()); row.setCustomerId(r.customerId()); row.setEmployeeId(r.employeeId()); row.setPlotId(r.plotId());
            var customer = customers.get(r.customerId());
            var employee = employees.get(r.employeeId());
            var plot = plots.get(r.plotId());
            row.setCustomerFullName(customer == null ? String.valueOf(r.customerId()) : customer.fullName());
            row.setEmployeeFullName(employee == null ? "Не назначен" : employee.fullName());
            var sector = plot == null ? null : sectors.get(plot.sectorId());
            row.setPlotAddress(plot == null ? String.valueOf(r.plotId()) : String.format("Сектор %s, Ряд %d, № %d",
                    sector == null ? plot.sectorId() : sector.name(), plot.rowNumber(), plot.plotNumber()));
            row.setDeceasedFullName(r.deceasedFullName()); row.setDeceasedBirthday(r.deceasedBirthday());
            row.setDeceasedDeathday(r.deceasedDeathday()); row.setDeceasedCertificate(r.deceasedCertificate());
            row.setStatus(r.requestStatus()); row.setTotalCost(r.totalCost()); row.setNote(r.note());
            row.setCreatedAt(r.createdAt()); row.setRequestDate(r.requestDate()); row.setProcessedDate(r.processedDate());
            return row;
        }).toList();
    }
    public void validate(RequestRow r) {
        if (r.getCustomerId() == null) throw new ValidationException("Выберите заявителя");
        if (r.getPlotId() == null) throw new ValidationException("Выберите место захоронения");
        if (r.getStatus() == null) throw new ValidationException("Выберите статус заявки");
        if (r.getTotalCost() == null || r.getTotalCost().signum() < 0 || r.getTotalCost().scale() > 2
                || r.getTotalCost().compareTo(new BigDecimal("99999999.99")) > 0)
            throw new ValidationException("Стоимость: от 0 до 99999999.99, не более двух знаков после запятой");
        if (r.getDeceasedBirthday() != null && r.getDeceasedDeathday() != null
                && r.getDeceasedDeathday().isBefore(r.getDeceasedBirthday()))
            throw new ValidationException("Дата смерти не может быть раньше даты рождения");
    }
    public void create(RequestRow row) { save("POST", "api/requests", row); }
    public void update(RequestRow row) { save("PUT", "api/requests/" + row.getId(), row); }
    private void save(String method, String path, RequestRow r) {
        validate(r);
        var input = new ApiModels.RequestInput(r.getCustomerId(), r.getPlotId(), r.getEmployeeId(),
                r.getDeceasedFullName(), r.getDeceasedBirthday(), r.getDeceasedDeathday(), r.getDeceasedCertificate(),
                r.getStatus(), r.getRequestDate() == null ? LocalDateTime.now() : r.getRequestDate(),
                r.getProcessedDate(), r.getTotalCost(), r.getNote());
        var saved = api.call(method, path, input, ApiModels.Request.class);
        r.setId(saved.id()); r.setCreatedAt(saved.createdAt()); r.setRequestDate(saved.requestDate());
    }
    public void archive(UUID id) { api.call("DELETE", "api/requests/" + id, null, Void.class); }
    public List<ComboItem> getCustomersForCombo() { return getCustomersForCombo(null); }
    public List<ComboItem> getCustomersForCombo(UUID current) {
        return api.list("api/customers", ApiModels.Customer.class).stream()
                .filter(c -> c.status() == ApiModels.Status.ACTIVE || c.id().equals(current))
                .map(c -> new ComboItem(c.id(), c.fullName() + " (" + c.phone() + ")")).toList();
    }
    public List<ComboItem> getEmployeesForCombo() { return getEmployeesForCombo(null); }
    public List<ComboItem> getEmployeesForCombo(UUID current) {
        return api.list("api/employees", ApiModels.Employee.class).stream()
                .filter(e -> e.status() == ApiModels.Status.ACTIVE && e.active() || e.id().equals(current))
                .map(e -> new ComboItem(e.id(), e.fullName())).toList();
    }
    public List<ComboItem> getPlotsForCombo(UUID current) {
        var sectors = index(api.list("api/sectors", ApiModels.Sector.class), ApiModels.Sector::id);
        return api.list("api/plots", ApiModels.Plot.class).stream()
                .filter(p -> p.status() == ApiModels.Status.ACTIVE && p.plotStatus() == PlotStatus.FREE || p.id().equals(current))
                .map(p -> new ComboItem(p.id(), String.format("Сектор %s, Ряд %d, № %d (%s)",
                        sectors.containsKey(p.sectorId()) ? sectors.get(p.sectorId()).name() : p.sectorId(),
                        p.rowNumber(), p.plotNumber(), p.plotStatus().getValue()))).toList();
    }
    public StatisticsData getStatistics() {
        var requests = api.list("api/requests", ApiModels.Request.class).stream().filter(r -> r.status() == ApiModels.Status.ACTIVE).toList();
        var plots = api.list("api/plots", ApiModels.Plot.class).stream().filter(p -> p.status() == ApiModels.Status.ACTIVE).toList();
        var stats = new StatisticsData();
        for (var status : RequestStatus.values()) stats.getRequestStatusCounts().put(status.getValue(), (int) requests.stream().filter(r -> r.requestStatus() == status).count());
        for (var status : PlotStatus.values()) stats.getPlotStatusCounts().put(status.getValue(), (int) plots.stream().filter(p -> p.plotStatus() == status).count());
        stats.setTotalRequests(requests.size()); stats.setTotalPlots(plots.size());
        stats.setNewRequests(stats.getRequestStatusCounts().get(RequestStatus.NEW.getValue()));
        stats.setProcessingRequests(stats.getRequestStatusCounts().get(RequestStatus.PROCESSING.getValue()));
        stats.setCompletedRequests(stats.getRequestStatusCounts().get(RequestStatus.COMPLETED.getValue()));
        stats.setFreePlots(stats.getPlotStatusCounts().get(PlotStatus.FREE.getValue()));
        stats.setOccupiedPlots(stats.getPlotStatusCounts().get(PlotStatus.OCCUPIED.getValue()));
        stats.setTotalRevenue(requests.stream().map(ApiModels.Request::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add));
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

            Sheet sheet = workbook.createSheet("Р—Р°СЏРІРєРё");
            String[] headers = {"ID", "Р¤РРћ РЈРјРµСЂС€РµРіРѕ", "Р—Р°СЏРІРёС‚РµР»СЊ", "РЎРѕС‚СЂСѓРґРЅРёРє", "РђРґСЂРµСЃ РјРµСЃС‚Р°", "РЎС‚Р°С‚СѓСЃ", "РЎС‚РѕРёРјРѕСЃС‚СЊ (в‚Ѕ)", "Р”Р°С‚Р° СЃРѕР·РґР°РЅРёСЏ", "РџСЂРёРјРµС‡Р°РЅРёРµ"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (RequestRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId().toString());
                row.createCell(1).setCellValue(r.getDeceasedFullName() != null ? r.getDeceasedFullName() : "");
                row.createCell(2).setCellValue(r.getCustomerFullName() != null ? r.getCustomerFullName() : "");
                row.createCell(3).setCellValue(r.getEmployeeFullName() != null ? r.getEmployeeFullName() : "");
                row.createCell(4).setCellValue(r.getPlotAddress() != null ? r.getPlotAddress() : "");
                row.createCell(5).setCellValue(r.getStatusRussian() != null ? r.getStatusRussian() : "");
                row.createCell(6).setCellValue(r.getTotalCost() != null ? r.getTotalCost().doubleValue() : 0.0);
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
            writer.println("ID;Р¤РРћ РЈРјРµСЂС€РµРіРѕ;Р—Р°СЏРІРёС‚РµР»СЊ;РЎРѕС‚СЂСѓРґРЅРёРє;РђРґСЂРµСЃ РјРµСЃС‚Р°;РЎС‚Р°С‚СѓСЃ;РЎС‚РѕРёРјРѕСЃС‚СЊ (в‚Ѕ);Р”Р°С‚Р° СЃРѕР·РґР°РЅРёСЏ;РџСЂРёРјРµС‡Р°РЅРёРµ");
            for (RequestRow r : rows) {
                String createdStr = r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "";
                String costStr = String.format(Locale.US, "%.2f", r.getTotalCost() != null ? r.getTotalCost() : 0.0);
                writer.printf("%s;\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";%s;\"%s\";\"%s\"%n",
                        r.getId(),
                        r.getDeceasedFullName() != null ? r.getDeceasedFullName().replace("\"", "\"\"") : "",
                        r.getCustomerFullName() != null ? r.getCustomerFullName().replace("\"", "\"\"") : "",
                        r.getEmployeeFullName() != null ? r.getEmployeeFullName().replace("\"", "\"\"") : "",
                        r.getPlotAddress() != null ? r.getPlotAddress().replace("\"", "\"\"") : "",
                        r.getStatusRussian() != null ? r.getStatusRussian() : "",
                        costStr,
                        createdStr,
                        r.getNote() != null ? r.getNote().replace("\"", "\"\"") : ""
                );
            }
        }
    }
}

