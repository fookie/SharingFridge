package android.assignment.sharingfridge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class GroupActivity extends AppCompatActivity {

    private Button submit=null;
    private EditText groupname;
    private RadioButton joingroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group_layout);
        submit=(Button)findViewById(R.id.join_group_submit);
        groupname=(EditText)findViewById(R.id.groupname_edittext);
        joingroup=(RadioButton)findViewById(R.id.join_group_radio);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
