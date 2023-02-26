//
//  FragmentResultList.java
//
//  Created by Mathieu Delehaye on 21/01/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2022 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.main.list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter.ResultPageType;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.beautyorder.androidclient.controller.main.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.main.map.OverlayItemWithImage;
import com.beautyorder.androidclient.controller.main.search.FragmentWithSearch;
import com.beautyorder.androidclient.databinding.FragmentResultListBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class FragmentResultList extends FragmentWithSearch {
    private FragmentResultListBinding mBinding;
    private ArrayList<String> mFoundRPImageUrls;
    private int mFoundRPNumber = 0;
    private int mReceivedImageNumber = 0;
    private final Object mImageUpdateLock = new Object();
    private ArrayList<ResultItemInfo> mResultItems;
    private boolean mIsViewVisible = false;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentResultListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Result list view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        changeSearchSwitch(ResultPageType.MAP);

        showHelp();
    }

    @Override
    protected void searchAndDisplayItems() {

        // Search for the RP around the user
        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                Log.v("BeautyAndroid", "Results received from database at timestamp: "
                    + Helpers.getTimestamp());

                var resultList = (ListView) getView().findViewById(R.id.result_list_view);

                mFoundRPNumber = mFoundRecyclePoints.size();
                mReceivedImageNumber = 0;
                mResultItems = new ArrayList<>();
                mFoundRPImageUrls = new ArrayList<>();

                for (int i = 0; i < mFoundRPNumber; i++) {
                    final var point = (OverlayItemWithImage) mFoundRecyclePoints.get(i);

                    mResultItems.add(new ResultItemInfo(point.getTitle(), point.getSnippet(), null));

                    mFoundRPImageUrls.add(point.getImage());
                }

                var adapter = new ResultListAdapter(getContext(), mResultItems);
                resultList.setAdapter(adapter);

                resultList.setOnItemClickListener((adapterView, view, position, l) -> {
                    final var itemInfo = ((ResultItemInfo)adapter.getItem(position));
                    String title = itemInfo.getTitle();
                    Log.d("BeautyAndroid", "Tapped item: " + title);
                    String description = itemInfo.getDescription();
                    final byte[] imageBytes = itemInfo.getImage();

                    var activity = (MainActivity) getActivity();
                    activity.setSelectedRecyclePoint(new ResultItemInfo(title, description, imageBytes));
                    activity.showFragment(MainActivity.FragmentType.DETAIL);
                });

                // Asynchronously download the images then update the view adapter
                for (int i = 0; i < mFoundRPImageUrls.size(); i++) {
                    downloadAndDisplayImage(mFoundRPImageUrls.get(i), mResultItems.get(i), adapter);
                }
            }

            @Override
            public void onFailure() {
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            mIsViewVisible = true;

            Log.d("BeautyAndroid", "Result list view becomes visible");

            changeSearchSwitch(ResultPageType.MAP);

            var activity = (MainActivity)getActivity();
            if ((activity) != null) {
                activity.enableTabSwiping();
            }

            //updateSearchResults();

            showHelp();
        } else {
            mIsViewVisible = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void downloadAndDisplayImage(String imageUrl, ResultItemInfo itemInfo, ResultListAdapter viewAdapter) {

        if (imageUrl == null || imageUrl.equals("") || itemInfo == null || viewAdapter == null) {
            Log.w("BeautyAndroid", "Try to download an image but one parameter is missing");
            return;
        }

        var storage = FirebaseStorage.getInstance();

        StorageReference gsReference = storage.getReferenceFromUrl(imageUrl);

        final long ONE_MEGABYTE = 1024 * 1024;

        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {

            synchronized (mImageUpdateLock) {
                itemInfo.setImage(bytes);

                viewAdapter.notifyDataSetChanged();

                mReceivedImageNumber++;
                if (mReceivedImageNumber == mFoundRPNumber) {
                    Log.v("BeautyAndroid", "Last result image received at timestamp: "
                        + Helpers.getTimestamp());
                }
            }
        }).addOnFailureListener(exception -> {
        });
    }

    private void showHelp() {

        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("list_help_displayed", "false"))) {
                mSharedPref.edit().putString("list_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog("Select an item in the list and display some "
                    + " info about a drop-off location!");
                dialogFragment.show(getChildFragmentManager(), "List help dialog");
            }
        }
    }
}