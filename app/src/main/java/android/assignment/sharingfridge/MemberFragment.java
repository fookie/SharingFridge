package android.assignment.sharingfridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;


public class MemberFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //stuffs that join layout would use
    private Button submit=null;
    private SendRequestTask mAuthTask = null;
    private EditText groupname;
    private RadioButton joingroup;

    private List<MemberItem> memberItemList;
    private MemberViewAdapter memberViewAdapter;
    RecyclerView memberView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SQLiteDatabase mainDB;

    private OnLoginStatusListener loginRefreshListener;

    public MemberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberFragment newInstance(String param1, String param2) {
        MemberFragment fragment = new MemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        memberItemList = initMemberList();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, "http://178.62.93.103/SharingFridge/");
        // Inflate the layout for this fragment

        View view = null;
        view = inflater.inflate(R.layout.fragment_member, container, false);
        memberView = (RecyclerView) view.findViewById(R.id.memberView);
        memberView.setHasFixedSize(true);
        memberView.setLayoutManager(gridLayoutManager);
        memberView.setAdapter(memberViewAdapter);

        return view;
    }

    public void updateUI(){
        memberItemList = initMemberList();
        memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, "http://178.62.93.103/SharingFridge/");
        memberView.setAdapter(memberViewAdapter);
        memberViewAdapter.notifyDataSetChanged();
    }

    public void onResume(){
        super.onResume();
        updateUI();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnLoginStatusListener) {
//            loginRefreshListener = (OnLoginStatusListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnLoginStatusListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginRefreshListener = null;
//        if (mainDB != null) {
//            mainDB.close();
//        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginStatusListener {
        // TODO: Update argument type and name
        void refreshDueToLogin();
    }

    public List<MemberItem> initMemberList() {
        List<MemberItem> memberItems = new LinkedList<>();
        if (UserStatus.hasLogin == false) {
            memberItems.add(new MemberItem(getString(R.string.login_hint), getString(R.string.nousr_group_hint),"noimg"));
            return memberItems;
        }
        String sql = "select owner,count(*),sum(amount) as o from items where groupname='" + UserStatus.groupName + "' group by owner";
        Cursor c = mainDB.rawQuery(sql, null);
        while (c.moveToNext()) {
            String owner = c.getString(0);
            int count = c.getInt(1);
            int amount = c.getInt(2);
            memberItems.add(new MemberItem(owner, count + getString(R.string.group_sta_hint) + amount, owner + ".png"));
        }
        return memberItems;
    }

    private class submitClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(groupname.getText().toString().equals("")){
                groupname.setError(getString(R.string.need_group_name));
                return;
            }
            mAuthTask =new SendRequestTask(joingroup.isChecked()?"join":"create",groupname.getText().toString());
            mAuthTask.execute();
        }
    }

    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/group.php";
        private String action,groupname;
        public SendRequestTask(String _action, String _groupname) {
            action=_action;
            groupname=_groupname;
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
                //conn.setRequestProperty("Content-Type", "application/json");
                //make json object
                JSONObject jo = new JSONObject();
                jo.put("action", action);
                jo.put("username", UserStatus.username);
                jo.put("groupname",groupname);
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
                    Log.d("GROUP", action+" SUCCESS");
                    Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                    UserStatus.inGroup=true;
                    UserStatus.groupName=groupname;
                }else {
                    Log.d("GROUP",action+ "FAILED");
                    Toast.makeText(getContext(), getString(R.string.faild), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException je) {
                je.printStackTrace();
                Log.d("GROUP",action+ "FAILED");
                Toast.makeText(getContext(), getString(R.string.faild), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
