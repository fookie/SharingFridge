package android.assignment.sharingfridge;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mahon on 2016/10/17.
 */


public class SendRequestTask extends AsyncTask<String, Void, String> {
    private String urlString = "http://178.62.93.103/SharingFridge/login.php";
    private String username, password;

    public SendRequestTask(String um, String pw) {
        username = um;
        password = pw;
    }

    protected String doInBackground(String... params) {
        String response = performPostCall();
        return response;
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
            //conn.setRequestProperty("Content-Type", "application/json");
            //make json object
            JSONObject jo = new JSONObject();
            jo.put("username", "laowang");
            jo.put("password", "233");
            String tosend = jo.toString();
            Log.d("JSON",tosend);
//            byte[] outputBytes = tosend.getBytes("UTF-8");
//            OutputStream os = conn.getOutputStream();
//            os.write(outputBytes);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            outputStreamWriter.write("login=" + tosend);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            int responseCode = conn.getResponseCode();

            InputStream inputStream = conn.getInputStream();

            // Convert the InputStream into a string
            int length = 500;
            String contentAsString = convertInputStreamToString(inputStream, length);
            return contentAsString;
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                String line;
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                while ((line = br.readLine()) != null) {
//                    response += line;
//                }
//            } else {
//                response = "";
//            }
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
        String permission="Nothing received";

        try {
            JSONObject confirm = new JSONObject(result);
            permission = confirm.get("permission").toString();
            if( permission.equals("granted")){
               Log.d("LOGIN","SUCCESS");
            }
        }
        catch (JSONException je){
            je.printStackTrace();
            Log.d("LOGIN","FAILED");
        }
        //Log.d("send post", permission);
    }
}
