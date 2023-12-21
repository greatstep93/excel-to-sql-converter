package ru.greatstep.exceltosqlconverter.service.impl;

import static ru.greatstep.exceltosqlconverter.service.impl.SqlServiceImpl.DATE_FORMAT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.greatstep.exceltosqlconverter.exception.FormatsNotFoundException;
import ru.greatstep.exceltosqlconverter.models.ExcelContext;
import ru.greatstep.exceltosqlconverter.service.ExcelService;
import ru.greatstep.exceltosqlconverter.service.SqlService;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final ObjectMapper objectMapper;
    private final SqlService sqlService;

    @Override
    public JsonNode excelToSqlSaveFile(MultipartFile multipartFile) {
        sqlService.generateSql(sheetProcess(multipartFile), multipartFile);
        return objectMapper.valueToTree(sheetProcess(multipartFile));
    }

    @Override
    public Resource excelToSql(MultipartFile multipartFile) {
        return sqlService.generateSqlAndReturn(sheetProcess(multipartFile), multipartFile);
    }

    //Sheet - Лист
    //Row - строка листа
    @SneakyThrows({IOException.class, NumberFormatException.class})
    private ExcelContext sheetProcess(MultipartFile multipartFile) {

        try (InputStream file = multipartFile.getInputStream(); ReadableWorkbook wb = new ReadableWorkbook(file)) {
            return ExcelContext.builder()
                    .rows(writeRows(wb.getFirstSheet(), getHeaderRow(wb.getFirstSheet())))
                    .formats(getFormats(wb.getSheets().toList()))
                    .build();
        }

    }

    private List<Cell> getHeaderRow(Sheet sheet) throws IOException {
        return sheet.read().stream()
                .filter(row -> row.getRowNum() == 1)
                .findFirst()
                .map(r -> r.stream().toList())
                .orElseThrow(() -> new FormatsNotFoundException("Нет строки с заголовками"));
    }

    private List<Map<String, String>> writeRows(Sheet sheet, List<Cell> firstRow) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        for (Row row : sheet.read()) {
            if (row.getRowNum() == 1) {
                continue;
            }
            var map = row.stream()
                    .filter(col -> !Objects.isNull(col) && !col.getType().equals(CellType.EMPTY))
                    .collect(Collectors.toMap(
                                    c -> getColumnHeader(firstRow, c),
                                    c -> getColumnHeader(firstRow, c).contains("[date]")
                                            && !c.getText().equals("current_date")
                                            ? c.asDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
                                            : c.getText(),
                                    (key1, key2) -> key2,
                                    LinkedHashMap::new
                            )
                    );
            if (!map.isEmpty()) {
                result.add(map);
            }

        }
        return result;
    }

    private String getColumnHeader(List<Cell> firstRow, Cell c) {
        return firstRow.stream()
                .filter(nameCell -> nameCell.getColumnIndex() == c.getColumnIndex())
                .map(Cell::getText)
                .findFirst().orElse(null);
    }

    @SneakyThrows
    private Map<String, String> getFormats(List<Sheet> sheets) {
        var formatSheet = getFormatsSheet(sheets);
        return formatSheet != null
                ? formatSheet.read().stream()
                .filter(row -> row.getRowNum() > 1)
                .collect(Collectors.toMap(
                        row -> row.getCellText(0),
                        row -> row.getCellText(1),
                        (key1, key2) -> key2,
                        LinkedHashMap::new
                ))
                : null;
    }

    private Sheet getFormatsSheet(List<Sheet> sheets) {
        return sheets.stream()
                .filter(sheet -> sheet.getName().equals("formats"))
                .findFirst()
                .orElse(null);
    }

}
