package com.example.uberproject;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriversMapsActivity extends FragmentActivity  {
    GoogleMap map;
    String [] permission={Manifest.permission.ACCESS_FINE_LOCATION};
    public static final  int MY_PERMISSION_LOCATION_CODE=500;
    SupportMapFragment mapView;
   FusedLocationProviderClient client;
    boolean checkAvailability=false;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button logout,setting;
    DatabaseReference requestReference,assignedReference;
    String driverId,customerId="";
    Marker marker;
   ValueEventListener valueEventListener;
    TextView txtNameCustomer,txtPhoneCustomer;
    CircleImageView profilePicCustomer;
    RelativeLayout relativeLayoutDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        driverId=currentUser.getUid();

        logout=findViewById(R.id.logout_driver_btn);
        setting=findViewById(R.id.setting_driver_btn);

        txtNameCustomer=findViewById(R.id.customer_name);
        txtPhoneCustomer=findViewById(R.id.customer_phone);
        profilePicCustomer=findViewById(R.id.profile_image_customer);
        relativeLayoutDriver=findViewById(R.id.rel_driver);



        mapView = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
       client = LocationServices.getFusedLocationProviderClient(this);

        if (isLocatinPermissionAllowed()) {
            //doMyFunction
            getRequestDriver();
            getDriverLocation();
        } else {

            requestLocationPermission();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAvailability=true;
                mAuth.signOut();
                driverForbidden();
       Intent startPageIntent=  new Intent(DriversMapsActivity.this,Welcome.class);
                startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startPageIntent);

                finish();
            }
        });
       // getRequestDriver();

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DriversMapsActivity.this,SettingActivity.class);
                intent.putExtra(Dao.Type,Dao.Driver);
                startActivity(intent);
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
            new AlertDialog.Builder(DriversMapsActivity.this).setTitle("permission").setMessage("this permission to know your location im google map").setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(DriversMapsActivity.this,permission,MY_PERMISSION_LOCATION_CODE);
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
                    getDriverLocation();
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


    private void getRequestDriver() {
        requestReference=Dao.saveDriver().child(driverId).child("CustomerRideId");
        requestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId=dataSnapshot.getValue().toString();
                    getAssignedPickUpLocation();
                    relativeLayoutDriver.setVisibility(View.VISIBLE);
                    getAssignedCustomerInfo();
                }
                else{
                    customerId="";
                    if(marker!=null){
                        marker.remove();
                    }
                    if(valueEventListener!=null){
                    assignedReference.removeEventListener(valueEventListener);
                }
                    relativeLayoutDriver.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//endGetRequestDriver()

private void getAssignedPickUpLocation() {
       assignedReference=Dao.getCustomerRequests().child(customerId).child("l");
    valueEventListener=    assignedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> customerLocationMap=(List<Object>)dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLang=0;

                    if(customerLocationMap.get(0)!=null){
                        locationLat=Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if(customerLocationMap.get(1)!=null){
                        locationLang=Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng latLng=new LatLng(locationLat,locationLang);
                    MarkerOptions markerOptions=new MarkerOptions().position(latLng).title("pick your customer from here").icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
                     map.addMarker(markerOptions);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }//endGetAssignedPickUpLocation()


    private void getDriverLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    //Toast.makeText(getApplicationContext(),"Location"+location.getAltitude()+location.getLongitude(),Toast.LENGTH_LONG).show();
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map=googleMap;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("current position");
                          marker=  googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
                           // String driverId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference driverAvailability= Dao.getDriversAvailability();
                            GeoFire geoFireAvability=new GeoFire(driverAvailability);
                            //geoFireAvability.setLocation(driverId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                            DatabaseReference driverWorking= Dao.getDriversWorking();
                            GeoFire geoFireWorking=new GeoFire(driverWorking);

                            switch (customerId){
                                case "":
                                    geoFireWorking.removeLocation(driverId);
                                    geoFireAvability.setLocation(driverId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                    break;
                                default:
                                    geoFireAvability.removeLocation(driverId);
                                    geoFireWorking.setLocation(driverId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                    break;

                            }

                        }
                    });
                }
            }
        });
    }//endGetDriverLocation()






    @Override
    protected void onStop() {
        super.onStop();
        if(!checkAvailability){
            driverForbidden();
        }


    }//endOnStop()
    private void driverForbidden(){

        DatabaseReference driverAvailability= Dao.getDriversAvailability();
        GeoFire geoFire=new GeoFire(driverAvailability);
        geoFire.removeLocation(driverId);
    }//endDriverForbidden()
    public void getAssignedCustomerInfo(){
        DatabaseReference driverInfo=Dao.saveCustomer().child(customerId);
        driverInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name=dataSnapshot.child("name").getValue().toString();
                    String phoneF=dataSnapshot.child("phone").getValue().toString();
                    txtNameCustomer.setText(name);

                    txtPhoneCustomer.setText(phoneF);

                    if(dataSnapshot.hasChild("image")){
                        String image=dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePicCustomer);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}//enClass