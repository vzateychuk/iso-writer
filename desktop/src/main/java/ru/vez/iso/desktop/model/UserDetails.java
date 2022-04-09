package ru.vez.iso.desktop.model;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private String username = "";
    private String password = "";
    private String token = "";

    public static UserDetails NOT_SIGNED_USER = new UserDetails();

    public boolean isLogged() {
        return token != null && !token.isEmpty();
    }
}
