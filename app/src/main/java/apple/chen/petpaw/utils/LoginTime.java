package apple.chen.petpaw.utils;

/**
 * Created by apple on 2017/10/29.
 */

public class LoginTime {
    int loginDate;
    int loginMonth;
    int loginYear;

    public LoginTime() {

    }
    public LoginTime(int loginDate, int loginMonth, int loginYear) {
        this.loginDate = loginDate;
        this.loginMonth = loginMonth;
        this.loginYear = loginYear;
    }

    public int getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(int loginDate) {
        this.loginDate = loginDate;
    }

    public int getLoginMonth() {
        return loginMonth;
    }

    public void setLoginMonth(int loginMonth) {
        this.loginMonth = loginMonth;
    }

    public int getLoginYear() {
        return loginYear;
    }

    public void setLoginYear(int loginYear) {
        this.loginYear = loginYear;
    }
}
