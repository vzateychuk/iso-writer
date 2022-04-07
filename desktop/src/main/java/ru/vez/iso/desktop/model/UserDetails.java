package ru.vez.iso.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private String login;
    private String name;
    private String password;
    private String token;

    public boolean isLogged() {
        return token != null && !token.isEmpty();
    }
}
