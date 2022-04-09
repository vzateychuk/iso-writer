package ru.vez.iso.desktop.state;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppStateData<T> {

    private T value;

}
