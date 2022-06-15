package ru.vez.iso.desktop.main.operdays;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OperationDayMapperTest {

    private String json;
    private OperationDayMapper mapper;

    @BeforeEach
    void setUp() {
        // read JSON
        json = UtilsHelper.readJsonFromFile("operationDays.json");

        // create Mapper
        mapper = new OperationDayMapper();
    }

    @Test
    void whenMapOperationDayDto_thenReturnOperatingDayFX() {

        // Arrange
        OperationDaysResponse response = new Gson().fromJson(json, OperationDaysResponse.class);

        // Act
        List<OperatingDayFX> listDays = response.getObjects().stream()
                .peek(d -> System.out.println(d.getObjectId()))
                .map(mapper::map)
                .collect(Collectors.toList());

        // Assert
        assertEquals(response.getCount(), listDays.size());
        listDays.forEach(d ->{
            assertNotNull(d.getObjectId());
            assertNotNull(d.getOperatingDay());
            assertNotNull(d.getTypeSu());
            assertNotNull(d.getStatus());
            assertNotNull(d.createdAtProperty());
        });
    }
}