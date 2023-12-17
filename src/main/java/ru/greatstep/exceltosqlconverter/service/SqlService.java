package ru.greatstep.exceltosqlconverter.service;

import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface SqlService {

    void generateSql(List<Map<String, String>> objects, MultipartFile multipartFile);

    Resource generateSqlAndReturn(List<Map<String, String>> objects, MultipartFile multipartFile);

}
