/*
 * Copyright 2016 Maurice Kenmeue Fonwe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dynamite.zafroshops.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import dynamite.zafroshops.app.data.MobileZop;
import dynamite.zafroshops.app.data.ZopType;
import dynamite.zafroshops.app.iap.IabHelper;
import dynamite.zafroshops.app.data.FullMobileZop;
import dynamite.zafroshops.app.data.LocationBase;
import dynamite.zafroshops.app.data.MobileOpeningHour;
import dynamite.zafroshops.app.data.MobileOpeningHourData;
import dynamite.zafroshops.app.data.MobileZopService;
import dynamite.zafroshops.app.data.StorageKeys;
import dynamite.zafroshops.app.data.ZopServiceType;
import dynamite.zafroshops.app.fragment.AddZopFragment;
import dynamite.zafroshops.app.fragment.AllZopsFragment;
import dynamite.zafroshops.app.fragment.OpeningsDialogFragment;
import dynamite.zafroshops.app.fragment.ReviewDialogFragment;
import dynamite.zafroshops.app.fragment.SimpleDialogFragment;
import dynamite.zafroshops.app.fragment.TypedZopsFragment;
import dynamite.zafroshops.app.fragment.ZopItemFragment;
import dynamite.zafroshops.app.fragment.ZopServicesFragment;
import dynamite.zafroshops.app.iap.IabResult;
import dynamite.zafroshops.app.iap.Inventory;
import dynamite.zafroshops.app.iap.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public boolean AdsActive = true;
    private ProgressDialog upgradeDialog;
    private IabHelper inAppHelper;
    private static final int REQUEST_IAP_ADS = 1011;

    public static String EXTRA_ID = "id";

    /**
     * Mobile Service Client reference
     */
    public static MobileServiceClient MobileClient;
    public static LocationBase LastLocation;
    public static android.location.Location AndroidLastLocation;
    public ArrayList<MobileOpeningHourData> NewZopOpenings;
    public ArrayList<ZopServiceType> NewZopServices;

    /**
     * Google API Client
     */
    private GoogleApiClient googleApiClient;
    private boolean resolvingError = false;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private AddZopFragment addZopFragment;
    private String zopID;
    private MobileZop currentZop;
    private AdView adView;

    public InterstitialAd Interstitial;
    public int DataVersion;
    public Hashtable<ZopType, Integer> Versions;
    public Hashtable<ZopType, Integer> Counts;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private int position;
    private int lastPosition;
    private CharSequence[] titles;
    private int depth;
    private boolean locationToggleDefault;

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EXTRA_ID = getApplicationContext().getPackageName() + ".id";
        titles = new CharSequence[]{
                getString(R.string.title_section1),
                getString(R.string.title_section2)
        };
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        NewZopOpenings = new ArrayList();
        NewZopServices = new ArrayList();
        depth = 1;
        locationToggleDefault = true;

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        SharedPreferences preferences = getPreferences(0);
        final SharedPreferences.Editor editor = preferences.edit();

        Switch locationToggle = (Switch) findViewById(R.id.location_toggle);
        locationToggle.setChecked(preferences.getBoolean(StorageKeys.LOCATION_TOGGLE_KEY, locationToggleDefault));
        locationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(StorageKeys.LOCATION_TOGGLE_KEY, b);
                editor.commit();

                if (b) {
                    getLocation(true);
                    getAddress(true);
                }
            }
        });

        if (!preferences.contains(StorageKeys.COUNTRY_KEY)) {
            editor.putString(StorageKeys.COUNTRY_KEY, "");
            editor.commit();
        }

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            MobileClient = new MobileServiceClient(
                    getString(R.string.azure_site),
                    getString(R.string.azure_mobile_service_app_key),
                    this);
        } catch (MalformedURLException ignored) {
        }

        NotificationsManager.handleNotifications(this, getString(R.string.google_api_name), PushHandler.class);
        registerWithNotificationHubs();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_ID)) {
            nextMenu(ZopItemFragment.newInstance(bundle.getString(EXTRA_ID)), false, 100);
        }

        // setup ads
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        adView.loadAd(adRequest);

        Interstitial = new InterstitialAd(this);
        Interstitial.setAdUnitId(getString(R.string.ads_interstitial_unit_id));
        Interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
        // setup in app package
        if (!getResources().getBoolean(R.bool.debug)) {
            inAppHelper = new IabHelper(this, getString(R.string.inappkey));
            inAppHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        return;
                    }
                    if (inAppHelper == null) {
                        return;
                    }
                    inAppHelper.queryInventoryAsync(gotInventoryListener);
                }
            });
        }

        DataVersion = 0;
        Versions = new Hashtable<>();
        Counts = new Hashtable<>();
        setDataVersion();
    }

    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }
    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation(false);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!resolvingError) {
            if (connectionResult.hasResolution()) {
                resolvingError = true;
                try {
                    connectionResult.startResolutionForResult(this, Integer.parseInt(getString(R.string.REQUEST_RESOLVE_ERROR)));
                } catch (IntentSender.SendIntentException e) {
                    googleApiClient.connect();
                }
            } else {
                showErrorDialog(connectionResult.getErrorCode());
                resolvingError = true;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        resolvingError = false;
    }

    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();

        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errorDialog");
    }

    public void onDialogDismissed() {
        resolvingError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        private static final String DIALOG_ERROR = "dialog_error";

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);

            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        } else if (requestCode == REQUEST_IAP_ADS) {
            if (inAppHelper != null) {
                inAppHelper.flagEndAsync();
            }
            if (upgradeDialog != null) {
                upgradeDialog.dismiss();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        onNavigationDrawerItemSelected(position, true);
    }

    public void onNavigationDrawerItemSelected(int position, boolean addToStack) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = null;

        switch (position) {
            case 0: // all zops
                nextMenu(AllZopsFragment.newInstance(position, false), false, 1);
                break;

            case 1: // new zop
                NewZopServices.clear();
                NewZopOpenings.clear();
                addZopFragment = AddZopFragment.newInstance(position);
                nextMenu(addZopFragment, addToStack, 1);
                break;

//            case 2: // login
//                break;
        }
    }

    public void onSectionAttached(int number) {
        lastPosition = position;
        position = number;
    }

    public void nextMenu(android.support.v4.app.Fragment fragment, boolean addToStack, int depth) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction;

        fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.container, fragment);
        if (addToStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        this.depth = depth;
    }

    public void restoreActionBar() {
        restoreActionBar(position);
    }

    public void restoreActionBar(int position) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(titles[position]);
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();
        Interstitial.loadAd(adRequest);
    }

    public void getLocation(boolean force) {
        SharedPreferences preferences = getPreferences(0);
        SharedPreferences.Editor editor = preferences.edit();
        boolean locationToggle = preferences.getBoolean(StorageKeys.LOCATION_TOGGLE_KEY, locationToggleDefault);

        if (!force && preferences.contains(StorageKeys.LATITUDE_KEY)) {
            LastLocation = new LocationBase();
            LastLocation.Latitude = getDouble(preferences, StorageKeys.LATITUDE_KEY, 0);
            LastLocation.Longitude = getDouble(preferences, StorageKeys.LONGITUDE_KEY, 0);
        } else if (locationToggle) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            AndroidLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient); // use telnet to fix currentZop
            if (AndroidLastLocation != null) {
                LastLocation = new LocationBase();
                LastLocation.Latitude = AndroidLastLocation.getLatitude();
                LastLocation.Longitude = AndroidLastLocation.getLongitude();

                putDouble(editor, StorageKeys.LATITUDE_KEY, LastLocation.Latitude);
                putDouble(editor, StorageKeys.LONGITUDE_KEY, LastLocation.Longitude);
                editor.commit();
                getAddress(false);
            }
        }
    }

    public LocationBase getAddress(boolean force) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        SharedPreferences preferences = getPreferences(0);
        SharedPreferences.Editor editor = preferences.edit();
        boolean locationToggle = preferences.getBoolean(StorageKeys.LOCATION_TOGGLE_KEY, locationToggleDefault);

        try {
            if (!force && LastLocation != null) {
                if (preferences.contains(StorageKeys.COUNTRY_KEY)) {
                    LastLocation.CountryCode = preferences.getString(StorageKeys.COUNTRY_KEY, "");
                    LastLocation.Town = preferences.getString(StorageKeys.TOWN_KEY, "");
                    LastLocation.Street = preferences.getString(StorageKeys.STREET_KEY, "");
                    LastLocation.StreetNumber = preferences.getString(StorageKeys.STREETNUMBER_KEY, "");
                } else if (locationToggle) {
                    List<Address> addresses = geocoder.getFromLocation(LastLocation.Latitude, LastLocation.Longitude, 1);

                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);

                        LastLocation.CountryCode = address.getCountryCode();
                        LastLocation.Town = address.getLocality();

                        if (address.getMaxAddressLineIndex() > 0) {
                            String line = address.getAddressLine(0);

                            if(line.compareTo(LastLocation.Town) < 0) {
                                LastLocation.Street = line.replaceAll("(\\D+) \\d+.*", "$1");
                                LastLocation.StreetNumber = line.replaceAll("\\D+ (\\d+.*)", "$1");
                            }
                        }

                        editor.putString(StorageKeys.COUNTRY_KEY, LastLocation.CountryCode);
                        editor.putString(StorageKeys.TOWN_KEY, LastLocation.Town);
                        editor.putString(StorageKeys.STREET_KEY, LastLocation.Street);
                        editor.putString(StorageKeys.STREETNUMBER_KEY, LastLocation.StreetNumber);
                        editor.commit();
                    }
                } else {
                    return null;
                }
            }
            return LastLocation;
        } catch (IOException e) {
            return null;
        }
    }

    public void setCurrentItem(String id, MobileZop zop) {
        zopID = id;
        this.currentZop = zop;
    }

    public void removeAds(View view) {
        final Activity activity = this;

        String payload = getEmail();
        upgradeDialog = ProgressDialog.show(activity, "Please wait", "Billing request in progress", true);
        try {
            inAppHelper.launchPurchaseFlow(activity, getString(R.string.inappads), REQUEST_IAP_ADS, purchaseFinishedListener, payload);
        } catch (IllegalStateException e) {
            upgradeDialog.dismiss();
        }
    }

    public void goToAddZop(View view) {
        onNavigationDrawerItemSelected(1, false);
    }

    private IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (inAppHelper == null) {
                upgradeDialog.dismiss();
                return;
            }

            if (result.isFailure()) {
                alert("Error while purchasing: " + result, "uprade_failed");
                upgradeDialog.dismiss();
            } else if (purchase.getSku().equals(getString(R.string.inappads))) {
                alert("Thank you for the upgrade", "upgrade_done");
                upgradeDialog.dismiss();
                AdsActive = false;
                updateView();
            }
        }
    };
    private IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (inAppHelper == null) {
                return;
            }

            if (result.isFailure()) {
                return;
            }

            AdsActive = !inventory.hasPurchase(getString(R.string.inappads));
            updateView();
        }
    };

    public void updateView() {
        if (!AdsActive) {
            adView.setVisibility(View.GONE);
            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
            if (scrollView != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
            }
        } else {
            adView.setVisibility(View.VISIBLE);
        }
    }

    public String getEmail() {
        String email = "";

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
            }
        }

        return email;
    }

    public void alert(String message, String tag) {
        SimpleDialogFragment dialog = new SimpleDialogFragment();
        Bundle args = new Bundle();

        args.putString(SimpleDialogFragment.DIALOG_MESSAGE, message);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), tag);
    }

    public void setRating(View view) {
        ImageView self = (ImageView) view;
        final View main = (View) self.getParent();
        ArrayList<ImageView> imageViews = new ArrayList<ImageView>() {{
            add((ImageView) main.findViewById(R.id.star_1));
            add((ImageView) main.findViewById(R.id.star_2));
            add((ImageView) main.findViewById(R.id.star_3));
            add((ImageView) main.findViewById(R.id.star_4));
            add((ImageView) main.findViewById(R.id.star_5));
        }};
        int score = Integer.parseInt((String) self.getTag());

        score = ReviewDialogFragment.rating == score ? 0 : score;
        ReviewDialogFragment.rating = score;
        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get(i).setImageResource(R.drawable.stare);
        }
        ReviewDialogFragment.rating = score;
        for (int i = 0; i < score; i++) {
            imageViews.get(i).setImageResource(R.drawable.star);
        }
    }

    public void editOpeningHours(View view) {
        OpeningsDialogFragment dialog = new OpeningsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(OpeningsDialogFragment.NEW_ZOP_OPENINGS, NewZopOpenings);
        dialog.setArguments(args);
        dialog.setTargetFragment(addZopFragment, OpeningsDialogFragment.NEW_ZOP_OPENINGS_RESULT_CODE);
        dialog.show(getSupportFragmentManager(), "openings");
    }

    public void editServices(View view) {
        ZopServicesFragment dialog = new ZopServicesFragment();
        Bundle args = new Bundle();

        args.putSerializable(ZopServicesFragment.NEW_ZOP_SERVICES, NewZopServices);
        dialog.setArguments(args);
        dialog.setTargetFragment(addZopFragment, ZopServicesFragment.NEW_ZOP_SERVICES_RESULT_CODE);
        dialog.show(getSupportFragmentManager(), "services");
    }

    public void openWebsite(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
        startActivity(i);
    }

    public void emailUs(View view) {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        String uri = "mailto:" + Uri.encode(getString(R.string.zafroshops_email)) + "?subject=" + Uri.encode(getString(R.string.email_subject)) + "&body=" + Uri.encode(getString(R.string.email_content));

        i.setData(Uri.parse(uri));
        startActivity(Intent.createChooser(i, getString(R.string.email_us)));
    }

    private void setDataVersion() {
        ListenableFuture<JsonElement> result = MainActivity.MobileClient.invokeApi("mobileZop", "GET", new ArrayList<Pair<String, String>>() {{
            add(new Pair<>("version", "0"));
        }});

        Futures.addCallback(result, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                DataVersion = result.getAsInt();
            }

            @Override
            public void onFailure(@NonNull Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;

            case R.id.new_zop_add:
                addZopFragment.setVisibility();
                FullMobileZop newZop = addZopFragment.getFullMobileZop();
                ArrayList<MobileOpeningHour> moh;
                ArrayList<MobileZopService> ms;

                if (NewZopOpenings != null) {
                    moh = new ArrayList();
                    for (int i = 0; i < NewZopOpenings.size(); i++) {
                        moh.addAll(NewZopOpenings.get(i).Hours);
                    }
                    newZop.OpeningHours = moh;
                }

                if (NewZopServices != null) {
                    ms = new ArrayList();
                    for (int i = 0; i < NewZopServices.size(); i++) {
                        MobileZopService mzs = new MobileZopService();

                        mzs.Service = NewZopServices.get(i);
                        ms.add(mzs);
                    }
                    newZop.Services = ms;
                }

                if (addZopFragment.ValidateNewZop(newZop)) {
                    addZopFragment.setVisibility();
                    if (PushHandler.ids == null) {
                        PushHandler.ids = new ArrayList<>();
                    }
                    PushHandler.ids.add(newZop.Name);
                    ListenableFuture<FullMobileZop> result = MainActivity.MobileClient.invokeApi("mobileZop", newZop, FullMobileZop.class);
                    Futures.addCallback(result, new FutureCallback<FullMobileZop>() {
                        @Override
                        public void onSuccess(FullMobileZop result) {
                            SimpleDialogFragment dialog = new SimpleDialogFragment();
                            Bundle args = new Bundle();
                            if (result == null) {
                                args.putString(SimpleDialogFragment.DIALOG_MESSAGE, getString(R.string.new_zop_failed) + "\n" + getString(R.string.new_zop_check_location));
                            } else if (result.id.compareTo("-1") == 0) {
                                args.putString(SimpleDialogFragment.DIALOG_MESSAGE, getString(R.string.new_zop_failed) + "\n" + result.Name);
                            } else {
                                PushHandler.ids.add(result.id);
                                args.putString(SimpleDialogFragment.DIALOG_MESSAGE, getString(R.string.new_zop_success));
                                NewZopOpenings.clear();
                                NewZopServices.clear();
                                addZopFragment.clearForm(null);
                            }
                            addZopFragment.resetVisibility();
                            dialog.setArguments(args);
                            dialog.show(getSupportFragmentManager(), "new_zop_success");
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            SimpleDialogFragment dialog = new SimpleDialogFragment();
                            Bundle args = new Bundle();

                            addZopFragment.resetVisibility();
                            args.putString(SimpleDialogFragment.DIALOG_MESSAGE, getString(R.string.new_zop_failed));
                            dialog.setArguments(args);
                            dialog.show(getSupportFragmentManager(), "new_zop_failed");
                        }
                    });
                } else {
                    addZopFragment.resetVisibility();
                    addZopFragment.setMessage();
                }
                break;

            case R.id.menu_zop_review:
                if(currentZop != null) {
                    ReviewDialogFragment dialog = new ReviewDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(ReviewDialogFragment.DIALOG_ZOP_ID, zopID);
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "review");
                }
                break;

            case R.id.menu_zop_drive_to:
                if(currentZop != null && currentZop.Location != null && currentZop.Location.Latitude != null) {
                    Uri uri = Uri.parse(String.format("google.navigation:q=%f,%f", currentZop.Location.Latitude, currentZop.Location.Longitude));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
                break;

            case R.id.menu_zop_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                if (currentZop != null && currentZop.CountryPhoneCode != null && currentZop.PhoneNumber != null && !currentZop.PhoneNumber.trim().equals("")) {
                    callIntent.setData(Uri.parse("tel:" + currentZop.CountryPhoneCode + currentZop.PhoneNumber));
                    startActivity(callIntent);
                }
                break;

            case R.id.menu_location_refresh:
                getLocation(true);
                getAddress(true);
                break;

            case R.id.menu_zop_refresh:
                nextMenu(TypedZopsFragment.newInstance(TypedZopsFragment.zopType, true), false, 1);
                break;

            case R.id.menu_zops_refresh:
                nextMenu(AllZopsFragment.newInstance(position, true), false, 1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!resolvingError) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (this.depth == 1) {
            restoreActionBar(lastPosition);
            mNavigationDrawerFragment.drawerListView.setItemChecked(lastPosition, true);
            onNavigationDrawerItemSelected(lastPosition, false);
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
