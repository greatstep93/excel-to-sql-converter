package ru.greatstep.exceltosqlconverter.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import lombok.SneakyThrows;
import ru.greatstep.exceltosqlconverter.models.FakeName;

public class FakeNameDeserializer extends JsonDeserializer<FakeName> {

    @Override
    @SneakyThrows({IOException.class})
    public FakeName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonNode node = jsonParser.readValueAsTree();
        return new FakeName(
                getFullName(node),
                node.get("FirstName") != null ? node.get("FirstName").asText() : null,
                node.get("FatherName") != null ? node.get("FatherName").asText() : null,
                node.get("LastName") != null ? node.get("LastName").asText() : null,
                node.get("Phone") != null ? node.get("Phone").asText() : null,
                node.get("Login") != null ? node.get("Login").asText() : null,
                node.get("Email") != null ? node.get("Email").asText() : null,
                node.get("DateOfBirth") != null ? node.get("DateOfBirth").asText() : null,
                node.get("PasportNum") != null ? node.get("PasportNum").asText() : null,
                node.get("Address") != null ? node.get("Address").asText() : null,
                node.get("Country") != null ? node.get("Country").asText() : null,
                node.get("Region") != null ? node.get("Region").asText() : null,
                node.get("City") != null ? node.get("City").asText() : null,
                node.get("Street") != null ? node.get("Street").asText() : null,
                node.get("House") != null ? node.get("House").asText() : null,
                node.get("Apartment") != null ? node.get("Apartment").asText() : null
        );
    }

    private String getFullName(JsonNode node) {
        return String.join(" ",
                node.get("FirstName") != null ? node.get("FirstName").asText() : null,
                node.get("FatherName") != null ? node.get("FatherName").asText() : null,
                node.get("LastName") != null ? node.get("LastName").asText() : null);
    }

}
