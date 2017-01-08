package com.dc.scribe.activities;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.scribe.PersistentCookieStore;
import com.dc.scribe.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.CookieJar;
import okhttp3.FormBody;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private final String URL_BASE = "http://52.39.6.195/api";
    private String URL_REQ = "";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserRegisterTask mRegisterTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView, registerEmail, registerName;
    private EditText mPasswordView, registerPassword, registerConfirmPassword;
    private View mProgressView;
    private View mLoginFormView, mRegisterFormView;
    private String token = "";
    private String userId = "";
    private String fullName = "";
    private String fbToken = "";
    private JSONArray following;

    // Fb login
    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;

    // Maintaining login state
    private static final String APP_SHARED_PREFS = "scribe_preferences";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    private boolean isUserLoggedIn;

    // Cookie Persistence
    private CookieJar cookieJar;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPrefs = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);

        // Redirect if user is already logged in
        isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
        if (isUserLoggedIn) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        // Initialize for cookie persistence
        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(getBaseContext()));

        okHttpClient = new OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .build();

        fbLoginButton = (LoginButton)findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions(Arrays.asList(
                "email", "user_friends", "public_profile"));

        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbToken = loginResult.getAccessToken().getToken().toString();
                Log.d("Graph req:", fbToken);
                fbloginAPI();
