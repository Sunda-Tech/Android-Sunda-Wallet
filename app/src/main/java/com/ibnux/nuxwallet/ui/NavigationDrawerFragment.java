package com.ibnux.nuxwallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.NavigasiAdapter;
import com.ibnux.nuxwallet.data.Navigasi;
import com.ibnux.nuxwallet.utils.NavCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.TextCallback;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements NavCallback {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private NavigasiAdapter adapter;
    private RecyclerView listView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;



    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        listView = layout.findViewById(R.id.listViewNav);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        String navTemp = Aplikasi.sp.getString("navigasi","[]");
        long navTime = Aplikasi.sp.getLong("navigasi_time",0L);
        Utils.log("navTime: "+navTime);
        if(System.currentTimeMillis()-navTime<36000){
            Utils.log("navigasi cache");
            try{
                JsonArray json = new JsonParser().parse(navTemp).getAsJsonArray();
                int jml = json.size();
                List<Navigasi> list = new ArrayList<>();
                for(int n=0;n<jml;n++){
                    Navigasi nav = new Navigasi();
                    nav.title = json.get(n).getAsJsonObject().get("title").getAsString();
                    if( json.get(n).getAsJsonObject().has("description")) {
                        nav.description = json.get(n).getAsJsonObject().get("description").getAsString();
                    }
                    nav.url = json.get(n).getAsJsonObject().get("url").getAsString();
                    nav.openAt = json.get(n).getAsJsonObject().get("openAt").getAsString();
                    if( json.get(n).getAsJsonObject().has("openAt")) {
                        nav.icon = json.get(n).getAsJsonObject().get("openAt").getAsString();
                    }
                    list.add(nav);
                }
                setNavigasi(list);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Utils.log("navigasi online");
            NuxCoin.getFromUrl(Constants.navigasiServer, new TextCallback() {
                @Override
                public void onTextCallback(String string) {
                    try{
                        JsonArray json = new JsonParser().parse(string).getAsJsonArray();
                        int jml = json.size();
                        List<Navigasi> list = new ArrayList<>();
                        for(int n=0;n<jml;n++){
                            Navigasi nav = new Navigasi();
                            nav.title = json.get(n).getAsJsonObject().get("title").getAsString();
                            if( json.get(n).getAsJsonObject().has("description"))
                                nav.description = json.get(n).getAsJsonObject().get("description").getAsString();
                            nav.url = json.get(n).getAsJsonObject().get("url").getAsString();
                            nav.openAt = json.get(n).getAsJsonObject().get("openAt").getAsString();
                            if( json.get(n).getAsJsonObject().has("openAt")) {
                                nav.icon = json.get(n).getAsJsonObject().get("openAt").getAsString();
                            }
                            list.add(nav);
                        }
                        setNavigasi(list);
                        Aplikasi.sp.edit().putString("navigasi",string).putLong("navigasi_time",System.currentTimeMillis()).apply();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorCallback(int errorCode, String errorMessage) {
                    Utils.log(errorMessage);
                }
            });
        }
        return layout;
    }

    private void setNavigasi(List<Navigasi> list){
        Utils.log("nav "+list.size()+" masuk");
        adapter = new NavigasiAdapter(list,this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onNavCallback(Navigasi navigasi) {
        if(navigasi.openAt.equals("internal")){
            Intent iweb = new Intent(getContext(), WebViewActivity.class);
            iweb.putExtra("url", navigasi.url);
            startActivity(iweb);
        }else if(navigasi.openAt.equals("external")){
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(navigasi.url)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(navigasi.openAt.equals("tabs")){
            try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();;
            customTabsIntent.launchUrl(getContext(), Uri.parse(navigasi.url));
            }catch (Exception e){
                e.printStackTrace();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(navigasi.url)));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_menu,             /* nav drawer image to replace 'Up' caret */
                R.string.open,  /* "open drawer" description for accessibility */
                R.string.close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void openDrawer(){
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
        void onNavigationDrawerItemSelectedTitle(String title);
    }

    public int getPosition(){
        return mCurrentSelectedPosition;
    }

}
