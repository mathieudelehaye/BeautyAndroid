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
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.OverlayItemWithImage;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.ResultListAdapter;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.databinding.FragmentResultListBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

public class FragmentResultList extends FragmentWithSearch {
    private FragmentResultListBinding mBinding;
    private final GeoPoint mUserLocation = new GeoPoint(0, 0);
    private ArrayList<String> mFoundRPImageUrls;
    private final Object mImageUpdateLock = new Object();
    private ArrayList<ResultItemInfo> mResultItems;
    private ListView mListView;

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

        changeSearchSwitch(R.id.action_ResultFragment_to_AppFragment, 0, R.drawable.map);
    }

    private void searchItemsToDisplay() {

        // Search for the RP around the user
        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                var resultList = (ListView) getView().findViewById(R.id.result_list_view);

                mResultItems = new ArrayList<>();
                mFoundRPImageUrls = new ArrayList<>();

                for (int i = 0; i < mFoundRecyclePoints.size(); i++) {
                    final var point = (OverlayItemWithImage) mFoundRecyclePoints.get(i);

                    String title = point.getTitle() + "\n\n" + point.getSnippet();
                    mResultItems.add(new ResultItemInfo(title, null));

                    mFoundRPImageUrls.add(point.getImage());
                }

                var adapter = new ResultListAdapter(getContext(), mResultItems);
                resultList.setAdapter(adapter);

                resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String value = ((ResultItemInfo)adapter.getItem(position)).getTitle();
                        Log.d("BeautyAndroid", "Tapped item: " + value);
                    }
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
            Log.d("BeautyAndroid", "Result list view becomes visible");

            changeSearchSwitch(R.id.action_ResultFragment_to_AppFragment, 0, R.drawable.map);
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

        final int itemPosition = mResultItems.indexOf(itemInfo);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference gsReference =
            storage.getReferenceFromUrl(imageUrl);

        final long ONE_MEGABYTE = 1024 * 1024;

        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new
            OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    synchronized (mImageUpdateLock) {
                        itemInfo.setImage(bytes);

                        viewAdapter.notifyDataSetChanged();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
        });
    }
}