package ashnext.telegram.control.read_later;

import ashnext.model.ReadLater;
import ashnext.telegram.api.types.InlineKeyboardButton;
import ashnext.telegram.api.types.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.List;

public class ReadLaterMenu {

    private ReadLaterMenu() {
    }

    public static InlineKeyboardMarkup getButtonsWithAdd() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{{
                        new InlineKeyboardButton("\uD83D\uDCE5", ReadLaterButton.builder()
                                .actionReadLaterButton(ActionReadLaterButton.PUT)
                                .build().toString(), ""),
                        new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
                }}
        );
    }

    public static InlineKeyboardMarkup getButtonsWithRemove() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{{
                        new InlineKeyboardButton("\uD83D\uDCE4", ReadLaterButton.builder()
                                .actionReadLaterButton(ActionReadLaterButton.PULL)
                                .build().toString(), ""),
                        new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
                }}
        );
    }

    public static ReadLaterButton getButton(String data) {
        List<String> dataList = Arrays.stream(data.split(":")).toList();

        return new ReadLaterButton(
                ActionReadLaterButton.fromString(dataList.get(1)),
                dataList.size() < 3 ? null : dataList.get(2)
        );
    }

    public static InlineKeyboardMarkup getReadLaterButtons(List<ReadLater> readLaterList) {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[readLaterList.size() + 1][1];

        for (int i = 0; i < readLaterList.size(); i++) {
            InlineKeyboardButton button =
                    new InlineKeyboardButton(
                            readLaterList.get(i).getPostTitle(),
                            ReadLaterButton.builder()
                                    .actionReadLaterButton(ActionReadLaterButton.GET)
                                    .data(readLaterList.get(i).getId().toString())
                                    .build().toString(),
                            "");
            buttons[i][0] = button;
        }

        buttons[readLaterList.size()][0] = new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }
}
