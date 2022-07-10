package ru.vez.iso.desktop.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * File stored in file cache
 * */
@Getter
@AllArgsConstructor
public class FileISO {
    private final String fileName;
    private final LocalDate createdAt;
}
