package ashnext.telegram.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Menu {
    TG("tg"),
    RL("rl");

    private final String text;

    public static Menu fromString(String text) {
        for (Menu startNameButton : Menu.values()) {
            if (startNameButton.text.equalsIgnoreCase(text)) {
                return startNameButton;
            }
        }

        return null;
    }
}
