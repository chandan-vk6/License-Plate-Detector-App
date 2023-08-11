package com.example.license_plate_detector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.license_plate_detector.model.Adapter;
import com.example.license_plate_detector.model.Counter;
import com.example.license_plate_detector.model.LicensePlateData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.Utils;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.mastercoding.license_plate_detector.R;
import com.mastercoding.license_plate_detector.model.Adapter;
import com.mastercoding.license_plate_detector.model.Counter;
import com.mastercoding.license_plate_detector.model.LicensePlateData;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import util.JournalUser;

public class LpdActivity extends AppCompatActivity {
    private Adapter adapter;
    private RecyclerView recyle;

    private ArrayList<LicensePlateData> list = new ArrayList<LicensePlateData>();
    public static final int PICK_IMAGE = 123;
    private static final int CAMERA_CAPTURE = 456;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 789;

    private AlertDialog dialog;
    InputImage inputImage;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private String currentUserId;
    private String currentUserName;
    ImageView orginal;

    TextRecognizer recognizer;
    Boolean val = false;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference reference = firestore.collection("Users");
    TextView textView;
    LicensePlateData licensedata;
    ArrayList<LicensePlateData> results = new ArrayList<LicensePlateData>();
    MaterialButton image, speech;

    Handler handler = new Handler();
    int index = 0;
    String[] licenseplatenumber;
    TextToSpeech t;
    int count_index;
    public Bitmap textImage;
    LinearLayout imageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lpd);
        recyle = findViewById(R.id.recyle);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        image = findViewById(R.id.images);
        orginal = findViewById(R.id.original_image_view);
        // imageContainer = findViewById(R.id.imageContainer);
        textView = findViewById(R.id.text);
        speech = findViewById(R.id.speech);

        adapter = new Adapter(this, list, LpdActivity.this);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getApplicationContext());
        recyle.setLayoutManager(layout);
        recyle.setAdapter(adapter);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LpdActivity.this);
                builder.setTitle("Choose Image Source")
                        .setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    openGallery();
                                } else if (i == 1) {
                                    openCamera();
                                }
                            }
                        });
                dialog = builder.create();
                dialog.show();
            }
        });


        t = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t.setLanguage(Locale.US);
