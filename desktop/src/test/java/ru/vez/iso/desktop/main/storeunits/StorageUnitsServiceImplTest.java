package ru.vez.iso.desktop.main.storeunits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.http.StorageUnitsHttpClient;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.vez.iso.desktop.main.storeunits.StorageUnitsServiceImpl.API_STORAGE_UNITS;

@ExtendWith(MockitoExtension.class)
class StorageUnitsServiceImplTest {

    private static final String API = "https://host:port/api";

    @Mock private ApplicationState state;
    @Mock private StorageUnitsHttpClient httpClient;
    @Mock private DataMapper<StorageUnitDto, StorageUnitFX> mapper;
    @InjectMocks private StorageUnitsServiceImpl srv;

    @Captor private ArgumentCaptor<String> apiCaptor;
    @Captor private ArgumentCaptor<String> bodyCaptor;

    @BeforeEach
    void setUp() {
        AppSettings settings = AppSettings.builder().backendAPI(API).build();
        when(state.getSettings()).thenReturn(settings);
        UserDetails user = new UserDetails("username", "password", "token");
        when(state.getUserDetails()).thenReturn(user);
    }

    @Test
    void sendBurnComplete_WhenExceptionString_ThenShieldSpecialSymbols() {

        // arrange
        Exception e = new RuntimeException("com4j.ComException: ('error'): если !@#$%^&*=+_true.: .\\invoke.cpp:517 \"");
        final String objectId = "objectID";

        // act
        srv.sendBurnComplete(objectId, e);

        // assert
        Mockito.verify(httpClient).post(apiCaptor.capture(), eq("token"), bodyCaptor.capture());
        assertEquals(API + API_STORAGE_UNITS + "/" + objectId + "/recorded", apiCaptor.getValue());
        final String expectedBody = "{\"errorMessage\":\"com4j*ComException****error************$******_true*****invoke*cpp*517**\"}";
        assertEquals(expectedBody, bodyCaptor.getValue());
    }

    @Test
    void sendBurnComplete_WhenExceptionNull_ThenPostEmptyBody() {

        // arrange
        Exception e = null;
        final String objectId = "objectID";

        // act
        srv.sendBurnComplete(objectId, e);

        // assert
        Mockito.verify(httpClient).post(apiCaptor.capture(), eq("token"), bodyCaptor.capture());
        assertEquals(API + API_STORAGE_UNITS + "/" + objectId + "/recorded", apiCaptor.getValue());
        assertEquals("", bodyCaptor.getValue());
    }

}