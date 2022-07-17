package ru.vez.iso.desktop.main.operdays;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysDto;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        String json = UtilsHelper.readJsonFromFile("./operationDays-missed-enum-values.json");
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
    void givenIncorrectOperationDayStatus_whenMap_thenReturnValueUnknown() {

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
            assertEquals(OperDayStatus.UNKNOWN, d.getStatus());
        });
    }

}