//                    t.setLanguage(new Locale("bn_BD"));
                }
            }
        });

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               for(LicensePlateData data: results){
                   String speech = data.getCaseNumber().toString() + data.getLicensePlateNumber().toString();
                t.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
            }}
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                // Going to Add Journal Activity
                if (currentUser != null && firebaseAuth != null){
                    startActivity(new Intent(
                            LpdActivity.this,
                            LPDListActivity.class
                    ));
                }
                break;

            case R.id.action_signout:
                // Signing out the user
                if (currentUser != null && firebaseAuth != null){
                    firebaseAuth.signOut();

                    startActivity(new Intent(
                            LpdActivity.this,
                            MainActivity.class
                    ));
                }
                break;

        }

        return super.onOptionsItemSelected(item);


    }

    private void generateUniqueCaseNumber(Bitmap cropped, String imagedata) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference counterRef = firestore.collection("counters").document("caseNumberCounter");

        counterRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Long counter = documentSnapshot.getLong("value");

                if (counter == null) {
                    // Counter does not exist, initialize it
                    counter = 1L;
                } else {
                    // Increment the counter
                    counter++;
                }

                // Generate the case number using the counter value
                String caseNumber = "CASE" + counter;

                // Save the updated counter value to Firestore
                counterRef.set(new Counter(counter))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Counter value updated successfully
                                // Use the generated case number as needed
                                displayCroppedImageAndText(cropped, imagedata, caseNumber);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error occurred while updating the counter
                                // Handle the failure
                            }
                        });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {

                    Bitmap imageBitmap;
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    LicensePlateDetector licensePlateDetector = new LicensePlateDetector(LpdActivity.this);
                    MatOfRect plates = licensePlateDetector.detectLicensePlates(imageBitmap);


                    if (plates.empty()) {
                        Toast.makeText(this, "could not crop and detect license plate", Toast.LENGTH_LONG).show();
                        //inputImage = InputImage.fromBitmap(imageBitmap, 0);
                        orginal.setImageBitmap(imageBitmap);
                       // performOCR();
                    } else {
                        count_index = plates.toArray().length;
                       // Toast.makeText(this,  "count_index " +String.valueOf(count_index).toString(), Toast.LENGTH_SHORT).show();
                        licenseplatenumber = new String[count_index];
                        Mat imageMat = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC4);
                        Utils.bitmapToMat(imageBitmap, imageMat);
                        Scalar color = new Scalar(0, 255, 255);
                        int thickness = 5;
                        for (org.opencv.core.Rect plateRect : plates.toArray()) {
                            // Draw the bounding box on the image
                            Imgproc.rectangle(imageMat, plateRect.tl(), plateRect.br(), color, thickness);
                            index++;
                            //Toast.makeText(this, String.valueOf(index) ,Toast.LENGTH_SHORT).show();
                            // Crop the license plate region
                            Mat licensePlate = new Mat(imageMat, plateRect);

                            // Convert the cropped region back to bitmap
                            Bitmap croppedBitmap = Bitmap.createBitmap(licensePlate.cols(), licensePlate.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(licensePlate, croppedBitmap);
                            inputImage = InputImage.fromBitmap(croppedBitmap, 0);
                            performOCR(inputImage);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();
                            String imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);


                            //   Toast.makeText(this, licenseplatenumber, Toast.LENGTH_SHORT).show();
                            generateUniqueCaseNumber(croppedBitmap, imageBase64);
                            // reference.add(licensedata);

                        }
                        Bitmap org_img = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(imageMat, org_img);
                        orginal.setImageBitmap(org_img);
                        for (LicensePlateData val : results) {
                            reference.add(val)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // Data added successfully
                                            // Handle any additional actions after saving the data
                                            Toast.makeText(LpdActivity.this, "License plate data saved", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Error occurred while saving the data
                                            // Handle the failure
                                            Toast.makeText(LpdActivity.this, "Failed to save license plate data", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }

                    }
                }
            }
        }


     else if(requestCode ==CAMERA_CAPTURE &&resultCode ==RESULT_OK)

    {
        if (data != null && data.getExtras() != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {

                LicensePlateDetector licensePlateDetector = new LicensePlateDetector(LpdActivity.this);
                MatOfRect plates = licensePlateDetector.detectLicensePlates(imageBitmap);


                if (plates.empty()) {
                    Toast.makeText(this, "could not crop and detect license plate", Toast.LENGTH_LONG).show();
                    //inputImage = InputImage.fromBitmap(imageBitmap, 0);
                    orginal.setImageBitmap(imageBitmap);
                    // performOCR();
                } else {
                    count_index = plates.toArray().length;
                    licenseplatenumber = new String[count_index];
                    Mat imageMat = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC4);
                    Utils.bitmapToMat(imageBitmap, imageMat);
                    Scalar color = new Scalar(0, 255, 255);
                    int thickness = 5;
                    for (org.opencv.core.Rect plateRect : plates.toArray()) {
                        // Draw the bounding box on the image
                        Imgproc.rectangle(imageMat, plateRect.tl(), plateRect.br(), color, thickness);
                        index++;
                        //Toast.makeText(this, String.valueOf(index) ,Toast.LENGTH_SHORT).show();
                        // Crop the license plate region
                        Mat licensePlate = new Mat(imageMat, plateRect);

                        // Convert the cropped region back to bitmap
                        Bitmap croppedBitmap = Bitmap.createBitmap(licensePlate.cols(), licensePlate.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(licensePlate, croppedBitmap);
                        inputImage = InputImage.fromBitmap(croppedBitmap, 0);
                        performOCR(inputImage);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageData = baos.toByteArray();
                        String imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);


                        //   Toast.makeText(this, licenseplatenumber, Toast.LENGTH_SHORT).show();
                        generateUniqueCaseNumber(croppedBitmap, imageBase64);
                        // reference.add(licensedata);

                    }
                    Bitmap org_img = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(imageMat, org_img);
                    orginal.setImageBitmap(org_img);
                    for (LicensePlateData val : results) {
                        reference.add(val)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Data added successfully
                                        // Handle any additional actions after saving the data
                                        Toast.makeText(LpdActivity.this, "License plate data saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error occurred while saving the data
                                        // Handle the failure
                                        Toast.makeText(LpdActivity.this, "Failed to save license plate data", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                }
            }
        }
    }

//        for(LicensePlateData val : results){
//            reference.add(val);
//        }

}



            // Perform the Firestore data saving operation here



    private void displayCroppedImageAndText(Bitmap croppedBitmap,String imageBase64,  String casenumber) {

            // Create a new LinearLayout to hold the image and text
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_licenseplate, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);
            TextView tex = view.findViewById(R.id.textVS2);
            ImageView img_view = view.findViewById(R.id.image1);
            EditText et1 = view.findViewById(R.id.et1);
            EditText et2 = view.findViewById(R.id.et2);
            img_view.setImageBitmap(croppedBitmap);
            et1.setText(casenumber);
            Toast.makeText(this,  "in here " +String.valueOf(index), Toast.LENGTH_SHORT).show();
           // index--;
            String number = licenseplatenumber[index + count_index - 1];
            et2.setText(number);
            count_index--;
            if (JournalUser.getInstance() != null) {
                currentUserName = JournalUser.getInstance().getUsername();
                currentUserId = JournalUser.getInstance().getUserId();
            }

            // Toast.makeText(this, String.valueOf(index-1), Toast.LENGTH_SHORT).show();
        builder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Save the cropped image and extracted text
                    // Implement your save logic here
                    licensedata = new LicensePlateData(number, imageBase64, casenumber, currentUserId, currentUserName, new Timestamp(new Date()));
                    reference.add(licensedata)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Data added successfully
                                    // Handle any additional actions after saving the data
                                    Toast.makeText(LpdActivity.this, "License plate data saved", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error occurred while saving the data
                                    // Handle the failure
                                    Toast.makeText(LpdActivity.this, "Failed to save license plate data", Toast.LENGTH_SHORT).show();
                                }
                            });

                    //  reference.add(licensedata);
                   // val = true;
                   list.add(0, licensedata);

                  results.add(licensedata);

                   adapter.notifyDataSetChanged();
                   // dialog.cancel();
                    // saveLicensePlateData(licensePlateData);
                }
            })
            .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Remove the cropped image and extracted text
                    // Implement your remove logic here
                 //   val = false;
                   // dialog.cancel();
                }
            });

            // Show the AlertDialog
             dialog = builder.create();
            dialog.show();

    }


    public void editandsave(LicensePlateData licensedata, int position){

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_licenseplate,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        TextView tex = view.findViewById(R.id.textVS2);
        ImageView img_view  = view.findViewById(R.id.image1);
        EditText et1 = view.findViewById(R.id.et1);
        EditText et2 = view.findViewById(R.id.et2);
        byte[] imageData = Base64.decode(licensedata.getImageBase64(), Base64.DEFAULT);

        img_view.setImageBitmap( BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
        et1.setText(licensedata.getCaseNumber());
        //String number = licenseplatenumber[index + count_index -1 ];
        et2.setText(licensedata.getLicensePlateNumber());


        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the cropped image and extracted text
                // Implement your remove logic here
               //
                handleDeleteButtonClick(licensedata, position);
                dialog.dismiss();
            }
        });

        // Show the AlertDialog
        dialog = builder.create();
        dialog.show();

    }



    public void handleDeleteButtonClick(LicensePlateData licensedata, int position) {
             list.remove(position);
            // Retrieve the license plate data associated with the delete button
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");
            collectionReference.whereEqualTo("userId", JournalUser.getInstance().getUserId())
                    .whereEqualTo("caseNumber", licensedata.getCaseNumber())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                DocumentReference documentReference = documentSnapshot.getReference();
                                documentReference.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Document successfully deleted
                                                // Handle any additional actions after deletion
                                                Toast.makeText(LpdActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // An error occurred while deleting the document
                                                Toast.makeText(LpdActivity.this,
                                                        "Failed to delete document: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Any Failure
                            Toast.makeText(LpdActivity.this,
                                    "Oops! Something went wrong!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        adapter.notifyDataSetChanged();
        }




    private void performOCR(InputImage inputImage) {

        Task<Text> result = recognizer.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        Toast.makeText(LpdActivity.this, "you enterd processtextbloxk()", Toast.LENGTH_LONG).show();
                         processTextBlock(visionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // Handle the failure
                        Toast.makeText(LpdActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                       System.out.println("error " + e.toString());
                    }
                });
       // Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,PICK_IMAGE);
    }



    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera(){

        if (checkCameraPermission()) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_CAPTURE);

        } else {
            requestCameraPermission();
        }

    }

    private void processTextBlock(Text result) {
        // [START mlkit_process_text_block]
        String data = "";
        String resultText = result.getText();
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
          //  Toast.makeText(this, blockText, Toast.LENGTH_SHORT).show();
           // textView.append("\n");
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                //
                //  textView.append("\n");
                Point[] lineCornerPoints = line.getCornerPoints();

                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements()) {
                 //   textView.append(" ");
                    String elementText = element.getText();
                    data +=elementText;
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }

            }

        }

        Toast.makeText(this, "before " +String.valueOf(index), Toast.LENGTH_LONG).show();
        System.out.println(index);
        licenseplatenumber[index-1] =  data;
        index--;



       Toast.makeText(this,"after " + String.valueOf(index).toString(), Toast.LENGTH_LONG).show();
       // System.out.println("ocr- "+licenseplatenumber[]index);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!t.isSpeaking()){
            super.onPause();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }





    // Check camera permission
    private boolean checkCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



        }




