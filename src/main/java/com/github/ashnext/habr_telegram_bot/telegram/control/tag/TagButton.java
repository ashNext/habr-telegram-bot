package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import com.github.ashnext.habr_telegram_bot.telegram.control.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TagButton {

    private final Menu tgMenu = Menu.TG;
    private GroupTag groupTag;
    private ActionTagButton actionTagButton;
    private int page = 0;
    private String data = "";

    public TagButton(GroupTag groupTag, ActionTagButton actionTagButton, int page) {
        this(groupTag, actionTagButton, page, "");
    }

    public TagButton(GroupTag groupTag, ActionTagButton actionTagButton) {
        this(groupTag, actionTagButton, 0, "");
    }

    @Override
    public String toString() {
        return tgMenu.getText() +
                ":" + groupTag.getText() +
                ":" + actionTagButton.getText() +
                ":" + page +
                ":" + data;
    }

    public static Builder newBuilder() {
        return new TagButton().new Builder();
    }

    public class Builder {

        private Builder() {
            TagButton.this.groupTag = GroupTag.EMPTY;
            TagButton.this.actionTagButton = ActionTagButton.EMPTY;
        }

        public Builder setGroup(GroupTag groupHub) {
            TagButton.this.groupTag = groupHub;
            return this;
        }

        public Builder setGroupMy() {
            TagButton.this.groupTag = GroupTag.MY_TAGS;
            return this;
        }

        public Builder setGroupAdd() {
            TagButton.this.groupTag = GroupTag.NEW_TAG;
            return this;
        }

        public Builder setAction(ActionTagButton actionHubButton) {
            TagButton.this.actionTagButton = actionHubButton;
            return this;
        }

        public Builder setActionManagement() {
            TagButton.this.actionTagButton = ActionTagButton.MANAGEMENT;
            return this;
        }

        public Builder setActionShow() {
            TagButton.this.actionTagButton = ActionTagButton.SHOW;
            return this;
        }

        public Builder setActionAddNew() {
            TagButton.this.actionTagButton = ActionTagButton.NEW;
            return this;
        }

        public Builder setActionRemove() {
            TagButton.this.actionTagButton = ActionTagButton.REMOVE;
            return this;
        }

        public Builder setPage(int page) {
            TagButton.this.page = page;
            return this;
        }

        public Builder setData(String data) {
            TagButton.this.data = data;
            return this;
        }

        public TagButton build() {
            return TagButton.this;
        }
    }
}
