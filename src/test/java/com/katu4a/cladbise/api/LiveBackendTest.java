package com.katu4a.cladbise.api;

import com.katu4a.cladbise.model.*;
import com.katu4a.cladbise.service.*;
import com.katu4a.cladbise.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Run only against a disposable backend database: -DliveBackendUrl=http://localhost:18080 */
@EnabledIfSystemProperty(named = "liveBackendUrl", matches = "https?://.+")
class LiveBackendTest {
    @Test void clientServicesWorkWithActualBackend() {
        var api = new ApiClient(System.getProperty("liveBackendUrl"));
        String suffix = UUID.randomUUID().toString();
        var sector = api.call("POST", "api/sectors", java.util.Map.of("name", "Smoke " + suffix), ApiModels.Sector.class);
        var customers = new HttpCustomerService(api);
        var employees = new HttpEmployeeService(api);
        var plots = new HttpPlotService(api);
        var requests = new RequestService(api);
        var customer = customers.create(new Customer(null, "Проверка клиента", "123", null, null));
        var employee = employees.create(new Employee(null, "Проверка сотрудника", Role.MANAGER, suffix, true, null), "StrongPassword!1");
        var updated = employees.update(new Employee(employee.id(), "Изменён", employee.role(), employee.login(), false, employee.createdAt()), "");
        assertEquals("Изменён", updated.fullName()); assertFalse(updated.isActive());
        var plot = plots.create(new Plot(null, sector.id(), 1, 1, PlotStatus.FREE, 200, 100, null));
        var request = new RequestRow(); request.setCustomerId(customer.id()); request.setPlotId(plot.id());
        request.setStatus(RequestStatus.NEW); request.setTotalCost(new BigDecimal("123.45"));
        requests.create(request);
        assertNotNull(request.getId()); assertEquals(PlotStatus.RESERVED, getPlot(api, plot.id()).plotStatus());
        var loaded = requests.findAll().stream().filter(r -> r.getId().equals(request.getId())).findFirst().orElseThrow();
        assertEquals(customer.fullName(), loaded.getCustomerFullName()); assertEquals(new BigDecimal("123.45"), loaded.getTotalCost());
        assertNull(loaded.getEmployeeId());
        loaded.setStatus(RequestStatus.COMPLETED); requests.update(loaded);
        assertEquals(PlotStatus.OCCUPIED, getPlot(api, plot.id()).plotStatus());
        customers.archive(customer.id());
        assertTrue(customers.findAll().stream().noneMatch(c -> c.id().equals(customer.id())));
        assertEquals(customer.fullName(), requests.findAll().stream().filter(r -> r.getId().equals(request.getId())).findFirst().orElseThrow().getCustomerFullName());
        assertTrue(requests.getCustomersForCombo(customer.id()).stream().anyMatch(c -> c.id().equals(customer.id())));
        requests.archive(request.getId()); assertEquals(PlotStatus.FREE, getPlot(api, plot.id()).plotStatus());
        api.call("POST", "api/requests/" + request.getId() + "/restore", null, ApiModels.Request.class);
        assertEquals(PlotStatus.OCCUPIED, getPlot(api, plot.id()).plotStatus());
        requests.archive(request.getId());
        api.call("DELETE", "api/requests/" + request.getId() + "/permanent", null, Void.class);
        plots.archive(plot.id()); employees.archive(employee.id());
        api.call("DELETE", "api/plots/" + plot.id() + "/permanent", null, Void.class);
        api.call("DELETE", "api/employees/" + employee.id() + "/permanent", null, Void.class);
        api.call("DELETE", "api/customers/" + customer.id() + "/permanent", null, Void.class);
        api.call("DELETE", "api/sectors/" + sector.id(), null, Void.class);
        api.call("DELETE", "api/sectors/" + sector.id() + "/permanent", null, Void.class);
    }
    private ApiModels.Plot getPlot(ApiClient api, UUID id) {
        return api.call("GET", "api/plots/" + id, null, ApiModels.Plot.class);
    }
}
