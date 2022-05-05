package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vez.iso.desktop.docs.reestr.Reestr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {

    private String json;
    private DocumentMapper mapper;

    @BeforeEach
    void setUp() {
        // read JSON
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("REESTR.json");
        BufferedReader readr = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        json = readr.lines().collect(Collectors.joining(System.getProperty("line.separator")));

        // create Mapper
        mapper = new DocumentMapperImpl();
    }

    @Test
    void ToDocumentFX() {

        // Arrange
        Reestr reestr = new Gson().fromJson(json, Reestr.class);

        // Act
        List<DocumentFX> docs = reestr.getDocs().stream().map(d -> mapper.mapToDocFX(d)).collect(Collectors.toList());

        // Assert
        assertEquals(6, docs.size());
    }
}
