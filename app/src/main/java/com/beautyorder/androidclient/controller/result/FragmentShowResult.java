//
//  FragmentShowResult.java
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

package com.beautyorder.androidclient.controller.result;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.*;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter;
import com.beautyorder.androidclient.controller.tabview.CollectionPagerAdapter.ResultPageType;
import com.beautyorder.androidclient.controller.tabview.TabViewActivity;
import com.beautyorder.androidclient.controller.result.map.OverlayItemWithImage;
import com.beautyorder.androidclient.model.AppUser;
import com.beautyorder.androidclient.model.RecyclePointInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class FragmentShowResult extends Fragment {
    protected FirebaseFirestore mDatabase;
    protected SharedPreferences mSharedPref;
    protected GeoPoint mUserLocation;
    protected GeoPoint mSearchStart;
    protected MyLocationNewOverlay mLocationOverlay;
    protected ArrayList<OverlayItem> mFoundRecyclePoints;
    protected Context mCtx;
    protected final double mSearchRadiusInCoordinate = 0.045;
    protected abstract void searchAndDisplayItems();
    private Geocoder mGeocoder;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();

        mCtx = view.getContext();

        mSharedPref = mCtx.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE);

        mGeocoder = new Geocoder(requireContext(), Locale.getDefault());

        //load/initialize the osmdroid configuration, this can be done
        Configuration.getInstance().load(mCtx, PreferenceManager.getDefaultSharedPreferences(mCtx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        setupSearchBox();

        // Get the current user geolocation
        final boolean[] firstLocationReceived = {false};
        var locationProvider = new GpsMyLocationProvider(mCtx);
        locationProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {

                // TODO: improve the way we detect the first gps position fix
                if(!firstLocationReceived[0]) {
                    firstLocationReceived[0] = true;

                    Log.d("BeautyAndroid", "First received location for the user: " + location.toString());
                    mUserLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.v("BeautyAndroid", "First received location at timestamp: "
                        + Helpers.getTimestamp());

                    writeCachedUserLocation();

                    // Start a search if none happened so far
                    if (mSearchStart == null) {
                        setSearchStart(mUserLocation);
                        searchAndDisplayItems();
                    }
                }
            }
        });

        updateSearchResults();

        updateUserScore();
    }

    protected void updateSearchResults() {

        // If there is no search start yet, find it and get the items to display
        if (mSearchStart == null && getContext() != null) {
            String searchQuery = ((TabViewActivity)getContext()).getSearchQuery();

            boolean userLocationReadFromCache = readCachedUserLocation();

            if (!searchQuery.equals("") && !searchQuery.equals("usr")) {
                // If a query has been received by the searchable activity, use it
                // to find the search start
                Log.v("BeautyAndroid", "Searching for the query: " + searchQuery);
                setSearchStart(getCoordinatesFromAddress(searchQuery));
            } else if (userLocationReadFromCache) {
                // Otherwise, if the user location has been cached, search around it
                Log.v("BeautyAndroid", "Searching around the user location from the cache");
                setSearchStart(mUserLocation);
            } else {
                var activity = (TabViewActivity)getActivity();
                if(activity != null) {
                    activity.showDialog("Please wait until the app has found your position",
                        "No search until user position is found");
                }
            }
        }

        if  (mSearchStart != null) {
            // Search and display the items in the child fragment
            searchAndDisplayItems();
        }
    }

    protected void setupSearchBox() {
        // Get the SearchView and set the searchable configuration
        var searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        var searchView = (SearchView) getView().findViewById(R.id.search_box);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                saveShownFragmentBeforeSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                saveShownFragmentBeforeSearch();
                return false;
            }
        });

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }

    protected GeoPoint getCoordinatesFromAddress(@NotNull String locationName) {

        if (mGeocoder == null || locationName.equals("")) {
            Log.w("BeautyAndroid", "Cannot get coordinates, as no geocoder or empty address");
            return null;
        }

        try {
            List<Address> geoResults = mGeocoder.getFromLocationName(locationName, 1);

            if (!geoResults.isEmpty()) {
                final Address addr = geoResults.get(0);
                var location = new GeoPoint(addr.getLatitude(), addr.getLongitude());
                return location;
            } else {
                Log.w("BeautyAndroid", "No coordinate found for the address: " + locationName);
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
        String cacheLocation = mSharedPref.getString("user_location", "");

        if (cacheLocation != "") {
            Log.d("BeautyAndroid", "User location read from cache: " + cacheLocation);
            Log.v("BeautyAndroid", "User location read from cache at timestamp: "
                + Helpers.getTimestamp());

            mUserLocation = GeoPoint.fromDoubleString(cacheLocation, ',');

            return true;
        }

        return false;
    }

    protected void writeCachedUserLocation() {
        // Store the user location for the next startup
        String cacheLocation = mUserLocation.toDoubleString();

        // Do not use an R.string resource here to store "user_location". As sometimes
        // the context won't be available yet, when creating again the child view of
        // the FragmentTabView object (e.g.: after tapping the search view switch button).
        // In such a case, `getString` will throw an exception.
        mSharedPref.edit().putString("user_location", cacheLocation)
            .commit();

        Log.d("BeautyAndroid", "User location cached: " + cacheLocation);
        Log.v("BeautyAndroid", "User location cached at timestamp: "
            + Helpers.getTimestamp());
    }

    protected void setSearchStart(GeoPoint value) {
        Log.v("BeautyAndroid", "Search start set to: " + value);
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

                mFoundRecyclePoints = new ArrayList<>();

                for (int i = 0; i < pointInfo.getData().size(); i++) {

                    // Uncomment to write back to DB the coordinates from the RP address
                    //writeBackRPAddressCoordinatesToDB(pointInfo.getData().get(i).get("documentId"),
                    //    pointInfo.getAddressAtIndex(i));

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

    protected void changeSearchSwitch(ResultPageType destination) {

        var containerView = getView();
        if (containerView == null) {
            Log.w("BeautyAndroid", "No container view found when changing the search switch");
            return;
        }

        Button viewSwitch = containerView.findViewById(R.id.search_view_switch);
        if (viewSwitch == null) {
            Log.w("BeautyAndroid", "No view found when changing the search switch");
            return;
        }

        Log.v("BeautyAndroid", "Changing the search switch to the page: " + destination.toString());

        final int icon = (destination == ResultPageType.LIST) ? R.drawable.bullet_list : R.drawable.map;

        viewSwitch.setOnClickListener(view -> {

            // Select the page if the destination is a view pager
            if (destination == ResultPageType.LIST) {
                CollectionPagerAdapter.setPage(0);
            }

            // Toggle the tab swiping according to the destination view
            var activity = (TabViewActivity)getActivity();

            if ((activity) != null) {

                switch (destination) {
                    case MAP:
                        activity.toggleTabSwiping(false);
                        break;
                    case LIST:
                    default:
                        activity.toggleTabSwiping(true);
                        break;
                }
            }

            Log.d("BeautyAndroid", "Switch button pressed, navigate to: " + destination);

            final TabViewActivity.FragmentType destinationType = (destination == ResultPageType.LIST) ?
                TabViewActivity.FragmentType.APP :
                TabViewActivity.FragmentType.MAP;

            activity.navigate(destinationType);
        });

        viewSwitch.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    protected void displayScoreBox(String fragmentName, int layout_id) {
        // Show or hide the score box according to the locale
        if (!mustShowBrand()) {
            var fragmentRootView = getView();
            if (fragmentRootView == null) {
                Log.w("BeautyAndroid", "Cannot display or hide the score box in the " + fragmentName
                    + " fragment, as no fragment root view");
                return;
            }

            View scoreLayout = fragmentRootView.findViewById(layout_id);
            if (scoreLayout == null) {
                Log.w("BeautyAndroid", "Cannot display or hide the score box in the " + fragmentName
                    + " fragment, as no score layout");
                return;
            }

            Log.v("BeautyAndroid", "The score box is hidden in the " + fragmentName
                + " fragment");
            scoreLayout.setVisibility(View.GONE);
        } else {
            Log.v("BeautyAndroid", "The score box is shown in the " + fragmentName
                + " fragment");
        }
    }

    protected boolean mustShowBrand() {
        if (mCtx == null) {
            Log.w("BeautyAndroid", "Cannot check if brand must be shown, as no context");
            return false;
        }

        return !mCtx.getResources().getConfiguration().getLocales().get(0).getDisplayName().contains("Belgique");
    }

    private void saveShownFragmentBeforeSearch() {
        var activity = (TabViewActivity)getActivity();
        if (activity == null) {
            Log.w("BeautyAndroid", "No activity so cannot save the shown fragment before sending "
                + "the intent");
            return;
        }

        Log.v("BeautyAndroid", "Search intent sent, save the shown fragment");
        activity.saveSearchFragment();
    }

    private void updateUserScore() {

        if (mDatabase == null) {
            return;
        }

        // Display the user score
        mDatabase.collection("userInfos")
            .whereEqualTo("__name__", AppUser.getInstance().getId())
            .get()
            .addOnCompleteListener(task -> {
                // Display score
                int userScore = 0;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        var scoreData = document.getData().get("score").toString();
                        userScore = (!scoreData.equals("")) ? Integer.parseInt(scoreData) : 0;
                    }
                } else {
                    Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                }

                Log.d("BeautyAndroid", "userScore = " + userScore);

                var mainActivity = (TabViewActivity) getActivity();
                if (mainActivity == null) {
                    Log.w("BeautyAndroid", "Cannot update the score, as no main activity found");
                    return;
                }
                mainActivity.showScore(userScore);
            });
    }

    private void writeBackRPAddressCoordinatesToDB(String documentId, String address) {
        GeoPoint coordinates = getCoordinatesFromAddress(address);
        if (coordinates == null) {
            Log.d("BeautyAndroid", "No coordinates found for the RP address: " + address);
            return;
        }

        String propertyKey = "Coordinates";

        WriteBatch batch = mDatabase.batch();
        DocumentReference ref = mDatabase.collection("recyclePointInfos")
            .document(documentId);
        batch.update(ref, propertyKey, coordinates);

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("BeautyAndroid", "Correctly updated coordinates for the document with the key: "
                    + documentId);
            } else {
                Log.e("BeautyAndroid", "Error updating in DB the RP coordinates: ", task.getException());
            }
        });
    }
}
