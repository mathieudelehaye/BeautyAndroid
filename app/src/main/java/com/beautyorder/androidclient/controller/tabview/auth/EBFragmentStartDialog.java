//
//  EBFragmentStartDialog.java
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
import com.android.java.androidjavatools.controller.tabview.auth.FragmentStartDialog;
import com.android.java.androidjavatools.model.AuthManager;
import com.beautyorder.androidclient.R;

public class EBFragmentStartDialog extends FragmentStartDialog {
    public EBFragmentStartDialog(AuthManager manager, Integer layoutId) {
        super(manager, layoutId);
    }

    @Override
    protected Button getAnonymousLogIn() {
        return mContainerView.findViewById(R.id.anonymous_log_in_start);
    }

    @Override
    protected Button getEmailSignUp() {
        return mContainerView.findViewById(R.id.email_sign_up_start);
    }

    @Override
    protected Button getFacebookLogIn() {
        return mContainerView.findViewById(R.id.fb_log_in_start);
    }

    @Override
    protected Button getGoogleLogIn() {
        return mContainerView.findViewById(R.id.google_log_in_start);
    }

    @Override
    protected Button getRegisteredLogIn() {
        return mContainerView.findViewById(R.id.registered_log_in_start);
    }
}
