package ru.vez.iso.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpDay {
    private Long id;
    private LocalDate date;
    private OpState state;
}
