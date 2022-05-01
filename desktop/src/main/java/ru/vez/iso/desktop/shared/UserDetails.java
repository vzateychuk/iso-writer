package ru.vez.iso.desktop.shared;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {

    public static UserDetails NOT_SIGNED_USER = new UserDetails();

    private String username = "";
    private String password = "";
    private String token = "";

    public boolean isLogged() {
        return token != null && !token.isEmpty();
    }
}
