//
//  EBFragmentSignupDialog.java
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

package com.beautyorder.androidclient.controller.tabview.auth;

import android.widget.Button;
import android.widget.EditText;
import com.android.java.androidjavatools.controller.tabview.auth.FragmentSignupDialog;
import com.android.java.androidjavatools.model.AuthManager;
import com.beautyorder.androidclient.R;

public class EBFragmentSignupDialog extends FragmentSignupDialog {
    @Override
    protected Button getAnonymousLogIn() {
        return mContainerView.findViewById(R.id.anonymous_log_in_signup);
    }

    @Override
    protected EditText getEmailToRegister() {
        return mContainerView.findViewById(R.id.registered_email_signup);
    }

    @Override
    protected EditText getPasswordToRegister() {
        return mContainerView.findViewById(R.id.registered_password_signup);
    }

    @Override
    protected EditText getPasswordConfirmation() {
        return mContainerView.findViewById(R.id.repeated_password_signup);
    }

    @Override
    protected Button getConfirmSignUp() {
        return mContainerView.findViewById(R.id.confirm_signup);
    }

    @Override
    protected Button getBack() {
        return mContainerView.findViewById(R.id.back_signup);
    }

    public EBFragmentSignupDialog(AuthManager manager, Integer layoutId) {
        super(manager, layoutId);
    }
}
