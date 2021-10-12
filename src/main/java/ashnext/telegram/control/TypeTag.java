package ashnext.telegram.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TypeTag {
    COMMON("c"),
    BLOG("b");

    private final String text;

    public static TypeTag fromString(String text) {
        for (TypeTag typeTag : TypeTag.values()) {
            if (typeTag.text.equalsIgnoreCase(text)) {
                return typeTag;
            }
        }

        return null;
    }
}
