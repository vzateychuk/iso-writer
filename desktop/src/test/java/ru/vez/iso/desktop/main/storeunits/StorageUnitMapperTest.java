package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StorageUnitMapperTest {

    private String json;
    private StorageUnitMapper mapper;

    @BeforeEach
    void setUp() {
        // read JSON
        json = UtilsHelper.readJsonFromFile("storageUnits.json");

        // create Mapper
        mapper = new StorageUnitMapper();
    }

    @Test
    void whenMapStorageUnitDto_thenReturnStorageUnitFX() {

        // Arrange
        StorageUnitsResponse response = new Gson().fromJson(json, StorageUnitsResponse.class);

        // Act
        List<StorageUnitFX> listDays = response.getObjects().stream()
                .peek(su -> System.out.println(su.getObjectId()))
                .map(mapper::map)
                .collect(Collectors.toList());

        // Assert
        assertEquals(response.getCount(), listDays.size());
        listDays.forEach(su -> {
            assertNotNull(su.getObjectId());
            assertNotNull(su.getOperatingDayId());
            assertNotNull(su.getCreationDate());
            assertNotNull(su.getNumberSu());
            assertNotNull(su.getStorageUnitStatus());
        });
    }
}