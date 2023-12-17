package ru.greatstep.exceltosqlconverter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.greatstep.exceltosqlconverter.models.FakeName;
import ru.greatstep.exceltosqlconverter.utils.WebClientHelper;

@Service
@RequiredArgsConstructor
public class RandomNamesService {

    private final WebClientHelper webClientHelper;
    private final ObjectMapper objectMapper;
    private static final List<String> PARAMS = List.of("FirstName", "LastName", "FatherName");

    @Value("${external-api.random-data-tools}")
    private String host;

    private static final Integer MAX_COUNT = 100;

    @SneakyThrows
    public List<FakeName> getFakeNames(Integer count) {
        count = count == null ? 1 : count;
        if (count <= MAX_COUNT) {
            return getFakeNamesFromApi(count);
        } else {
            List<FakeName> fakeNamesFromApi = new ArrayList<>();
            while (count > 0) {
                var fakeNames = getFakeNamesFromApi(count);
                fakeNamesFromApi.addAll(fakeNames);
                count -= fakeNames.size();
            }
            return fakeNamesFromApi;
        }
    }

    @SneakyThrows
    private List<FakeName> getFakeNamesFromApi(Integer count) {
        return count == 1 ? getSingletonFake() : getListFake(count);
    }

    private List<FakeName> getSingletonFake() throws JsonProcessingException {
        var response = webClientHelper.getRequest(
                host,
                getParamsWithoutCount(),
                JsonNode.class
        ).block();
        var fakeName = objectMapper.treeToValue(response, FakeName.class);
        return List.of(fakeName);
    }

    private List<FakeName> getListFake(Integer count) throws JsonProcessingException {
        var response = Optional.ofNullable(
                        webClientHelper.getRequest(
                                        host,
                                        getParams(count),
                                        ArrayNode.class)
                                .block())
                .orElseThrow();
        Iterator<JsonNode> itr = response.elements();
        List<FakeName> FakeNames = new ArrayList<>();
        while (itr.hasNext()) {
            FakeNames.add(objectMapper.treeToValue(itr.next(), FakeName.class));
        }
        return FakeNames;
    }

    private Map<String, Object> getParamsWithoutCount() {
        return Map.of(
                "type", "all",
                "params", String.join(",", PARAMS)
        );
    }

    private Map<String, Object> getParams(Integer count) {
        return Map.of(
                "type", "all",
                "count", count > MAX_COUNT ? MAX_COUNT : count,
                "params", String.join(",", PARAMS)
        );
    }

}
