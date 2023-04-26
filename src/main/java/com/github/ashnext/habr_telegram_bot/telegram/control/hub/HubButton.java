package com.github.ashnext.habr_telegram_bot.telegram.control.hub;

import com.github.ashnext.habr_telegram_bot.telegram.control.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HubButton {

    private final Menu hbMenu = Menu.HB;
    private GroupHub groupHub;
    private TypeHub typeHub;
    private ActionHubButton actionHubButton;
    private int page = 0;
    private String data = "";

    public HubButton(GroupHub groupHub, TypeHub typeHub, ActionHubButton actionHubButton, int page) {
        this(groupHub, typeHub, actionHubButton, page, "");
    }

    public HubButton(GroupHub groupHub, TypeHub typeHub, ActionHubButton actionHubButton) {
        this(groupHub, typeHub, actionHubButton, 0, "");
    }

    @Override
    public String toString() {
        return hbMenu.getText() +
                ":" + groupHub.getText() +
                ":" + typeHub.getText() +
                ":" + actionHubButton.getText() +
                ":" + page +
                ":" + data;
    }

    public static Builder newBuilder() {
        return new HubButton().new Builder();
    }

    public class Builder {

        private Builder() {
            HubButton.this.groupHub = GroupHub.EMPTY;
            HubButton.this.typeHub = TypeHub.EMPTY;
            HubButton.this.actionHubButton = ActionHubButton.EMPTY;
        }

        public Builder setGroup(GroupHub groupHub) {
            HubButton.this.groupHub = groupHub;
            return this;
        }

        public Builder setGroupAll() {
            HubButton.this.groupHub = GroupHub.ALL_HUBS;
            return this;
        }

        public Builder setGroupWithoutMy() {
            HubButton.this.groupHub = GroupHub.WITHOUT_MY_HUBS;
            return this;
        }

        public Builder setGroupMy() {
            HubButton.this.groupHub = GroupHub.MY_HUBS;
            return this;
        }

        public Builder setType(TypeHub typeHub) {
            HubButton.this.typeHub = typeHub;
            return this;
        }

        public Builder setTypeCommon() {
            HubButton.this.typeHub = TypeHub.COMMON;
            return this;
        }

        public Builder setTypeBlog() {
            HubButton.this.typeHub = TypeHub.BLOG;
            return this;
        }

        public Builder setAction(ActionHubButton actionHubButton) {
            HubButton.this.actionHubButton = actionHubButton;
            return this;
        }

        public Builder setActionManagement() {
            HubButton.this.actionHubButton = ActionHubButton.MANAGEMENT;
            return this;
        }

        public Builder setActionShow() {
            HubButton.this.actionHubButton = ActionHubButton.SHOW;
            return this;
        }

        public Builder setActionAdd() {
            HubButton.this.actionHubButton = ActionHubButton.ADD;
            return this;
        }

        public Builder setActionRemove() {
            HubButton.this.actionHubButton = ActionHubButton.REMOVE;
            return this;
        }

        public Builder setPage(int page) {
            HubButton.this.page = page;
            return this;
        }

        public Builder setData(String data) {
            HubButton.this.data = data;
            return this;
        }

        public HubButton build() {
            return HubButton.this;
        }
    }
}
