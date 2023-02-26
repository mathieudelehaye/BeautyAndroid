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

package com.beautyorder.androidclient.controller.main.map;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter.ResultPageType;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.main.MainActivity;
import com.beautyorder.androidclient.controller.main.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.main.search.FragmentWithSearch;
import com.beautyorder.androidclient.databinding.FragmentMapBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;

public class FragmentMap extends FragmentWithSearch {
    private FragmentMapBinding mBinding;
    private MapView mMap = null;
    private IMapController mMapController;
    private boolean mZoomInitialized = false;
    private ItemizedOverlayWithFocus<OverlayItem> mRPOverlay;
    private ArrayList<ResultItemInfo> mResultItems;
    private boolean mIsViewVisible = false;
    private boolean mKeyboardDisplayed = false;
    private final int mMapInitialHeight = 1413;     // = 807 dp
    private final int mMapHeightDiff = 540; // = 309 dp
    private final int mMapReducedHeight = mMapInitialHeight - mMapHeightDiff;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        mBinding = FragmentMapBinding.inflate(inflater, container, false);

        // Disable StrictMode policy in onCreate, in order to make a network call in the main thread
        // TODO: call the network from a child thread instead
        var policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        return mBinding.getRoot();
    }

    @SuppressLint("ResourceAsColor")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.v("BeautyAndroid", "Map view created at timestamp: "
            + Helpers.getTimestamp());

        super.onViewCreated(view, savedInstanceState);

        setupMap(view);
        changeSearchSwitch(ResultPageType.LIST);

        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            var viewBorder = new Rect();

            // border will be populated with the overall visible display size in which the window this view is
            // attached to has been positioned in.
            view.getWindowVisibleDisplayFrame(viewBorder);

            final int viewBorderHeight = viewBorder.height();

            // height of the fragment root view
            View mapRootView = view.getRootView();
            View mapLayout = view.findViewById(R.id.mapLayout);

            if (mapRootView == null || mapLayout == null) {
                return;
            }

            final int viewPagerRootViewHeight = mapRootView.getHeight();

            final int heightDiff = viewPagerRootViewHeight - viewBorderHeight;

            if (heightDiff > 0.25*viewPagerRootViewHeight) {
                // if more than 25% of the screen, it's probably a keyboard
                if (!mKeyboardDisplayed) {
                    mKeyboardDisplayed = true;
                    Log.v("BeautyAndroid", "Keyboard displayed");

                    ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
                    params.height = mMapReducedHeight;

                    mapLayout.requestLayout();
                }
            } else {
                if (mKeyboardDisplayed) {
                    mKeyboardDisplayed = false;
                    Log.v("BeautyAndroid", "Keyboard hidden");

                    ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
                    params.height = mMapInitialHeight;
                    mapLayout.requestLayout();
                }
            }
        });

        mBinding.mapUserLocation.setOnClickListener(view1 -> {

            if(mUserLocation != null) {
                Log.d("BeautyAndroid", "Change map focus to user location");

                mMapController.animateTo(mUserLocation);
                // TODO: possibly change the zoom level
            }
        });

        showHelp();
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
            mIsViewVisible = true;

            Log.d("BeautyAndroid", "Map view becomes visible");
            Log.v("BeautyAndroid", "Map view becomes visible at timestamp: "
                + Helpers.getTimestamp());

            CollectionPagerAdapter.setPage(0);

            changeSearchSwitch(ResultPageType.LIST);

            var activity = (MainActivity)getActivity();
            if ((activity) != null) {
                activity.disableTabSwiping();
            }

            showHelp();
        } else {
            mIsViewVisible = false;
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

    @Override
    protected void searchAndDisplayItems() {

        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                // possibly remove the former RP overlay
                if (mRPOverlay != null) {
                    mMap.getOverlays().remove(mRPOverlay);
                }

                // display the overlay
                mRPOverlay = new ItemizedOverlayWithFocus<>(mFoundRecyclePoints,
                    new ItemizedIconOverlay.OnItemGestureListener<>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            Log.i("BeautyAndroid", "Single tap");
                            mMapController.animateTo(item.getPoint());

                            // Todo: display the detail view

                            return false;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, mCtx);

                mMap.getOverlays().add(mRPOverlay);

                // Refresh the map
                mMap.invalidate();

                if (!mZoomInitialized) {
                    setZoom(mSearchRadiusInCoordinate * 111);    // 111 km by latitude degree
                }

                mMapController.animateTo(mSearchStart);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private int computeZoomForRadius(double valueInMeter) {
        return (16 - (int)(Math.log(valueInMeter / 500) / Math.log(2)));
    }

    private void setZoom(double radiusInKilometer) {
        final int zoomLevel = computeZoomForRadius(radiusInKilometer * 1000);
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(zoomLevel)
            + " for radius of " + String.valueOf(radiusInKilometer) + " km");
        mZoomInitialized = true;
        mMapController.setZoom(zoomLevel);
    }

    private void setZoom(int level) {
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(level));
        mZoomInitialized = true;
        mMapController.setZoom(level);
    }

    private void setupMap(View view) {

        // inflate and create the map
        mMap = view.findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);

        mMapController = mMap.getController();
        setZoom(14);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mCtx), mMap);
        mLocationOverlay.enableMyLocation();

        mMap.getOverlays().add(this.mLocationOverlay);
    }

    private void showHelp() {

        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("map_help_displayed", "false"))) {
                mSharedPref.edit().putString("map_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog("Click on a point on the map to find the " +
                    " address of a drop-off location!");
                dialogFragment.show(getChildFragmentManager(), "Map help dialog");
            }
        }
    }
}