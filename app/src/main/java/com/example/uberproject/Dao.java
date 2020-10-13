package com.example.uberproject;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Dao {
    public static final String DriverBranchAvaliable="DriversAvailability";


    public static final String DriverBranchWorking="DriversWorking";
    public static final String CustomerRequest="CustomerRequests";
    public static final String UsersBranch="Users";
    public static final String CustomerBranch="Customers";
    public static final String DriverBranch="Drivers";
    public static final String ProfilePicture="ProfilePictures";



    public static DatabaseReference getDriversAvailability(){
        return FirebaseDatabase.getInstance()
                .getReference().child(DriverBranchAvaliable);
    }
    public static DatabaseReference getDriversWorking(){
        return FirebaseDatabase.getInstance()
                .getReference().child(DriverBranchWorking);
    }
    public static DatabaseReference getCustomerRequests(){
        return FirebaseDatabase.getInstance()
                .getReference().child(CustomerRequest);
    }
    public static DatabaseReference saveDriver(){
        return FirebaseDatabase.getInstance()
                .getReference().child(UsersBranch).child(DriverBranch);
    }
    public static DatabaseReference saveCustomer(){
        return FirebaseDatabase.getInstance()
                .getReference().child(UsersBranch).child(CustomerBranch);
    }
    public static DatabaseReference userBranch(){
        return FirebaseDatabase.getInstance()
                .getReference().child(UsersBranch);
    }

    public static StorageReference profilePictures(){
        return FirebaseStorage.getInstance().getReference().child(ProfilePicture);
    }
    public static final String Type="type";
    public static final String Driver="Driver";
    public static final String Customer="Customer";
}
