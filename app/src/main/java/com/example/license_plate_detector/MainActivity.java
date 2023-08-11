package com.example.license_plate_detector;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mastercoding.license_plate_detector.R;

import util.JournalUser;

public class MainActivity extends AppCompatActivity {

    // Widgets
    Button loginBTN;
    Button createAccBTN;
    private EditText emailET;
    private EditText passET;


    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent i = new Intent(MainActivity.this, LpdActivity.class);
//         startActivity(i);


        loginBTN = findViewById(R.id.email_sign_in_button);
        createAccBTN = findViewById(R.id.create_acct_BTN);
        emailET = findViewById(R.id.email);
        passET  = findViewById(R.id.password);



        // Forget to initialize the Auth Ref
        firebaseAuth = FirebaseAuth.getInstance();


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null){
                    // User Already Logged IN

                }else{
                    // No user yet!
                }
            }
        };





        createAccBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });


        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginEmailPasswordUser(
                        emailET.getText().toString().trim(),
                        passET.getText().toString().trim()
                );
            }
        });

    }

    private void LoginEmailPasswordUser(String email, String pwd) {
        // Checking for empty texts
        // Checking for empty texts
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    final String currentUserId = user.getUid();

                                    collectionReference
                                            .whereEqualTo("userId", currentUserId)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        // Assuming there's only one matching document
                                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                                        String username = documentSnapshot.getString("userName");

                                                        // Set the user ID and username in JournalUser singleton
                                                        JournalUser.getInstance().setUserId(currentUserId);
                                                        JournalUser.getInstance().setUsername(username);

                                                        // Start LPDActivity
                                                        startActivity(new Intent(MainActivity.this, LPDListActivity.class));
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // If failed to retrieve user data
                                                    Toast.makeText(MainActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                // If authentication failed
                                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // If Failed:
                            Toast.makeText(MainActivity.this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Please enter email & password", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}