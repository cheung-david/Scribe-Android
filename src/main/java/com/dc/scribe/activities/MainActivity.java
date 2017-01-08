package com.dc.scribe.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dc.scribe.R;
import com.dc.scribe.fragments.FeedActivityFragment;
import com.dc.scribe.fragments.HomeFragment;
import com.dc.scribe.fragments.ProfileFragment;
import com.dc.scribe.fragments.SearchFragment;
import com.dc.scribe.model.ScribeCard;
import com.dc.scribe.model.ScribeFollowing;
import com.dc.scribe.model.ScribeLiked;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static final String URL_BASE = "http://52.39.6.195/api";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // Maintaining login state
    private static final String APP_SHARED_PREFS = "scribe_preferences";
    private static CookieJar cookieJar;
    private static OkHttpClient okHttpClient;
    private static OkHttpClient okHttpClient2;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private boolean isUserLoggedIn;
    private static String currentlyLoggedInUserName = "";
    private static String currentlyLoggedInUser;
    private static String token;
    private static Gson gson;
    private static Set<String> followingList = new HashSet<>();
    private static Set<String> likedList = new HashSet<>();

    private final int PERMISSION_CAMERA = 123;


    public static OkHttpClient getOkHttpClient2() {
        return okHttpClient2;
    }

    public static void setOkHttpClient2(OkHttpClient okHttpClient2) {
        MainActivity.okHttpClient2 = okHttpClient2;
    }

    public static String getCurrentlyLoggedInUserName() {
        return currentlyLoggedInUserName;
    }

    public static void setCurrentlyLoggedInUserName(String currentlyLoggedInUserName) {
        MainActivity.currentlyLoggedInUserName = currentlyLoggedInUserName;
    }

    public static Set<String> getLikedList() {
        return likedList;
    }

    public static void setLikedList(Set<String> likedList) {
        MainActivity.likedList = likedList;
    }

    public static Set<String> getFollowingList() {
        return followingList;
    }

    public static void setFollowingList(Set<String> followingList) {
        MainActivity.followingList = followingList;
    }

    public static String getURL_BASE() {
        return URL_BASE;
    }

    public static Gson getGson() {
        return gson;
    }

    public static String getToken() {
        return token;
    }

    public static String getCurrentlyLoggedInUser() {
        return currentlyLoggedInUser;
    }

    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public SharedPreferences getSharedPrefs() {
        return sharedPrefs;
    }

    public static CookieJar getCookieJar() {
        return cookieJar;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static String getAppSharedPrefs() {
        return APP_SHARED_PREFS;
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public void getFollowing() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/following")
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        ScribeFollowing scribeFollowing = MainActivity.getGson().fromJson(obj, ScribeFollowing.class);
                        followingList = scribeFollowing.getFollowing();
                    }
                    Log.d("getFollowing", "" + followingList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public void getLiked() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/likes")
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        ScribeLiked scribeLiked = MainActivity.getGson().fromJson(obj, ScribeLiked.class);
                        likedList = scribeLiked.getLiked();
                    }
                    Log.d("getLikes", "" + likedList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void followUser(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                followingList.add(userId);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/follow/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        JsonArray arr = obj.getAsJsonArray("following");
                        Log.d("SEARCHFRAG", jsonData);
                        addFollowerTo(userId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void unfollowUser(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                followingList.remove(userId);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/unfollow/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        //JsonObject obj = new JsonParser().parse(jsonData).getAsJsonObject();
                        //JsonArray arr = obj.getAsJsonArray("following");
                        removeFollowerFrom(userId);
                        //Log.d("unfollow", jsonData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void addFollowerTo(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/followlist/add/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        // Do something
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void removeFollowerFrom(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/followlist/remove/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        // Do something
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void likePost(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/likes/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        likedList.add(userId);
                        //Log.d("likeUser", jsonData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void unlikePost(final String userId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("", "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/unlikes/" + userId)
                        .post(requestBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        likedList.remove(userId);
                        //Log.d("likeUser", jsonData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }

    public static void notify(final String userId, final String action, final String from, final String content, final String commentId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("notify", "NOTIFY");
                RequestBody formBody = new FormBody.Builder()
                        .add("action", action)
                        .add("from", from)
                        .add("content", content)
                        .add("commentId", commentId)
                        .add("isPersistent", "true")
                        .add("setCookie", "true")
                        .add("withCredentials", "true")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("authorization", MainActivity.getToken())
                        .url(MainActivity.getURL_BASE() + "/notify/notification/" + userId)
                        .post(formBody)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = MainActivity.getOkHttpClient().newCall(request).execute();
                    String jsonData = response.body().string();
                    Log.d("notify", jsonData);
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        //Log.d("notify", jsonData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response != null) {
                    response.body().close();
                }
            }
        });
    }
    /**api/following
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);

        // Login redirect if not logged in
        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
        currentlyLoggedInUser = sharedPrefs.getString("currentLoggedInUserId", "null");
        token = sharedPrefs.getString("token", "null");
        currentlyLoggedInUserName = sharedPrefs.getString("currentLoggedInUserName", "Unknown");

        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.new_post_btn));

        final Context context = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent intent = new Intent(context, CameraActivity.class);
                startActivity(intent);
            }
        });

        gson = new Gson();

        // Set up persistent cookies
        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(getBaseContext()));

        // Initialize okHttp with cookie jar
        okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        okHttpClient2 = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        getFollowing();
        getLiked();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        }
        verifyStoragePermissions(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSION_CAMERA: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do something, not required at the moment
                } else {
                    // Show error dialog, permissions denied
                    Toast.makeText(this, "You denied permissions to run the camera", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
        if (!isUserLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_sign_out) {
            // Sign out user and clear preferences
            editor = sharedPrefs.edit();
            editor.putBoolean("userLoggedInState", false);
            editor.putString("currentLoggedInUserId", "");
            editor.putString("token", "");
            editor.putString("userid", "");
            editor.apply();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_rate) {
            Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return HomeFragment.newInstance();
                case 1:
                    return SearchFragment.newInstance();
                case 2:
                    return FeedActivityFragment.newInstance();
                default:
                    return ProfileFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HOME";
                case 1:
                    return "SEARCH";
                case 2:
                    return "ACTIVITY";
                case 3:
                    return "PROFILE";
            }
            return null;
        }
    }
}
