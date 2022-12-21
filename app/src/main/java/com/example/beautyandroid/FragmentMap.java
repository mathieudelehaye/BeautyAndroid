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

package com.beautyorder.androidclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.databinding.FragmentMapBinding;
import java.util.ArrayList;

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
    private MapView map = null;

    private MyLocationNewOverlay mLocationOverlay;

    private FirebaseFirestore mDatabase;

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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the DB
        mDatabase = FirebaseFirestore.getInstance();

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = view.getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        requestPermissionsIfNecessary(
            view.getContext(),
            // if you need to show the current location, uncomment the line below
            // WRITE_EXTERNAL_STORAGE is required in order to show the map
            permissions
        );

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14.0);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx),map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        map.getOverlays().add(this.mLocationOverlay);

        RoadManager roadManager = new OSRMRoadManager(ctx, "MyOwnUserAgent/1.0");

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {

                // Overlay to display the road to a recycling point
                final Polyline[] roadOverlay = {null};

                try {
                    final GeoPoint userLocation = mLocationOverlay.getMyLocation();
                    final double userLatitude = userLocation.getLatitude();
                    final double userLongitude = userLocation.getLongitude();
                    final String userLatitudeText = userLatitude+"";
                    final String userLongitudeText = userLongitude+"";
                    Log.d("BeautyAndroid", "user coordinates: latitude " + userLatitudeText + ", longitude "
                            + userLongitudeText);

                    // Search for the recycling points
                    final double truncatedLatitude = Math.floor(userLatitude * 100) / 100;
                    final double truncatedLongitude = Math.floor(userLongitude * 100) / 100;
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

                                        Log.d("BeautyAndroid", "longitude = " + String.valueOf(longitude));

                                        // Due to Firestore query limitation, we need to filter the longitude on the
                                        // device.
                                        // TODO: add e.g.: the country or the continent to the query, to limit the
                                        //  response result number.
                                        if (longitude > minSearchLongitude && longitude < maxSearchLongitude) {
                                            Log.d("BeautyAndroid", document.getId() + " => " + document.getData());

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

                                            Log.d("BeautyAndroid", "itemTitle = " + itemTitle +
                                                ", latitude = " + latitude + ", itemSnippet = " + itemSnippet);

                                            items.add(new OverlayItem(itemTitle, itemSnippet,
                                                new GeoPoint(latitude,longitude)));
                                        }
                                    }

                                    //the overlay
                                    ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                            @Override
                                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                                Log.i("BeautyAndroid", "Single tap");

                                                // Remove the previous road overlay
                                                if (roadOverlay[0] != null) {
                                                    map.getOverlays().remove(roadOverlay[0]);
                                                }

                                                final IGeoPoint itemILocation = item.getPoint();
                                                final GeoPoint itemLocation = new GeoPoint(itemILocation.getLatitude(),
                                                    itemILocation.getLongitude());

                                                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                                                waypoints.add(userLocation);
                                                waypoints.add(itemLocation);

                                                Road road = roadManager.getRoad(waypoints);

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
                                                Log.d("BeautyAndroid", "Direction text: " +
                                                    directionText);

                                                TextView descriptionTextArea = (TextView) view.findViewById(R.id.textArea_mapDirectionText);
                                                View descriptionTextAreaBackground = (View) view.findViewById(R.id.view_mapDirectionTextBackground);

                                                if (directionText != "") {
                                                    descriptionTextArea.setText(directionText);

                                                    // Activate the scrollbar
                                                    descriptionTextArea.setMovementMethod(new ScrollingMovementMethod());

                                                    // Change background color
                                                    descriptionTextAreaBackground.setBackgroundColor(getResources().getColor(R.color.black));
                                                } else {
                                                    descriptionTextArea.setText("");

                                                    // Remove background color
                                                    descriptionTextAreaBackground.setBackgroundColor(getResources().getColor(R.color.BgOrange));
                                                }

                                                // Display the road as a map overlay
                                                roadOverlay[0] = RoadManager.buildRoadOverlay(road);

                                                // Add the polyline to the overlays of your map
                                                map.getOverlays().add(roadOverlay[0]);

                                                // Refresh the map
                                                map.invalidate();

                                                mapController.animateTo(item.getPoint());

                                                return true;
                                            }
                                            @Override
                                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                                Log.i("BeautyAndroid", "Long press");
                                                return false;
                                            }
                                        }, ctx);
                                    mOverlay.setFocusItemsOnTap(true);

                                    map.getOverlays().add(mOverlay);

                                    // Refresh the map
                                    map.invalidate();
                                } else {
                                    Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                }
                catch (Exception e) {}
            }
        });

        mDatabase.collection("userInfos")
            .whereEqualTo("__name__", "mathieu.delehaye@gmail.com")
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

                    TextView scoreTextArea = (TextView) view.findViewById(R.id.textArea_score);
                    View scoreBackground = (View) view.findViewById(R.id.view_scoreBackground);

                    scoreTextArea.setText(String.valueOf(userScore) + " pts");

                    // Change background color
                    scoreBackground.setBackgroundColor(getResources().getColor(R.color.black));
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
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("BeautyAndroid", "mdl Map view becomes visible");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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
}