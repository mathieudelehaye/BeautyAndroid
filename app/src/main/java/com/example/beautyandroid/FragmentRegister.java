//
//  FragmentRegister.java
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

package com.beautyorder.androidclient;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.databinding.FragmentRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FragmentRegister extends Fragment {
    private FragmentRegisterBinding binding;

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mAddress;
    private EditText mCity;
    private EditText mPostCode;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mRepeatedPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
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

        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirstName = view.findViewById(R.id.register_first_name);
        mLastName = view.findViewById(R.id.register_last_name);
        mAddress = view.findViewById(R.id.register_address);
        mCity = view.findViewById(R.id.register_city);
        mPostCode = view.findViewById(R.id.register_post_code);
        mEmail = view.findViewById(R.id.register_email);
        mPassword = view.findViewById(R.id.register_password);
        mRepeatedPassword = view.findViewById(R.id.register_repeat_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        binding.backRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FragmentRegister.this)
                        .navigate(R.id.action_RegisterFragment_to_HomeFragment);
            }
        });

        binding.confirmRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean navigate = true;

                if (isEmpty(mFirstName)) {
                    mFirstName.setError("First name is required!");
                    navigate = false;
                }

                if (isEmpty(mLastName)) {
                    mLastName.setError("Last name is required!");
                    navigate = false;
                }

                if (isEmpty(mAddress)) {
                    mAddress.setError("Address is required!");
                    navigate = false;
                }

                if (isEmpty(mCity)) {
                    mCity.setError("City is required!");
                    navigate = false;
                }

                if (isEmpty(mPostCode)) {
                    mPostCode.setError("Post code is required!");
                    navigate = false;
                }

                if (isEmail(mEmail) == false) {
                    mEmail.setError("Enter valid email!");
                    navigate = false;
                }

                if (isEmpty(mPassword)) {
                    mPassword.setError("Password is required!");
                    navigate = false;
                }

                if (isEmpty(mRepeatedPassword)) {
                    mRepeatedPassword.setError("Password is required!");
                    navigate = false;
                } else {

                    String passwordText = mPassword.getText().toString();
                    String repeatedPasswordText = mRepeatedPassword.getText().toString();

                    if (!repeatedPasswordText.equals(passwordText)) {
                        mRepeatedPassword.setError("The two passwords are different!");
                        navigate = false;
                    }
                }

                if (navigate) {

                    String emailText = mEmail.getText().toString();
                    String passwordText = mPassword.getText().toString();

                    // Create user
                    mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("BeautyAndroid", "createUserWithEmail:success");

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Add user infos to the database
                                    Map<String, Object> userInfo = new HashMap<>();
                                    userInfo.put("first_name", mFirstName.getText().toString());
                                    userInfo.put("last_name", mLastName.getText().toString());
                                    userInfo.put("address", mAddress.getText().toString());
                                    userInfo.put("city", mCity.getText().toString());
                                    userInfo.put("post_code", mPostCode.getText().toString());
                                    userInfo.put("score", 0);

                                    mDatabase.collection("userInfos").document(emailText)
                                        .set(userInfo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("BeautyAndroid", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("BeautyAndroid", "Error writing document", e);
                                            }
                                        });

                                    user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("BeautyAndroid", "Verification email sent.");

                                                    Toast toast = Toast.makeText(getContext(), "Verification email sent", Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            }
                                        });

                                    SystemClock.sleep(1000);

                                    // Navigate to the next fragment
                                    NavHostFragment.findNavController(FragmentRegister.this)
                                        .navigate(R.id.action_RegisterFragment_to_LoginFragment);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("BeautyAndroid", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(view.getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
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

}