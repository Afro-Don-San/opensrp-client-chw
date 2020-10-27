package org.smartregister.chw.activity;

import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;

import org.smartregister.chw.core.activity.CorePathfinderFpMemberMapActivity;
/**
 * Created by cozej4 on 10/26/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class PathfinderFpMapActivity extends CorePathfinderFpMemberMapActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, "pk.eyJ1IjoiY296ZWo0IiwiYSI6ImNrZ3FnaGZ5ajBsZzAyemw4bDJzejR6bDgifQ.8mXJY4_luIjzvUGbgGXqcQ");
        super.onCreate(savedInstanceState);
    }
}
