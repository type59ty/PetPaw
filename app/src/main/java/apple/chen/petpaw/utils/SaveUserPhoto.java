package apple.chen.petpaw.utils;

/**
 * Created by apple on 2017/11/4.
 */

public class SaveUserPhoto {
    String downloadUrl;

    public SaveUserPhoto() {
    }
    public SaveUserPhoto(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
