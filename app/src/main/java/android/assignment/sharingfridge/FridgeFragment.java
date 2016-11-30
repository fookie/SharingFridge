package android.assignment.sharingfridge;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;


/**
 * The fragment class for the main fridge items displaying
 */
public class FridgeFragment extends Fragment {
    private SendRequestTask mAuthTask = null;
    private RecyclerView fridgeView;
    private List<FridgeItem> fridgeItemList;
    private GridLayoutManager gridLayoutManager;
    private FridgeViewAdapter fridgeViewAdapter;
    private SQLiteDatabase mainDB;
    private OnFragmentInteractionListener mListener;
    private boolean isDataLoaded = false;


    private static final String[] CATEGORYS = new String[]{"Fruit", "Vegetable", "Pork", "Chicken", "Beef", "Fish", "Others"};
    private static final String[] CATEGORYS_CHINESE = new String[]{"水果", "蔬菜", "猪肉", "鸡肉", "牛肉", "鱼肉", "其他"};

    public FridgeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fridge, container, false);

        fridgeView = (RecyclerView) v.findViewById(R.id.fridgeView);
        gridLayoutManager = new GridLayoutManager(getContext(), 1);
        fridgeView.setHasFixedSize(true);
        fridgeView.setLayoutManager(gridLayoutManager);
        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        Log.d("database", "create table if not exist");
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), getString(R.string.refreshing), LENGTH_SHORT).show();
                isDataLoaded = false;
                updateUI();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mainDB != null) {
            mainDB.close();
        }
        mAuthTask = null;
    }

    public void onResume() {
        super.onResume();
        if (!isDataLoaded) {
            if (!UserStatus.hasLogin) {
                updateUI();
            } else {
                Log.d("database", "database updating..");
                updateFromServer();
            }
        } else {
            Log.d("database", "not updated, using local database instead");
            fridgeItemList = refreshFridgeList();
            fridgeViewAdapter = new FridgeViewAdapter(getContext(), fridgeItemList, ((SharingFridgeApplication) getContext().getApplicationContext()).getServerAddr());
            fridgeView.setAdapter(fridgeViewAdapter);
            fridgeViewAdapter.notifyDataSetChanged();
        }
        Log.i("FridgeResume", "resume and refreshed");
    }

    /**
     * @param a "zh" for Simplified-Chinese, other for English
     * @return index of categories
     */
    public String language(String a) {
        int index = 3;
        for (int i = 0; i < CATEGORYS.length; i++) {
            if (a.equals(CATEGORYS[i]) || a.equals(CATEGORYS_CHINESE[i])) {
                index = i;
                break;
            }
        }
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            a = CATEGORYS_CHINESE[index];
        else
            a = CATEGORYS[index];
        return a;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Refresh the list
     * @return refreshed list
     */
    private List<FridgeItem> refreshFridgeList() {
        if (!isDataLoaded) {
            Log.d("database", "database updating..");
            updateFromServer();
        } else {
            Log.d("database", "not updated, using local database instead");
        }

        List<FridgeItem> itemsList = new ArrayList<>();
        Cursor cursor = mainDB.rawQuery("SELECT * from items where groupname = '" + UserStatus.groupName + "'", null);
        while (cursor.moveToNext()) {
            long expday = 0;
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                Date nd = cal.getTime();
                Date ed = df.parse(cursor.getString(cursor.getColumnIndex("expiretime")));
                expday = (ed.getTime() - nd.getTime()) / (1000 * 60 * 60 * 24);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String cat = language(cursor.getString(cursor.getColumnIndex("category")));
            FridgeItem tempfi = new FridgeItem(cursor.getString(cursor.getColumnIndex("item")), expday, cursor.getString(cursor.getColumnIndex("imageurl")), cursor.getString(cursor.getColumnIndex("owner")), cat, cursor.getInt(cursor.getColumnIndex("amount")));
            itemsList.add(tempfi);
            Collections.sort(itemsList, new expdayComparator());
            Log.i("usertest", cursor.getString(cursor.getColumnIndex("item")) + " at " + cursor.getString(cursor.getColumnIndex("expiretime")));
        }
        cursor.close();
        return itemsList;
    }

    /**
     * update the UI display by refreshing RecyclerView
     */
    public void updateUI() {
        if (isAdded()) {
            fridgeItemList = refreshFridgeList();
            fridgeViewAdapter = new FridgeViewAdapter(getContext(), fridgeItemList, "http://178.62.93.103/SharingFridge/");
            fridgeView.setAdapter(fridgeViewAdapter);
            fridgeViewAdapter.notifyDataSetChanged();
        } else {
            Log.d("updateUI", "FF - not added, failed.");
        }

    }

    /**
     * update the local data from server
     */
    private void updateFromServer() {
        if (!UserStatus.groupName.equals("Offline Mode")) {
            mAuthTask = new SendRequestTask(UserStatus.groupName);
            mAuthTask.execute("");
        } else {
            isDataLoaded = false;
        }
    }

    /**
     * tell the app the new signed in user need to download data from server
     */
    public void setNewUserDataNotLoaded() {
        isDataLoaded = false;
    }


    /**
     * The asynctask with HTTP requests to upload items data from our server.
     */
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/refresh.php";
        private String groupname;
        private SQLiteDatabase taskDB;

        SendRequestTask(String gn) {
            groupname = gn;
            taskDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "fridge.db", null);
            taskDB.execSQL("CREATE TABLE IF NOT EXISTS items(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
            taskDB.execSQL("CREATE TABLE IF NOT EXISTS dummy(item char(255),category char(64),amount int,addtime char(255),expiretime char(255),imageurl char(255),owner char(255),groupname char(255))");
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        String performPostCall() {
            Log.d("database", "start posting...");
            String response = "Failed to connect the server.";
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
                InputStream inputStream = conn.getInputStream();
                // Convert the InputStream into a string
                String contentAsString = getLongStringFromInputStream(inputStream);
                return contentAsString;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        /**
         * to get the result string that usually has a large length
         *
         * @param stream the inputstream used in receiving data from server
         * @return the result string
         * @throws IOException
         */
        public String getLongStringFromInputStream(InputStream stream) throws IOException {
            StringBuilder strBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String perLine;
            while ((perLine = reader.readLine()) != null) {
                strBuilder.append(perLine);
            }
            String result = strBuilder.toString();
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            try {
                JSONArray jr = new JSONArray(result);
                taskDB.execSQL("DELETE FROM items WHERE groupname != 'local'");
                taskDB.execSQL("DELETE FROM dummy");
                for (int i = 0; i < jr.length(); i++) {
                    JSONObject jo = jr.getJSONObject(i);
                    try {//remove all the data except local group data
                        if (jo.getString("item").equals("__dummy")) {
                            taskDB.execSQL("INSERT INTO dummy ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + jo.getString("item") + "', '" + jo.getString("category") + "', '" + jo.getString("amount") + "', '" + jo.getString("addtime") + "', '" + jo.getString("expiretime") + "', '" + jo.getString("imageurl") + "', '" + jo.getString("owner") + "', '" + jo.getString("groupname") + "')");
                        } else {
                            taskDB.execSQL("INSERT INTO items ('item' ,'category' ,'amount' ,'addtime' ,'expiretime' ,'imageurl' ,'owner' ,'groupname' )VALUES ('" + jo.getString("item") + "', '" + jo.getString("category") + "', '" + jo.getString("amount") + "', '" + jo.getString("addtime") + "', '" + jo.getString("expiretime") + "', '" + jo.getString("imageurl") + "', '" + jo.getString("owner") + "', '" + jo.getString("groupname") + "')");
                        }
                    } catch (SQLException e) {
                        Log.d("database", "error:" + e.toString());
                    }
                }
                Log.d("database", "updating complete!");
                isDataLoaded = true;

                updateFridgeList();

            } catch (Exception je) {
                Log.d("database", "Problem when updating :" + je);
                isDataLoaded = false;
            }
            taskDB.close();
        }
    }

    /**
     * load the fridge list from local database
     */
    public void updateFridgeList() {
        List<FridgeItem> itemsList = new ArrayList<>();
        Cursor cursor = mainDB.rawQuery("SELECT * from items where groupname = '" + UserStatus.groupName + "'", null);
        while (cursor.moveToNext()) {
            long expday = 0;
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                Date nd = cal.getTime();
                Date ed = df.parse(cursor.getString(cursor.getColumnIndex("expiretime")));
                expday = (ed.getTime() - nd.getTime()) / (1000 * 60 * 60 * 24);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String cat = language(cursor.getString(cursor.getColumnIndex("category")));
            FridgeItem tempfi = new FridgeItem(cursor.getString(cursor.getColumnIndex("item")), expday, cursor.getString(cursor.getColumnIndex("imageurl")), cursor.getString(cursor.getColumnIndex("owner")), cat, cursor.getInt(cursor.getColumnIndex("amount")));
            itemsList.add(tempfi);
        }
        cursor.close();
        fridgeItemList = itemsList;
        Collections.sort(itemsList, new expdayComparator());
        fridgeViewAdapter = new FridgeViewAdapter(getContext(), fridgeItemList, ((SharingFridgeApplication) getContext().getApplicationContext()).getServerAddr());
        fridgeView.setAdapter(fridgeViewAdapter);
        fridgeViewAdapter.notifyDataSetChanged();
    }

    /**
     * a helper class that compare two items so that we can sort them in the list
     */
    public class expdayComparator implements Comparator<FridgeItem> {

        @Override
        public int compare(FridgeItem lhs, FridgeItem rhs) {
            return lhs.compareTo(rhs);
        }
    }
}
