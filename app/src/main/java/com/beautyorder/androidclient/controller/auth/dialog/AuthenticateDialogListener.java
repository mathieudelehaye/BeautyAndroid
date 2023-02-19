//
//  AuthenticateDialogListener.java
//
//  Created by Mathieu Delehaye on 4/03/2023.
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

package com.beautyorder.androidclient.controller.auth.dialog;

import android.widget.EditText;
import androidx.fragment.app.DialogFragment;

/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
public interface AuthenticateDialogListener {
    class SigninDialogCredentialViews {
        private EditText mEmail;
        private EditText mPassword;

        private EditText mRepeatPassword;

        public SigninDialogCredentialViews(EditText email, EditText password, EditText repeatPassword) {
            mEmail = email;
            mPassword = password;
            mRepeatPassword = repeatPassword;
        }

        public EditText getEmail() {
            return mEmail;
        }

        public EditText getPassword() {
            return mPassword;
        }

        public EditText getRepeatPassword() {
            return mRepeatPassword;
        }
    };

    void onDialogAnonymousSigninClick(DialogFragment dialog);
    void onDialogRegisteredSigninClick(DialogFragment dialog, SigninDialogCredentialViews credentials);
    void onDialogSignupClick(DialogFragment dialog, SigninDialogCredentialViews credentials);
    void onDialogResetPasswordClick(DialogFragment dialog, SigninDialogCredentialViews credentials);
}
