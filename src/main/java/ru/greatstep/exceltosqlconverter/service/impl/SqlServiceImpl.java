package ru.greatstep.exceltosqlconverter.service.impl;

import static java.lang.String.format;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.DATE;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.ENUM;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.FKEY;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.IN_BRACKET;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.IS_NUMBER;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.NUMBER;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.OUT_BRACKET;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.POINT;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.REFERENCE_COLUMN;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.SEARCH_COLUMN;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.SEARCH_IS_NUMBER;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.SEMICOLON;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.TIMESTAMP;
import static ru.greatstep.exceltosqlconverter.utils.Constants.HeaderColumnConstants.TYPE_NAME;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SpecialValues.RANDOM_FIRST_NAME;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SpecialValues.RANDOM_FULL_NAME;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SpecialValues.RANDOM_LAST_NAME;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SpecialValues.RANDOM_MIDDLE_NAME;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SqlPatterns.DO_END_TEMPLATE;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SqlPatterns.INSERT_PATTERN;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SqlPatterns.SUB_SELECT_NUMBER_TEMPLATE;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SqlPatterns.SUB_SELECT_TEMPLATE;
import static ru.greatstep.exceltosqlconverter.utils.Constants.SqlPatterns.VALUE_PATTERN;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.greatstep.exceltosqlconverter.models.ExcelContext;
import ru.greatstep.exceltosqlconverter.models.FakeName;
import ru.greatstep.exceltosqlconverter.service.DataRandomIntegrationService;
import ru.greatstep.exceltosqlconverter.service.SqlService;
import ru.greatstep.exceltosqlconverter.utils.Constants.PostgresFunc;
import ru.greatstep.exceltosqlconverter.utils.Constants.SpecialValues;

@Service
@RequiredArgsConstructor
public class SqlServiceImpl implements SqlService {

    private final DataRandomIntegrationService dataRandomIntegrationService;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public void generateSql(ExcelContext context, MultipartFile multipartFile) {
        saveFile(sqlProcess(context, multipartFile));
    }

    public Resource generateSqlAndReturn(ExcelContext context, MultipartFile multipartFile) {
        return returnFile(sqlProcess(context, multipartFile));
    }

    @SneakyThrows(IOException.class)
    private String sqlProcess(ExcelContext context, MultipartFile multipartFile) {
        var objects = context.getRows();
        StringBuilder sb = new StringBuilder();
        var insertColumnNames = getInsertColumnNames(objects);

        checkNullValuesFromColumns(objects, insertColumnNames);
        sb.append(format(INSERT_PATTERN, getTableName(multipartFile), String.join(", ", insertColumnNames)));
        addValuesFromSb(getValues(objects), sb);
        return format(DO_END_TEMPLATE, sb);
    }

    @SneakyThrows(IOException.class)
    private void saveFile(String sql) {
        String filePostfix = LocalDateTime.now().format(ofPattern("yyyy-MM-dd'T'HH_mm_ss"));
        File file = new File("src/main/resources/sql/test_" + filePostfix + ".sql");
        var success = file.createNewFile();
        if (success) {
            try (var fos = new FileOutputStream(file, false)) {
                fos.write(sql.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Ошибка при создании файла");
        }

        System.out.println(sql);
    }

    private Resource returnFile(String sql) {
        return new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8));
    }

    private void addValuesFromSb(List<String> values, StringBuilder sb) {
        for (int i = 0; i < values.size(); i++) {
            if (i != values.size() - 1) {
                sb.append(values.get(i));
                continue;
            }
            var value = values.get(i);
            sb.append(value, 0, value.length() - 2);

        }
        sb.append(SEMICOLON);
    }

