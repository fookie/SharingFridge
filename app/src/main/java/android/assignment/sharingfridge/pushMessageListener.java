package android.assignment.sharingfridge;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.notification.PushNotificationMessage;

/**
 * Created by EveLIn3 on 2016/11/24.
 */

public class pushMessageListener implements RongIMClient.OnReceivePushMessageListener{
    @Override
    public boolean onReceivePushMessage(PushNotificationMessage pushNotificationMessage) {
        return false;
    }
}
