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
                node.get("FirstName").asText(),
                node.get("FatherName").asText(),
                node.get("LastName").asText()
        );
    }

    private String getFullName(JsonNode node) {
        return String.join(" ",
                node.get("FirstName").asText(),
                node.get("FatherName").asText(),
                node.get("LastName").asText());
    }

}
