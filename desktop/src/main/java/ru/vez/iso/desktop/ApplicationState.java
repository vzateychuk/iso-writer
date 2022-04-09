package ru.vez.iso.desktop;

import lombok.Data;
import ru.vez.iso.desktop.model.UserDetails;

@Data
public class ApplicationState {

    public static final String USER_DETAILS = "userDetails";

    private UserDetails userDetails = UserDetails.NOT_SIGNED_USER;

}
