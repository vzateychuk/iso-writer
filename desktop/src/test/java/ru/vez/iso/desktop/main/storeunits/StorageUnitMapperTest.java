package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitListResponse;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsDto;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
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
        String json = UtilsHelper.readJsonFromFile("./storageUnits-missed-enum-values.json");
        StorageUnitListResponse response = new Gson().fromJson(json, StorageUnitListResponse.class);

        StorageUnitsDto dto = response.getData();

        // Act
        List<StorageUnitFX> listDays = dto.getObjects().stream()
                .peek(
                        su -> log.debug("SU: '{}', OperDay: '{}'",
                                su.getObjectId(),
                                su.getOperatingDay().getObjectId()
                        )
                )
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
            assertTrue(su.isDeleted());
            assertTrue(su.isPresent());
        });
    }
}
