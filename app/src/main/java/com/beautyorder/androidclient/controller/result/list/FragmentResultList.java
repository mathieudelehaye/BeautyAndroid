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

package com.beautyorder.androidclient.controller.result.list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter.ResultPageType;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.controller.tabview.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.result.FragmentShowResult;
import com.beautyorder.androidclient.controller.result.map.OverlayItemWithImage;
import com.beautyorder.androidclient.databinding.FragmentResultListBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.beautyorder.androidclient.model.SearchResult;

public class FragmentResultList extends FragmentShowResult {
    private FragmentResultListBinding mBinding;
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

        displayScoreBox("List", R.id.score_layout_list);

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

                var activity = (TabViewActivity) getActivity();

                var resultList = (ListView) getView().findViewById(R.id.result_list_view);

                var result = new SearchResult();

                final boolean showBrand = mustShowBrand();
                for (int i = 0; i < mFoundRecyclePoints.size(); i++) {
                    final var point = (OverlayItemWithImage) mFoundRecyclePoints.get(i);
                    result.add(new ResultItemInfo(point.getTitle(), point.getSnippet(), null, showBrand),
                        point.getImage());
                }

                var adapter = new ResultListAdapter(getContext(), result.getResultItems());
                resultList.setAdapter(adapter);

                resultList.setOnItemClickListener((adapterView, view, position, l) -> {

                    if (activity == null) {
                        Log.w("BeautyAndroid", "List item tapped but activity not available");
                        return;
                    }

                    final var itemInfo = ((ResultItemInfo)adapter.getItem(position));
                    String title = itemInfo.getTitle();
                    Log.d("BeautyAndroid", "Tapped item: " + title);
                    String description = itemInfo.getDescription();
                    final byte[] imageBytes = itemInfo.getImage();

                    activity.setSelectedRecyclePoint(new ResultItemInfo(title, description, imageBytes, showBrand));
                    activity.navigate(TabViewActivity.FragmentType.DETAIL);
                });

                result.downloadImages(new TaskCompletionManager() {

                    @Override
                    public void onSuccess() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure() {
                    }
                });

                activity.setSearchResult(result);
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

            var activity = (TabViewActivity)getActivity();
            if ((activity) != null) {
                activity.toggleTabSwiping(true);
            }

            //updateSearchResults();

            displayScoreBox("List", R.id.score_layout_list);

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

    private void showHelp() {

        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("list_help_displayed", "false"))) {
                mSharedPref.edit().putString("list_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog(getString(R.string.list_help));
                dialogFragment.show(getChildFragmentManager(), "List help dialog");
            }
        }
    }
}