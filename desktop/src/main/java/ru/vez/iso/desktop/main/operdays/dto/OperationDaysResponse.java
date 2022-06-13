package ru.vez.iso.desktop.main.operdays.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OperationDays DTO wrapper received from backend
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationDaysResponse {

    private int count;
    private List<OperDayDto> objects;
}
