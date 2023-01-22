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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.beautyorder.androidclient.R;
import com.google.firebase.firestore.FirebaseFirestore;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentWithSearch extends Fragment {
    protected final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    protected GeoPoint mUserLocation;
    protected GeoPoint mSearchResult;
    protected GeoPoint mSearchStart;
    // TODO: do not use `OverlayItem` here, as it belongs to the map
    protected ArrayList<OverlayItem> mFoundResults;
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
    }

    protected GeoPoint getCoordinatesFromAddress(String locationName) {

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

    protected void setSearchStart(GeoPoint value) {
        Log.v("BeautyAndroid", "Search start set to: " + value.toString());
        mSearchStart = value;
    }

    protected void requestPermissionsIfNecessary(Context context, String[] permissions) {
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
