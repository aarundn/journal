package com.example.journalapp.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journalapp.R;
import com.example.journalapp.model.Journal;
import com.example.journalapp.utilities.Constants;
import com.example.journalapp.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.Date;

import Utils.JournalUser;


public class AddJournal extends AppCompatActivity {

    private static final int GALLERY_CODE = 1;
    // Widgets
    private ConstraintLayout saveButton;
    private ProgressBar progressBar;
    private LinearLayout addPhotoButton;
    private EditText titleEditText;
    private EditText thoughsEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    // User Id & Username
    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;


    // Connection to Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private final CollectionReference collectionReference = db.collection(Constants.KEY_COLLECTIONS_JOURNALS);
    private Uri imageUri;

    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        titleEditText = findViewById(R.id.titleEditText);
        thoughsEditText = findViewById(R.id.descriptionEditText);
        //currentUserTextView = findViewById(R.id.post_username_textview);

        imageView = findViewById(R.id.imageAddNote);
        saveButton = findViewById(R.id.floatingSaveBtn);
        addPhotoButton = findViewById(R.id.addPhoto);
         preferencesManager = new PreferencesManager(getApplicationContext());

        progressBar.setVisibility(View.INVISIBLE);


        if (JournalUser.getInstance() != null){
            currentUserId = JournalUser.getInstance().getUserId();
            currentUserName = JournalUser.getInstance().getUsername();

        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null){
                    currentUserId = user.getUid();
                    currentUserName = user.getDisplayName();
                    JournalUser.getInstance().setUserId(currentUserId);
                }else{

                }
            }
        };


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveJournal();
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Getting image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
            }
        });


    }

    private void SaveJournal() {
        final String title = titleEditText.getText().toString().trim();
        final String thoughts = thoughsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(thoughts)
                && imageUri != null) {

            // the saving path of the images in Storage Firebase:
            // ...../journal_images/our_image.png
            final StorageReference filepath = storageReference
                    .child("journal_images")
                    .child("my_image_"+ Timestamp.now().getSeconds());


            // Uploading the image
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    // Creating object of Journal
                                    // Let's Create this model class "Journal"
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setDescription(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimesAdd(new Timestamp(new Date()));
                                    journal.setUserName(JournalUser.getInstance().getUsername());
                                    journal.setUserId(preferencesManager.getString(Constants.KEY_USER_ID));



                                    // Invoking Collection Reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(getApplicationContext(), "journal added..", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(),
                                                            MainActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });




                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

        }else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            if (data != null){
                imageUri = data.getData();        // Getting the actual image path
                imageView.setImageURI(imageUri);  // Showing the image
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}





//package com.example.journalapp;
//
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.constraintlayout.widget.ConstraintLayout;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.Gallery;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import com.example.journalapp.model.Journal;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.Timestamp;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.util.Collection;
//import java.util.Date;
//
//import Utils.JournalUser;
//
//
//public class AddJournal extends AppCompatActivity {
//    private static final int GALLERY_CODE = 1;
//    private EditText titleEt,descriptionEt;
//    private ProgressBar progressBar;
//    private ConstraintLayout saveBtn;
//    private ImageView addUpImage,AddedImage;
//    private LinearLayout addNewLine,addLink,addImage,backBtn;
//
//    private String currentUserId,currentUserName;
//    private FirebaseAuth firebaseAuth;
//    private FirebaseAuth.AuthStateListener authStateListener;
//    private FirebaseUser user;
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private StorageReference storageReference;
//    private CollectionReference collectionReference = db.collection("journal");
//    private Uri imageUri;
//
//    private ActivityResultLauncher<Intent> resultLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_journal);
//
//        storageReference = FirebaseStorage.getInstance().getReference();
//        firebaseAuth = FirebaseAuth.getInstance();
//
//        titleEt = findViewById(R.id.titleEditText);
//        descriptionEt = findViewById(R.id.descriptionEditText);
//        saveBtn = findViewById(R.id.floatingSaveBtn);
//        addImage = findViewById(R.id.addPhoto);
//        addLink = findViewById(R.id.addLink);
//        addNewLine = findViewById(R.id.addNewLine);
//        backBtn = findViewById(R.id.backButton);
//        addUpImage = findViewById(R.id.addimage);
//        progressBar = findViewById(R.id.progressBar);
//        AddedImage = findViewById(R.id.imageAddNote);
//
//        if (JournalUser.getInstance() != null){
//            currentUserId = JournalUser.getInstance().getUserId();
//            currentUserName = JournalUser.getInstance().getUsername();
//
//        }
//
//        authStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                user = firebaseAuth.getCurrentUser();
//                if (user != null){
//
//                }else {
//
//                }
//            }
//        };
//
//        saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SaveJournal();
//            }
//        });
//        registeredImage();
//        addImage.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
//            @Override
//            public void onClick(View view) {
//                Intent galleryIntent = new Intent(MediaStore.ACTION_PICK_IMAGES);
//                galleryIntent.setType("image/*");
//                resultLauncher.launch(galleryIntent);
//            }
//        });
//
//    }
//
//
//    private void SaveJournal() {
//        final String title  = titleEt.getText().toString().trim();
//        final  String description = descriptionEt.getText().toString().trim();
//        progressBar.setVisibility(View.VISIBLE);
//
//        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) ){
//
//
//                            Journal journal = new Journal();
//
//                            journal.setTitle(title);
//                            journal.setDescription(description);
//                            journal.setTimesAdd(new Timestamp(new Date()));
//                            journal.setUserId(JournalUser.getInstance().getUserId());
//                            journal.setUserName(JournalUser.getInstance().getUsername());
//
//                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                @Override
//                                public void onSuccess(DocumentReference documentReference) {
//                                    progressBar.setVisibility(View.VISIBLE);
//                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                    finish();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(AddJournal.this, "failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        } else {
//            progressBar.setVisibility(View.INVISIBLE);
//        }
//
//    }
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK ){
////            if (data != null){
////                Log.v("TAG", "Image URI: " + imageUri.toString());
////                imageUri = data.getData();
////                AddedImage.setImageURI(imageUri);
////            }
////        }
////    }
//
//    private void registeredImage(){
//        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        try {
//                            Log.v("TAG", "Image URI: " + imageUri.toString());
//                            assert result.getData() != null;
//                            imageUri = result.getData().getData();
//                            AddedImage.setImageURI(imageUri);
//                        }catch (Exception e){
//                            Toast.makeText(AddJournal.this, "failed!"+ e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });
//    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (firebaseAuth != null){
//            firebaseAuth.removeAuthStateListener(authStateListener);
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        user = firebaseAuth.getCurrentUser();
//        firebaseAuth.addAuthStateListener(authStateListener);
//    }
//}