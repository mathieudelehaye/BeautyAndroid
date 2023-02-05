//
//  Signin.java
//
//  Created by Mathieu Delehaye on 4/02/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.signin;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.signin.dialog.FragmentSigninDialog;
import com.beautyorder.androidclient.controller.signin.dialog.FragmentStartDialog;
import com.beautyorder.androidclient.controller.signin.dialog.SigninDialogListener;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.ScoreTransferer;
import com.beautyorder.androidclient.model.UserInfoDBEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SigninActivity extends ActivityWithStart implements SigninDialogListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private StringBuilder mDeviceId;
    private StringBuilder mPrefUserId;
    private SigninActivity mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("BeautyAndroid", "Sign-up screen started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        mThis = this;

        // Navigate to the App screen if there is a registered uid in the app preferences
        getPreferenceIds();
        String lastUId = mPrefUserId.toString();
        if (!lastUId.equals("") && Helpers.isEmail(lastUId)) {
            startAppWithUser(lastUId, AppUser.AuthenticationType.REGISTERED);
        }

        DialogFragment newFragment = new FragmentStartDialog();
        newFragment.show(getSupportFragmentManager(), "FragmentStartDialog");
    }

    @Override
    public void onDialogAnonymousSigninClick(DialogFragment dialog) {
        Log.v("BeautyAndroid", "Anonymous sign-in button pressed");
        dialog.dismiss();

        String anonymousUid = getAnonymousUidFromPreferences();
        if (!anonymousUid.equals("")) {
            // Reuse the anonymous uid if it already exists in the app preferences
            Log.v("BeautyAndroid", "Anonymous uid reused: " + anonymousUid);

            startAppWithUser(anonymousUid, AppUser.AuthenticationType.NOT_REGISTERED);
        } else {
            searchDBForAutoUserId();
        }
    }

    @Override
    public void onDialogRegisteredSigninClick(DialogFragment dialog, SigninDialogCredentialViews credentials) {

        boolean navigate = true;

        EditText email = credentials.getEmail();
        String emailText = email.getText().toString();

        EditText password = credentials.getPassword();
        String passwordText = password.getText().toString();

        if (!Helpers.isEmail(emailText)) {
            email.setError("Enter valid email!");
            navigate = false;
        }

        if (Helpers.isEmpty(passwordText)) {
            password.setError("Password is required!");
            navigate = false;
        }

        if (navigate) {

            mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("BeautyAndroid", "signInWithEmail:success");

                            // Check if the user email is verified
                            FirebaseUser dbUser = mAuth.getCurrentUser();

                            if (dbUser.isEmailVerified()) {

                                new ScoreTransferer(FirebaseFirestore.getInstance(),
                                    getAnonymousUidFromPreferences(),
                                    emailText, mThis)
                                    .run();

                                startAppWithUser(emailText, AppUser.AuthenticationType.REGISTERED);
                            } else {
                                Log.e("BeautyAndroid", "Email is not verified");

                                Toast.makeText(mThis, "Email not verified",
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("BeautyAndroid", "signInWithEmail:failure", task.getException());
                            Toast.makeText(mThis, "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

        dialog.dismiss();
    }

    @Override
    public void onDialogSignupClick(DialogFragment dialog, SigninDialogCredentialViews credentials) {
        EditText email = credentials.getEmail();
        String emailText = email.getText().toString();

        EditText password = credentials.getPassword();
        String passwordText = password.getText().toString();

        EditText repeatPassword = credentials.getRepeatPassword();
        String repeatPasswordText = repeatPassword.getText().toString();

        boolean navigate = true;

        if (!Helpers.isEmail(credentials.getEmail().getText().toString())) {
            credentials.getEmail().setError("Enter valid email!");
            navigate = false;
        }

        if (Helpers.isEmpty(passwordText)) {
            password.setError("Password is required!");
            navigate = false;
        }

        if (Helpers.isEmpty(repeatPasswordText)) {
            repeatPassword.setError("Password is required!");
            navigate = false;
        } else {
            if (!repeatPasswordText.equals(passwordText)) {
                repeatPassword.setError("The two passwords are different!");
                navigate = false;
            }
        }

        if (navigate) {

            // Create user
            mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("BeautyAndroid", "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            // Add userInfos table entry to the database matching the new user
                            Map<String, String> userInfoMap = new HashMap<>();
                            userInfoMap.put("first_name", "");
                            userInfoMap.put("last_name", "");
                            userInfoMap.put("address", "");
                            userInfoMap.put("city", "");
                            userInfoMap.put("post_code", "");
                            userInfoMap.put("score", "");
                            userInfoMap.put("score_time", "");
                            userInfoMap.put("device_id", mThis.getSharedPreferences(
                                getString(R.string.app_name), Context.MODE_PRIVATE).getString(
                                getString(R.string.device_id), ""));

                            UserInfoDBEntry userInfo = new UserInfoDBEntry(mDatabase, emailText, userInfoMap);
                            userInfo.createAllDBFields();

                            user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("BeautyAndroid", "Verification email sent.");

                                            Toast toast = Toast.makeText(mThis, "Verification email sent", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });

                            SystemClock.sleep(1000);

                            // Navigate to the login dialog
                            dialog.dismiss();
                            var dialog = new FragmentSigninDialog();
                            dialog.show(getSupportFragmentManager(), "FragmentSigninDialog");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("BeautyAndroid", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mThis, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    @Override
    public void onDialogResetPasswordClick(DialogFragment dialog, SigninDialogCredentialViews credentials) {

        EditText email = credentials.getEmail();
        String emailText = email.getText().toString();

        if (Helpers.isEmail(emailText)) {

            mAuth.sendPasswordResetEmail(emailText)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("BeautyAndroid", "Password reset email sent.");

                            Toast toast = Toast.makeText(mThis, "Password reset email sent",
                                Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Log.w("BeautyAndroid", "Password reset didn't work.");
                        }
                    }
                });
        }
    }

    private void searchDBForAutoUserId() {

        // Query the database for an anonymous user with the same device id
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("userInfos")
            .whereEqualTo("device_id", mDeviceId.toString())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("BeautyAndroid", "Unsuccessful search for a userInfos DB entry matching the "
                            + "device");
                    }

                    QuerySnapshot snapshot = task.getResult();

                    final var anonymousUId = new StringBuilder("");
                    if (snapshot.size() != 0) {
                        var userInfosEntry = snapshot.getDocuments().get(0);
                        String uid = userInfosEntry.getId();

                        if (!Helpers.isEmail(uid)) {
                            anonymousUId.append(uid);
                        }
                    }

                    if (anonymousUId.length() == 0) {
                        Log.v("BeautyAndroid", "No userInfos entry found in the DB for the device: "
                            + mDeviceId);

                        // Create an anonymous user
                        tryAndCreateAutoUserId();
                        return;
                    }

                    final String anonymousUidText = anonymousUId.toString();
                    Log.v("BeautyAndroid", "Anonymous uid read from the database: " + anonymousUidText
                        + ", matching the device id: " + mDeviceId);

                    setAnonymousUidToPreferences(anonymousUidText);

                    startAppWithUser(anonymousUidText, AppUser.AuthenticationType.NOT_REGISTERED);
                }
            });
    }

    private void tryAndCreateAutoUserId() {

        // Query the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("userInfos")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get the user number already in the database
                    QuerySnapshot snapshot = task.getResult();
                    int userNumber = snapshot.size();

                    // Get the timestamp
                    Date date = new Date();
                    String time = String.valueOf(date.getTime());

                    // Compute the uid
                    String tmpId = mDeviceId.toString() + time + String.valueOf(userNumber);

                    byte[] hash = {};
                    StringBuilder uid = new StringBuilder("");

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-1");

                        hash = md.digest(tmpId.getBytes(StandardCharsets.UTF_8));

                        uid.append(UUID.nameUUIDFromBytes(hash).toString());

                        // Add userInfos table entry to the database for the anonymous user
                        var userInfo = new UserInfoDBEntry(mDatabase, uid.toString());
                        userInfo.setScoreTime(UserInfoDBEntry.scoreTimeFormat.format(
                            UserInfoDBEntry.getDayBeforeDate(date)));
                        userInfo.setDeviceId(mSharedPref.getString(getString(R.string.device_id), ""));

                        userInfo.createAllDBFields(new TaskCompletionManager() {
                            @Override
                            public void onSuccess() {

                                String uidText = uid.toString();

                                Log.d("BeautyAndroid", "The user automatic identifier was created: "
                                        + uidText);

                                setAnonymousUidToPreferences(uidText);

                                startAppWithUser(uidText, AppUser.AuthenticationType.NOT_REGISTERED);
                            }

                            @Override
                            public void onFailure() {
                                // If the user id wasn't created in the database, try to generate another one
                                // and to write it again
                                tryAndCreateAutoUserId();
                            }
                        });
                    } catch (NoSuchAlgorithmException e) {
                        Log.e("BeautyAndroid", e.toString());
                    }
                }
            });
    }

    private void getPreferenceIds() {
        final Context ctxt = this;

        if (ctxt == null) {
            Log.w("BeautyAndroid", "No context to get the app preferences");
            return;
        }

        // Get the last uid
        mPrefUserId = new StringBuilder();
        mPrefUserId.append(mSharedPref.getString(getString(R.string.app_uid), ""));

        if (!mPrefUserId.toString().equals("")) {
            Log.v("BeautyAndroid", "Latest uid loaded from the app preferences: " + mPrefUserId.toString());
        }

        // Get the device id
        mDeviceId = new StringBuilder("");
        mDeviceId.append(mSharedPref.getString(getString(R.string.device_id), ""));

        if (!mDeviceId.toString().equals("")) {
            Log.v("BeautyAndroid", "The device id was read from the app preferences: " + mDeviceId.toString());
        } else {
            // If not found in the app preferences, read the device id and store it there
            readPhoneId();
        }
    }

    private void readPhoneId() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // From Android 10
            mDeviceId.append(Settings.Secure.getString(
                mThis.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        } else {
            var telephonyManager = (TelephonyManager) mThis.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDeviceId() != null) {
                mDeviceId.append(telephonyManager.getDeviceId());
            } else {
                mDeviceId.append(Settings.Secure.getString(
                    mThis.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            }
        }

        if (mDeviceId.toString().equals("")) {
            Log.e("BeautyAndroid", "Cannot determine the device id. Use a fake one instead");
            mDeviceId.append("1234");
            mSharedPref.edit().putString(getString(R.string.device_id), mDeviceId.toString()).commit();
        } else {
            mSharedPref.edit().putString(getString(R.string.device_id), mDeviceId.toString()).commit();
            Log.v("BeautyAndroid", "The device id was found on the device and written to the app "
                + "preferences: " + mDeviceId.toString());
        }
    }
}
