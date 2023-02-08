//
//  FragmentWithSearch.java
//
//  Created by Mathieu Delehaye on 22/01/2023.
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

package com.beautyorder.androidclient.controller.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.main.CollectionPagerAdapter.FirstPageView;
import com.beautyorder.androidclient.model.RecyclePointInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentWithSearch extends Fragment {
    protected final double mSearchRadiusInCoordinate = 0.045;
    protected GeoPoint mUserLocation;
    protected GeoPoint mSearchResult;
    protected GeoPoint mSearchStart;
    protected MyLocationNewOverlay mLocationOverlay;
    protected ArrayList<OverlayItem> mFoundRecyclePoints;
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;
    protected Context mCtx;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the DB
        mDatabase = FirebaseFirestore.getInstance();

        mCtx = view.getContext();

        // Get the app preferences
        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        //load/initialize the osmdroid configuration, this can be done
        Configuration.getInstance().load(mCtx, PreferenceManager.getDefaultSharedPreferences(mCtx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string
    }

    protected void updateUserLocation() {
        if (mLocationOverlay == null) {
            Log.w("BeautyAndroid", "Cannot update user location because no overlay");
            return;
        }

        mUserLocation = mLocationOverlay.getMyLocation();
        if (mUserLocation == null) {
            Log.w("BeautyAndroid", "Cannot update user location as not available");
            return;
        }

        Log.d("BeautyAndroid", "User location updated: latitude "
            + String.valueOf(mUserLocation.getLatitude()) + ", longitude "
            + String.valueOf(mUserLocation.getLongitude()));
    }

    protected void setupSearchBox(TaskCompletionManager... cbManager) {

        // Get the SearchView and set the searchable configuration
        var searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        var searchView = (SearchView) getView().findViewById(R.id.search_box);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // Remove the magnifier icon from the search view
        int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        var magImage = (ImageView) searchView.findViewById(magId);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    protected GeoPoint getCoordinatesFromAddress(String locationName) {

        try {
            var geocoder = new Geocoder(requireContext(), Locale.getDefault());

            List<Address> geoResults = geocoder.getFromLocationName(locationName, 1);

            if (!geoResults.isEmpty()) {
                final Address addr = geoResults.get(0);
                var location = new GeoPoint(addr.getLatitude(), addr.getLongitude());

                return location;
            } else {
                Toast toast = Toast.makeText(requireContext(),"Location Not Found",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        } catch (IOException e) {
            Log.e("BeautyAndroid", "Error getting a location: " + e.toString());
        }

        return null;
    }

    protected boolean readCachedUserLocation() {
        String cacheLocation = mSharedPref.getString(getString(R.string.user_location), "");

        if (cacheLocation != "") {
            Log.d("BeautyAndroid", "User location read from cache: " + cacheLocation);
            Log.v("BeautyAndroid", "User location read from cache at timestamp: "
                + String.valueOf(Helpers.getTimestamp()));

            mUserLocation = GeoPoint.fromDoubleString(cacheLocation, ',');

            return true;
        }

        return false;
    }

    protected void writeCachedUserLocation() {
        // Store the user location for the next startup
        String cacheLocation = mUserLocation.toDoubleString();
        mSharedPref.edit().putString(getString(R.string.user_location), cacheLocation)
            .commit();
        Log.d("BeautyAndroid", "User location cached: " + cacheLocation);
        Log.v("BeautyAndroid", "User location cached at timestamp: "
            + Helpers.getTimestamp());
    }

    protected void setSearchStart(GeoPoint value) {
        Log.v("BeautyAndroid", "Search start set to: " + value.toString());
        mSearchStart = value;
    }

    protected void searchRecyclingPoints(TaskCompletionManager... cbManager) {
        if (mSearchStart == null) {
            Log.w("BeautyAndroid", "Cannot display the recycling points because no search start");
            return;
        }

        final double startLatitude = mSearchStart.getLatitude();
        final double startLongitude = mSearchStart.getLongitude();
        final String startLatitudeText = startLatitude+"";
        final String startLongitudeText = startLongitude+"";
        Log.d("BeautyAndroid", "Display the recycling points around the location (" + startLatitudeText
            + ", " + startLongitudeText + ")");

        // Search for the recycling points (RP)
        final double truncatedLatitude = Math.floor(startLatitude * 100) / 100;
        final double truncatedLongitude = Math.floor(startLongitude * 100) / 100;
        final double maxSearchLatitude = truncatedLatitude + mSearchRadiusInCoordinate;
        final double minSearchLatitude = truncatedLatitude - mSearchRadiusInCoordinate;
        final double maxSearchLongitude = truncatedLongitude + mSearchRadiusInCoordinate;
        final double minSearchLongitude = truncatedLongitude - mSearchRadiusInCoordinate;

        String[] outputFields = { "Latitude", "Longitude", "PointName", "BuildingName", "BuildingNumber",
            "Address", "Postcode", "City", "3Words", "RecyclingProgram", "ImageUrl" };
        String[] filterFields = { "Latitude", "Longitude" };
        double[] filterMinRanges = { minSearchLatitude, minSearchLongitude };
        double[] filterMaxRanges = { maxSearchLatitude, maxSearchLongitude };

        var pointInfo = new RecyclePointInfo(mDatabase);
        pointInfo.SetFilter(filterFields, filterMinRanges, filterMaxRanges);
        pointInfo.readAllDBFields(outputFields, new TaskCompletionManager() {
            @Override
            public void onSuccess() {

                mFoundRecyclePoints = new ArrayList<OverlayItem>();

                for (int i = 0; i < pointInfo.getData().size(); i++) {

                    final double latitude = pointInfo.getLatitudeAtIndex(i);
                    final double longitude = pointInfo.getLongitudeAtIndex(i);

                    String itemTitle = pointInfo.getTitleAtIndex(i);
                    String itemSnippet = pointInfo.getSnippetAtIndex(i);
                    String itemImageUrl = pointInfo.getImageUrlAtIndex(i);

                    mFoundRecyclePoints.add(new OverlayItemWithImage(itemTitle, itemSnippet,
                        new GeoPoint(latitude,longitude), itemImageUrl));
                }

                if (cbManager.length >= 1) {
                    cbManager[0].onSuccess();
                }
            }

            @Override
            public void onFailure() {
                if (cbManager.length >= 1) {
                    cbManager[0].onFailure();
                }
            }
        });
    }

    protected void changeSearchSwitch(FirstPageView destinationView, int destinationPage, int icon) {

        View containerView = getView();
        if (containerView == null) {
            Log.w("BeautyAndroid", "No container view found when changing the search switch");
            return;
        }

        Button viewSwitch = containerView.findViewById(R.id.search_view_switch);
        if (viewSwitch == null) {
            Log.w("BeautyAndroid", "No view found when changing the search switch");
            return;
        }

        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Select the page if the destination is a view pager
                if (destinationPage >= 0) {
                    var activity = (MainActivity)getActivity();
                    CollectionPagerAdapter.setAppPage(destinationPage);
                }

                Log.d("BeautyAndroid", "View pager first page set to: " + destinationView.toString());
                CollectionPagerAdapter.setFirstPageView(destinationView);

                ViewPager pager = getActivity().findViewById(R.id.appPager);
                pager.getAdapter().notifyDataSetChanged();
            }
        });

        viewSwitch.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }
}
