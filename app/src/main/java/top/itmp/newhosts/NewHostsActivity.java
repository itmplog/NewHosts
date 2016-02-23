package top.itmp.newhosts;

import android.app.Fragment;
import android.app.FragmentManager;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class NewHostsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_hosts);
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOffscreenPageLimit(6);

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mViewPager.removeOnPageChangeListener(onPageChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.addOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_hosts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_settings:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.action_about:
                mViewPager.setCurrentItem(2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


/*  useless
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_new_hosts, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
*/

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                default:
                    /*return   new Fragment(){
                        @Nullable
                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            View rootView = inflater.inflate(R.layout.fragment_new_hosts, container, false);

                            return rootView;

                        }
                    };*/
                    return new NewHostsFragment();
                case 1:
                    return new PreferenceFragment(){
                        @Override
                        public void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);
                            addPreferencesFromResource(R.xml.settings);
                        }
                    };
                case 2:
                    return new Fragment(){};
            }
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
                    return getString(R.string.app_name);
                case 1:
                    return getString(R.string.settings);
                case 2:
                    return getString(R.string.about);
            }
            return null;
        }
    }
}
