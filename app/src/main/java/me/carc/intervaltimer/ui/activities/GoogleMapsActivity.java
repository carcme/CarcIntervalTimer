package me.carc.intervaltimer.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.services.interfaces.LocationObserver;
import me.carc.intervaltimer.services.location.GpsTracker;
import me.carc.intervaltimer.utils.MapUtils;

public class GoogleMapsActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = GoogleMapsActivity.class.getName();

    public static final String MAP_SHOW_MY_LOCATION = "MAP_SHOW_MY_LOCATION";
    public static final String MAP_POINTS    = "MAP_POINTS";
    public static final String MAP_TIME      = "MAP_TIME";
    public static final String MAP_TITLE     = "MAP_TITLE";

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLngBounds mBounds;
    private GpsTracker gpsTracker;
    private List<LatLng> route;

    @Nullable
    @BindView(R.id.mapToolbar)      Toolbar mapToolbar;
    @BindView(R.id.fabMapLayers)    FloatingActionButton fabMapLayers;
    @BindView(R.id.mapDistance)     TextView mapDistance;
    @BindView(R.id.mapDistanceSuffix)   TextView mapDistanceSuffix;
    @BindView(R.id.mapTime)         TextView mapTime;
    @BindView(R.id.mapPace)         TextView mapPace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        if (mapToolbar != null) setSupportActionBar(mapToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        Intent callingIntent = getIntent();

        if (callingIntent.hasExtra(MAP_SHOW_MY_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(callingIntent.getBooleanExtra(MAP_SHOW_MY_LOCATION, true));

                gpsTracker = new GpsTracker(GoogleMapsActivity.this, locationObserver);

                mMap.setLocationSource(gpsTracker);
                gpsTracker.activate(onLocationChangedListener);
            }
        }

        if (callingIntent.hasExtra(MAP_TITLE) && getSupportActionBar() != null && getSupportActionBar() != null) {
            String title = callingIntent.getStringExtra(MAP_TITLE);
            getSupportActionBar().setTitle(TextUtils.isEmpty(title) ? "<Untitled>" : title);
        }


        if (callingIntent.hasExtra(MAP_POINTS)) {
            ArrayList<LatLon> historyPoints;
            historyPoints = callingIntent.getParcelableArrayListExtra(MAP_POINTS);
            if (historyPoints != null && historyPoints.size() > 1) {
                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                route = new ArrayList<>();
                for (LatLon latLon : historyPoints) {
                    LatLng gpsPoint = new LatLng(latLon.getLatitude(), latLon.getLongitude());
                    latLngBuilder.include(gpsPoint);
                    route.add(gpsPoint);
                }
                mBounds = latLngBuilder.build();

                double distance = 0, temp;
                double mod, nextMod;
                ArrayList<Double> km = new ArrayList<>();

                List<LatLng> wayPoints = new ArrayList<>();
                if (route.size() >= 2) {
                    if (route.size() >= 2) {
                        for (int i = 1; i < route.size(); i++) {
                            mod = distance % 1000;
                            temp = distance;
                            distance += MapUtils.getDistance(route.get(i - 1), route.get(i));
                            nextMod = distance % 1000;
                            if (mod > nextMod) {
                                wayPoints.add(route.get(i - 1));
                                km.add(temp);
                            }
                        }
                    }
                }

                if (!mMap.isMyLocationEnabled()) {
                    addMarker(route.get(0), R.drawable.ic_marker_start, "Start", "", true);
                    addMarker(route.get(route.size() - 1), R.drawable.ic_marker_finish, MapUtils.getFormattedDistance(distance), "", true);
                }

                for (int i = 0; i < wayPoints.size(); i++) {
                    addMarker(wayPoints.get(i), R.drawable.ic_marker_way, MapUtils.getFormattedDistance(km.get(i)), "", false);
                }

                mMap.addPolyline(new PolylineOptions()
                        .addAll(route)
                        .color(Color.parseColor("#f57c00"))
                        .width(5));


                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (height * 0.20); // offset from edges of screen (20%)

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, width, height, padding));

                mMap.setOnMapClickListener(onSingleClick);

                String fmtDistance = MapUtils.getFormattedDistance(distance);

                String[] split = fmtDistance.split(" ");
                mapDistance.setText(split[0]);
                mapDistanceSuffix.setText(split[1]);

                if (callingIntent.hasExtra(MAP_TIME) && distance > 0) {
                    String time = callingIntent.getStringExtra(MAP_TIME);
                    mapTime.setText(time);
                    calculatePace(distance, time);
                } else {
                    mapTime.setText("--:--");
                    mapPace.setText("--:--");
                }

                return;
            }
        }

        // Show msg on error or no data
        Snackbar snackbar = Snackbar.make(mapFragment.getView(), "No Route Data found", Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.BLACK)
                .setAction("Show", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_500));
        snackbar.show();
    }

    private void calculatePace(double distance, String time) {
        String[] split = time.split(":");
        double pace, temp;

        if (split.length > 2) {
            int hours = Integer.valueOf(split[0]);
            int minutes = Integer.valueOf(split[1]);
            int seconds = Integer.valueOf(split[2]);
            temp = (hours * 60 * 60) + (minutes * 60) + seconds;

        } else {
            int minutes = Integer.valueOf(split[0]);
            int seconds = Integer.valueOf(split[1]);
            temp = (minutes * 60) + seconds;
        }

        pace = round(temp / distance / 60 * 1000, 2);
        temp = round((pace % 1) * 60, 0);

        mapPace.setText(String.format(Locale.US, "%2.0f\'%02.0f\"", pace, temp));
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @SuppressWarnings("unused")
    private void addMarker(LatLng latLng, @DrawableRes int iconRes, @StringRes int titleRes, @StringRes int snippetRes, boolean useOffset) {
        addMarker(latLng, iconRes, getString(titleRes), getString(snippetRes), useOffset);
    }

    @SuppressWarnings("deprecation")
    private void addMarker(LatLng latLng, @DrawableRes int iconRes, String title, String snippet, boolean useOffset) {
        Drawable circleDrawable = getResources().getDrawable(iconRes);
        BitmapDescriptor icon = getMarkerIconFromDrawable(circleDrawable);

        MarkerOptions markerOptions = new MarkerOptions()
                .icon(icon)
                .position(latLng)
                .snippet(snippet)
                .title(title);
        if (useOffset)
            markerOptions.anchor(0.5f, 0.8f);
        else
            markerOptions.anchor(0.5f, 0.5f);

        mMap.addMarker(markerOptions);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private final GoogleMap.OnMapClickListener onSingleClick = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(@NonNull LatLng point) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 50));
        }
    };

    @OnClick(R.id.fabMapLayers)
    void changeStyle() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_layers);
        builderSingle.setTitle("Select Map Style");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Route Only");
        arrayAdapter.add("Satellite");
        arrayAdapter.add("Terrain");
        arrayAdapter.add("Hybrid");

        builderSingle.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 4:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    default:
                        throw new RuntimeException("Value not handled. Add to switch statement");
                }
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if(gpsTracker != null)
            gpsTracker.cleanUp();
        super.onDestroy();
    }

    private final LocationSource.OnLocationChangedListener onLocationChangedListener = new LocationSource.OnLocationChangedListener() {
        @Override
        public void onLocationChanged(Location location) {
            route.add(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.addPolyline(new PolylineOptions()
                    .addAll(route)
//                    .color(Color.parseColor("#f57c00"))
                    .color(Color.parseColor("#43a047"))
                    .width(5));

        }
    };

    private final LocationObserver locationObserver = new LocationObserver() {

        @Override
        public void canGetLocation(boolean canGet) {
        }

        @Override
        public void locationUpdate(Location location) {
            Log.d(TAG, "locationUpdate: ");
        }

        @Override
        public void statusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void providerEnabled(boolean enabled, String provider) {
        }
    };
}
