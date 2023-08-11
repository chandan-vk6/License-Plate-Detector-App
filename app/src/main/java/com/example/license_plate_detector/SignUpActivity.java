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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mastercoding.license_plate_detector.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalUser;

public class SignUpActivity extends AppCompatActivity {

    EditText password_create ;
    EditText username_create;
    EditText email_create;
    Button createBTN;


    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Firebase Auth require Google Account on the device to run successfully


        // Congratz!!
        //  Creating Users and logging into AddJournalActivity


        createBTN = findViewById(R.id.acc_sign_up_button);
        password_create = findViewById(R.id.password_create);
        email_create = findViewById(R.id.email_create);
        username_create = findViewById(R.id.userName_create_ET);

        // Authentication
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

        createBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(email_create.getText().toString())
                        && !TextUtils.isEmpty(username_create.getText().toString())
                         && !TextUtils.isEmpty(password_create.getText().toString())){

                    String email = email_create.getText().toString().trim();
                    String password = password_create.getText().toString().trim();
                    String username = username_create.getText().toString().trim();

                    CreateUserEmailAccount(email,password,username);
                }else{
                    Toast.makeText(SignUpActivity.this,
                            "Empty Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void CreateUserEmailAccount(String email, String password, final String username) {
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // we take user to Next Activity: (AddJournalActivity)
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                // Create a userMap so we can create a user in the User Collection in Firestore
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("userName", username);


                                //Adding Users to Firestore
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    String name = task.getResult().getString("userName");


                                                                    JournalUser journalUser = JournalUser.getInstance();
                                                                    journalUser.setUserId(currentUserId);
                                                                    journalUser.setUsername(name);


                                                                    Intent i = new Intent(SignUpActivity.this,
                                                                            LPDListActivity.class);

                                                                    i.putExtra("userName", name);
                                                                    i.putExtra("userId", currentUserId);
                                                                    startActivity(i);
                                                                } else {
                                                                    Toast.makeText(SignUpActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Display Failed Message
                                                        Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });


                            }
                        }
                    });
        }


    }


    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}