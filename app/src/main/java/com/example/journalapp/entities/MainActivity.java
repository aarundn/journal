package com.example.journalapp.entities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.journalapp.R;
import com.example.journalapp.adapter.JournalAdapter;
import com.example.journalapp.model.Journal;
import com.example.journalapp.utilities.Constants;
import com.example.journalapp.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import Utils.JournalUser;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton addNoteButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private ArrayList<Journal> journals;
    private JournalAdapter journalAdapter;
    private PreferencesManager preferencesManager;
    private CollectionReference collectionReference = db.collection(Constants.KEY_COLLECTIONS_JOURNALS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        addNoteButton = findViewById(R.id.addNoteButton);
        recyclerView = findViewById(R.id.rcv1);
        journals = new ArrayList<>();
        preferencesManager = new PreferencesManager(getApplicationContext());

        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddJournal.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.SignOut){
            logout();
            ToastMessage("Signing Out...");
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = collectionReference.getId();

                String userId1 = preferencesManager.getString(Constants.KEY_USER_ID);
                // Now you have the userId, you can use it wherever you need
                // For example, you can use it to query the journals specific to this user
                if (userId1 != null){
                    collectionReference.whereEqualTo(Constants.KEY_USER_ID,userId1 )
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    for (QueryDocumentSnapshot journal : queryDocumentSnapshots){
                                        Journal journal1 = journal.toObject(Journal.class);
                                        journals.add(journal1);
                                    }
                                    journalAdapter = new JournalAdapter(MainActivity.this, journals);
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setAdapter(journalAdapter);
                                    recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
                                    journalAdapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> ToastMessage(e.getMessage()));
                } else {
                    // Handle the case where there's no logged-in user
                }
                }

        }
    void ToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void logout() {

        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
        preferencesManager.clear();
        Intent intent = new Intent(getApplicationContext(), LogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

