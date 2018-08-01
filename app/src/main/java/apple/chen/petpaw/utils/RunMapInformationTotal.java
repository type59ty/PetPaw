package apple.chen.petpaw.utils;

/**
 * Created by apple on 2017/10/29.
 */

public class RunMapInformationTotal {
    int totaltime;
    float totaldistance;
    int totalcount;
    float totalspeed;

    public RunMapInformationTotal() {

    }
    public RunMapInformationTotal(int totaltime, float totaldistance, int totalcount,float totalspeed) {
        this.totaltime = totaltime;
        this.totaldistance = totaldistance;
        this.totalcount = totalcount;
        this.totalspeed = totalspeed;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
    }

    public float getTotaldistance() {
        return totaldistance;
    }

    public void setTotaldistance(float totaldistance) {
        this.totaldistance = totaldistance;
    }

    public int getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(int totalcount) {
        this.totalcount = totalcount;
    }

    public float getTotalspeed() {
        return totalspeed;
    }

    public void setTotalspeed(float speed) {
        this.totalspeed = speed;
    }
}
