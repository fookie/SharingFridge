package android.assignment.sharingfridge;

import android.app.Application;

import net.gotev.uploadservice.BuildConfig;
import net.gotev.uploadservice.UploadService;

import io.rong.imkit.RongIM;


/**
 * Store some global variables and initiate some in-app service when the application starts.
 */

public class SharingFridgeApplication extends Application {
    private String serverAddr = "http://178.62.93.103/SharingFridge/";

    public void onCreate() {
        super.onCreate();
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        RongIM.init(this);
    }

    public String getServerAddr() {
        return serverAddr;
    }

}
