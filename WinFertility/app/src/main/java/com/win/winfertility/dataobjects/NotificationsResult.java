package com.win.winfertility.dataobjects;

/**
 * Created by ideamac on 05/11/17.
 */

public class NotificationsResult {
    public String getNotificationHeader() {
        return NotificationHeader;
    }

    public void setNotificationHeader(String notificationHeader) {
        NotificationHeader = notificationHeader;
    }

    public String getNotificationBody() {
        return NotificationBody;
    }

    public void setNotificationBody(String notificationBody) {
        NotificationBody = notificationBody;
    }

    public String getNotificationDate() {
        return NotificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        NotificationDate = notificationDate;
    }

    public String getRedirectScreen() {
        return RedirectScreen;
    }

    public void setRedirectScreen(String redirectScreen) {
        RedirectScreen = redirectScreen;
    }

    public String NotificationHeader;
    public String NotificationBody;
    public String NotificationDate;
    public String RedirectScreen;

    public String getRedirect_screen() {
        return redirect_screen;
    }

    public void setRedirect_screen(String redirect_screen) {
        this.redirect_screen = redirect_screen;
    }

    public String redirect_screen;

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String redirect;

}
