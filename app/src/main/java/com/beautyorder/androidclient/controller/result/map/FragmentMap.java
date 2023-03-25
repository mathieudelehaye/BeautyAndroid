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

package com.beautyorder.androidclient.controller.result.map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter.ResultPageType;
import com.beautyorder.androidclient.Helpers;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.TaskCompletionManager;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.controller.tabview.dialog.FragmentHelpDialog;
import com.beautyorder.androidclient.controller.result.FragmentShowResult;
import com.beautyorder.androidclient.databinding.FragmentMapBinding;
import com.beautyorder.androidclient.model.ResultItemInfo;
import com.beautyorder.androidclient.model.SearchResult;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class FragmentMap extends FragmentShowResult {
    private FragmentMapBinding mBinding;
    private MapView mMap = null;
    private IMapController mMapController;
    private ItemizedOverlayWithFocus<OverlayItem> mRPOverlay;
    private boolean mIsViewVisible = false;
    private boolean mKeyboardDisplayed = false;
    private final int mMapInitialHeight = 1413; // = 807 dp
    private final int mMapHeightDiff = 540; // = 309 dp
    private final int mMapReducedHeight = mMapInitialHeight - mMapHeightDiff;
    private final int mKilometerByCoordinateDeg = 111;  // # km by latitude degree

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

                    ViewGroup.LayoutParams mapLayoutParams = mapLayout.getLayoutParams();
                    mapLayoutParams.height = mMapReducedHeight;
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
                setZoomLevel(14);
            }
        });

        toggleDetailsView(false);

        displayScoreBox("Map", R.id.score_layout_map);

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

            var activity = (TabViewActivity)getActivity();
            if ((activity) != null) {
                activity.toggleTabSwiping(false);
            }

            displayScoreBox("Map", R.id.score_layout_map);

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

    public void toggleDetailsView(boolean visible) {

        View rootView = getView();

        if (rootView == null) {
            Log.w("BeautyAndroid", "Cannot toggle the details view, as no root view available");
        }

        View detailLayout = rootView.findViewById(R.id.detail_map_layout);
        detailLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void searchAndDisplayItems() {

        searchRecyclingPoints(new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                // Prepare the search results and download the images
                // TODO: make the following code reusable and share it with `FragmentResultList`
                var result = new SearchResult();

                final boolean showBrand = mustShowBrand();
                for (int i = 0; i < mFoundRecyclePoints.size(); i++) {
                    final var point = (OverlayItemWithImage) mFoundRecyclePoints.get(i);
                    result.add(new ResultItemInfo(point.getTitle(), point.getSnippet(), null, showBrand),
                        point.getImage());
                }

                result.downloadImages(new TaskCompletionManager() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure() {
                    }
                });

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

                            var activity = (TabViewActivity) getActivity();
                            if (activity == null) {
                                Log.w("BeautyAndroid", "Cannot show the result item, as no activity "
                                    + "available");
                                return false;
                            }

                            var result = activity.getSearchResult();
                            if (result == null) {
                                Log.w("BeautyAndroid", "Cannot show the result item, as no result "
                                    + "available");
                                return false;
                            }

                            showDetails(result.get(index));

                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return true;
                        }
                    }, mCtx);

                mMap.getOverlays().add(mRPOverlay);

                // Refresh the map
                mMap.invalidate();

                setZoomInKilometer(mSearchRadiusInCoordinate * mKilometerByCoordinateDeg);

                mMapController.animateTo(mSearchStart);

                var activity = (TabViewActivity)getActivity();
                if (activity == null) {
                    Log.w("BeautyAndroid", "Cannot set the search result from the map as no activity");
                    return;
                }

                activity.setSearchResult(result);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private int computeZoomLevelForRadius(double valueInMeter) {
        return (16 - (int)(Math.log(valueInMeter / 500) / Math.log(2)));
    }

    private void setZoomInKilometer(double radiusInKilometer) {
        final int zoomLevel = computeZoomLevelForRadius(radiusInKilometer * 1000);
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(zoomLevel)
            + " for radius of " + String.valueOf(radiusInKilometer) + " km");
        mMapController.setZoom(zoomLevel);
    }

    private void setZoomLevel(int level) {
        Log.v("BeautyAndroid", "Map zoom set to level " + String.valueOf(level));
        mMapController.setZoom(level);
    }

    private void setupMap(View view) {

        // inflate and create the map
        mMap = view.findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);

        mMapController = mMap.getController();
        setZoomInKilometer(mSearchRadiusInCoordinate * mKilometerByCoordinateDeg);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mCtx), mMap);
        mLocationOverlay.enableMyLocation();

        mMap.getOverlays().add(this.mLocationOverlay);
    }

    private void showHelp() {

        if (mIsViewVisible && mSharedPref != null) {
            if (!Boolean.parseBoolean(mSharedPref.getString("map_help_displayed", "false"))) {
                mSharedPref.edit().putString("map_help_displayed", "true").commit();
                var dialogFragment = new FragmentHelpDialog(getString(R.string.map_help));
                dialogFragment.show(getChildFragmentManager(), "Map help dialog");
            }
        }
    }

    private void showDetails(ResultItemInfo itemInfo) {

        final byte[] itemImageBytes = itemInfo.getImage();
        final boolean showImage = itemInfo.isImageShown();

        String itemTitle = showImage ? itemInfo.getTitle() : "Lorem ipsum dolor sit";
        String itemDescription = showImage ? itemInfo.getDescription() : "Lorem ipsum dolor sit amet. Ut enim "
            + "corporis ea labore esse ea illum consequatur. Et reiciendis ducimus et repellat magni id ducimus "
            + "nesc.";

        ImageView resultImage = getView().findViewById(R.id.detail_map_image);
        if (itemImageBytes != null && showImage) {
            Bitmap image = BitmapFactory.decodeByteArray(itemImageBytes, 0, itemImageBytes.length);
            resultImage.setImageBitmap(image);
        } else {
            // Use a placeholder if the image has not been set
            resultImage.setImageResource(R.drawable.camera);
        }
        resultImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView resultDescription = getView().findViewById(R.id.detail_map_description);
        resultDescription.setText(itemTitle + "\n\n" + itemDescription);

        mBinding.detailMapLayout.setOnClickListener(view1 -> {
            var activity = (TabViewActivity)getActivity();

            activity.setSelectedRecyclePoint(itemInfo);
            activity.navigate(TabViewActivity.FragmentType.DETAIL);
        });

        toggleDetailsView(true);
    }
}