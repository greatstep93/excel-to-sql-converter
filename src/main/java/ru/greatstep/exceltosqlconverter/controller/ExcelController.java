package ru.greatstep.exceltosqlconverter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.greatstep.exceltosqlconverter.models.FakeName;
import ru.greatstep.exceltosqlconverter.service.ExcelService;
import ru.greatstep.exceltosqlconverter.service.RandomNamesService;

@RestController
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;
    private final RandomNamesService randomNamesService;

    @PostMapping(value = "/test", consumes = {"multipart/form-data"})
    public JsonNode test(@RequestParam("file") MultipartFile file) {
        return excelService.excelToSqlSaveFile(file);
    }

    @PostMapping(value = "/test2", consumes = {"multipart/form-data"}, produces = {"application/x-sql"})
    public ResponseEntity<Resource> test2(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelService.excelToSql(file));
    }

    @GetMapping(value = "/testFakeNames")
    public List<FakeName> testFakeNames(@RequestParam(value = "count", required = false) Integer count) {
        var result = randomNamesService.getFakeNames(count);
        System.out.println(result.stream().distinct().toList().size());
        return result;
    }

}
