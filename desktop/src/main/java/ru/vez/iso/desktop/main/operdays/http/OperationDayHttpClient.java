package ru.vez.iso.desktop.main.operdays.http;

import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;

import java.time.LocalDate;

/**
 * HttpClientWrapper for OperationDays to work with backend API
 * */
public interface OperationDayHttpClient {

    /**
     * Http request of operationDaysFX from backend
     * */
    OperationDaysHttpResponse loadOperationDays(String API, String token, LocalDate from);
}
