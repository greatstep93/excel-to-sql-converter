package ru.greatstep.exceltosqlconverter.service;

import static java.lang.String.format;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.greatstep.exceltosqlconverter.models.FakeName;

@Service
@RequiredArgsConstructor
public class GenerateSqlService {

    private final RandomNamesService randomNamesService;
    private static final String INSERT_PATTERN = "INSERT INTO %s ( %s )\nVALUES\n";
    private static final String VALUE_PATTERN = "(%s),\n";
    private static final List<String> POSTGRES_FUNC = List.of("current_timestamp", "current_date", "current_time");
    private static final String SUB_SELECT_TEMPLATE = "(SELECT %s FROM %s WHERE %s = '%s')";
    private static final String SUB_SELECT_NUMBER_TEMPLATE = "(SELECT %s FROM %s WHERE %s = %s)";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String REFERENCE_COLUMN = "[reference_column=";
    private static final String SEARCH_COLUMN = "search_column=";

    public void generateSql(List<Map<String, String>> objects, MultipartFile multipartFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        var insertColumnNames = getInsertColumnNames(objects);

        checkNullValuesFromColumns(objects, insertColumnNames);
        sb.append(format(INSERT_PATTERN, getTableName(multipartFile), String.join(", ", insertColumnNames)));
        addValuesFromSb(getValues(objects), sb);
        saveFile(sb);
    }

    public Resource generateSqlAndReturn(List<Map<String, String>> objects, MultipartFile multipartFile)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        var insertColumnNames = getInsertColumnNames(objects);

        checkNullValuesFromColumns(objects, insertColumnNames);
        sb.append(format(INSERT_PATTERN, getTableName(multipartFile), String.join(", ", insertColumnNames)));
        addValuesFromSb(getValues(objects), sb);
        return returnFile(sb);
    }

    private void saveFile(StringBuilder sb) throws IOException {
        String filePostfix = LocalDateTime.now().format(ofPattern("yyyy-MM-dd'T'HH_mm_ss"));
        File file = new File("src/main/resources/sql/test_" + filePostfix + ".sql");
        var success = file.createNewFile();
        if (success) {
            try (var fos = new FileOutputStream(file, false)) {
                fos.write(sb.toString().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Ошибка при создании файла");
        }

        System.out.println(sb);
    }

    private Resource returnFile(StringBuilder sb) {
        return new ByteArrayResource(sb.toString().getBytes());
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
    }

    //значения для строк
    private List<String> getValues(List<Map<String, String>> objects) {
        var sorted = objects.stream().sorted(Comparator.comparing(Map::size, Comparator.reverseOrder())).toList();
        int randomNameCount = sorted.stream().filter(this::containRandomName).mapToInt(e -> 1).sum();
        var fakeNames = randomNamesService.getFakeNames(randomNameCount);
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
        return map.containsValue("RANDOM_FULL_NAME") || map.containsValue("RANDOM_FIRST_NAME")
                || map.containsValue("RANDOM_LAST_NAME") || map.containsValue("RANDOM_MIDDLE_NAME");
    }

    private String generateValues(Map<String, String> map, FakeName fakeName) {
        var resultValues = map.entrySet().stream()
                .map(entry -> generateValue(entry, fakeName))
                .toList();
        return format(VALUE_PATTERN, String.join(", ", resultValues));
    }

    private String generateValue(Map.Entry<String, String> entry, FakeName fakeName) {
        if (POSTGRES_FUNC.contains(entry.getValue()) || entry.getKey().endsWith("[number]")) {
            return entry.getValue();
        }

        if (entry.getValue().equals("RANDOM_FULL_NAME")) {
            return toVarchar(fakeName.fullName());
        }

        if (entry.getValue().equals("RANDOM_FIRST_NAME")) {
            return toVarchar(fakeName.firstName());
        }

        if (entry.getValue().equals("RANDOM_LAST_NAME")) {
            return toVarchar(fakeName.lastName());
        }

        if (entry.getValue().equals("RANDOM_MIDDLE_NAME")) {
            return toVarchar(fakeName.fatherName());
        }

        if (entry.getValue().equals("null")) {
            return null;
        }

        if (entry.getKey().endsWith("[date]")) {
            return toVarchar(parse(entry.getValue(), ofPattern(DATE_FORMAT)).toString());
        }

        if (entry.getKey().endsWith("[timestamp]")) {
            return toVarchar(LocalDateTime.parse(entry.getValue(), ofPattern(DATE_TIME_FORMAT))
                    .toString());
        }

        if (entry.getKey().contains("[enum]")) {
            var enumTable = substringBetween(entry.getValue(), "[enum=", "]");
            return toVarchar(entry.getValue()) + "::" + enumTable;
        }

        if (entry.getKey().contains("[fkey]") && !entry.getKey().contains("[number]")) {
            var reference = substringBetween(entry.getKey(), "[fkey_", "]");
            var schemaName = substringBefore(reference, ".");
            var tableName = substringBetween(reference, schemaName + ".", ".");
            var referenceColumnName = substringAfter(reference, tableName + ".");
            var schemaTable = substringBefore(reference, "." + referenceColumnName);
            return format(SUB_SELECT_TEMPLATE,
                    referenceColumnName,
                    schemaTable,
                    reference,
                    toVarchar(entry.getValue()));
        }

        if (entry.getKey().contains(REFERENCE_COLUMN) && entry.getKey()
                .contains(SEARCH_COLUMN)) {
            var reference = substringBetween(entry.getKey(), REFERENCE_COLUMN, "]");
            var schemaName = substringBefore(reference, ".");
            var tableName = substringBetween(reference, schemaName + ".", ".");
            var referenceColumnName = substringAfter(reference, tableName + ".");
            var schemaTable = substringBefore(reference, "." + referenceColumnName);
            var searchColumn = substringBetween(entry.getKey(), SEARCH_COLUMN, "]");
            return format(SUB_SELECT_TEMPLATE,
                    reference,
                    schemaTable,
                    schemaTable + "." + searchColumn,
                    entry.getValue());
        }

        return toVarchar(entry.getValue());
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
                .map(c -> c.contains("[") ? StringUtils.substringBefore(c, "[") : c)
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

}
