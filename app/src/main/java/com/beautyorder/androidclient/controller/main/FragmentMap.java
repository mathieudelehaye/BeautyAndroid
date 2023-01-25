//
//  FragmentmMap.java
//
//  Created by Mathieu Delehaye on 1/12/2022.
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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.databinding.FragmentMapBinding;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.ScoreTransferer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.*;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class FragmentMap extends FragmentWithSearch {
    private FragmentMapBinding mBinding;
    private MapView mMap = null;
    private IMapController mMapController;
    private ItemizedOverlayWithFocus<OverlayItem> mRPOverlay;
    private RoadManager mRoadManager;
    private Polyline[] mRoadOverlay = {null};   // Overlay to display the road to a recycling point

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentMapBinding.inflate(inflater, container, false);

        // Disable StrictMode policy in onCreate, in order to make a network call in the main thread
        // TODO: call the network from a child thread instead
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        return mBinding.getRoot();
    }

    @SuppressLint("ResourceAsColor")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // inflate and create the map
        mMap = (MapView) view.findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        var editText = (EditText)view.findViewById(R.id.search_box);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    Log.d("BeautyAndroid", "Search button pressed");

                    editText.clearFocus();

                    String query = editText.getText().toString();

                    if (query != "") {
                        GeoPoint location = getCoordinatesFromAddress(query);

                        if (location != null) {
                            mMapController.animateTo(location);

                            mSearchResult = location;
                            Log.d("BeautyAndroid", "Search result set to: (" + mSearchResult.getLatitude()
                                + ", " + mSearchResult.getLongitude() + ")");

                            Log.d("BeautyAndroid", "Change focus to search result");
                            focusOnTargetAndUpdateMap(mSearchResult, /*isUser=*/false);

                            return true;
                        }
                    }
                }

                return false;
            }
        });

        setupMap();

        updateUserScore();

        mBinding.mapUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserLocation();

                Log.d("BeautyAndroid", "Change focus to user location");
                focusOnTargetAndUpdateMap(mUserLocation, /*isUser=*/true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "Map view becomes visible");

            //updateUserScore();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private int computeZoomForRadius(double valueInMeter) {
        return (16 - (int)(Math.log(valueInMeter / 500) / Math.log(2)));
    }

    private void setZoom(double radiusInKilometer) {
        final int zoomLevel = computeZoomForRadius(radiusInKilometer * 1000);
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(zoomLevel)
            + " for radius of " + String.valueOf(radiusInKilometer) + " km");
        mMapController.setZoom(zoomLevel);
    }

    private void setZoom(int level) {
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(level));
        mMapController.setZoom(level);
    }
    
    private void setupMap() {

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);

        mMapController = mMap.getController();
        setZoom(14);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mCtx), mMap);
        mLocationOverlay.enableMyLocation();

        // Check the last known location in cache
        String cacheLocation = mSharedPref.getString(getString(R.string.user_location), "");

        // If found, focus on it
        if ((mSearchStart == null) && (cacheLocation != "")) {
            Log.d("BeautyAndroid", "User location read from cache: " + cacheLocation);
            mUserLocation = GeoPoint.fromDoubleString(cacheLocation, ',');

            Log.d("BeautyAndroid", "Change focus to user location");
            focusOnTargetAndUpdateMap(mUserLocation, /*isUser=*/true);
        }

        mMap.getOverlays().add(this.mLocationOverlay);
        mRoadManager = new OSRMRoadManager(mCtx, getString(R.string.app_name)); // Initialize it later, whe/n needed

        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {

                final View view = getView();

                try {

                    updateUserLocation();

                    // Store in cache the location for the next startup
                    String cacheLocation = mUserLocation.toDoubleString();
                    mSharedPref.edit().putString(getString(R.string.user_location), cacheLocation)
                        .commit();
                    Log.v("BeautyAndroid", "User location cached: " + cacheLocation);

                    // Focus on the user only if a search has not been done yet
                    if (mSearchStart == null) {
                        Log.d("BeautyAndroid", "Change focus to user location");
                        focusOnTargetAndUpdateMap(mUserLocation, /*isUser=*/true);
                    }

                } catch (Exception e) {
                    Log.e("BeautyAndroid", "Error updating the map: " + e.toString());
                }
            }
        });
    }

    private void focusOnTargetAndUpdateMap(GeoPoint target, Boolean isUser) {
        if (target == null) {
            Log.w("BeautyAndroid", "Cannot focus on target because none available");
            return;
        }

        super.setSearchStart(target);

        // UI action (like the map animation) needs to be processed in a UI Thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isUser) {
                    // If target is the user, follow its location
                    Log.v("BeautyAndroid", "Map starts to follow the user location");
                    mLocationOverlay.enableFollowLocation();
                } else {
                    // Otherwise, stop following the user location
                    Log.v("BeautyAndroid", "Map stops to follow the user location");
                    mLocationOverlay.disableFollowLocation();
                }

                Log.v("BeautyAndroid", "Map focus set on target");
                mMapController.animateTo(target);

                searchAndDisplayOnMapRP();
            }
        });
    }

    private void updateUserScore() {

        if (mDatabase == null) {
            return;
        }

        // Display the user score
        mDatabase.collection("userInfos")
            .whereEqualTo("__name__", AppUser.getInstance().getId())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    // Display score
                    Integer userScore = 0;

                    if (task.isSuccessful()) {
                        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userScore = Integer.parseInt(document.getData().get("score").toString());
                        }
                    } else {
                        Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                    }

                    Log.d("BeautyAndroid", "userScore = " + String.valueOf(userScore));

                    new ScoreTransferer(mDatabase, (MainActivity)getActivity()).displayScoreOnScreen(userScore);
                }
            });
    }

    private void searchAndDisplayOnMapRP() {

        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                // possibly remove the former RP overlay
                if (mRPOverlay != null) {
                    mMap.getOverlays().remove(mRPOverlay);
                }

                // display the overlay
                mRPOverlay = new ItemizedOverlayWithFocus<OverlayItem>(mFoundRecyclePoints,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            Log.i("BeautyAndroid", "Single tap");
                            return false;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, mCtx);
                mRPOverlay.setFocusItemsOnTap(true);

                mMap.getOverlays().add(mRPOverlay);

                // Refresh the map
                mMap.invalidate();

                setZoom(mSearchRadiusInCoordinate * 111);    // 111 km by latitude degree
            }

            @Override
            public void onFailure() {
            }
        });
    }
}