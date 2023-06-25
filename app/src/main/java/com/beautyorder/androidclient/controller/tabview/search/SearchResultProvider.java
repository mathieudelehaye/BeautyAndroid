//
//  SearchResultProvider.java
//
//  Created by Mathieu Delehaye on 23/04/2023.
//
//  BeautyAndroid: An Android app to order and recycle cosmetics.
//
//  Copyright © 2023 Mathieu Delehaye. All rights reserved.
//
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
//  Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
//  warranty of MERCHANTABILITY or FITNESS
//  FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this program. If not, see
//  <https://www.gnu.org/licenses/>.

package com.beautyorder.androidclient.controller.tabview.search;

import android.util.Log;
import com.android.java.androidjavatools.controller.template.SearchProvider;
import com.android.java.androidjavatools.model.GeoPosition;
import com.android.java.androidjavatools.model.ResultItemInfo;
import com.android.java.androidjavatools.model.SearchResult;
import com.android.java.androidjavatools.model.TaskCompletionManager;
import com.beautyorder.androidclient.model.ResultInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import org.osmdroid.util.GeoPoint;

public class SearchResultProvider implements SearchProvider {
    private SearchResult mSearchResults;

    @Override
    public SearchResult getSearchResults() {
        return mSearchResults;
    }

    @Override
    public void searchGeoPointResults(GeoPosition searchStart, double searchRadiusInCoordinate,
                                      FirebaseFirestore database, TaskCompletionManager... cbManager) {

        final double startLatitude = searchStart.getLocation().getLatitude();
        final double startLongitude = searchStart.getLocation().getLongitude();
        final String startLatitudeText = startLatitude+"";
        final String startLongitudeText = startLongitude+"";
        Log.d("EBT", "Display the recycling points around the location (" + startLatitudeText
            + ", " + startLongitudeText + ")");

        // Search for the recycling points (RP)
        final double truncatedLatitude = Math.floor(startLatitude * 100) / 100;
        final double truncatedLongitude = Math.floor(startLongitude * 100) / 100;
        final double maxSearchLatitude = truncatedLatitude + searchRadiusInCoordinate;
        final double minSearchLatitude = truncatedLatitude - searchRadiusInCoordinate;
        final double maxSearchLongitude = truncatedLongitude + searchRadiusInCoordinate;
        final double minSearchLongitude = truncatedLongitude - searchRadiusInCoordinate;

        String[] outputFields = { "Latitude", "Longitude", "PointName", "BuildingName", "BuildingNumber",
            "Address", "Postcode", "City", "3Words", "RecyclingProgram", "ImageUrl" };
        String[] filterFields = { "Latitude", "Longitude" };
        double[] filterMinRanges = { minSearchLatitude, minSearchLongitude };
        double[] filterMaxRanges = { maxSearchLatitude, maxSearchLongitude };

        var pointInfo = new ResultInfo(database);
        pointInfo.setRangeBasedFilter(filterFields, filterMinRanges, filterMaxRanges);

        mSearchResults = new SearchResult();

        pointInfo.readAllDBFields(outputFields, new TaskCompletionManager() {
            @Override
            public void onSuccess() {
                for (int i = 0; i < pointInfo.getData().size(); i++) {
                    // Uncomment to write back to DB the coordinates from the RP address
                    //writeBackRPAddressCoordinatesToDB(pointInfo.getData().get(i).get("documentId"),
                    //    pointInfo.getAddressAtIndex(i));

                    final String key = pointInfo.getKeyAtIndex(i);

                    final double latitude = pointInfo.getLatitudeAtIndex(i);
                    final double longitude = pointInfo.getLongitudeAtIndex(i);

                    String itemTitle = pointInfo.getTitleAtIndex(i);
                    String itemSnippet = pointInfo.getSnippetAtIndex(i);
                    String itemImageUrl = pointInfo.getImageUrlAtIndex(i);

                    mSearchResults.add(
                        key,
                        new ResultItemInfo(
                            key, itemTitle, itemSnippet, new GeoPoint(latitude,longitude),
                            null, true),
                        itemImageUrl);

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
}
