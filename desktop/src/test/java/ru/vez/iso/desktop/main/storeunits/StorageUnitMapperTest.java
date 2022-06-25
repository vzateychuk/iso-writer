package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitHttpResponse;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsDto;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StorageUnitMapperTest {

    private StorageUnitMapper mapper;

    @BeforeEach
    void setUp() {
        // create Mapper
        mapper = new StorageUnitMapper();
    }

    @Test
    void whenMapStorageUnitDto_thenReturnStorageUnitFX() {

        // Arrange
        String json = UtilsHelper.readJsonFromFile("noop/data/storageUnits.json");
        StorageUnitHttpResponse response = new Gson().fromJson(json, StorageUnitHttpResponse.class);

        StorageUnitsDto dto = response.getData();

        // Act
        List<StorageUnitFX> listDays = dto.getObjects().stream()
                .peek(su -> System.out.println(su.getObjectId()))
                .map(mapper::map)
                .collect(Collectors.toList());

        // Assert
        assertEquals(dto.getCount(), listDays.size());
        listDays.forEach(su -> {
            assertNotNull(su.getObjectId());
            assertNotNull(su.getOperatingDayId());
            assertNotNull(su.getCreationDate());
            assertNotNull(su.getNumberSu());
            assertNotNull(su.getStorageUnitStatus());
        });
    }
}
