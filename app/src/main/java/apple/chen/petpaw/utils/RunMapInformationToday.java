package apple.chen.petpaw.utils;

/**
 * Created by apple on 2017/10/29.
 */

public class RunMapInformationToday {
    int time;
    float distance;
    int count;
    float speed;

    public RunMapInformationToday() {

    }
    public RunMapInformationToday(int time, float distance, int count,float speed) {
        this.time = time;
        this.distance = distance;
        this.count = count;
        this.speed = speed;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
