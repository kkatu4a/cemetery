package com.katu4a.cladbise.util;

import com.arnor4eck.repository.*;

public class BackendProvider {
    private static DataBase dataBase;
    private static RequestRepository requestRepository;
    private static CustomerRepository customerRepository;
    private static EmployeeRepository employeeRepository;
    private static PlotRepository plotRepository;
    private static SectorRepository sectorRepository;

    public static synchronized DataBase getDataBase() {
        if (dataBase == null) {
            dataBase = new DataBase();
        }
        return dataBase;
    }

    public static synchronized RequestRepository getRequestRepository() {
        if (requestRepository == null) {
            requestRepository = new RequestRepository(getDataBase());
        }
        return requestRepository;
    }

    public static synchronized CustomerRepository getCustomerRepository() {
        if (customerRepository == null) {
            customerRepository = new CustomerRepository(getDataBase());
        }
        return customerRepository;
    }

    public static synchronized EmployeeRepository getEmployeeRepository() {
        if (employeeRepository == null) {
            employeeRepository = new EmployeeRepository(getDataBase());
        }
        return employeeRepository;
    }

    public static synchronized PlotRepository getPlotRepository() {
        if (plotRepository == null) {
            plotRepository = new PlotRepository(getDataBase());
        }
        return plotRepository;
    }

    public static synchronized SectorRepository getSectorRepository() {
        if (sectorRepository == null) {
            sectorRepository = new SectorRepository(getDataBase());
        }
        return sectorRepository;
    }
}