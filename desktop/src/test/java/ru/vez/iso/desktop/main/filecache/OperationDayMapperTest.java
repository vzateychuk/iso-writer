package ru.vez.iso.desktop.main.filecache;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.main.operdays.OperationDayMapper;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("operationDays.json");
        assert is != null;
        BufferedReader readr = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        json = readr.lines().collect(Collectors.joining(System.getProperty("line.separator")));

        // create Mapper
        mapper = new OperationDayMapper();
    }

    @Test
    void map_thenResponse() {

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