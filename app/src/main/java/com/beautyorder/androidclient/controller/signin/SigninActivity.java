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

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.beautyorder.androidclient.SigninDialogListener;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.main.dialog.FragmentStartDialog;

public class SigninActivity extends AppCompatActivity implements SigninDialogListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        DialogFragment newFragment = new FragmentStartDialog();
        newFragment.show(getSupportFragmentManager(), "FragmentStartDialog");
    }

    @Override
    public void onDialogAnonymousSigninClick(DialogFragment dialog) {
        Log.v("BeautyAndroid", "Anonymous sign-in button pressed");
        dialog.dismiss();
    }

    @Override
    public void onDialogRegisteredSigninClick(DialogFragment dialog) {
        Log.v("BeautyAndroid", "Registered sign-in button pressed");
        dialog.dismiss();
    }
}
