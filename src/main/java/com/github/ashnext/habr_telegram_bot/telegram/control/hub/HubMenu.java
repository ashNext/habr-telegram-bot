package com.github.ashnext.habr_telegram_bot.telegram.control.hub;

import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardButton;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardMarkup;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HubMenu {

    private HubMenu() {
    }

    public static HubButton getButton(String data) {
        List<String> dataList = Arrays.stream(data.split(":")).toList();

        return new HubButton(
                GroupHub.fromString(dataList.get(1)),
                TypeHub.fromString(dataList.get(2)),
                ActionHubButton.fromString(dataList.get(3)),
                Integer.parseInt(dataList.get(4)),
                dataList.size() < 6 ? null : dataList.get(5)
        );
    }

    public static InlineKeyboardMarkup getHubManagementButtons() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("All common",
                        HubButton.newBuilder().setGroupAll().setTypeCommon().setActionShow().build().toString(), ""),
                new InlineKeyboardButton("All company blogs",
                        HubButton.newBuilder().setGroupAll().setTypeBlog().setActionShow().build().toString(), ""),
        }, {
                new InlineKeyboardButton("Without my common",
                        HubButton.newBuilder().setGroupWithoutMy().setTypeCommon().setActionShow().build().toString(),
                        ""),
                new InlineKeyboardButton("Without my company blogs",
                        HubButton.newBuilder().setGroupWithoutMy().setTypeBlog().setActionShow().build().toString(),
                        ""),
        }, {
                new InlineKeyboardButton("My common",
                        HubButton.newBuilder().setGroupMy().setTypeCommon().setActionShow().build().toString(), ""),
                new InlineKeyboardButton("My company blogs",
                        HubButton.newBuilder().setGroupMy().setTypeBlog().setActionShow().build().toString(), "")
        }, {
                new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }

    public static InlineKeyboardMarkup getHubsPageableButtons(Page<Hub> pageHubs, HubButton pressedHubButton) {
        ActionHubButton action;
        switch (pressedHubButton.getGroupHub()) {
            case ALL_HUBS, WITHOUT_MY_HUBS -> action = ActionHubButton.ADD;
            case MY_HUBS -> action = ActionHubButton.REMOVE;
            default -> throw new IllegalStateException("Unexpected value: " + pressedHubButton.getGroupHub());
        }

        List<Hub> hubs;
        int page = 0;
        if (pageHubs != null && pageHubs.hasContent()) {
            hubs = pageHubs.getContent();
            page = pageHubs.getNumber();
        } else {
            hubs = List.of();
        }

        int kbCountLines = hubs.size() % 2 == 0 ? hubs.size() / 2 : hubs.size() / 2 + 1;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[kbCountLines + 1][];

        Iterator<Hub> iterator = hubs.iterator();
        for (int i = 0; i < kbCountLines; i++) {
            if (i == kbCountLines - 1 && hubs.size() % 2 != 0) {
                buttons[i] = new InlineKeyboardButton[1];
            } else {
                buttons[i] = new InlineKeyboardButton[2];
            }

            for (int j = 0; j < 2 && iterator.hasNext(); j++) {
                Hub hub = iterator.next();
                String buttonCaption = hub.getName();
                if (buttonCaption.startsWith("Блог компании")) {
                    buttonCaption = buttonCaption.substring(14);
                }

                InlineKeyboardButton button =
                        new InlineKeyboardButton(buttonCaption,
                                HubButton.newBuilder()
                                        .setGroup(pressedHubButton.getGroupHub())
                                        .setType(pressedHubButton.getTypeHub())
                                        .setAction(action)
                                        .setPage(page)
                                        .setData(hub.getId().toString())
                                        .build().toString(),
                                ""
                        );
                buttons[i][j] = button;
            }
        }

        HubButton.Builder controlButtonBuilder = HubButton.newBuilder()
                .setGroup(pressedHubButton.getGroupHub())
                .setType(pressedHubButton.getTypeHub())
                .setActionShow();

        if (pageHubs != null && pageHubs.getTotalPages() != 0) {
            buttons[kbCountLines] = new InlineKeyboardButton[6];
            buttons[kbCountLines][4] = new InlineKeyboardButton(
                    pageHubs.isLast() ? "" : "\u25B6", controlButtonBuilder.setPage(page + 1).build().toString(), "");
            buttons[kbCountLines][5] = new InlineKeyboardButton(
                    pageHubs.isLast() ? "" : "\u23E9",
                    controlButtonBuilder.setPage(pageHubs.getTotalPages() - 1).build().toString(), "");
        } else {
            buttons[kbCountLines] = new InlineKeyboardButton[4];
        }

        buttons[kbCountLines][0] = new InlineKeyboardButton(
                page == 0 ? "" : "\u23EA", controlButtonBuilder.setPage(0).build().toString(), "");
        buttons[kbCountLines][1] = new InlineKeyboardButton(
                page == 0 ? "" : "\u25C0", controlButtonBuilder.setPage(page - 1).build().toString(), "");
        buttons[kbCountLines][2] = new InlineKeyboardButton("\u21A9", HubButton.newBuilder().setActionManagement().build().toString(), "");
        buttons[kbCountLines][3] = new InlineKeyboardButton("\uD83C\uDD91", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }
}
