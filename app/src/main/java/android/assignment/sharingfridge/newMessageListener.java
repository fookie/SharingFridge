package android.assignment.sharingfridge;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by EveLIn3 on 2016/11/24.
 */

public class newMessageListener implements RongIMClient.OnReceiveMessageListener{
    @Override
    public boolean onReceived(Message message, int i) {
        return false;
    }
}
