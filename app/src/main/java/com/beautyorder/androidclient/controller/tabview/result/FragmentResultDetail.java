//
//  FragmentResultDetail.java
//
//  Created by Mathieu Delehaye on 14/02/2023.
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

package com.beautyorder.androidclient.controller.tabview.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.databinding.FragmentResultDetailBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;

public class FragmentResultDetail extends Fragment {
    private FragmentResultDetailBinding mBinding;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentResultDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Result detail view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        // Back button
        mBinding.backResultDetail.setOnClickListener(view1 -> {
            // Go back to the previous fragment
            var activity = (TabViewActivity)getActivity();
            activity.navigateBack();
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Result detail view becomes visible");

            updateDetails();
        }
    }

    private void updateDetails() {
        // Description and image
        final var activity = (TabViewActivity) getActivity();
        final ResultItemInfo info = activity.getSelectedRecyclePoint();

        final byte[] imageBytes = info.getImage();
        final boolean showImage = info.isImageShown();

        String title = showImage ? info.getTitle() : "Lorem ipsum dolor sit";
        String description = showImage ? info.getDescription() : "Lorem ipsum dolor sit amet. Ut enim "
            + "corporis ea labore esse ea illum consequatur. Et reiciendis ducimus et repellat magni id ducimus "
            + "nesc.";

        TextView resultDescription = getView().findViewById(R.id.description_result_detail);
        resultDescription.setText(title + "\n\n" + description);

        ImageView resultImage = getView().findViewById(R.id.image_result_detail);
        if (imageBytes != null && showImage) {
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            resultImage.setImageBitmap(image);
        } else {
            // Use a placeholder if the image has not been set
            resultImage.setImageResource(R.drawable.camera);
        }
        resultImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
