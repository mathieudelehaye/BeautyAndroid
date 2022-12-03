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
import com.beautyorder.androidclient.databinding.FragmentThirdBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;

    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;

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

        binding = FragmentThirdBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = view.findViewById(R.id.register_email);
        password = view.findViewById(R.id.register_password);
        mAuth = FirebaseAuth.getInstance();

        binding.backThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_FirstFragment);
            }
        });

        binding.confirmThird.setOnClickListener(new View.OnClickListener() {
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

//                    Auth.auth().createUser(withEmail: username, password: password) { authResult, error in
//
//                        if let e = error {
//                            print(e.localizedDescription)
//                        } else {
//                            print("User registration succesful")
//
//                            welcomeCallbacks.segueCallback?(K.welcomeToDaySegueIdentifier)
//                        }
//                    }

                    String emailText = email.getText().toString();
                    String passwordText = password.getText().toString();

                    mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("BeautyOrder", "createUserWithEmail:success");

                                    NavHostFragment.findNavController(ThirdFragment.this)
                                        .navigate(R.id.action_ThirdFragment_to_FourthFragment);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("BeautyOrder", "createUserWithEmail:failure", task.getException());
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