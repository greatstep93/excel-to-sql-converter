package ru.greatstep.exceltosqlconverter.service.impl;

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
import ru.greatstep.exceltosqlconverter.service.DataRandomIntegrationService;
import ru.greatstep.exceltosqlconverter.utils.WebClientHelper;

@Service
@RequiredArgsConstructor
public class DataRandomIntegrationServiceImpl implements DataRandomIntegrationService {

    private final WebClientHelper webClientHelper;
    private final ObjectMapper objectMapper;
    private static final List<String> PARAMS = List.of("FirstName", "LastName", "FatherName", "Phone", "Login", "Email",
            "DateOfBirth", "PasportNum", "Address", "Country", "Region", "City", "Street", "House", "Apartment");

    @Value("${external-api.random-data-tools}")
    private String host;

    private static final Integer MAX_COUNT = 100;

    @Override
    public List<FakeName> getFakeNames(Integer count) {
        count = count == null || count == 0 ? 1 : count;
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

    private List<FakeName> getFakeNamesFromApi(Integer count) {
        return count == 1 ? getSingletonFake() : getListFake(count);
    }

    @SneakyThrows(JsonProcessingException.class)
    private List<FakeName> getSingletonFake() {
        var response = getFake(JsonNode.class, getParamsWithoutCount());
        var fakeName = objectMapper.treeToValue(response, FakeName.class);
        return List.of(fakeName);
    }

    @SneakyThrows(JsonProcessingException.class)
    private List<FakeName> getListFake(Integer count) {
        var response = Optional.ofNullable(getFake(ArrayNode.class, getParams(count))).orElseThrow();
        Iterator<JsonNode> itr = response.elements();
        List<FakeName> FakeNames = new ArrayList<>();
        while (itr.hasNext()) {
            FakeNames.add(objectMapper.treeToValue(itr.next(), FakeName.class));
        }
        return FakeNames;
    }

    private <T> T getFake(Class<T> respClass, Map<String, Object> params) {
        return webClientHelper.getRequest(host, params, respClass).block();
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
