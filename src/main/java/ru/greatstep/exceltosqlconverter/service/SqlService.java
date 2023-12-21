package ru.greatstep.exceltosqlconverter.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.greatstep.exceltosqlconverter.models.ExcelContext;

public interface SqlService {

    void generateSql(ExcelContext context, MultipartFile multipartFile);

    Resource generateSqlAndReturn(ExcelContext excelContext, MultipartFile multipartFile);

}
