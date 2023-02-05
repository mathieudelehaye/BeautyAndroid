//
//  FragmentSignupDialog.java
//
//  Created by Mathieu Delehaye on 5/02/2023.
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

public class FragmentSignupDialog extends FragmentDialog {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        Dialog dialog = buildDialogFromLayout(R.layout.fragment_signup_dialog);

        Button anonymousSignIn = mContainerView.findViewById(R.id.anonymous_log_in_signup);
        if (anonymousSignIn == null) {
            Log.e("BeautyAndroid", "No view found for the anonymous sign-in button on signup dialog");
            return null;
        }

        anonymousSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDialogAnonymousSigninClick(mThis);
            }
        });

        Button confirm = mContainerView.findViewById(R.id.confirm_signup);
        if (confirm == null) {
            Log.e("BeautyAndroid", "No view found for the confirm button on signup dialog");
            return null;
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDialogSignupClick(mThis,
                    new SigninDialogListener.SigninDialogCredentialViews(
                        mContainerView.findViewById(R.id.registered_email_signup),
                        mContainerView.findViewById(R.id.registered_password_signup),
                        mContainerView.findViewById(R.id.repeated_password_signup)));
            }
        });

        Button back = mContainerView.findViewById(R.id.back_signup);
        if (back == null) {
            Log.e("BeautyAndroid", "No view found for the back button on signup dialog");
            return null;
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                var dialog = new FragmentStartDialog();
                dialog.show(getFragmentManager(), "FragmentStartDialog");
            }
        });

        return dialog;
    }
}