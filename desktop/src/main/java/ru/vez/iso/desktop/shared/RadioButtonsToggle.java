package ru.vez.iso.desktop.shared;

import javafx.scene.control.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер radio-buttons
 * Сделан для внешнего вида кнопочек, требуемых заказчиком
 * */
public class RadioButtonsToggle {

    public Map<Button, PairButton<Button, Button>> buttonPairs = new HashMap<>();

    public void add(Button outerButton, Button innerButton) {
        final PairButton<Button, Button> pair = new PairButton<>(outerButton, innerButton);
        buttonPairs.put(outerButton, pair);
    }

    public void setActive(Button active) {

        if (buttonPairs.containsKey(active)) {

            for (Button but : buttonPairs.keySet()) {
                buttonPairs.get(but).setActive(false);
            }

            buttonPairs.get(active).setActive(true);
        }
    }

}

class PairButton<A extends Button, B extends Button> {
    A outerButton;
    B innerButton;

    public PairButton(A outerButton, B innerButton) {
        this.outerButton = outerButton;
        this.innerButton = innerButton;
    }

    public void setActive(boolean active){
        this.outerButton.getStyleClass().clear();
        final String styleClass = active ? "desktop-radio-active" : "desktop-radio";
        this.outerButton.getStyleClass().add(styleClass);
        this.innerButton.setVisible(active);
    }
}
