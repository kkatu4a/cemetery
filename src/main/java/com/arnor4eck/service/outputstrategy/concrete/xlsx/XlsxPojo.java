package com.arnor4eck.service.outputstrategy.concrete.xlsx;

import com.arnor4eck.repository.Repository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class XlsxPojo {

    private Workbook workbook;

    public XlsxPojo() {
        this.workbook = new XSSFWorkbook();
    }

    public <T> void addSheet(
            String sheetName,
            Repository<T> repository,
            Function<T, String> mapToString
    ) {
        List<T> all;
        try {
            all = new ArrayList<>(repository.getAll());
        } catch (SQLException e) {
            System.out.printf("Не удалось получить все сущности для %s: %s\n%n", sheetName, e.getMessage());
            return;
        }
        Sheet sheet = workbook.createSheet(sheetName);

        for (int i = 0; i < all.size(); i++) {
            T val = all.get(i);
            Row row = sheet.createRow(i);
            String valueString = mapToString.apply(val);

            String[] values = valueString.split(";");
            for(int j = 0; j < values.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(values[j]);
            }
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }

}
