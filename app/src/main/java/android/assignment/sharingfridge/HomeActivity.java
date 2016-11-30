package android.assignment.sharingfridge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.TabItemBuilder;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

/**
 * The core activity of this application.<br/><br/>
 * Used external libraries: Glide, AVLoadingIndicatorView, CircularImageView
 * Author: Glide-Sam Judd. @samajudd on Twitter; jack wang-AVLoadingIndicatorView; Pkmmte Xeleon-CircularImageView
 * @see <a href="https://github.com/bumptech/glide">Glide</a>, <a href="https://github.com/81813780/AVLoadingIndicatorView">AVLoadingIndicatorView</a>, <a href="https://github.com/Pkmmte/CircularImageView">CircularImageView</a>
 */

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FridgeFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener {

    int[] tabColors = {0xFFB71C1C, 0xFFF57F17, 0xFF0D47A1, 0xFF9C27B0, 0xFFF57C00};
    Controller tabController;
    List<Fragment> myFragments;
    DrawerLayout drawer;
    TextView usernameView, groupnameView;
    ImageView avatarView;
    SendRequestTask mAuthTask = null;
    FridgeFragment friFrag;
    MemberFragment memFrag;
    MapViewFragment mapFrag;
    StatisticsFragment setFrag;
    AVLoadingIndicatorView avatarProgress;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int i = 0;
        SharedPreferences userSettings = getSharedPreferences("setting", 0);
        int ID = userSettings.getInt("language", i);
        if (ID == 1)
            config.locale = Locale.ENGLISH;
        if (ID == 2)
            config.locale = Locale.SIMPLIFIED_CHINESE;
        setTitle(getString(R.string.title_activity_home));
        resources.updateConfiguration(config, dm);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initLocation();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        avatarProgress = (AVLoadingIndicatorView) headerView.findViewById(R.id.sideBarProgress);
        usernameView = (TextView) headerView.findViewById(R.id.username_view);
        groupnameView = (TextView) headerView.findViewById(R.id.groupname_view);
        avatarView = (ImageView) headerView.findViewById(R.id.gravatarView);
        LinearLayout headerLayout = (LinearLayout) headerView.findViewById(R.id.header_layout);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserStatus.hasLogin) {
                    Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    Toast.makeText(HomeActivity.this, getString(R.string.logged_in_as) + UserStatus.username, Toast.LENGTH_SHORT).show();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        initFragments();
        // initialize the bottom navigation bar
        initNavBar();

        //check the status of auto login
        SharedPreferences preferences = getSharedPreferences("user-status", Context.MODE_PRIVATE);
        String uname = preferences.getString("username", null);
        String ugroup = preferences.getString("groupName", null);
        String utoken = preferences.getString("token", null);
        Log.d("auto-login", uname + " " + ugroup);
        if (uname != null && ugroup != null && !uname.equals("_null") && !ugroup.equals("_null")) {
            UserStatus.username = uname;//auto login
            UserStatus.groupName = ugroup;
            UserStatus.token = utoken;
            UserStatus.hasLogin = true;
            UserStatus.inGroup = !UserStatus.groupName.equals("none");
        }
        if (UserStatus.token != null && !UserStatus.token.equals("")) {
            RongIM.getInstance().setCurrentUserInfo(findUserById(UserStatus.username));
            RongIM.getInstance().setMessageAttachedUserInfo(true);
            RongIM.connect(UserStatus.token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                }

                @Override
                public void onSuccess(String s) {
                    UserStatus.chatConnected = true;
                    Log.e("onSuccess", "onSuccess userid:" + s);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Log.e("onError", "onError userid:" + errorCode.getValue());
                    Toast.makeText(getApplicationContext(), getString(R.string.chat_err), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent addIntent = new Intent(HomeActivity.this, AddActivity.class);
            startActivity(addIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_join_group) {
            Intent groupIntent = new Intent(HomeActivity.this, GroupActivity.class);
            startActivity(groupIntent);
        } else if (id == R.id.nav_about) {
            Intent settingIntent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        } else if (id == R.id.nav_logout) {
            if (UserStatus.hasLogin) {
                UserStatus.resetStatus();
                refreshUserStatus();
                SharedPreferences preferences = getSharedPreferences("user-status", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", "_null");//clear the shared preference
                editor.putString("groupName", "_null");
                editor.putString("token", "");
                editor.apply();
                UserStatus.hasChanged = true;
                friFrag.setNewUserDataNotLoaded();
                Toast.makeText(getApplicationContext(), "You have successfully logged out", Toast.LENGTH_SHORT).show();
                friFrag.updateUI();
                memFrag.updateUI();
            } else {
                Toast.makeText(getApplicationContext(), "You are already logged out!", Toast.LENGTH_SHORT).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initFragments() {
        myFragments = new ArrayList<>();

        friFrag = new FridgeFragment();
        memFrag = new MemberFragment();
        mapFrag = new MapViewFragment();
        setFrag = new StatisticsFragment();

        myFragments.add(friFrag);
        myFragments.add(memFrag);
        myFragments.add(mapFrag);
        myFragments.add(setFrag);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container_home, myFragments.get(0));
        transaction.commit();
    }

    OnTabItemSelectListener tabListener = new OnTabItemSelectListener() {
        @Override
        public void onSelected(int index, Object tag) {
            Log.i("tab", "onSelected:" + index + "   TAG: " + tag.toString());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_home, myFragments.get(index));
            transaction.commit();
        }

        @Override
        public void onRepeatClick(int index, Object tag) {
            Log.i("tab", "onRepeatClick:" + index + "   TAG: " + tag.toString());
        }
    };

    private void initNavBar() {
        PagerBottomTabLayout pagerBottomTabLayout = (PagerBottomTabLayout) findViewById(R.id.tab);

        //Build a tab giving some initial custom settings
        TabItemBuilder tabItemBuilder = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_home)
                .setText(getString(R.string.hom_frg))
                .setSelectedColor(tabColors[0])
                .setTag(getString(R.string.hom_frg))
                .build();

        //finish the navigation bar by adding more tabs
        tabController = pagerBottomTabLayout.builder()
                .addTabItem(tabItemBuilder)
                .addTabItem(R.drawable.ic_member, getString(R.string.mem_frg), tabColors[1])
                .addTabItem(R.drawable.ic_map, getString(R.string.map_frg), tabColors[2])
                .addTabItem(R.drawable.ic_statistics, getString(R.string.set_frg), tabColors[3])
                .build();

        tabController.addTabItemClickListener(tabListener);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    protected void onResume() {
        super.onResume();
        refreshUserStatus();
        memFrag.updateUI();
        if (UserStatus.hasChanged && UserStatus.hasLogin) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_home, myFragments.get(0));
            transaction.commit();
            tabController.setSelect(0);
            UserStatus.hasChanged = false;
        }
        if (UserStatus.hasLogin && UserStatus.needToUploadLoaction) {
            mAuthTask = new SendRequestTask(UserStatus.location);
            mAuthTask.execute();
        }
        Log.i("resume", "Activity resumed. Should've updated.");
    }

    //refresh user information on the side navigate bar.
    public void refreshUserStatus() {
        usernameView.setText(UserStatus.username);
        groupnameView.setText(UserStatus.groupName);
        if (!UserStatus.username.equals("Click here to login")) {
            // set avatar!
            Glide.with(this).load("http://178.62.93.103/SharingFridge/avatars/" + UserStatus.username + ".png")
                    .centerCrop()
                    .placeholder(R.drawable.image_loading)//don't know why, placeholder is necessary or the app will crash. Probably just a minor bug of Glide.
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            avatarProgress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            avatarProgress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.image_corrupt)
                    .dontAnimate()
                    .into(avatarView);
        } else {
            avatarProgress.setVisibility(View.GONE);
            Drawable icon = getResources().getDrawable(R.drawable.ic_default, getTheme());
            avatarView.setImageDrawable(icon);
        }
    }

    /**
     * initiate the location lisetener, permission is required
     * there are two paramiters that decided the upload interval and the minimum upload distance
     */
    private final int LOCATION_INTERVAL = 5000;
    private final int LOCATION_MIN_DISTANCE = 10;

    /**
     *
     * request permission and initiate location upload
     */
    public void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(HomeActivity.this, permissions, 5230);
        } else {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_MIN_DISTANCE, locationListener);
            } catch (IllegalArgumentException e) {
                Log.d("LOCATION", "Does not support network_provider");
            }
        }
    }

    public UserInfo findUserById(String uid) {
        return new UserInfo(uid, uid, Uri.parse("http://178.62.93.103/SharingFridge/avatars/" + uid + ".png"));
    }

    /**
     * LocationListener that listen to the locatoin change, upload the loadtion to server if the location changed a lot
     */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            String longitude = "Longitude: " + location.getLongitude();
            Log.v("LOCATION", longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.v("LOCATION", latitude);
            if (UserStatus.hasLogin) {//upload the location when user has login
                Log.v("LOCATION", "post");
                mAuthTask = new SendRequestTask(location);
                mAuthTask.execute();
            } else {//cache the location and upload after login
                Log.v("LOCATION", "wait");
                UserStatus.location = location;
                UserStatus.needToUploadLoaction = true;
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    /**
     * add location listener when get permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_MIN_DISTANCE, locationListener);
        }
    }

    /**
     *
     * the AsynTask to upload location, trigger by location listener
     */
    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/location.php";
        private Double Longitude, Latitude;

        SendRequestTask(Location location) {
            this.Latitude = location.getLatitude();
            this.Longitude = location.getLongitude();
        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        String performPostCall() {
            Log.d("send post-location", "performPostCall");
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
                jo.put("action", "upload");
                jo.put("user", UserStatus.username);
                jo.put("lo", Longitude);
                jo.put("la", Latitude);
                String tosend = jo.toString();
                Log.d("JSON", tosend);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("location=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = conn.getResponseCode();
                InputStream inputStream = conn.getInputStream();
                // Convert the InputStream into a string
                int length = 500;
                String contentAsString = convertInputStreamToString(inputStream, length);
                conn.disconnect();
                return contentAsString;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
