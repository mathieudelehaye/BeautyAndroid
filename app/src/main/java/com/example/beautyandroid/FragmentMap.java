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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
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

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {

                try {
                    double userLatitude = mLocationOverlay.getMyLocation().getLatitude();
                    double userLongitude = mLocationOverlay.getMyLocation().getLongitude();
                    GeoPoint geopoint = new GeoPoint((double) (userLatitude), (double) (userLongitude));

                    final String userLatitudeText = userLatitude+"";
                    final String userLongitudeText = userLongitude+"";
                    Log.d("BeautyAndroid", "user coordinates: latitude " + userLatitudeText + ", longitude "
                            + userLongitudeText);

                    double truncatedLatitude = Math.floor(userLatitude * 100) / 100;
                    double truncatedLongitude = Math.floor(userLongitude * 100) / 100;
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
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        final double longitude = (double)document.getData().get("Longitude");
                                        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

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
                                                (postcode.equals("?") ? "" : postcode) + "\n\nBrands: " +
                                                (recyclingProgram.equals("?") ? "" : recyclingProgram);

                                            Log.d("BeautyAndroid", "itemTitle = " + itemTitle +
                                                ", latitude = " + latitude + ", itemSnippet = " + itemSnippet);

                                            items.add(new OverlayItem(itemTitle, itemSnippet,
                                                new GeoPoint(latitude,longitude)));
                                        }

                                        //the overlay
                                        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                                                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                                    @Override
                                                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                                        Log.i("BeautyAndroid", "Single tap");
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
                                    }
                                } else {
                                    Log.d("BeautyAndroid", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                }
                catch (Exception e) {}
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