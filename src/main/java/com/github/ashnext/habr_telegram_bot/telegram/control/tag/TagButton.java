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
    private TypeTag typeTag;
    private ActionTagButton actionTagButton;
    private int page = 0;
    private String data = "";

    public TagButton(GroupTag groupTag, TypeTag typeTag, ActionTagButton actionTagButton, int page) {
        this(groupTag, typeTag, actionTagButton, page, "");
    }

    public TagButton(GroupTag groupTag, TypeTag typeTag, ActionTagButton actionTagButton) {
        this(groupTag, typeTag, actionTagButton, 0, "");
    }

    @Override
    public String toString() {
        return tgMenu.getText() +
                ":" + groupTag.getText() +
                ":" + typeTag.getText() +
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
            TagButton.this.typeTag = TypeTag.EMPTY;
            TagButton.this.actionTagButton = ActionTagButton.EMPTY;
        }

        public Builder setGroup(GroupTag groupTag) {
            TagButton.this.groupTag = groupTag;
            return this;
        }

        public Builder setGroupAll() {
            TagButton.this.groupTag = GroupTag.ALL_TAGS;
            return this;
        }

        public Builder setGroupWithoutMy() {
            TagButton.this.groupTag = GroupTag.WITHOUT_MY_TAGS;
            return this;
        }

        public Builder setGroupMy() {
            TagButton.this.groupTag = GroupTag.MY_TAGS;
            return this;
        }

        public Builder setType(TypeTag typeTag) {
            TagButton.this.typeTag = typeTag;
            return this;
        }

        public Builder setTypeCommon() {
            TagButton.this.typeTag = TypeTag.COMMON;
            return this;
        }

        public Builder setTypeBlog() {
            TagButton.this.typeTag = TypeTag.BLOG;
            return this;
        }

        public Builder setAction(ActionTagButton actionTagButton) {
            TagButton.this.actionTagButton = actionTagButton;
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

        public Builder setActionAdd() {
            TagButton.this.actionTagButton = ActionTagButton.ADD;
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