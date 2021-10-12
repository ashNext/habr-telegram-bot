package ashnext.telegram.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StartNameButton {
    TG("tg");

    private final String text;

    public static StartNameButton fromString(String text) {
        for (StartNameButton startNameButton : StartNameButton.values()) {
            if (startNameButton.text.equalsIgnoreCase(text)) {
                return startNameButton;
            }
        }

        return null;
    }
}
