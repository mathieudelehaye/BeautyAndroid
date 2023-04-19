//
//  EBFragmentLoginDialog.java
//
//  Created by Mathieu Delehaye on 19/04/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
//  Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
//  warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.auth.dialog;

import android.app.Dialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.android.java.androidjavatools.controller.auth.dialog.AuthenticateDialogListener;
import com.android.java.androidjavatools.controller.auth.dialog.FragmentLoginDialog;
import com.beautyorder.androidclient.R;

public class EBFragmentLoginDialog extends FragmentLoginDialog {
    @Override
    protected Dialog initializeGUI(Dialog parentDialog) {
        // Do not use the parent dialog

        Dialog dialog = buildDialogFromLayout(R.layout.fragment_signin_dialog);

        Button anonymousSignIn = mContainerView.findViewById(R.id.anonymous_log_in_signin);
        if (anonymousSignIn == null) {
            Log.e("BeautyAndroid", "No view found for the anonymous sign-in button on login dialog");
            return null;
        }

        anonymousSignIn.setOnClickListener(view -> mListener.onDialogAnonymousSigninClick(mThis));

        Button facebookSignIn = mContainerView.findViewById(R.id.fb_log_in_signin);
        if (facebookSignIn == null) {
            Log.e("BeautyAndroid", "No view found when setting the Facebook sign-in button");
            return null;
        }

        facebookSignIn.setOnClickListener(view -> Toast.makeText(getContext(),
                "Facebook sign-in not yet available", Toast.LENGTH_SHORT).show());

        Button googleSignIn = mContainerView.findViewById(R.id.google_log_in_signin);
        if (googleSignIn == null) {
            Log.e("BeautyAndroid", "No view found when setting the Google sign-in button");
            return null;
        }

        googleSignIn.setOnClickListener(view -> Toast.makeText(getContext(),
                "Google sign-in not yet available", Toast.LENGTH_SHORT).show());

        Button confirm = mContainerView.findViewById(R.id.confirm_signin);
        if (confirm == null) {
            Log.e("BeautyAndroid", "No view found for the confirm button on login dialog");
            return null;
        }

        confirm.setOnClickListener(view -> mListener.onDialogRegisteredSigninClick(mThis,
            new AuthenticateDialogListener.SigningDialogCredentialViews(
                mContainerView.findViewById(R.id.registered_email_signin),
                mContainerView.findViewById(R.id.registered_password_signin),
                null)));

        Button resetPassword = mContainerView.findViewById(R.id.reset_password_signin);
        if (resetPassword == null) {
            Log.e("BeautyAndroid", "No view found for the reset password button on login dialog");
            return null;
        }

        resetPassword.setOnClickListener(view -> mListener.onDialogResetPasswordClick(mThis,
            new AuthenticateDialogListener.SigningDialogCredentialViews(
                mContainerView.findViewById(R.id.registered_email_signin),
                mContainerView.findViewById(R.id.registered_password_signin),
                null)));

        Button signUp = mContainerView.findViewById(R.id.email_sign_up_signin);
        if (signUp == null) {
            Log.e("BeautyAndroid", "No view found for the sign-up button on login dialog");
            return null;
        }

        signUp.setOnClickListener(view -> {
            dismiss();
            var dialog1 = new EBFragmentSignupDialog();
            dialog1.show(getFragmentManager(), "FragmentSignupDialog");
        });

        return dialog;
    }
}
