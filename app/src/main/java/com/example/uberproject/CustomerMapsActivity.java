package com.example.uberproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapsActivity extends FragmentActivity  {
    GoogleMap map;

    String [] permission={Manifest.permission.ACCESS_FINE_LOCATION};
    public static final  int MY_PERMISSION_LOCATION_CODE=500;
    SupportMapFragment mapView;
    FusedLocationProviderClient client;
    boolean checkAvailability=false;
    double latitude,longitude;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button logout,getCab,setting;
    DatabaseReference customerRequests= Dao.getCustomerRequests();
    GeoFire geoFireAvailable=new GeoFire(customerRequests);
    int radius=1;
    Location userLocation=null;
    String userId;
    String driverFoundId;
    boolean driverFound=false,requestType=false;
    DatabaseReference driverRef;
    DatabaseReference driverLocationRef;
    Marker driverMarker,userMarker;
    GeoQuery geoQuery;
    ValueEventListener driverValueEventListener;
    TextView txtNameDriver,txtCarName,txtPhoneDriver;
    CircleImageView profilePicDriver;
    RelativeLayout relativeLayoutCustomer;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapView = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(this);
        driverLocationRef=Dao.getDriversWorking();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        getCab=findViewById(R.id.get_cab_btn);
        if (isLocatinPermissionAllowed()) {
            //doMyFunction
            makeRequest();
        } else {

            requestLocationPermission();
        }

        logout=findViewById(R.id.logout_customer_btn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();

                Intent startPageIntent = new Intent(CustomerMapsActivity.this, Welcome.class);
                startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startPageIntent);
                finish();
                finish();
            }
        });

             setting=findViewById(R.id.setting_customer_btn);
             setting.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent=new Intent(CustomerMapsActivity.this,SettingActivity.class);
                     intent.putExtra(Dao.Type,Dao.Customer);
                     startActivity(intent);
                 }
             });

             txtCarName=findViewById(R.id.driver_car);
             txtNameDriver=findViewById(R.id.name_driver);
             txtPhoneDriver=findViewById(R.id.driver_phone);
             profilePicDriver=findViewById(R.id.profile_image_driver);
             relativeLayoutCustomer=findViewById(R.id.rel_customer);

    }
    public void makeRequest(){
        getCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestType){
                    requestType=false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverValueEventListener);
                    if( !driverFound ){
                        driverRef=   Dao.saveDriver().child(driverFoundId).child("CustomerRideId");
                        driverRef.removeValue();
                        driverFoundId=null;
                    }
                    driverFound=false;
                    radius=1;
                    userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    geoFireAvailable.removeLocation(userId);
                    if(userMarker!=null){
                        userMarker.remove();
                    }
                    if(driverMarker!=null){
                        driverMarker.remove();
                    }
                    getCab.setText("call a cab");
                    relativeLayoutCustomer.setVisibility(View.GONE);


                }
                else{
                    requestType=true;
                    getUserLocation();
                    getClosestDriverCab();
                }
            }
        });
    }
    boolean isLocatinPermissionAllowed(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
        {
            new AlertDialog.Builder(CustomerMapsActivity.this).setTitle("permission").setMessage("this permission to know your location im google map").setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(CustomerMapsActivity.this,permission,MY_PERMISSION_LOCATION_CODE);
                }
            }).create().show();


        } else {
            ActivityCompat.requestPermissions(this,permission,MY_PERMISSION_LOCATION_CODE);

        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_LOCATION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //callMyFunction
                    makeRequest();
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this,"unavailable",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }
    private void getUserLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    userLocation=location;
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                    //Toast.makeText(getApplicationContext(),"Location"+location.getAltitude()+location.getLongitude(),Toast.LENGTH_LONG).show();
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map=googleMap;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
                            userMarker= googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
                            userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference  customerRequest=Dao.getCustomerRequests();
                            GeoFire geoFire=new GeoFire(customerRequest);
                            geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude())  );
                            getCab.setText("Getting your Driver...");



                            //   geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));



                        }
                    });
                }
            }
        });
    }//endGetUserLocation()


    private void getClosestDriverCab() {
        DatabaseReference driverAvaliable=Dao.getDriversAvailability();
        GeoFire geoFireAvaliabledriver=new GeoFire(driverAvaliable);

        geoQuery=geoFireAvaliabledriver.queryAtLocation(new GeoLocation(latitude,longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
              if(!driverFound&&requestType){
                  driverFound=true;
             driverFoundId=   dataSnapshot.getKey();
             driverRef=Dao.saveDriver().child(driverFoundId);
                  HashMap driverMap=new HashMap();
                  driverMap.put("CustomerRideId",currentUser);
                  driverRef.updateChildren(driverMap);
                  gettingDriverLocation();
                  getCab.setText("Looking for Driver Location...");
              }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound) {
                    radius = radius + 1;
                    getClosestDriverCab();
                    getCab.setText("Looking for Driver Location...");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }//endGetClosestDriverCab()

    private void gettingDriverLocation() {
        DatabaseReference driveWorking=Dao.getDriversWorking();
       driverValueEventListener= driveWorking.child(driverFoundId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&requestType){
                    List<Object> driverLocationMap=(List<Object>)dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLang=0;
                    getCab.setText("Driver Found");
                    relativeLayoutCustomer.setVisibility(View.VISIBLE);
                    getAssignedDriverInfo();
                    if(driverLocationMap.get(0)!=null){
                        locationLat=Double.parseDouble(driverLocationMap.get(0).toString());
                    }
                    if(driverLocationMap.get(1)!=null){
                        locationLang=Double.parseDouble(driverLocationMap.get(1).toString());
                    }
                    LatLng latLng=new LatLng(locationLat,locationLang);
                    if(driverMarker!=null){
                        driverMarker.remove();
                    }
                    Location location1=new Location("");
                    location1.setLatitude(userLocation.getLatitude());
                    location1.setLongitude(userLocation.getLongitude());

                    Location location2=new Location("");
                    location2.setLongitude(latLng.longitude);
                    location2.setLatitude(latLng.latitude);

                    float distance=location1.distanceTo(location2);

                    if (distance < 90)
                    {
                        getCab.setText("Driver's Reached");
                    }
                    else
                    {
                        getCab.setText("Driver Found: " + String.valueOf(distance));
                    }
                    MarkerOptions markerOptions=new MarkerOptions().position(latLng).title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                  driverMarker=  map.addMarker(markerOptions);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }//endGettingDriverLocation()



    @Override
    protected void onStop() {
        super.onStop();


    }//endOnStop()
    public void getAssignedDriverInfo(){
        DatabaseReference driverInfo=Dao.saveCustomer().child(driverFoundId);
        driverInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(dataSnapshot.exists()){
                               String name=dataSnapshot.child("name").getValue().toString();
                               txtNameDriver.setText(name);
                               String phoneF=dataSnapshot.child("phone").getValue().toString();
                               txtPhoneDriver.setText(phoneF);
                               String carF=dataSnapshot.child("car").getValue().toString();
                               txtCarName.setText(carF);
                               if(dataSnapshot.hasChild("image")){
                                   String image=dataSnapshot.child("image").getValue().toString();
                                   Picasso.get().load(image).into(profilePicDriver);
                               }
                           }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }







}//endClass