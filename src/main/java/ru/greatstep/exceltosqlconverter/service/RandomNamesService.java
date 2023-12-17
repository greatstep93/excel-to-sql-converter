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
import org.springframework.stereotype.Service;
import ru.greatstep.exceltosqlconverter.models.FakeName;
import ru.greatstep.exceltosqlconverter.models.FakeNameFromApi;
import ru.greatstep.exceltosqlconverter.utils.WebClientHelper;

@Service
@RequiredArgsConstructor
public class RandomNamesService {

    private final WebClientHelper webClientHelper;
    private final ObjectMapper objectMapper;
    private static final List<String> PARAMS = List.of("FirstName", "LastName", "FatherName");
    private static final String API = "https://api.randomdatatools.ru";
    private static final Integer MAX_COUNT = 100;

    @SneakyThrows
    public List<FakeName> getFakeNames(Integer count) {
        count = count == null ? 1 : count;
        if (count <= MAX_COUNT) {
            return getFakeNamesFromApi(count).stream().map(this::toFakeName).toList();
        } else {
            List<FakeNameFromApi> fakeNamesFromApi = new ArrayList<>();
            while (count > 0) {
                var fakeNames = getFakeNamesFromApi(count);
                fakeNamesFromApi.addAll(fakeNames);
                count -= fakeNames.size();
            }
            return fakeNamesFromApi.stream().map(this::toFakeName).toList();
        }
    }

    @SneakyThrows
    private List<FakeNameFromApi> getFakeNamesFromApi(Integer count) {
        return count == 1 ? getSingletonFake() : getListFake(count);
    }

    private List<FakeNameFromApi> getSingletonFake() throws JsonProcessingException {
        var response = webClientHelper.webClientGetRequest(
                API,
                "",
                Map.of(),
                List.of(),
                getParamsWithoutCount(),
                JsonNode.class
        ).block();
        var fakeName = objectMapper.treeToValue(response, FakeNameFromApi.class);
        return List.of(fakeName);
    }

    private List<FakeNameFromApi> getListFake(Integer count) throws JsonProcessingException {
        var response = Optional.ofNullable(webClientHelper.webClientGetRequest(
                        API,
                        "",
                        Map.of(),
                        List.of(),
                        getParams(count),
                        ArrayNode.class).block())
                .orElseThrow();
        Iterator<JsonNode> itr = response.elements();
        List<FakeNameFromApi> fakeNameFromApis = new ArrayList<>();
        while (itr.hasNext()) {
            fakeNameFromApis.add(objectMapper.treeToValue(itr.next(), FakeNameFromApi.class));
        }
        return fakeNameFromApis;
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

    private FakeName toFakeName(FakeNameFromApi fromAPi) {

        return new FakeName(
                String.join(" ", fromAPi.getFirstName(), fromAPi.getFatherName(), fromAPi.getLastName()),
                fromAPi.getFirstName(),
                fromAPi.getFatherName(),
                fromAPi.getLastName()
        );
    }

}
