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

package com.beautyorder.androidclient;

import android.os.Bundle;
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
import com.beautyorder.androidclient.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FragmentLogin extends Fragment {

    private FragmentLoginBinding binding;
    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;

    // TODO: put those methods in a shared module
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

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = view.findViewById(R.id.signin_email);
        password = view.findViewById(R.id.signin_password);
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

                if (isEmail(email) == false) {
                    email.setError("Enter valid email!");
                    navigate = false;
                }

                if (isEmpty(password)) {
                    password.setError("Password is required!");
                    navigate = false;
                }

                if (navigate) {

                    String emailText = email.getText().toString();
                    String passwordText = password.getText().toString();

                    mAuth.signInWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("BeautyAndroid", "signInWithEmail:success");

                                    // Check if the user email is verified
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user.isEmailVerified()) {
                                        NavHostFragment.findNavController(FragmentLogin.this)
                                            .navigate(R.id.action_LoginFragment_to_AppFragment);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}