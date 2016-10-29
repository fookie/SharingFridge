package android.assignment.sharingfridge;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FridgeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FridgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FridgeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int JSON_MAX_SIZE = 50000;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SendRequestTask mAuthTask = null;
    private List<FridgeItem> fridgeItemList;
    private GridLayoutManager gridLayoutManager;
    private FridgeViewAdapter fridgeViewAdapter;
    private SQLiteDatabase mainDB;
    private OnFragmentInteractionListener mListener;
    private boolean isDataLoaded= false;


    public FridgeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FridgeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FridgeFragment newInstance(String param1, String param2) {
        FridgeFragment fragment = new FridgeFragment();
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
        fridgeItemList = refreshFridgeList();
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        fridgeViewAdapter = new FridgeViewAdapter(getContext(), fridgeItemList, ((SharingFridgeApplication) getContext().getApplicationContext()).getServerAddr());
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fridge, container, false);

        RecyclerView fridgeView = (RecyclerView) v.findViewById(R.id.fridgeView);
        fridgeView.setHasFixedSize(true);
        fridgeView.setLayoutManager(gridLayoutManager);
        fridgeView.setAdapter(fridgeViewAdapter);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            //Here might need a listener for UI update(referring to TimelineFragment)
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        Log.d("database","create table");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mainDB != null) {
            mainDB.close();
        }
        mAuthTask=null;
    }

    public void onResume(){
        super.onResume();
        refreshFridgeList();
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public List<FridgeItem> refreshFridgeList() {
        if(!isDataLoaded){
            Log.d("database","database updating..");
            updateFromServer();
        }else{
            Log.d("database","did not update,using local database");
        }

        List<FridgeItem> list = new ArrayList<>();
        Cursor cursor = mainDB.rawQuery("SELECT * from items where groupname = '"+UserStatus.grouoname+"'",null);
        while(cursor.moveToNext()){
            String expday="Unkonwn";
            Calendar cal= Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date nd=cal.getTime();
                Date ed=df.parse(cursor.getString(cursor.getColumnIndex("expiretime")));
                long days = (ed.getTime() - nd.getTime())/(1000*60*60*24);
                expday=days+1+((days+1<=1)?" day left":" days left");//+1 ensure expire today shows 0 days
            } catch (ParseException e) {
                e.printStackTrace();
            }
            FridgeItem tempfi=new FridgeItem(cursor.getString(cursor.getColumnIndex("item")),expday,cursor.getString(cursor.getColumnIndex("imageurl")));


            list.add(tempfi);
        }
        cursor.close();
        if(fridgeViewAdapter!=null) {
            fridgeViewAdapter.notifyDataSetChanged();
        }
        return list;
    }

    private void updateFromServer(){
        if(!UserStatus.grouoname.equals("local")) {
            mAuthTask = new SendRequestTask(UserStatus.grouoname);
            mAuthTask.execute("");
        }else{
            isDataLoaded=false;
        }
    }

    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/refresh.php";
        private String groupname;
        private SQLiteDatabase taskDB;

        SendRequestTask(String gn) {
            groupname = gn;
            taskDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
            taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        String performPostCall() {
            Log.d("database","start posting...");
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
                jo.put("groupname", groupname);
                String tosend = jo.toString();
                Log.d("JSON", tosend);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("refresh=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                //int responseCode = conn.getResponseCode();

                InputStream inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                int length = JSON_MAX_SIZE;
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
            try {
                JSONArray jr = new JSONArray(result);
                taskDB.execSQL("delete from items where groupname != 'local'");
                for (int i = 0; i < jr.length(); i++) {
                    JSONObject jo = jr.getJSONObject(i);
                    try {//remove all the data except local group data
                        taskDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + jo.getString("item") + "', '" + jo.getString("category") + "', '" + jo.getString("amount") + "', '" + jo.getString("addtime") + "', '" + jo.getString("expiretime") + "', '" + jo.getString("imageurl") + "', '" + jo.getString("owner") + "', '" + jo.getString("groupname") + "')");
                        //Log.d("database","inserting: "+jo.getString("item"));
                    } catch (SQLException e) {
                        Log.d("database","error:"+e.toString());
                    }
                }
                Log.d("database","finish download from server!");
                isDataLoaded=true;
                refreshFridgeList();
            } catch (Exception je) {
                //je.printStackTrace();
                Log.d("database", "Excption when refreshing :" + je);
                isDataLoaded=false;
            }
            taskDB.close();
//
        }
    }
}
