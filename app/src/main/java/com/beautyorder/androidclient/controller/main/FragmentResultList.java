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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.databinding.FragmentResultListBinding;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import java.util.ArrayList;

public class FragmentResultList extends FragmentWithSearch {
    private FragmentResultListBinding mBinding;
    private final GeoPoint mUserLocation = new GeoPoint(0, 0);
    private ListView mListView;
    private ArrayList<String> mListItems = new ArrayList<>();

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

        // Get the user geolocation
        final boolean[] firstLocationReceived = {false};
        var locationProvider = new GpsMyLocationProvider(mCtx);
        locationProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {

                // TODO: improve the we we detect the first gps fix
                if(!firstLocationReceived[0]) {
                    firstLocationReceived[0] = true;

                    Log.d("BeautyAndroid", "First received location for the user: " + location.toString());
                    mUserLocation.setCoords(location.getLatitude(), location.getLongitude());
                    readItems();
                }
            }
        });
    }

    private void readItems() {

        // Set it as a search start
        setSearchStart(mUserLocation);

        // Search for the RP around the user
        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                var resultList = (ListView) getView().findViewById(R.id.listView);

                for (OverlayItem point : mCloseRecyclePoints) {
                    mListItems.add(point.getTitle() + "\n\n" + point.getSnippet());
                }

                var adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, mListItems);
                resultList.setAdapter(adapter);

                resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String value=adapter.getItem(position);
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