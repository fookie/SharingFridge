package android.assignment.sharingfridge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * The activity for instant messaging, the actual content is in the fragment inside the layout.
 */
public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
