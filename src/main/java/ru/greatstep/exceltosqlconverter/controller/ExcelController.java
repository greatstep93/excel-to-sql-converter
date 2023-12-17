package ru.greatstep.exceltosqlconverter.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
import ru.greatstep.exceltosqlconverter.service.RandomService;

@RestController
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;
    private final RandomService randomNamesService;

    @PostMapping(value = "/saveFile", consumes = {"multipart/form-data"})
    public JsonNode saveFile(@RequestParam("file") MultipartFile file) {
        return excelService.excelToSqlSaveFile(file);
    }

    @PostMapping(value = "/getSqlFromExcel", consumes = {"multipart/form-data"}, produces = {"application/x-sql"})
    public ResponseEntity<Resource> getSqlFromExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok()
                .header("Content-Type", "application/x-sql; charset=utf-8")
                .body(excelService.excelToSql(file));
    }

    @GetMapping(value = "/getFakeNames")
    public List<FakeName> getFakeNames(@RequestParam(value = "count", required = false) Integer count) {
        var result = randomNamesService.getFakeNames(count);
        System.out.println(result.stream().distinct().toList().size());
        return result;
    }

}
