package com.example.jakob.qrreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import static com.example.jakob.qrreader.R.id.container;

public class Main2Activity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static SQLiteDatabase db;
    public JSONArray ongoingOrders;
    public static String preferenceKey = "com.example.jakob.qrreader.PREFERENCE_KEY";
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // shared preference file for storing data
        sharedPref = this.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE);

        // check if user is already logged in
        checkIfUserLoggedIn();

        setContentView(R.layout.activity_main2);

        // prepare DB
        DatabaseHandler mDbHelper = new DatabaseHandler(this);
        db = mDbHelper.getWritableDatabase();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // set icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_local_shipping_white_36px);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_check_circle_white_24px);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_history_white_24px);
    }


    private void checkIfUserLoggedIn() {

        String user = sharedPref.getString("user", null);
        Log.v("USER", "this is user:" + user);
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            // clear back button stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainTabFragment());
        adapter.addFragment(new MainTabFragment());
        adapter.addFragment(new MainTabFragment());
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                //newGame();
                return true;
            case R.id.action_logout:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void logoutUser() {
        // remove user credentials from shared prefs
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("user");
        editor.commit();

        // go to login screen immediately
        checkIfUserLoggedIn();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = mFragmentList.get(position);
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            return f;
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ONGOING";
                case 1:
                    return "DONE";
                case 2:
                    return "HISTORY";
            }
            return null;
        }
    }


    // add new document - open QR reader
    public void scanCode(View view) {
        Intent intent = new Intent(this, ReadQRActivity.class);
        startActivity(intent);
    }


    public void startMonitoring(View view) {
        Intent intent = new Intent(this, MonitoringActivity.class);
        startActivity(intent);
    }
}
