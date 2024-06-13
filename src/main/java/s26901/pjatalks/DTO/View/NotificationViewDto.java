package s26901.pjatalks.DTO.View;

import s26901.pjatalks.DTO.Output.NotificationOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;

public class NotificationViewDto {
    public NotificationOutputDto notification;
    public String iconUrl;
    public UserOutputDto causer;
    public NotificationViewDto() {
    }

    public NotificationViewDto(NotificationOutputDto notification, String iconUrl, UserOutputDto causer) {
        this.notification = notification;
        this.iconUrl = iconUrl;
        this.causer = causer;
    }

    public NotificationOutputDto getNotification() {
        return notification;
    }

    public void setNotification(NotificationOutputDto notification) {
        this.notification = notification;
    }

    public String getIcon() {
        return iconUrl;
    }

    public void setIcon(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public UserOutputDto getCauser() {
        return causer;
    }

    public void setCauser(UserOutputDto causer) {
        this.causer = causer;
    }
}
