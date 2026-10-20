package com.arnor4eck.service.outputstrategy.concrete.xlsx;

import com.arnor4eck.repository.Repository;
import com.arnor4eck.service.outputstrategy.OutputStrategy;
import com.arnor4eck.service.outputstrategy.concrete.xlsx.XlsxPojo;
import org.apache.poi.ss.usermodel.Workbook;
import com.arnor4eck.model.ExportModel;

import java.io.FileOutputStream;
import java.util.List;

public class XlsxOutputStrategy implements OutputStrategy {
    private final List<XlsxPair<?>> xlsxPairList;

    public XlsxOutputStrategy(
            List<XlsxPair<?>> xlsxPairList
    ) {
        this.xlsxPairList = xlsxPairList;
    }

    @Override
    public String act() {
        String fileName = "output.xlsx";

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            XlsxPojo pojo = new XlsxPojo();

            for (var pair : xlsxPairList) {
                pojo.addSheet(pair.sheetName(),
                        pair.repository(),
                        ExportModel::toExportString);
            }


            Workbook workbook = pojo.getWorkbook();

            workbook.write(fos);
        } catch (Exception e) {
            return "Не удалось экспортировать файл: %s".formatted(e.getMessage());
        }

        return String.format("Файл созранен как %s", fileName);
    }

    public record XlsxPair<T extends ExportModel>(String sheetName, Repository<T> repository) {
        public static <R extends ExportModel> XlsxPair<R> of(String sheetName, Repository<R> repository) {
            return new XlsxPair<>(sheetName, repository);
        }
    }
}