//                GraphRequest request = GraphRequest.newMeRequest(
//                        loginResult.getAccessToken(),
//                        new GraphRequest.GraphJSONObjectCallback() {
//                            @Override
//                            public void onCompleted(JSONObject object, GraphResponse response) {
//                                Log.v("LoginActivity", response.toString());
//                                Log.d("FBLogin", "" + object);
//                                // Application code
//                                // String email = object.getString("email");
//                                // String birthday = object.getString("birthday"); // 01/31/1980 format
//                                try {
//                                    String profileId = object.getString("id");
//                                } catch (Exception e) {
//                                    Log.d("Graph req: Json Except", "" + e);
//                                }
//
//                            }
//                        });
//                        Bundle parameters = new Bundle();
//                        parameters.putString("fields", "id, name, email, gender");
//                        request.setParameters(parameters);
//                        request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email_field);
        populateAutoComplete();

        registerEmail = (AutoCompleteTextView) findViewById(R.id.register_email_field);
        registerName = (AutoCompleteTextView) findViewById(R.id.name_field);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        registerPassword = (EditText) findViewById(R.id.register_password);
        registerConfirmPassword = (EditText) findViewById(R.id.register_password_confirm);
        registerConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.login_progress);

        TextView registerSwitch = (TextView) findViewById(R.id.register_switch);
        registerSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mRegisterFormView.setVisibility(View.VISIBLE);
                mLoginFormView.setVisibility(View.GONE);
            }
        });
        TextView loginSwitch = (TextView) findViewById(R.id.login_switch);
        loginSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginFormView.setVisibility(View.VISIBLE);
                mRegisterFormView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void loginAPI() {
//        Map<String,String> params = new HashMap<String,String>();
//        params.put("email", mEmailView.getText().toString());
//        params.put("password", mPasswordView.getText().toString());
//        JSONObject jsonParams = new JSONObject(params);
//
//        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_BASE + "/signin", jsonParams, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.v("LOGIN RES", "SUCCESS:" + response.toString());
//                try {
//                    token = response.getString("token");
//                    userId = response.getString("userId");
//                } catch(JSONException e) {
//                    Log.v("JSON", "Exception: " + e.getLocalizedMessage());
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.v("LOGIN RES", "ERR:" + error.getLocalizedMessage());
//            }
//        });

        //Volley.newRequestQueue(this).add(jsonRequest);
        Map<String,String> params = new HashMap<String,String>();
        params.put("email", mEmailView.getText().toString());
        params.put("password", mPasswordView.getText().toString());
        JSONObject jsonParams = new JSONObject(params);
        //RequestBody body = RequestBody.create(JSON, jsonParams.toString());

        RequestBody formBody = new FormBody.Builder()
                .add("email", mEmailView.getText().toString())
                .add("password", mPasswordView.getText().toString())
                .add("isPersistent", "true")
                .add("setCookie", "true")
                .add("withCredentials", "true")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(URL_BASE + "/signin")
                            .post(formBody)
                            .build();
        try {
            okhttp3.Response response = null;
            String jsonData = "";
            try {
                response = okHttpClient.newCall(request).execute();
                jsonData = response.body().string();
                //Log.v("LOGIN", "SUCCESS: " + jsonData);
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject(jsonData);
            token = jsonObject.getString("token");
            userId = jsonObject.getString("userid");
            fullName = jsonObject.getString("fullName");
        }
        catch (JSONException e) {
             Log.v("JSON", "Exception: " + e.getLocalizedMessage());
        }
    }

    private void registerAPI() {
        RequestBody formBody = new FormBody.Builder()
                .add("email", registerEmail.getText().toString())
                .add("password", registerPassword.getText().toString())
                .add("name", registerName.getText().toString())
                .add("isPersistent", "true")
                .add("setCookie", "true")
                .add("withCredentials", "true")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(URL_BASE + "/signup")
                .post(formBody)
                .build();
        try {
            okhttp3.Response response = null;
            String jsonData = "";
            try {
                response = okHttpClient.newCall(request).execute();
                jsonData = response.body().string();
                //Log.v("LOGIN", "SUCCESS: " + jsonData);
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject(jsonData);
            token = jsonObject.getString("token");
            userId = jsonObject.getString("userid");
            fullName = jsonObject.getString("fullName");
        }
        catch (JSONException e) {
            Log.v("JSON", "Exception: " + e.getLocalizedMessage());
        }
    }

    private void fbloginAPI() {;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .header("access_token", fbToken)
                        .header("withCredentials", "true")
                        .header("setCookie", "true")
                        .header("isPersistent", "true")
                        .url(URL_BASE + "/signin/auth/facebook")
                        .build();

                okhttp3.Response response = null;
                String jsonData = "";
                try {
                    response = okHttpClient.newCall(request).execute();
                    jsonData = response.body().string();
                    //Log.v("LOGIN", "SUCCESS: " + jsonData);
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonData);
                            token = jsonObject.getString("token");
                            userId = jsonObject.getString("userid");
                            fullName = jsonObject.getString("fullName");
                            isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
                            editor = sharedPrefs.edit();
                            editor.putBoolean("userLoggedInState", true);
                            editor.putString("currentLoggedInUserId", userId);
                            editor.putString("token", token);
                            editor.putString("currentLoggedInUserName", fullName);
                            editor.apply();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent signUpSuccess = new Intent(getBaseContext(), MainActivity.class);
                                    signUpSuccess.putExtra("reqFrom", "login");
                                    startActivity(signUpSuccess);
                                    finish();
                                }
                            });
                        } catch (JSONException e) {
                            Log.v("JSON", "Exception: " + e.getLocalizedMessage());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

        // Reset errors.
        registerEmail.setError(null);
        registerPassword.setError(null);

        // Store values at the time of the register attempt.
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();
        String confirmPassword = registerConfirmPassword.getText().toString();
        String fullName = registerName.getText().toString();
        //Log.d("pass", password + ":" + confirmPassword + "-" + (password != confirmPassword));

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(password)) {
            registerPassword.setError(getString(R.string.error_field_required));
            focusView = registerPassword;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            registerPassword.setError(getString(R.string.error_invalid_password));
            focusView = registerPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            registerEmail.setError(getString(R.string.error_field_required));
            focusView = registerEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            registerEmail.setError(getString(R.string.error_invalid_email));
            focusView = registerEmail;
            cancel = true;
        }

        if(TextUtils.isEmpty(fullName)) {
            registerName.setError(getString(R.string.error_field_required));
            focusView = registerName;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!password.equals(confirmPassword)) {
            registerPassword.setError(getString(R.string.error_password_not_match));
            focusView = registerPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showRegisterProgress(true);
            mRegisterTask = new UserRegisterTask(email, password);
            mRegisterTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showRegisterProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                loginAPI();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }

            if(token != "") {
                return true;
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
                editor = sharedPrefs.edit();
                editor.putBoolean("userLoggedInState", true);
                editor.putString("currentLoggedInUserId", userId);
                editor.putString("token", token);
                editor.putString("currentLoggedInUserName", fullName);
                editor.apply();

                Intent signUpSuccess = new Intent(getBaseContext(), MainActivity.class);
                signUpSuccess.putExtra("reqFrom", "login");
                startActivity(signUpSuccess);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserRegisterTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                registerAPI();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }

            if(token != "") {
                return true;
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegisterTask = null;
            showRegisterProgress(false);

            if (success) {
                isUserLoggedIn = sharedPrefs.getBoolean("userLoggedInState", false);
                editor = sharedPrefs.edit();
                editor.putBoolean("userLoggedInState", true);
                editor.putString("currentLoggedInUserId", userId);
                editor.putString("currentLoggedInUserName", fullName);
                editor.putString("token", token);
                editor.apply();

                Intent signUpSuccess = new Intent(getBaseContext(), MainActivity.class);
                signUpSuccess.putExtra("reqFrom", "login");
                startActivity(signUpSuccess);
                finish();
            } else {
                registerEmail.setError(getString(R.string.error_email_taken));
                registerEmail.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            showRegisterProgress(false);
        }
    }
}

