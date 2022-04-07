module desktop {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires lombok;

    opens ru.vez.iso.desktop;
    opens ru.vez.iso.desktop.nav;
    opens ru.vez.iso.desktop.disks;
    opens ru.vez.iso.desktop.login;
    opens ru.vez.iso.desktop.main;
    opens ru.vez.iso.desktop.model;
}