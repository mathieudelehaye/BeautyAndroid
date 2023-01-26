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

package com.beautyorder.androidclient.controller.main;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.ResultListAdapter;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.databinding.FragmentResultListBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

public class FragmentResultList extends FragmentWithSearch {
    private FragmentResultListBinding mBinding;
    private final GeoPoint mUserLocation = new GeoPoint(0, 0);
    private ListView mListView;
    private ArrayList<String> mListItemTitles = new ArrayList<>();
    private ArrayList<Integer> mListItemImages = new ArrayList<>();

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentResultListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSearchBox(new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                Log.d("BeautyAndroid", "Change focus to search result");
                setSearchStart(mSearchResult);
                searchItemsToDisplay();
            }

            @Override
            public void onFailure() {
            }
        });

        // Get the user geolocation
        final boolean[] firstLocationReceived = {false};
        var locationProvider = new GpsMyLocationProvider(mCtx);
        locationProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {

                // TODO: improve the way we detect the first gps position fix
                if(!firstLocationReceived[0]) {
                    firstLocationReceived[0] = true;

                    Log.d("BeautyAndroid", "First received location for the user: " + location.toString());
                    mUserLocation.setCoords(location.getLatitude(), location.getLongitude());
                    setSearchStart(mUserLocation);
                    searchItemsToDisplay();
                }
            }
        });

        var viewSwitch = (Button) getView().findViewById(R.id.search_view_switch);
        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Select the map page
                var activity = (MainActivity)getActivity();
                activity.setAppPage(0);

                NavHostFragment.findNavController(FragmentResultList.this)
                    .navigate(R.id.action_ResultFragment_to_AppFragment);
            }
        });
    }

    private void searchItemsToDisplay() {

        // Search for the RP around the user
        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                var resultList = (ListView) getView().findViewById(R.id.result_list_view);

                // Reset the item list if many search are done
                mListItemTitles.clear();

                for (OverlayItem point : mFoundRecyclePoints) {
                    mListItemTitles.add(point.getTitle() + "\n\n" + point.getSnippet());
                    mListItemImages.add(R.drawable.camera);  // placeholder image
                }

                ArrayList<ResultItemInfo> data = new ArrayList<>();
                for (int i = 0; i< mListItemTitles.size(); i++) {
                    data.add(new ResultItemInfo(mListItemTitles.get(i), mListItemImages.get(i)));
                }

                var adapter = new ResultListAdapter(getContext(), data);
                resultList.setAdapter(adapter);

                resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String value = ((ResultItemInfo)adapter.getItem(position)).getTitle();
                        Log.d("BeautyAndroid", "Tapped item: " + value);
                    }
                });
            }

            @Override
            public void onFailure() {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}