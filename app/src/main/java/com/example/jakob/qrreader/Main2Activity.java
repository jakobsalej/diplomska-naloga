package com.example.jakob.qrreader;

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

import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import database.OrderDocumentJSON;

import static android.R.attr.fragment;
import static com.example.jakob.qrreader.R.id.container;
import static database.DatabaseHandler.getOrders;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // set icons and text
        /*
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_layout_icon);
        View tab1View = tabLayout.getTabAt(0).getCustomView();
        TextView tab1Text = (TextView) tab1View.findViewById(R.id.tabText);
        tab1Text.setText("ONGOING");
        ImageView tab1Image = (ImageView) tab1View.findViewById(R.id.tabIcon);
        tab1Image.setImageResource(R.drawable.ic_local_shipping_white_36px);
        */

        // set icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_local_shipping_white_36px);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_check_circle_white_24px);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_history_white_24px);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();

            ArrayList<OrderDocumentJSON> dbData = getDataFromDB(sectionNumber);
            Log.v("NEW TAB", dbData.toString());

            ArrayList<String> dataFields = new ArrayList<String>();
            for (OrderDocumentJSON od : dbData) {
                dataFields.add(0, od.getData());
            }

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("dbDataString", dbData.toString());
            args.putStringArrayList("data", dataFields);
            fragment.setArguments(args);
            return fragment;
        }

        private static ArrayList<OrderDocumentJSON> getDataFromDB(int sectionNumber) {
            // get data from DB, based on status: 0 = not started, 1 = running, 2 = done, 3 = cleared
            if (sectionNumber == 0) {
                ArrayList<OrderDocumentJSON> odArray0 = DatabaseHandler.getOrders(0);
                ArrayList<OrderDocumentJSON> odArray1 = DatabaseHandler.getOrders(1);
                odArray0.addAll(odArray1);
                return odArray0;
            } else if(sectionNumber == 1) {
                return getOrders(2);
            } else {
                return getOrders(3);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_tab, container, false);
            return rootView;
        }
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
}
