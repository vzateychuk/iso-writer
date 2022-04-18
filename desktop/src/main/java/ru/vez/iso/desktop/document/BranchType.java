package ru.vez.iso.desktop.document;

/**
 * Подразделение / Филиал
 * */
public enum BranchType {
    HEAD_OFFICE("Головной офис"),
    REGIONAL_BRANCH("Региональный филиал"),
    UNKNOWN_BRANCH("Неизвестный филиал");

    private final String title;

    BranchType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
