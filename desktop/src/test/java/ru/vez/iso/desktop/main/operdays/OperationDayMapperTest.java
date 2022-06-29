package ru.vez.iso.desktop.main.operdays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysDto;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

class OperationDayMapperTest {

    private OperationDayMapper mapper;

    @BeforeEach
    void setUp() {
        // create Mapper
        mapper = new OperationDayMapper();
    }

    @Test
    void whenMapOperationDayDto_thenReturnOperatingDayFX() {

        // Arrange
        String json = UtilsHelper.readJsonFromFile("noop/data/operationDays.json");
        OperationDaysHttpResponse response = new Gson().fromJson(json, OperationDaysHttpResponse.class);

        OperationDaysDto dtos = response.getData();

        // Act
        List<OperatingDayFX> listDays = dtos.getObjects().stream()
                .peek(d -> System.out.println(d.getObjectId()))
                .map(mapper::map)
                .collect(Collectors.toList());

        // Assert
        assertEquals(dtos.getCount(), listDays.size());
        listDays.forEach(d ->{
            assertNotNull(d.getObjectId());
            assertNotNull(d.getOperatingDay());
            assertNotNull(d.getTypeSu());
            assertNotNull(d.getStatus());
            assertNotNull(d.createdAtProperty());
        });
    }

    @Test
    void givenMissedEnumValues_whenMap_thenReturnValue() {

        // Arrange
        String json = UtilsHelper.readJsonFromFile("operationDays-missed-enum-values.json");
        OperationDaysHttpResponse response = new Gson().fromJson(json, OperationDaysHttpResponse.class);

        OperationDaysDto dtos = response.getData();

        // Act
        List<OperatingDayFX> listDays = dtos.getObjects().stream()
            .peek(d -> System.out.println(d.getObjectId()))
            .map(mapper::map)
            .collect(Collectors.toList());

        // Assert
        assertEquals(dtos.getCount(), listDays.size());
        listDays.forEach(d ->{
            assertEquals(TypeSu.UNKNOWN, d.getTypeSu());
            assertEquals(OperDayStatus.UNKNOWN, d.getStatus());
        });
    }

}
