package ru.vez.iso.desktop.shared;

import java.util.Observer;

public interface MessageSrv {

    void addObserver(Observer o);

    void deleteObserver(Observer o);

    void news(String str);
}
