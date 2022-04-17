package ru.vez.iso.desktop.document;

/**
 * Вид EX
 * used "Список операционных дней"
 * */
public enum DocType {
    PAYMENT_ORDER("Платежное поручение"),
    PAYMENT_REQUEST("Платежное требование"),
    BANK_WARRANT("Банковский ордер"),
    ACCOUNT_CASH_WARRANT("Расходный кассовый ордер"),
    COLLECTION_ORDER("Инкассовое поручение");

    private final String title;

    DocType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
