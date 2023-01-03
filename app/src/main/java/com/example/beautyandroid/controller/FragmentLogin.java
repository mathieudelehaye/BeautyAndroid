//
//  FragmentLogin.java
//
//  Created by Mathieu Delehaye on 1/12/2022.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.example.beautyandroid.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentLoginBinding;
import com.example.beautyandroid.Helpers;
import com.example.beautyandroid.TaskCompletionManager;
import com.example.beautyandroid.model.AppUser;
import com.example.beautyandroid.model.UserInfoEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class FragmentLogin extends FragmentWithStart {

    private FragmentLoginBinding binding;
    private EditText mEmail;
    private EditText mPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;
    private StringBuilder mVerifiedRegisteredUid = new StringBuilder("");

    // TODO: put those methods in a shared module
    boolean isEmail(EditText text) {

        if (text == null) {
            Log.e("BeautyAndroid", "Email format check on a null object");
            return false;
        }

        return Helpers.isEmail(text.getText().toString());
    }

    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedPref = getContext().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        mEmail = view.findViewById(R.id.signin_email);
        mPassword = view.findViewById(R.id.signin_password);
        mAuth = FirebaseAuth.getInstance();

        binding.backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentLogin.this)
                    .navigate(R.id.action_LoginFragment_to_HomeFragment);
            }
        });

        binding.confirmLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean navigate = true;

                if (isEmail(mEmail) == false) {
                    mEmail.setError("Enter valid email!");
                    navigate = false;
                }

                if (isEmpty(mPassword)) {
                    mPassword.setError("Password is required!");
                    navigate = false;
                }

                if (navigate) {

                    String emailText = mEmail.getText().toString();
                    String passwordText = mPassword.getText().toString();

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

                                        mVerifiedRegisteredUid.append(emailText);

                                        checkAndTransferScoreFromAnonymousUser();

                                        startAppWithUser(mSharedPref, R.id.action_LoginFragment_to_AppFragment,
                                            emailText, AppUser.AuthenticationType.REGISTERED);
                                    } else {
                                        Log.e("BeautyAndroid", "Email is not verified");

                                        Toast.makeText(view.getContext(), "Email not verified",
                                            Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("BeautyAndroid", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(view.getContext(), "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                                }
                            }
                    });
                }
            }
        });

        binding.resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if (isEmail(mEmail)) {
                String emailText = mEmail.getText().toString();

                mAuth.sendPasswordResetEmail(emailText)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("BeautyAndroid", "Password reset email sent.");

                                Toast toast = Toast.makeText(getContext(), "Password reset email sent",
                                    Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                Log.w("BeautyAndroid", "Password reset didn't work.");
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkAndTransferScoreFromAnonymousUser() {

        String anonymousUid = getAnonymousUidFromPreferences(mSharedPref);
        if (!anonymousUid.equals("")) {
            // An anonymous uid already was found in the app preferences
            Log.v("BeautyAndroid", "Try to transfer score from the anonymous uid found in the app preferences: "
                    + anonymousUid);

            // Get the DB
            mDatabase = FirebaseFirestore.getInstance();

            UserInfoEntry anonymousUserEntry = new UserInfoEntry(mDatabase, anonymousUid);
            anonymousUserEntry.readScoreDBFields(new TaskCompletionManager() {
                @Override
                public void onSuccess() {

                    if (anonymousUserEntry.getScore() > 0) {

                        clearAndTransferFromAnonymousUser(anonymousUserEntry);
                    }
                }

                @Override
                public void onFailure() {
                }
            });
        }
    }
    
    private void clearAndTransferFromAnonymousUser(UserInfoEntry anonymousUserEntry) {

        // Clear the anonymous user score in the DB
        anonymousUserEntry.setScore(0);
        anonymousUserEntry.setScoreTime(UserInfoEntry.scoreTimeFormat.format(
            UserInfoEntry.getDayBeforeDate(new Date())));
        anonymousUserEntry.updateScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                readAndAddToRegisteredUser(anonymousUserEntry.getScore(), anonymousUserEntry.getScoreTime());
            }

            @Override
            public void onFailure() {
            }
        });
    }
    
    private void readAndAddToRegisteredUser(int scoreToAddValue, Date scoreToAddTimestamp) {

        // Read the registered user info from the DB
        UserInfoEntry registeredUserEntry = new UserInfoEntry(mDatabase, mVerifiedRegisteredUid.toString());
        registeredUserEntry.readScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                if (scoreToAddTimestamp.compareTo(registeredUserEntry.getScoreTime()) < 0) {
                    // If the score to add date is older than the registered user score date, add the former to the
                    // registered user score.

                    updateUserScore(registeredUserEntry,
                        registeredUserEntry.getScore() + scoreToAddValue);
                } else {
                    // Otherwise, add the (anonymous score - 1) to the registered one

                    updateUserScore(registeredUserEntry,
                        registeredUserEntry.getScore() + scoreToAddValue - 1);
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private void updateUserScore(UserInfoEntry userEntry, int newScore) {
        // Clear the anonymous user score in the DB
        userEntry.setScore(0);
        userEntry.updateScoreDBFields(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
            }
        });
    }
}