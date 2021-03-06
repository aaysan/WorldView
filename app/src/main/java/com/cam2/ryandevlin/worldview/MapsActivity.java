package com.cam2.ryandevlin.worldview;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;


import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import android.location.Address;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

///////////////////////
import android.widget.Toast;
import android.content.Context;
import android.widget.Button; //for button code
import android.widget.CompoundButton; //for button code
import android.widget.*; //for button code
import android.view.*; //for button code
import android.graphics.*;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.animation.*;
import android.widget.SearchView;
import android.support.v4.widget.DrawerLayout;
import android.widget.Adapter;
import android.view.View;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/////////////////


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null; //new
    private static final String TAG = MapsActivity.class.getSimpleName();


    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    LocationManager locationManager;
    String curr_location = null;
    LatLng latLng = new LatLng(0, 0);
    LatLng search_latLng = new LatLng(0, 0);
    String search_name = null;
    LatLngBounds search_zoom = null;
    int complement0 = 0;
    int complement1 = 0;
    int complement2 = 0;

    private ListView mDrawerList;
    private ArrayList<String> mList;
    private ListAdapter editList;
    private ArrayAdapter<String> mAdapter;

    List<Marker> markers = new ArrayList<Marker>();



    @Override
    protected void onCreate(Bundle savedInstanceState) { //CREATING THE MAP
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mDrawerList = (ListView)findViewById(R.id.navList);
        defaultDrawerItems();



    }
    private void defaultDrawerItems() {
        String[] menuArray = { "Standard", "Drawn", "Hide Markers"};
        //mList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuArray);
        mList = new ArrayList<>(Arrays.asList(menuArray));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);
        mDrawerList.setAdapter(mAdapter);
    }

    /////////////////////////////////////////////////////
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123; //REQUEST CODE USED IN THE PERMISSION REQUEST.  STILL NOT SURE IF THIS NUMBER MATTERS. "123" IS A RANDOM NUMBER.


    @Override
    public void onMapReady(final GoogleMap googleMap) { //THE MAP IS NOW RUNNING

        mMap = googleMap; //OBJECT FOR MAP MANIPULATION


        //INITIALIZATION OF PERMISSION CHECK
        int permissionCheck = ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        //ASK THE USER IF WORLDVIEW CAN TRACK THEIR LOCATION
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Context context = getApplicationContext();
                CharSequence text = "WorldView needs to access your location to enable all features."; //WE NEED TO EXPLAIN WHY WE MUST TRACK THEM
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);

                // REQUEST_CODE_ASK_PERMISSIONS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Location Services Enabled."; //WE HAVE PERMISSION TO TRACK THEM
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }





        MarkerOptions temp_search = new MarkerOptions()
                .position(search_latLng) //CREATE A MARKER FOR THE USER'S LOCATION
                .alpha(0.0f); //weird fix for marker issues. When the app loads the marker is placed at 0,0
        // until the device finds the user location. This code makes the marker transparent
        // until later when the user location is found.
        final Marker search_location = mMap.addMarker(temp_search);

        markers.add(search_location);

        /*ENTRY POINT FOR PLACES API*/

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                search_latLng = place.getLatLng();
                search_name = (String)place.getName();
                search_zoom = place.getViewport();
                search_location.setTitle(search_name);
                search_location.setPosition(search_latLng);
                search_location.setAlpha(0.75f);
                markers.add(search_location);

                if(search_zoom != null) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(search_zoom, 0);
                    mMap.animateCamera(cu);
                    //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(search_Position));
                }
                else{
                    CameraPosition search_Position = new CameraPosition.Builder()
                            .target(search_latLng)
                            .zoom(15)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(search_Position));
                }
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });



                           // Creates a CameraPosition from the builder

        /*THIS IS TO GRAB THE PROPER MAP STYLE JSON FILE. SEE THE APP/RES/RAW FOLDER FOR THE JSON FILENAME OPTIONS*/
        ToggleButton toggle = (ToggleButton) findViewById(R.id.button1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO NIGHTMODE
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    context, R.raw.night_mode));
                } else {
                    // The toggle is disabled
                    Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO STANDARD MODE
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    context, R.raw.standard));
                }
            }
        });




        final Button button = findViewById(R.id.location_zoom);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Context context = getApplicationContext();
                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                v.startAnimation(shake);
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                CameraPosition default_Position = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(default_Position));
            }
        });



        // Initializing proper UI settings
        UiSettings map_settings = mMap.getUiSettings();
        map_settings.setZoomControlsEnabled(true);
        map_settings.setCompassEnabled(true);

        /*MARKER INITIALIZATION*/

        Context context = getApplicationContext();
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                R.drawable.user_location);
        Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 80, 80, true); //RESCALE BITMAP ICON TO PROPER SIZE


        MarkerOptions a = new MarkerOptions()
                            .position(latLng) //CREATE A MARKER FOR THE USER'S LOCATION
                            .icon(BitmapDescriptorFactory.fromBitmap(custom_marker))
                            .alpha(0.0f); //weird fix for marker issues. When the app loads the marker is placed at 0,0
                                        // until the device finds the user location. This code makes the marker transparent
                                        // until later when the user location is found.
        final Marker user_location = mMap.addMarker(a);

        markers.add(user_location);

        /*START OF LOCATION TRACKING CODE*/

        //check whether the network provider is enabled
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){ //USING THE NETWORK PROVIDER FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    user_location.setAlpha(0.7f);
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        curr_location = addressList.get(0).getAddressLine(0);
                        user_location.setPosition(latLng); //UPDATE THE MARKER AS THEY MOVE AROUND
                        user_location.setTitle(curr_location);
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)); //JUST FOR DEBUGGING. THIS LINE CAUSES THE CAMERA TO RESET TOO OFTEN

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //USING GPS DATA FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    LatLng latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    user_location.setAlpha(1.0f);
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        curr_location = addressList.get(0).getAddressLine(0);
                        user_location.setPosition(latLng); //UPDATE THE MARKER AS THEY MOVE AROUND
                        user_location.setTitle(curr_location);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }


        /* THIS CODE IS FOR THE SIDE MENU */

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MapsActivity.this, "Position = " + position, Toast.LENGTH_SHORT).show();
                if(position == 0){
                    complement0 = 1 ^ complement0;
                    if(complement0 == 1){
                        Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO NIGHTMODE
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        context, R.raw.night_mode));
                        mList.set(position,"NightMode");
                        mAdapter.notifyDataSetChanged();

                    } else {
                        // The toggle is disabled
                        Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO STANDARD MODE
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        context, R.raw.standard));
                        mList.set(position,"Standard");
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(position == 1){
                    complement1++;
                    if(complement1 == 1){
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mList.set(position,"Satellite");
                        mAdapter.notifyDataSetChanged();
                    }
                    else if(complement1 == 2){
                        // The toggle is disabled
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mList.set(position,"Hybrid");
                        mAdapter.notifyDataSetChanged();
                    }
                    else if(complement1 == 3){
                        // The toggle is disabled
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mList.set(position,"Terrain");
                        mAdapter.notifyDataSetChanged();
                    }
                    else{
                        // The toggle is disabled
                        complement1 = 0;
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mList.set(position,"Drawn");
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(position == 2){
                    complement0 = 1 ^ complement0;
                    if(complement0 == 1){ //ADD ALL MARKERS HERE!!!!
                        int len = markers.size();
                        len--;
                        while(len != 0) {
                            Marker temp = markers.get(len);
                            temp.setAlpha(0.0f);
                            len--;
                        }
                        mList.set(position,"Reveal Markers");
                        mAdapter.notifyDataSetChanged();
                    } else {
                        // The toggle is disabled
                        int len = markers.size();
                        len--;
                        while(len != 0) {
                            Marker temp = markers.get(len);
                            temp.setAlpha(0.75f);
                            len--;
                        }
                        mList.set(position,"Hide Markers");
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(position == 3){

                }
            }
        });

        DirectionsResult test = new DirectionsResult();
        try {
            test = setDirections(mMap, "West Lafayette", "New York");
            addMarkersToMap(test,mMap);
            addPolyline(test,mMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int alp = 0;


    }

    private GeoApiContext getGeoContext(){

        //connection timeout : default connection timeout for new connections
        //query rate: max number of queries that will be executed in 1 second intervals
        //the default read timeout for new connections
        //the default write timeout for new connections

        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey("AIzaSyBUk43bX4UmObgrUZooRrsS-86PxSYelbU")
                .setConnectTimeout(1, TimeUnit.SECONDS).setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    public DirectionsResult setDirections(GoogleMap mMap, String origin, String destination) throws InterruptedException, ApiException, IOException {
        DateTime now = new DateTime();

        //mode = travelmode which can be walking, driving, etc...
        //origin is where you start
        //destination is where you want to go.
        //departure time is when you want to depart.

        DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                .mode(TravelMode.DRIVING).origin(origin)
                .destination(destination).departureTime(now)
                .await();

        return result;
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {

        if(results == null){
            return;
        }

        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].
                legs[0].startLocation.lat,results.routes[0].legs[0].startLocation.lng)).
                title(results.routes[0].legs[0].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].
                legs[0].endLocation.lat,results.routes[0].legs[0].endLocation.lng)).
                title(results.routes[0].legs[0].endAddress).snippet(getEndLocationTitle(results)));
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable
                + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }


    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

    }

}


