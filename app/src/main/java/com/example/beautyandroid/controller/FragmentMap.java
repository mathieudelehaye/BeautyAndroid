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

package com.example.beautyandroid.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.beautyorder.androidclient.R;
import com.beautyorder.androidclient.databinding.FragmentMapBinding;
import com.example.beautyandroid.model.AppUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class FragmentMap extends Fragment {

    private FragmentMapBinding binding;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mMap = null;
    private IMapController mMapController;
    private MyLocationNewOverlay mLocationOverlay;
    private GeoPoint mUserLocation;
    private GeoPoint mSearchResult;
    private GeoPoint mSearchStart;
    private ItemizedOverlayWithFocus<OverlayItem> mRPOverlay;
    private RoadManager mRoadManager;
    private Polyline[] mRoadOverlay = {null};   // Overlay to display the road to a recycling point
    private FirebaseFirestore mDatabase;
    private SharedPreferences mSharedPref;
    private Context mCtx;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        // Disable StrictMode policy in onCreate, in order to make a network call in the main thread
        // TODO: call the network from a child thread instead
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        return binding.getRoot();
    }

    @SuppressLint("ResourceAsColor")
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

        // inflate and create the map
        mMap = (MapView) view.findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        // handle permissions
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        requestPermissionsIfNecessary(
            view.getContext(),
            // if you need to show the current location, uncomment the line below
            // WRITE_EXTERNAL_STORAGE is required in order to show the map
            permissions
        );

        EditText editText = view.findViewById(R.id.mapSearchBox);

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
                            focusOnTarget(mSearchResult);

                            return true;
                        }
                    }
                }

                return false;
            }
        });

        setupMap();

        updateUserScore();

        binding.mapUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserLocation();

                Log.d("BeautyAndroid", "Change focus to user location");
                focusOnTarget(mUserLocation);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private void requestPermissionsIfNecessary(Context context, String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                (Activity)context,
                permissionsToRequest.toArray(new String[0]),
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    
    private void setupMap() {

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);

        mMapController = mMap.getController();
        mMapController.setZoom(14.0);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mCtx), mMap);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();

        // Before finding the user location, set the map to the cached value
        String cacheLocation = mSharedPref.getString(getString(R.string.user_location), "");
        if (cacheLocation != "") {
            Log.d("BeautyAndroid", "User location read from cache: " + cacheLocation);
            mUserLocation = GeoPoint.fromDoubleString(cacheLocation, ',');

            Log.d("BeautyAndroid", "Change focus to user location");
            focusOnTarget(mUserLocation);
        }


        mMap.getOverlays().add(this.mLocationOverlay);
        mRoadManager = new OSRMRoadManager(mCtx, getString(R.string.app_name)); // Initialize it later, whe/n needed

        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {

                final View view = getView(); // mdl

                try {

                    updateUserLocation();

                    // Store in cache the location for the next startup
                    String cacheLocation = mUserLocation.toDoubleString();
                    mSharedPref.edit().putString(getString(R.string.user_location), cacheLocation)
                        .commit();
                    Log.v("BeautyAndroid", "User location cached: " + cacheLocation);

                    Log.d("BeautyAndroid", "Change focus to user location");
                    focusOnTarget(mUserLocation);

                } catch (Exception e) {
                    Log.e("BeautyAndroid", "Error udating the map: " + e.toString());
                }
            }
        });
    }

    private GeoPoint getCoordinatesFromAddress(String locationName) {

        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

            List<Address> geoResults = geocoder.getFromLocationName(locationName, 1);

            if (!geoResults.isEmpty()) {
                final Address addr = geoResults.get(0);
                GeoPoint location = new GeoPoint(addr.getLatitude(), addr.getLongitude());

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

    private void updateUserLocation() {
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

    private void focusOnTarget(GeoPoint _target) {
        if (_target == null) {
            Log.w("BeautyAndroid", "Cannot focus on target because none available");
            return;
        }

        Log.v("BeautyAndroid", "Search start set to target");
        mSearchStart = _target;

        // UI action (like the map animation) needs to be processed in a UI Thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v("BeautyAndroid", "Map focus set on target");
                mMapController.animateTo(_target);

                updateRecyclingPoints();
            }
        });
    }

    private void updateUserScore() {
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
                            Log.d("BeautyAndroid", document.getId() + " => " + document.getData());

                            userScore = Integer.parseInt(document.getData().get("score").toString());
                        }
                    } else {
                        Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                    }

                    Log.d("BeautyAndroid", "userScore = " + String.valueOf(userScore));

                    TextView score = (TextView) getView().findViewById(R.id.mapScore);
                    score.setText(String.valueOf(userScore) + " pts");
                }
            });
    }

    private void updateRecyclingPoints() {
        if (mSearchStart == null) {
            Log.w("BeautyAndroid", "Cannot display the recycling points because no search start");
            return;
        }

        final double startLatitude = mSearchStart.getLatitude();
        final double startLongitude = mSearchStart.getLongitude();
        final String startLatitudeText = startLatitude+"";
        final String startLongitudeText = startLongitude+"";
        Log.d("BeautyAndroid", "Display the recycling points around: latitude " + startLatitudeText + ", longitude "
            + startLongitudeText);

        // Search for the recycling points (RP)
        final double truncatedLatitude = Math.floor(startLatitude * 100) / 100;
        final double truncatedLongitude = Math.floor(startLongitude * 100) / 100;
        final double maxSearchLatitude = truncatedLatitude + 0.05;
        final double minSearchLatitude = truncatedLatitude - 0.05;
        final double maxSearchLongitude = truncatedLongitude + 0.05;
        final double minSearchLongitude = truncatedLongitude - 0.05;

        mDatabase.collection("recyclePointInfos")
            .whereLessThan("Latitude", maxSearchLatitude)
            .whereGreaterThan("Latitude", minSearchLatitude)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            final double longitude = (double)document.getData().get("Longitude");

                            //Log.v("BeautyAndroid", "longitude = " + String.valueOf(longitude));

                            // Due to Firestore query limitation, we need to filter the longitude on the
                            // device.
                            // TODO: add e.g.: the country or the continent to the query, to limit the
                            //  response result number.
                            if (longitude > minSearchLongitude && longitude < maxSearchLongitude) {
                                //Log.v("BeautyAndroid", document.getId() + " => " + document.getData());

                                final double latitude = (double)document.getData().get("Latitude");
                                final String pointName = (String)document.getData().get("PointName");
                                final String buildingName = (String)document.getData().get("BuildingName");
                                final String buildingNumber = (String)document.getData().get("BuildingNumber");
                                final String address = (String)document.getData().get("Address");
                                final String city = (String)document.getData().get("City");
                                final String postcode = (String)document.getData().get("Postcode");
                                final String recyclingProgram = (String)document.getData().get("RecyclingProgram");

                                final String itemTitle = (pointName.equals("?") ? "" : pointName);
                                final String itemSnippet =
                                    (buildingName.equals("?")  ? "" : buildingName) + " " +
                                        (buildingNumber.equals("?") ? "" : buildingNumber) + ", " +
                                        (address.equals("?") ? "" : address) + " " +
                                        (city.equals("?") ? "" : city) + " " +
                                        (postcode.equals("?") ? "" : postcode) +
                                        (recyclingProgram.equals("?") ? "" : ("\n\nBrands: " + recyclingProgram));

                                //Log.v("BeautyAndroid", "itemTitle = " + itemTitle +
                                //    ", latitude = " + latitude + ", itemSnippet = " + itemSnippet);

                                items.add(new OverlayItem(itemTitle, itemSnippet,
                                    new GeoPoint(latitude,longitude)));
                            }
                        }

                        // possibly remove the former RP overlay
                        if (mRPOverlay != null) {
                            mMap.getOverlays().remove(mRPOverlay);
                        }

                        // display the overlay
                        mRPOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @SuppressLint("ResourceAsColor")
                                @Override
                                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                    Log.i("BeautyAndroid", "Single tap");

                                    // Remove the previous road overlay
                                    if (mRoadOverlay[0] != null) {
                                        mMap.getOverlays().remove(mRoadOverlay[0]);
                                    }

                                    final IGeoPoint itemILocation = item.getPoint();
                                    final GeoPoint itemLocation = new GeoPoint(itemILocation.getLatitude(),
                                        itemILocation.getLongitude());

                                    drawRoadToPoint(itemLocation);

                                    return true;
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
                    } else {
                        Log.e("BeautyAndroid", "Error getting documents: ", task.getException());
                    }
                }
            });
    }
    
    private void drawRoadToPoint(GeoPoint itemLocation) {

        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(mSearchStart);
        waypoints.add(itemLocation);

        Road road = mRoadManager.getRoad(waypoints);

        // Generate the direction text
        StringBuilder directionTextBuilder = new StringBuilder();
        int directionItemIdx = 0;

        for (RoadNode node: road.mNodes) {
            // There is no useful info in the node of index 0 ("You have
            // reached a waypoint of your trip")
            if (directionItemIdx > 0) {
                final String instructions = node.mInstructions;
                if (instructions == "null") {
                    continue;
                }

                directionTextBuilder.append(directionItemIdx + ". ");
                directionTextBuilder.append(instructions);
                directionTextBuilder.append("\n");
            }

            directionItemIdx++;
        }

        final String directionText = directionTextBuilder.toString();
        //Log.v("BeautyAndroid", "Direction text: " +
        //    directionText);

        TextView direction = (TextView) getView().findViewById(R.id.mapDirection);
        View directionBackground = (View) getView().findViewById(R.id.mapDirectionBackground);

        if (directionText != "") {
            direction.setText(directionText);

            // Activate the scrollbar
            direction.setMovementMethod(new ScrollingMovementMethod());

            // Display view
            direction.setBackgroundResource(R.color.BgOrange);
            directionBackground.setBackgroundResource(R.color.black);
        } else {
            direction.setText("");

            // Hide view
            direction.setBackgroundResource(R.color.Transparent);
            directionBackground.setBackgroundResource(R.color.Transparent);
        }

        // Display the road as a map overlay
        mRoadOverlay[0] = RoadManager.buildRoadOverlay(road);

        // Add the polyline to the overlays of your map
        mMap.getOverlays().add(mRoadOverlay[0]);

        // Refresh the map
        mMap.invalidate();

        mMapController.animateTo(itemLocation);
    }
}