package ru.vez.iso.desktop.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {

    public final static UserDetails NOT_SIGNED_USER = new UserDetails();

    private String username = "";
    private String password = "";
    private String token = "";

    public boolean isLogged() {
        return token != null && !token.isEmpty();
    }
}
