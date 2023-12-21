package ru.greatstep.exceltosqlconverter.controller;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.greatstep.exceltosqlconverter.controller.test_files.PathConstants.RANDOM_ONE_RESP;
import static ru.greatstep.exceltosqlconverter.controller.test_files.PathConstants.RANDOM_RESP;
import static ru.greatstep.exceltosqlconverter.controller.test_files.PathConstants.SQL_TO_EXCEL_REQ;
import static ru.greatstep.exceltosqlconverter.controller.test_files.PathConstants.SQL_TO_EXCEL_RESP;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.greatstep.exceltosqlconverter.service.DataRandomIntegrationService;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ExcelControllerTest {

    @Autowired
    private WebApplicationContext wac;
    public static MockWebServer mockDataRandom;
    @Autowired
    private DataRandomIntegrationService dataRandomIntegrationService;
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        mockDataRandom = new MockWebServer();
        mockDataRandom.start();
        String baseUrl = String.format("http://localhost:%s", mockDataRandom.getPort());
        ReflectionTestUtils.setField(dataRandomIntegrationService, "host", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockDataRandom.shutdown();
    }

    @Test
    void When_GetSqlFromExcel_Expect_Success() throws Exception {
        mockDataRandom.enqueue(new MockResponse()
                .setBody(readFile(RANDOM_RESP))
                .addHeader("Content-Type", "application/json"));
        mockMvc.perform(multipart("/getSqlFromExcel").file(getMockFile(SQL_TO_EXCEL_REQ)))
                .andExpectAll(
                        status().isOk(),
                        content().encoding(StandardCharsets.UTF_8),
                        content().string(readFile(SQL_TO_EXCEL_RESP))
                );
    }

    @Test
    void When_GetFakeNames_Expect_Success() throws Exception {
        mockDataRandom.enqueue(new MockResponse()
                .setBody(readFile(RANDOM_ONE_RESP))
                .addHeader("Content-Type", "application/json"));
        mockMvc.perform(get("/getFakeNames").param("count", "1"))
                .andExpectAll(
                        status().isOk());
    }

    private MockMultipartFile getMockFile(String filePath) {
        String name = StringUtils.substringAfterLast(filePath, "/");
        byte[] content = null;
        try {
            content = Files.readAllBytes(Path.of(filePath));
        } catch (final IOException ignored) {
        }
        return new MockMultipartFile("file", name, MediaType.MULTIPART_FORM_DATA_VALUE, content);
    }

    private byte[] getFileBytes(String filePath) {
        byte[] content = null;
        try {
            content = Files.readAllBytes(Path.of(filePath));
        } catch (final IOException ignored) {
        }
        return content;
    }

    private String readFile(String filePath) throws IOException {
        return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
    }

    private <T> T getObject(String filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(readFile(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}