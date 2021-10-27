package ashnext.telegram.control.read_later;

import ashnext.telegram.control.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadLaterButton {

    private final Menu rlMenu = Menu.RL;
    private ActionReadLaterButton actionReadLaterButton;
    private String data = "";

    @Override
    public String toString() {
        return rlMenu.getText()
                + ":" + actionReadLaterButton.getText()
                + ":" + data;
    }
}
