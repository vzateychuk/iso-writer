package ru.vez.iso.desktop.shared;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppStateData<T> {

    private T value;

}
