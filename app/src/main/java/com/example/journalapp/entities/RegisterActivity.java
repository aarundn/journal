package com.example.journalapp.entities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.journalapp.R;
import com.example.journalapp.utilities.Constants;
import com.example.journalapp.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import Utils.JournalUser;

public class RegisterActivity extends AppCompatActivity {
    ImageView backButton;
    EditText emailR, passwordR,confirmPassR,username ;
    Button registerBtn;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferencesManager preferencesManager;
    private CollectionReference collectionReference = db.collection(Constants.KEY_COLLECTIONS_USER);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        backButton = findViewById(R.id.imageBack);
        emailR = findViewById(R.id.inputEmailR);
        passwordR = findViewById(R.id.passWordR);
        confirmPassR = findViewById(R.id.inputConfirmPasswordR);
        registerBtn = findViewById(R.id.registerBtn);
        username = findViewById(R.id.inputNameR);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LogIn.class);
                startActivity(intent);
            }
        });

        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null){

                }
                else {

                }
            }
        };
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(emailR.getText().toString())
                        && !TextUtils.isEmpty(passwordR.getText().toString())
                        && !TextUtils.isEmpty(passwordR.getText().toString())
                        && passwordR.getText().toString().equals(confirmPassR.getText().toString())
                        && !TextUtils.isEmpty(username.getText().toString())){

                    String email = emailR.getText().toString().trim();
                    String passWord = passwordR.getText().toString().trim();
                    String confirmPass = confirmPassR.getText().toString().trim();
                    String name = username.getText().toString().trim();
                    CreateAccount(email, passWord, name);

                    startActivity(new Intent(RegisterActivity.this, LogIn.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Empty Fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    private void CreateAccount(String email, String passWord, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email,passWord).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                currentUser = firebaseAuth.getCurrentUser();

                assert currentUser != null;
                final String currentUserId = currentUser.getUid();

                Map<String , String> userObj = new HashMap<>();
                userObj.put(Constants.KEY_USER_ID, currentUserId);
                userObj.put(Constants.KEY_NAME, name);
                userObj.put(Constants.KEY_PASSWORD, passWord);

                collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult().exists()){
                                    String name = task.getResult().getString(Constants.KEY_NAME);
                                    preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                    JournalUser journalUser = JournalUser.getInstance();
                                    journalUser.setUserId(currentUserId);
                                    journalUser.setUsername(name);
                                    preferencesManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.putExtra(Constants.KEY_NAME,name);
                                    intent.putExtra(Constants.KEY_USER_ID,currentUserId);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "something wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}