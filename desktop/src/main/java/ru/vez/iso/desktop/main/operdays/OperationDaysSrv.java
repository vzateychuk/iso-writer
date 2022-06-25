package ru.vez.iso.desktop.main.operdays;

import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get OperationDays from backend
 * */
public interface OperationDaysSrv {

    default String getAuthTokenOrException(ApplicationState state) {
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            throw new IllegalStateException("Not authenticated");
        }
        return userData.getToken();
    }

    /**
     * Load list of operationDaysFX from backend
     * */
    List<OperatingDayFX> loadOperationDays(LocalDate from);
}
