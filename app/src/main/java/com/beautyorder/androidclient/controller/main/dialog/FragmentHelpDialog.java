//
//  FragmentHelpDialog.java
//
//  Created by Mathieu Delehaye on 17/02/2023.
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.beautyorder.androidclient.R;

public class FragmentHelpDialog extends DialogFragment {

    private String mTextToDisplay;

    public FragmentHelpDialog(String text) {
        mTextToDisplay = text;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Use the Builder class for convenient dialog construction
        var builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        var inflater = requireActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_help_dialog, null);
        builder.setView(rootView);

        Dialog dialog = builder.create();

        // Configure the dialog
        Button closeHelpDialog = rootView.findViewById(R.id.close_help_dialog);
        if (closeHelpDialog == null) {
            Log.e("BeautyAndroid", "No view found for the close button on help dialog");
            return null;
        }
        closeHelpDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TextView dialogDescription = rootView.findViewById(R.id.description_help_dialog);
        if (dialogDescription == null) {
            Log.e("BeautyAndroid", "No view found for the description text on help dialog");
            return null;
        }
        dialogDescription.setText(mTextToDisplay);

        // Set the window background transparent, so the custom background is visible
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }
}