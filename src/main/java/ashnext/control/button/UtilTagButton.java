package ashnext.control.button;

import ashnext.control.ActionTagButton;
import ashnext.control.GroupTag;
import ashnext.control.TypeTag;
import java.util.Arrays;
import java.util.List;

public class UtilTagButton {

    private UtilTagButton() {
    }

    public static TagButton getButton(String data) {
        List<String> dataList = Arrays.stream(data.split(":")).toList();

        return new TagButton(
                GroupTag.fromString(dataList.get(1)),
                TypeTag.fromString(dataList.get(2)),
                ActionTagButton.fromString(dataList.get(3)),
                Integer.parseInt(dataList.get(4)),
                dataList.size() < 6 ? null : dataList.get(5)
        );
    }
}
