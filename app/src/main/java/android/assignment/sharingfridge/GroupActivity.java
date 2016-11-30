package android.assignment.sharingfridge;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The class for group activity that shows a list of members in the user's group.
 */
public class GroupActivity extends AppCompatActivity {

    private Button submit = null;
    private EditText groupname;
    private RadioButton joingroup;
    private TextView hint;
    private SendRequestTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group_layout);
        submit = (Button) findViewById(R.id.join_group_submit);
        groupname = (EditText) findViewById(R.id.groupname_edittext);
        joingroup = (RadioButton) findViewById(R.id.join_group_radio);
        hint = (TextView) findViewById(R.id.in_group_hint);
        submit.setOnClickListener(new submitClick());
    }

    protected void onResume() {
        if (!UserStatus.hasLogin) {//check if user has login
            hint.setText(getString(R.string.no_group_hint));
        }
        if (UserStatus.inGroup) {
            hint.setText(String.format(getString(R.string.in_group_hint), UserStatus.groupName));
        }

        super.onResume();
    }

    private class submitClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!UserStatus.hasLogin) {
                return;
            }
            if (groupname.getText().toString().equals("")) {
                groupname.setError(getString(R.string.need_group_name));
                return;
            }
            mAuthTask = new SendRequestTask(joingroup.isChecked() ? "join" : "create", groupname.getText().toString());
            mAuthTask.execute();

        }
    }

    /**
     * send join/add group request to server
     */
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/group.php";
        private String action, groupname;

        public SendRequestTask(String _action, String _groupname) {
            action = _action;
            groupname = _groupname;
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        public String performPostCall() {
            Log.d("send post", "performPostCall");
            String response = "";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);/* milliseconds */
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //make json object
                JSONObject jo = new JSONObject();
                jo.put("action", action);
                jo.put("username", UserStatus.username);
                jo.put("groupname", groupname);
                String tosend = jo.toString();
                Log.d("JSON", tosend);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("group=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = conn.getResponseCode();

                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                int length = 500;
                String contentAsString = convertInputStreamToString(inputStream, length);
                return contentAsString;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        public String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            String permission;
            try {
                JSONObject confirm = new JSONObject(result);
                permission = confirm.get("permission").toString();
                if (permission.equals("granted")) {
                    Log.d("GROUP", action + " SUCCESS");
                    Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                    UserStatus.inGroup = true;
                    UserStatus.groupName = groupname;
                    finish();
                } else {
                    Log.d("GROUP", action + "FAILED");
                    Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException je) {
                je.printStackTrace();
                Log.d("GROUP", action + "FAILED");
                Toast.makeText(getApplicationContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
