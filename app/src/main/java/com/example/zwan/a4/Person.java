package com.example.zwan.a4;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by sersh on 4/18/17.
 */

public class Person implements ClusterItem {
    public final int uid;
    public final String name;
    public final Bitmap profilePhoto;
    private final LatLng mPosition;

    public Person(int id, LatLng position, String name, Bitmap picture) {
        uid = id;
        this.name = name;
        profilePhoto = picture;
        mPosition = position;
    }
    public int getUId(){
        return uid;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
