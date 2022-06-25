package ru.vez.iso.desktop.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponseDto<T> {

    boolean ok;
    T data;

}
