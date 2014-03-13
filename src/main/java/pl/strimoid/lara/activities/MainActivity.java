package pl.strimoid.lara.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.koushikdutta.async.future.Cancellable;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Loader;

import java.io.IOException;
import java.util.Locale;

import pl.strimoid.lara.R;
import pl.strimoid.lara.fragments.ContentListFragment;
import pl.strimoid.lara.fragments.EntryListFragment;
import pl.strimoid.lara.gcm.Gcm;
import pl.strimoid.lara.utils.OAuth2;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public static final String API_URL = "http://api.strimoid.pl";

    private AccountManager mAccountManager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private OAuth2 mOAuth2;
    private Gcm mGcm;

    private boolean mLoggedIn = false;
    private boolean mTwoPane;
    private String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main_activity);

        //Ion.getDefault(getApplicationContext()).configure().proxy("192.168.1.3", 8888);

        final ActionBar actionBar = getActionBar();
        //actionBar.setTitle("");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (savedInstanceState != null)
            restoreInstanceState(savedInstanceState);

        // Find Strimoid accounts
        mAccountManager = AccountManager.get(this);
        Account accounts[] = mAccountManager.getAccountsByType("pl.strimoid");

        // Setup middleware to deal with OAuth2
        if (accounts.length == 1 && !mLoggedIn)
            setupOAuth2(accounts[0]);

        // Setup GCM if device has GPS installed
        if (checkPlayServices())
            mGcm = new Gcm(getApplicationContext());
        else
            Log.i("Strimoid", "No valid Google Play Services APK found.");

        // Register device registration id to get notifications
        if (mGcm != null && mLoggedIn)
            mGcm.sendRegistrationIdToBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void restoreInstanceState(Bundle inState) {
        mLoggedIn = inState.getBoolean("logged_in", false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("logged_in", mLoggedIn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        if (mLoggedIn) {
            menu.setGroupVisible(R.id.logged_in, true);
            menu.setGroupVisible(R.id.not_logged_in, false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                login();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void login() {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(
                "pl.strimoid", null, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();

                    if (bundle.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
                        Account accounts[] = mAccountManager.getAccountsByType("pl.strimoid");

                        setupOAuth2(accounts[0]);

                        invalidateOptionsMenu();
                    }
                } catch (Exception e) { }
            }
        }, null);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());

        View detail = findViewById(R.id.detail);

        // Toggle detail fragment visibility if in two pane mode
        //if (detail != null)
            //detail.setVisibility(tab.getPosition() == 2 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void changeGroup() {
        //mSectionsPagerAdapter
    }

    private void setupOAuth2(Account account) {
        OAuth2 oAuth2 = OAuth2.getInstance();
        oAuth2.useAccount(getApplicationContext(), account);

        Ion.getDefault(getApplicationContext()).getHttpClient().insertMiddleware(oAuth2);

        mLoggedIn = true;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                Log.i("strimoid", "This device is not supported.");
                finish();
            }
            return false;
        }

        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ContentListFragment(ContentListFragment.TypeFilter.POPULAR);
                case 1:
                    return new ContentListFragment(ContentListFragment.TypeFilter.NEW);
                case 2:
                    return new EntryListFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();

            switch (position) {
                case 0:
                    return "Popularne".toUpperCase(l);
                case 1:
                    return "Nowe".toUpperCase(l);
                case 2:
                    return "Wpisy".toUpperCase(l);
            }

            return null;
        }
    }

    public interface GroupChangeListener {
        public void onGroupChange();
    }

}
