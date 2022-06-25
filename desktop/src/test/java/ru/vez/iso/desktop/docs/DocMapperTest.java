package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocMapperTest {

    private String json;
    private DocMapper mapper;

    @BeforeEach
    void setUp() {
        // read JSON
        this.json = UtilsHelper.readJsonFromFile("REESTR.json");

        // create Mapper
        this.mapper = new DocMapperImpl();
    }

    @Test
    void whenMapReestr_thenReturnDocumentFX() {

        // Arrange
        Reestr reestr = new Gson().fromJson(json, Reestr.class);

        // Act
        List<DocumentFX> docs = reestr.getDocs().stream().map(d -> mapper.mapToDocFX(d)).collect(Collectors.toList());

        // Assert
        assertEquals(docs.size(), docs.size());
    }
}
