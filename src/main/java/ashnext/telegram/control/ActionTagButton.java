package ashnext.telegram.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionTagButton {
    SHOW("s"),
    ADD("a"),
    REMOVE("r");

    private final String text;

    public static ActionTagButton fromString(String text) {
        for (ActionTagButton actionTagButton : ActionTagButton.values()) {
            if (actionTagButton.text.equalsIgnoreCase(text)) {
                return actionTagButton;
            }
        }

        return null;
    }
}
