package com.example.beautyandroid;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.beautyandroid.databinding.FragmentThirdBinding;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;

    private EditText email;
    private EditText password;

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
                    NavHostFragment.findNavController(ThirdFragment.this)
                            .navigate(R.id.action_ThirdFragment_to_FourthFragment);
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