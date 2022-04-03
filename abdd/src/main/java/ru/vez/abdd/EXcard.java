package ru.vez.abdd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EXcard {
    private String key;
    private LocalDateTime start;
    private String desc;

}
