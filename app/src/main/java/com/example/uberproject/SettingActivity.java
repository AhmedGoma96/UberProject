package com.example.uberproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity
{
    CircleImageView profileImage;
    TextView changePicture;
    EditText nameEdit,telephone,car;
    ImageView save,close;
    String getType;
    String checker="";
    public static final String click="Clicked";
    Uri imageUri;
    String myUri;
    StorageTask uploadTask;
    StorageReference storageProfileRef;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        storageProfileRef=Dao.profilePictures();
        firebaseAuth=FirebaseAuth.getInstance();
        reference=Dao.userBranch();
        getType=getIntent().getStringExtra(Dao.Type);
        profileImage=findViewById(R.id.profile_image);
        changePicture=findViewById(R.id.change_picture);
        nameEdit=findViewById(R.id.name);
        telephone=findViewById(R.id.telephone_number);
        car=findViewById(R.id.type_of_car);
        Toast.makeText(SettingActivity.this,getType,Toast.LENGTH_LONG).show();
  if(getType.equals(Dao.Driver)){
      car.setVisibility(View.VISIBLE);
  }
        save=findViewById(R.id.save_btn);
        close=findViewById(R.id.close_btn);
     close.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (getType.equals(Dao.Driver)) {
                 startActivity(new Intent(SettingActivity.this,DriversMapsActivity.class));
             }else {
                 startActivity(new Intent(SettingActivity.this,CustomerMapsActivity.class));
             }

         }
     });
     changePicture.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             checker=click;
             CropImage.activity()
                     .setAspectRatio(1,1)
                     .start(SettingActivity.this);

         }

     });
    save.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checker.equals(click)) {
 validateControllers();
            }
            else{
                validateOnlyInfo();
            }
        }
    });



getUserInfo();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode==RESULT_OK&&data!=null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri=result.getUri();
            profileImage.setImageURI(imageUri);


        }
        else {

            Toast.makeText(SettingActivity.this,"ErrorHappen",Toast.LENGTH_LONG).show();
        }

    }
    public void validateControllers(){
        if(TextUtils.isEmpty(nameEdit.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your name..",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(telephone.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your telephone..",Toast.LENGTH_LONG).show();
        }
      else if(getType==Dao.Driver&&TextUtils.isEmpty(car.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your type of car..",Toast.LENGTH_LONG).show();
        }
      else if(checker.equals(click)){
          uploadProfilePicture();
        }


    }
    public  void uploadProfilePicture(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Settings Account Information");
        progressDialog.setMessage("Please wait, while we are settings your account information");
        progressDialog.show();
 if(imageUri!=null){
     final StorageReference fileRef=storageProfileRef.child(firebaseAuth.getUid() + ".jpg");
    uploadTask= fileRef.putFile(imageUri);
    uploadTask.continueWithTask(new Continuation() {
        @Override
        public Object then(@NonNull Task task) throws Exception {
            if(!task.isSuccessful()){
                throw task.getException();

            }
            return fileRef.getDownloadUrl();
        }
    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
        @Override
        public void onComplete(@NonNull Task<Uri> task) {
 if(task.isSuccessful()){
     Uri downloadUri=task.getResult();
     myUri=downloadUri.toString();
     HashMap<String,Object> userMap=new HashMap<>();
     userMap.put("uid",firebaseAuth.getUid());
     userMap.put("name",nameEdit.getText().toString());
     userMap.put("image",myUri);
     userMap.put("phone",telephone.getText().toString());
     if(getType.equals(Dao.Driver)){
         userMap.put("car",car.getText().toString());
     }
     reference.child(firebaseAuth.getUid()).updateChildren(userMap);
     progressDialog.dismiss();
     if (getType.equals(Dao.Driver))
     {
         startActivity(new Intent(SettingActivity.this, DriversMapsActivity.class));
     }
     else
     {
         startActivity(new Intent(SettingActivity.this, CustomerMapsActivity.class));
     }

 }
        }
    });
 }
 else
 {
     Toast.makeText(this, "Image is not selected.", Toast.LENGTH_SHORT).show();
 }
    }
    public void validateOnlyInfo(){
        if(TextUtils.isEmpty(nameEdit.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your name..",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(telephone.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your telephone..",Toast.LENGTH_LONG).show();
        }
        else if(getType==Dao.Driver&&TextUtils.isEmpty(car.getText().toString())){
            Toast.makeText(SettingActivity.this,"Enter your type of car..",Toast.LENGTH_LONG).show();
        }
        else{
            HashMap<String,Object> userMap=new HashMap<>();
            userMap.put("uid",firebaseAuth.getUid());
            userMap.put("name",nameEdit.getText().toString());
            userMap.put("phone",telephone.getText().toString());
            if(getType.equals(Dao.Driver)){
                userMap.put("car",car.getText().toString());
            }
            reference.child(firebaseAuth.getUid()).updateChildren(userMap);
            if (getType.equals(Dao.Driver))
            {
                startActivity(new Intent(SettingActivity.this, DriversMapsActivity.class));
            }
            else
            {
                startActivity(new Intent(SettingActivity.this, CustomerMapsActivity.class));
            }
        }
    }
    public void getUserInfo(){
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.hasChildren()){
                    String name=dataSnapshot.child("name").getValue().toString();
                    nameEdit.setText(name);
                    String phoneF=dataSnapshot.child("phone").getValue().toString();
                    telephone.setText(phoneF);
                    String carF=dataSnapshot.child("car").getValue().toString();
                    if(getType.equals(Dao.Driver)){
                        car.setText(carF);
                    }
                    if(dataSnapshot.hasChild("image")){
                    String image=dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}