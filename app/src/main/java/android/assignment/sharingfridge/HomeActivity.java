package android.assignment.sharingfridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.TabItemBuilder;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FridgeFragment.OnFragmentInteractionListener,
        MemberFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    int[] tabColors = {0xFF00796B, 0xFFF57C00, 0xFF607D8B, 0xFF5B4947, 0xFFF57C00};
    Controller tabController;
    List<Fragment> myFragments;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        (new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(getApplicationContext()).clearDiskCache();
            }
        })).start();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView usernameView = (TextView) headerView.findViewById(R.id.username_view);
        usernameView.setText("EveLIn3");
        LinearLayout headerLayout = (LinearLayout) headerView.findViewById(R.id.header_layout);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Header Clicked", Toast.LENGTH_SHORT).show();
                //drawer.closeDrawer(GravityCompat.START);
                Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        initFragments();
        // initialize the bottom navigation bar
        initNavBar();

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
        if (id == R.id.action_settings) {
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
        } else if (id == R.id.nav_account) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_logout) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initFragments() {
        myFragments = new ArrayList<>();
        FridgeFragment friFrag = FridgeFragment.newInstance("Home", "para2");
        MemberFragment memFrag = MemberFragment.newInstance("Member", "para2");
        //MapFragment mapFrag = MapFragment.newInstance("Map", "para2");
        MapViewFragment mapFrag = new MapViewFragment();
        SettingsFragment setFrag = SettingsFragment.newInstance("Settings", "para2");


        myFragments.add(friFrag);
        myFragments.add(memFrag);
        myFragments.add(mapFrag);
        myFragments.add(setFrag);

//        myFragments.add(initSingleFragment("Home"));
//        myFragments.add(initSingleFragment("Member"));
//        myFragments.add(initSingleFragment("Map"));
//        myFragments.add(initSingleFragment("Settings"));

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // transaction.setCustomAnimations(R.anim.push_up_in,R.anim.push_up_out);
        transaction.add(R.id.fragment_container_home, myFragments.get(0));
        transaction.commit();
    }

    OnTabItemSelectListener tabListener = new OnTabItemSelectListener() {
        @Override
        public void onSelected(int index, Object tag) {
            Log.i("tab", "onSelected:" + index + "   TAG: " + tag.toString());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.setCustomAnimations(R.anim.push_up_in,R.anim.push_up_out);
            transaction.replace(R.id.fragment_container_home, myFragments.get(index));
            transaction.commit();
        }

        @Override
        public void onRepeatClick(int index, Object tag) {
            Log.i("tab", "onRepeatClick:" + index + "   TAG: " + tag.toString());
        }
    };

    //A way of setting arguments for fragments
//    private Fragment initSingleFragment(String id){
//        TestFragment fragment = new TestFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("id", id);
//        fragment.setArguments(bundle);
//
//        return fragment;
//    }

    private void initNavBar() {
        PagerBottomTabLayout pagerBottomTabLayout = (PagerBottomTabLayout) findViewById(R.id.tab);

        //Build a tab giving some initial custom settings
        TabItemBuilder tabItemBuilder = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_home)
                .setText("Home")
                .setSelectedColor(tabColors[0])
                .setTag("Home")
                .build();

        //finish the navigation bar by adding more tabs
        tabController = pagerBottomTabLayout.builder()
                .addTabItem(tabItemBuilder)
                .addTabItem(R.drawable.ic_member, "Members", tabColors[1])
                .addTabItem(R.drawable.ic_map, "Map", tabColors[2])
                .addTabItem(R.drawable.ic_settings, "Settings", tabColors[3])
//                .setMode(TabLayoutMode.HIDE_TEXT)
//                .setMode(TabLayoutMode.CHANGE_BACKGROUND_COLOR)
//                .setMode(TabLayoutMode.HIDE_TEXT| TabLayoutMode.CHANGE_BACKGROUND_COLOR)
                .build();

        tabController.addTabItemClickListener(tabListener);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
