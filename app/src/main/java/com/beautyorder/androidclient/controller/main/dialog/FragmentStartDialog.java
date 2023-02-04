//
//  FragmentStartDialog.java
//
//  Created by Mathieu Delehaye on 3/02/2023.
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

package com.beautyorder.androidclient.controller.main.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.main.FragmentHelp;

public class FragmentStartDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        var builder = new Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View containerView = inflater.inflate(R.layout.fragment_start_dialog, null);
        builder.setView(containerView);

        // Set the background as transparent and prevent the dialog from cancelling
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Button anonymousSignIn = containerView.findViewById(R.id.anonymous_log_in_start);

        if (anonymousSignIn == null) {
            Log.e("BeautyAndroid", "No view found when setting the anonymous sign-in button");
            return null;
        }

        anonymousSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();
            }
        });

        Button registeredSignIn = containerView.findViewById(R.id.registered_log_in_start);

        if (registeredSignIn == null) {
            Log.e("BeautyAndroid", "No view found when setting the registered sign-in button");
            return null;
        }

        registeredSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();
                var dialog = new FragmentSigninDialog();
                dialog.show(getFragmentManager(), "FragmentSigninDialog");
            }
        });

        return dialog;
    }
}