    //значения для строк
    private List<String> getValues(List<Map<String, String>> objects) {
        var sorted = objects.stream().sorted(Comparator.comparing(Map::size, Comparator.reverseOrder())).toList();
        int randomNameCount = sorted.stream().filter(this::containRandomName).mapToInt(e -> 1).sum();
        List<FakeName> fakeNames = randomNameCount != 0
                ? dataRandomIntegrationService.getFakeNames(randomNameCount)
                : new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0, j = 0; i < objects.size(); i++) {
            if (containRandomName(sorted.get(i))) {
                values.add(generateValues(sorted.get(i), fakeNames.get(j)));
                j++;
            } else {
                values.add(generateValues(sorted.get(i), null));
            }

        }
        return values;
    }

    private boolean containRandomName(Map<String, String> map) {
        return map.containsValue(RANDOM_FULL_NAME) || map.containsValue(RANDOM_FIRST_NAME)
                || map.containsValue(RANDOM_LAST_NAME) || map.containsValue(RANDOM_MIDDLE_NAME);
    }

    private String generateValues(Map<String, String> map, FakeName fakeName) {
        quoteChecker(map);
        var resultValues = map.entrySet().stream()
                .map(entry -> generateValue(entry, fakeName))
                .toList();
        return format(VALUE_PATTERN, String.join(", ", resultValues));
    }

    private String generateValue(Map.Entry<String, String> entry, FakeName fakeName) {
        if (entry.getValue().equals("null")) {
            return null;
        }

        if (PostgresFunc.getAll().contains(entry.getValue()) || entry.getKey().endsWith(NUMBER)) {
            return entry.getValue();
        }

        if (SpecialValues.getAll().contains(entry.getValue())) {
            return generateVarcharFake(entry, fakeName);
        }

        return generateFromKeys(entry);
    }

    private String generateFromKeys(Map.Entry<String, String> entry) {
        if (entry.getKey().endsWith(DATE)) {
            return toVarchar(parse(entry.getValue(), ofPattern(DATE_FORMAT)).toString());
        }

        if (entry.getKey().endsWith(TIMESTAMP)) {
            return toVarchar(LocalDateTime.parse(entry.getValue(), ofPattern(DATE_TIME_FORMAT))
                    .toString());
        }

        if (entry.getKey().contains(ENUM)) {
            return entry.getValue().contains(IS_NUMBER)
                    ? entry.getValue() + "::" + substringBetween(entry.getValue(), TYPE_NAME, OUT_BRACKET)
                    : toVarchar(entry.getValue()) + "::" + substringBetween(entry.getValue(), TYPE_NAME, SEMICOLON);
        }
        //TODO Вынести подзапросы в переменные
        if (entry.getKey().contains(FKEY)) {
            boolean searchIsNumber = entry.getKey().contains(SEARCH_IS_NUMBER);
            var reference = substringBetween(entry.getKey(), REFERENCE_COLUMN, SEMICOLON);
            var schema = substringBefore(reference, POINT);
            var table = substringBetween(reference, schema + POINT, POINT);
            var schemaTable = schemaTable(schema, table);
            var column = substringAfter(reference, table + POINT);
            var search = searchIsNumber
                    ? substringBetween(entry.getKey(), SEARCH_COLUMN, SEMICOLON)
                    : substringBetween(entry.getKey(), SEARCH_COLUMN, OUT_BRACKET);

            return searchIsNumber
                    ? format(SUB_SELECT_NUMBER_TEMPLATE, column, schemaTable, search, entry.getValue())
                    : format(SUB_SELECT_TEMPLATE, column, schemaTable, search, entry.getValue());
        }
        return toVarchar(entry.getValue());
    }

    private String generateVarcharFake(Map.Entry<String, String> entry, FakeName fakeName) {
        return switch (entry.getValue()) {
            case RANDOM_FULL_NAME -> toVarchar(fakeName.fullName());
            case RANDOM_FIRST_NAME -> toVarchar(fakeName.firstName());
            case RANDOM_MIDDLE_NAME -> toVarchar(fakeName.fatherName());
            case RANDOM_LAST_NAME -> toVarchar(fakeName.lastName());
            default -> "unknown random key";
        };
    }

    private String schemaTable(String schema, String table) {
        return schema + POINT + table;
    }

    private String toVarchar(String string) {
        return "'" + string + "'";
    }

    private void checkNullValuesFromColumns(List<Map<String, String>> objects, Set<String> insertColumnNames) {
        objects.forEach(map -> {
            if (map.keySet().size() < insertColumnNames.size()) {
                insertColumnNames.forEach(column -> map.putIfAbsent(column, "null"));
            }
        });
    }

    private Set<String> getInsertColumnNames(List<Map<String, String>> objects) {
        var insertColumnNames = objects.stream()
                .max(Comparator.comparing(Map::size))
                .map(Map::keySet)
                .orElse(null);
        if (Objects.isNull(insertColumnNames)) {
            throw new RuntimeException("Empty column names");
        }
        return insertColumnNames.stream()
                .map(c -> c.contains(IN_BRACKET) ? StringUtils.substringBefore(c, IN_BRACKET) : c)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String getTableName(MultipartFile multipartFile) throws IOException {
        Sheet sheet;
        try (InputStream file = multipartFile.getInputStream();
                ReadableWorkbook wb = new ReadableWorkbook(file)) {
            sheet = wb.getFirstSheet();
        }
        return sheet != null
                ? sheet.getName()
                : null;
    }

    private void quoteChecker(Map<String, String> map) {
        map.replaceAll((k, v) -> v.contains("'") ? v.replaceAll("'", "''") : v);
    }

}
