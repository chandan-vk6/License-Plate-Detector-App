package com.example.license_plate_detector;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.license_plate_detector.model.JournalRecyclerAdapter;
import com.example.license_plate_detector.model.LicensePlateData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.mastercoding.license_plate_detector.R;


import java.util.ArrayList;
import java.util.List;

import util.JournalUser;

public class LPDListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private StorageReference storageReference;
    private List<LicensePlateData> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;
    private CollectionReference collectionReference = db.collection("Users");
    private TextView noPostsEntry;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        // 1- Initializing Variables a nd References
        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // Widgets
        noPostsEntry = findViewById(R.id.list_no_posts);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Posts Arraylist
        journalList = new ArrayList<>();

    }

    // 2- Adding the Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                // Going to Add Journal Activity
                if (user != null && firebaseAuth != null){
                    startActivity(new Intent(
                            LPDListActivity.this,
                            LpdActivity.class
                    ));
                }
                break;

            case R.id.action_signout:
                // Signing out the user
                if (user != null && firebaseAuth != null){
                    firebaseAuth.signOut();

                    startActivity(new Intent(
                            LPDListActivity.this,
                            MainActivity.class
                    ));
                }
                break;

        }

        return super.onOptionsItemSelected(item);


    }

    // 3- Getting All Posts


    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", JournalUser.getInstance()
                .getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots){
                                LicensePlateData journal = journals.toObject(LicensePlateData.class);
                                journalList.add(journal);
                            }

                            // RecyclerView:
                            // Let's continue the JournalListActivity
                            journalRecyclerAdapter = new JournalRecyclerAdapter(
                                    LPDListActivity.this, journalList);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            journalRecyclerAdapter.notifyDataSetChanged();

                        }else{
                            noPostsEntry.setVisibility(View.VISIBLE);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Any Failure
                Toast.makeText(LPDListActivity.this, "Opps! Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}