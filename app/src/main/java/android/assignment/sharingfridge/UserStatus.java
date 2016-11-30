package android.assignment.sharingfridge;

import android.location.Location;

/**
 * This class stores all the global properties of current users' status
 * By default the user is considered as not logged in and offline.
 */

public final class UserStatus {
    public static boolean hasLogin=false;
    public static String username="Click here to login";
    public static boolean inGroup=false;
    public static String groupName ="Offline Mode";
    public static boolean hasChanged=false;
    public static String token="";
    public static Location location;
    public static boolean needToUploadLoaction=false;
    public static boolean chatConnected = false;

    public static void resetStatus(){
        username = "Click here to login";
        inGroup = false;
        groupName = "Offline Mode";
        hasLogin = false;//do not reset location here
        chatConnected = false;
        token="";
    }

}
