//
//  FragmentSigninDialog.java
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

package com.beautyorder.androidclient.controller.signin.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.beautyorder.androidclient.R;

public class FragmentSigninDialog extends FragmentDialog {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        Dialog dialog = buildDialogFromLayout(R.layout.fragment_signin_dialog);

        Button anonymousSignIn = mContainerView.findViewById(R.id.anonymous_log_in_signin);

        if (anonymousSignIn == null) {
            Log.e("BeautyAndroid", "No view found for the anonymous sign-in button on login dialog");
            return null;
        }

        anonymousSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDialogAnonymousSigninClick(mThis);
            }
        });

        Button confirmSignIn = mContainerView.findViewById(R.id.confirm_signin);

        if (confirmSignIn == null) {
            Log.e("BeautyAndroid", "No view found for the confirm sign-in button on login dialog");
            return null;
        }

        confirmSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDialogRegisteredSigninClick(mThis,
                    new SigninDialogListener.SigninDialogCredentialViews(
                        mContainerView.findViewById(R.id.registered_email_signin),
                        mContainerView.findViewById(R.id.registered_password_signin)));
            }
        });

        Button resetPassword = mContainerView.findViewById(R.id.reset_password_signin);

        if (resetPassword == null) {
            Log.e("BeautyAndroid", "No view found for the reset password button on login dialog");
            return null;
        }

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDialogResetPasswordClick(mThis,
                    new SigninDialogListener.SigninDialogCredentialViews(
                        mContainerView.findViewById(R.id.registered_email_signin),
                        mContainerView.findViewById(R.id.registered_password_signin)));
            }
        });

        return dialog;
    }
}