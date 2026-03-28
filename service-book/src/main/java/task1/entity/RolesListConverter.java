package task1.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class RolesListConverter implements AttributeConverter<List<Roles>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Roles> attribute) {
        try {
            if (attribute == null) {
                return "[]";
            }
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Roles> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            Roles[] arr = mapper.readValue(dbData, Roles[].class);
            return Arrays.asList(arr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
