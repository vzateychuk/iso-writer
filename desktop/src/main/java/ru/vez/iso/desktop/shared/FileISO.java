package ru.vez.iso.desktop.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * File stored in file cache
 * */
@Getter
@AllArgsConstructor
public class FileISO {
    private final String fileName;
    private final LocalDate createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileISO fileISO = (FileISO) o;
        return fileName.equals(fileISO.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
