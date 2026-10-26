package com.katu4a.cladbise.api;

import com.katu4a.cladbise.model.*;
import com.katu4a.cladbise.service.*;
import com.katu4a.cladbise.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ApiContractTest {
    private TestBackend server;
    private ApiClient api;
    private static final UUID ID = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
    private static final String CUSTOMER = """
            {"id":"%s","status":"ACTIVE","fullName":"Иванов","phone":"+79991234567","email":null,"createdAt":"2026-10-26T12:00:00"}
            """.formatted(ID);
    @BeforeEach void start() throws Exception { server = new TestBackend(); api = new ApiClient(server.url()); }
    @AfterEach void stop() { server.close(); }

    @Test void customerCrudUsesServerUuidAndArchiveInsteadOfLocalFallback() throws Exception {
        var service = new HttpCustomerService(api);
        server.responses.put("/api/customers", "[" + CUSTOMER + "," + CUSTOMER.replace("ACTIVE", "ARCHIVE") + "]");
        assertEquals(1, service.findAll().size());
        server.responses.put("/api/customers", CUSTOMER);
        var saved = service.create(new Customer(null, "Иванов", "+79991234567", null, null));
        assertEquals(ID, saved.id()); assertEquals("POST", server.method);
        assertFalse(new ObjectMapper().readTree(server.body).has("id"));
        assertNotNull(saved.createdAt());
        server.responses.put("/api/customers/" + ID, CUSTOMER);
        service.update(saved); assertEquals("PUT", server.method);
        service.archive(ID); assertEquals("DELETE", server.method); assertEquals("/api/customers/" + ID, server.path);
        server.status = 409;
        server.responses.put("/api/customers", "{\"detail\":\"duplicate phone\"}");
        ApiException failure = assertThrows(ApiException.class, () -> service.create(saved));
        assertEquals(409, failure.status()); assertTrue(failure.getMessage().contains("duplicate phone"));
    }

    @Test void employeeUpdateOmitsNewPasswordAndCreateHashesIt() throws Exception {
        String employee = """
                {"id":"%s","status":"ACTIVE","fullName":"Employee","role":"MANAGER","login":"manager","active":true,"createdAt":"2026-10-26T12:00:00"}
                """.formatted(ID);
        server.responses.put("/api/employees", employee);
        server.responses.put("/api/employees/" + ID, employee);
        var service = new HttpEmployeeService(api);
        var input = new Employee(ID, "Employee", Role.MANAGER, "manager", true, null);
        service.create(input, "TestPassword!");
        assertEquals(PasswordHasher.sha256("TestPassword!"), new ObjectMapper().readTree(server.body).get("passwordHash").asText());
        service.update(input, "");
        assertTrue(new ObjectMapper().readTree(server.body).get("passwordHash").isNull());
        assertThrows(ValidationException.class, () -> service.create(input, ""));
    }

    @Test void errorsAndMalformedResponsesRemainErrors() {
        for (int code : new int[]{400, 404, 409, 500}) {
            server.status = code;
            ApiException failure = assertThrows(ApiException.class, () -> api.list("api/customers", ApiModels.Customer.class));
            assertEquals(code, failure.status());
        }
        server.status = 200; server.responses.put("/api/customers", "invalid json");
        assertThrows(ApiException.class, () -> api.list("api/customers", ApiModels.Customer.class));
        server.close();
        assertThrows(ApiException.class, () -> new HttpCustomerService(api).findAll());
    }

    @Test void requestStatisticsKeepDecimalPrecisionAndIgnoreArchive() {
        String request = """
                {"id":"%s","status":"ACTIVE","customerId":"%s","plotId":"%s","employeeId":null,
                "requestStatus":"NEW","requestDate":"2026-10-26T12:00:00","totalCost":0.10,"createdAt":"2026-10-26T12:00:00"}
                """.formatted(ID, ID, ID);
        server.responses.put("/api/requests", "[" + request + "," + request.replace("0.10", "0.20") + "," + request.replace("ACTIVE", "ARCHIVE") + "]");
        var stats = new RequestService(api).getStatistics();
        assertEquals(2, stats.getTotalRequests()); assertEquals(new BigDecimal("0.30"), stats.getTotalRevenue());
    }

    @Test void requestPayloadKeepsDatesAndOptionalFields() throws Exception {
        var row = new RequestRow(); row.setCustomerId(ID); row.setPlotId(ID); row.setStatus(RequestStatus.NEW);
        row.setTotalCost(new BigDecimal("123.45")); row.setRequestDate(LocalDateTime.of(2026, 10, 25, 12, 0));
        server.responses.put("/api/requests", """
                {"id":"%s","status":"ACTIVE","customerId":"%s","plotId":"%s","requestStatus":"NEW",
                "requestDate":"2026-10-25T12:00:00","totalCost":123.45,"createdAt":"2026-10-26T12:00:00"}
                """.formatted(ID, ID, ID));
        new RequestService(api).create(row);
        var body = new ObjectMapper().readTree(server.body);
        assertEquals("2026-10-25T12:00:00", body.get("requestDate").asText());
        assertTrue(body.get("employeeId").isNull()); assertEquals(ID, row.getId());
        assertEquals("123.45", body.get("totalCost").asText());
    }

    @Test void exportsUuidCyrillicAndEscapedCsv(@TempDir Path temp) throws Exception {
        var row = new RequestRow(); row.setId(ID); row.setCustomerFullName("Иванов; \"Иван\"");
        row.setTotalCost(new BigDecimal("123.45")); row.setNote("строка\nещё строка");
        var service = new RequestService(api);
        Path csv = temp.resolve("requests.csv"), xlsx = temp.resolve("requests.xlsx");
        service.exportFile(csv.toFile(), List.of(row)); service.exportFile(xlsx.toFile(), List.of(row));
        String text = Files.readString(csv);
        assertTrue(text.startsWith("\ufeffID;ФИО умершего")); assertTrue(text.contains(ID.toString()));
        assertTrue(text.contains("\"Иванов; \"\"Иван\"\"\"")); assertTrue(text.contains("123.45"));
        try (var workbook = new XSSFWorkbook(Files.newInputStream(xlsx))) {
            assertEquals("Заявки", workbook.getSheetAt(0).getSheetName());
            assertEquals(ID.toString(), workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
            assertEquals(123.45, workbook.getSheetAt(0).getRow(1).getCell(6).getNumericCellValue());
        }
    }
}
