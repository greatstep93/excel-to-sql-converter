package ru.greatstep.exceltosqlconverter.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

    JsonNode excelToSqlSaveFile(MultipartFile multipartFile);

    Resource excelToSql(MultipartFile multipartFile);

}
