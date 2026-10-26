package com.katu4a.cladbise.util;
import com.katu4a.cladbise.api.ApiClient;
import com.katu4a.cladbise.service.*;
public final class BackendProvider {
    private BackendProvider() {}
    private static final ApiClient CLIENT = new ApiClient();
    private static final CustomerService CUSTOMERS = new HttpCustomerService(CLIENT);
    private static final EmployeeService EMPLOYEES = new HttpEmployeeService(CLIENT);
    private static final PlotService PLOTS = new HttpPlotService(CLIENT);
    private static final RequestService REQUESTS = new RequestService(CLIENT);
    public static CustomerService getCustomerService() { return CUSTOMERS; }
    public static EmployeeService getEmployeeService() { return EMPLOYEES; }
    public static PlotService getPlotService() { return PLOTS; }
    public static RequestService getRequestService() { return REQUESTS; }
}
