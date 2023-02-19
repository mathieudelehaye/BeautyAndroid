//
//  FragmentAuthenticateDialog.java
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

package com.beautyorder.androidclient.controller.auth.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import org.jetbrains.annotations.NotNull;

public abstract class FragmentAuthenticateDialog extends DialogFragment {

    // Use this instance of the interface to deliver action events from the dialog modal
    protected AuthenticateDialogListener mListener;
    protected FragmentAuthenticateDialog mThis;
    protected View mContainerView;

    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mThis = this;
        return super.onCreateDialog(savedInstanceState);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AuthenticateDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                + " must implement AuthenticateDialogListener");
        }
    }

    protected Dialog buildDialogFromLayout(int layout_id) {
        // Use the Builder class for convenient dialog construction
        var builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        var inflater = requireActivity().getLayoutInflater();

        mContainerView = inflater.inflate(layout_id, null);
        builder.setView(mContainerView);

        // Set the background as transparent and prevent the dialog from cancelling
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
