//
//  OverlayItemWithImage.java
//
//  Created by Mathieu Delehaye on 28/01/2023.
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class OverlayItemWithImage extends OverlayItem {

    private String mImage;

    public OverlayItemWithImage(String aTitle, String aSnippet, IGeoPoint aGeoPoint, String anImage) {
        super(aTitle, aSnippet, aGeoPoint);

        mImage = anImage;
    }

    public String getImage() {
        return mImage;
    }
}
