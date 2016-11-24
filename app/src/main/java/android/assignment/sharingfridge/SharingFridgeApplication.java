package android.assignment.sharingfridge;

import android.app.Application;


import net.gotev.uploadservice.*;
import net.gotev.uploadservice.BuildConfig;

import io.rong.imkit.RongIM;


/**
 * Created by Paulay on 2016/10/25 0025.
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

    public void setServerAddr(String addr) {
        serverAddr = addr;
    }
}
