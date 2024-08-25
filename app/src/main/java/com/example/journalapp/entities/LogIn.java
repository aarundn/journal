package com.example.journalapp.entities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journalapp.R;
import com.example.journalapp.model.Journal;
import com.example.journalapp.utilities.Constants;
import com.example.journalapp.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.Nullable;

import Utils.JournalUser;


public class LogIn extends AppCompatActivity {

    // Widgets
    private Button loginBTN;
    private TextView createAccBTN;
    private EditText emailET;
    private EditText passET;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginBTN = findViewById(R.id.loginBtn);
        createAccBTN = findViewById(R.id.register);
        emailET = findViewById(R.id.inputEmail);
        passET = findViewById(R.id.inputPassword);

        // Forget to initialize the Auth Ref
        firebaseAuth = FirebaseAuth.getInstance();

        mAuth = FirebaseAuth.getInstance();
        preferencesManager = new PreferencesManager(getApplicationContext());
        if (preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        createAccBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });


        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(
                        emailET.getText().toString().trim(),
                        passET.getText().toString().trim()
                );
//                l(emailET.getText().toString().trim(),
//                        passET.getText().toString().trim());
            }
        });

    }

//    private void LoginEmailPasswordUser(String trim, String trim1) {
//    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
            preferencesManager.putString(Constants.KEY_USER_ID,currentUser.getUid());
        }
    }

    private void login(String email, String password) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTIONS_USER)
                .whereEqualTo(Constants.KEY_NAME, email)
                .whereEqualTo(Constants.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        JournalUser journalUser = new JournalUser();
                        journalUser.setUsername(documentSnapshot.getString(Constants.KEY_NAME));
                        preferencesManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable sign in!");
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            loginBTN.setVisibility(View.INVISIBLE);

        } else {
            loginBTN.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

//    private void LoginEmailPasswordUser(String email, String pwd) {
//        // Checking for empty texts
//        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
//
//            firebaseAuth.signInWithEmailAndPassword(email, pwd)
//                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//
//                            assert user != null;
//                            final String currentUserId = user.getUid();
//
//                            collectionReference.
//                                    whereEqualTo("userId", currentUserId)
//                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                                            if (error != null) {
//
//                                            }
//                                            assert value != null;
//                                            if (!value.isEmpty()) {
//                                                // Getting all QueryDocSnapShots
//                                                for (QueryDocumentSnapshot snapshot : value) {
//                                                    Journal journalUser = new Journal();
//
//
//                                                    // Go to ListActivity after successful login
//
//                                                    // Let's display the List of journals after login
//                                                    //  startActivity(new Intent(MainActivity.this, AddJournalActivity.class));
//                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                                }
//                                            }
//                                        }
//                                    });
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // If Failed:
//                            Toast.makeText(LogIn.this,
//                                    "Something went wront " + e, Toast.LENGTH_LONG).show();
//                        }
//                    });
//
//        } else {
//            Toast.makeText(LogIn.this,
//                    "Please Enter email & password"
//                    , Toast.LENGTH_SHORT).show();
//        }


