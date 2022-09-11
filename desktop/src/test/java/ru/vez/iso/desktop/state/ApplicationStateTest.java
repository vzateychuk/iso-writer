package ru.vez.iso.desktop.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationStateTest {

    private ApplicationState state;

    @BeforeEach
    void setUp() {
        this.state = new ApplicationState();
    }

    @Test
    void givenNotLoading_whenAddLoad_thenIsLoadingTrue() {

        // given
        final String fileName = "File_Name";
        assertFalse( this.state.isLoading(fileName) );

        // when
        this.state.addLoading(fileName);

        // then
        assertTrue( this.state.isLoading(fileName) );
    }

}