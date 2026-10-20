package com.arnor4eck;

import com.arnor4eck.MenuProvider;
import com.arnor4eck.model.Plot;
import com.arnor4eck.model.Request;
import com.arnor4eck.repository.*;
import com.arnor4eck.service.outputstrategy.DataExportStrategy;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.service.outputstrategy.OutputStrategyFactory;
import com.arnor4eck.service.outputstrategy.concrete.StatisticsOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.create.CreateCustomerOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.create.CreateEmployeeOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.create.CreatePlotOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.filter.*;
import com.arnor4eck.service.outputstrategy.concrete.update.UpdatePlotOutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.xlsx.XlsxOutputStrategy;
import com.arnor4eck.util.OutputStrategyPair;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public final class Application {

    private final MenuProvider menu;
    private final DataExportStrategy mainMenu;

    private static final String EXIT = "Выход";
    private static final List<String> MENU_UNITS = List.of("Заявители", "Заявки", "Места на кладбище", "Сотрудники", "Экспорт данных", "Статистика", EXIT);
    private static final int EXIT_CONDITION = MENU_UNITS.indexOf(EXIT) + 1;

    public Application(Scanner scanner) {
        this.menu = new MenuProvider(
                "========= СИСТЕМА УПРАВЛЕНИЯ КЛАДБИЩЕМ =========",
                scanner,
                MENU_UNITS
        );

        OutputStrategyFactory factory = new OutputStrategyFactory(scanner);

        var dataSource = new DataBase();
        var requestRepository = new RequestRepository(dataSource);
        var plotRepository = new PlotRepository(dataSource);
        var customerRepository = new CustomerRepository(dataSource);
        var sectorRepository = new SectorRepository(dataSource);
        var employeeRepository = new EmployeeRepository(dataSource);


        this.mainMenu = new DataExportStrategy(
            List.of(
                factory.menuProvider(
                        "========= ЗАЯВИТЕЛИ =========",
                        List.of(
                            OutputStrategyPair.of("Все заявители", OutputStrategyFactory.allValues(customerRepository)),
                            OutputStrategyPair.of("Конкретный заявитель (id)", factory.concreteValue(customerRepository)),
                            OutputStrategyPair.of("Поиск по содержанию текста в ФИО", new CustomerByFullNameFilterStrategy(customerRepository, scanner)),
                            OutputStrategyPair.of("Поиск по номеру телефона", new CustomerByPhoneFilterStrategy(customerRepository, scanner)),
                            OutputStrategyPair.of("Создать заявителя", new CreateCustomerOutputStrategy(customerRepository, scanner))
                        )
                ),
                factory.menuProvider(
                        "========= ЗАЯВКИ =========",
                        List.of(
                            OutputStrategyPair.of("Все заявки", OutputStrategyFactory.allValues(requestRepository)),
                            OutputStrategyPair.of("Конкретная заявка (id)", factory.concreteValue(requestRepository)),
                            OutputStrategyPair.of("Сортировка по дате создания", OutputStrategyFactory.sort(requestRepository, Comparator.comparing(Request::createdAt).reversed()))
                        )
                ),
                factory.menuProvider(
                        "========= МЕСТА НА КЛАДБИЩЕ =========",
                        List.of(
                            OutputStrategyPair.of("Все места", OutputStrategyFactory.allValues(plotRepository)),
                            OutputStrategyPair.of("Конкретное место (id)", factory.concreteValue(plotRepository)),
                            OutputStrategyPair.of("Сортировка по статусу", OutputStrategyFactory.sort(plotRepository, Comparator.comparing(Plot::status))),
                            OutputStrategyPair.of("Фильтрация по статусу", new PlotByStatusFilterStrategy(plotRepository, scanner)),
                            OutputStrategyPair.of("Создать место", new CreatePlotOutputStrategy(plotRepository, scanner)),
                            OutputStrategyPair.of("Обновить существующее место", new UpdatePlotOutputStrategy(plotRepository, scanner))
                        )
                ),
                factory.menuProvider(
                        "========= СОТРУДНИКИ =========",
                        List.of(
                            OutputStrategyPair.of("Все сотрудники", OutputStrategyFactory.allValues(employeeRepository)),
                            OutputStrategyPair.of("Конретный сотрудник (id)", factory.concreteValue(employeeRepository)), OutputStrategyPair.of("Поиск по содержанию текста в ФИО", new EmployeeByFullNameFilterStrategy(employeeRepository, scanner)),
                            OutputStrategyPair.of("Поиск по логину", new EmployeeByLoginFilterStrategy(employeeRepository, scanner)),
                            OutputStrategyPair.of("Создать сотрудника", new CreateEmployeeOutputStrategy(employeeRepository, scanner))
                            )
                    ),
                factory.menuProvider(
                        "========= ЭКСПОРТ ДАННЫХ =========",
                        OutputStrategyPair.of("Общий экспорт", new XlsxOutputStrategy(
                            List.of(
                                XlsxOutputStrategy.XlsxPair.of("Места", plotRepository),
                                XlsxOutputStrategy.XlsxPair.of("Запросы", requestRepository),
                                XlsxOutputStrategy.XlsxPair.of("Клиенты", customerRepository),
                                XlsxOutputStrategy.XlsxPair.of("Сектора", sectorRepository), XlsxOutputStrategy.XlsxPair.of("Сотрудники", employeeRepository)
                                )
                        ))
                ),
                factory.menuProvider(
                    "========= СТАТИСТИКА =========",
                    OutputStrategyPair.of("Общая статистика", new StatisticsOutputStrategy(plotRepository, requestRepository))
                )
            )
        );
    }

    public void run() {
        while (true) {
            int enteredNum = menu.menu();
            if (shouldBeExit(enteredNum)) {
                break;
            }

            OutputStrategy strategy = mainMenu.find(enteredNum);
            System.out.println(strategy.act());
        }
    }

    private boolean shouldBeExit(int num) {
        return num == EXIT_CONDITION;
    }
}
