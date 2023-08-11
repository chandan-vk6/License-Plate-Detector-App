package com.example.license_plate_detector;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LicensePlateDetector {
    CascadeClassifier plateCascade;
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error if OpenCV is not loaded
        }
    }

    public  LicensePlateDetector(Context context) {
        // Convert the input bitmap to Mat (OpenCV) format
         plateCascade = loadPlateCascade(context);


    }


    public MatOfRect detectLicensePlates(Bitmap inputBitmap) {
        Mat imageMat = new Mat(inputBitmap.getHeight(), inputBitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(inputBitmap, imageMat);

        // Convert the image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(imageMat, gray, Imgproc.COLOR_RGBA2GRAY);

        // Load the cascade classifier for license plate detection


        // Detect license plates using the cascade classifier
        MatOfRect plates = new MatOfRect();
        plateCascade.detectMultiScale(gray, plates, 1.1, 3, 0, new Size(30, 30), new Size());

        // Draw bounding boxes around detected license plates
//          Scalar color = new Scalar(0, 255, 0);
//          int thickness = 2;
//           for (Rect plateRect : plates.toArray()) {
//           // Draw the bounding box on the image
//           Imgproc.rectangle(imageMat, plateRect.tl(), plateRect.br(), color, thickness);
//
//           // Crop the license plate region
//            Mat licensePlate = new Mat(imageMat, plateRect);
//
//            // Convert the cropped region back to bitmap
//            Bitmap croppedBitmap = Bitmap.createBitmap(licensePlate.cols(), licensePlate.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(licensePlate, croppedBitmap);
//
//          }

        return plates;
    }

    private static CascadeClassifier loadPlateCascade(Context context) {
        try {
            // Load the cascade classifier XML file from the assets folder
            InputStream is = context.getAssets().open("haarcascade_russian_plate_number.xml");
            File cascadeFile = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeXmlFile = new File(cascadeFile, "haarcascade_russian_plate_number.xml");
            FileOutputStream os = new FileOutputStream(cascadeXmlFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier from the XML file
            CascadeClassifier cascadeClassifier = new CascadeClassifier(cascadeXmlFile.getAbsolutePath());

            // Delete the temporary XML file
            cascadeXmlFile.delete();

            return cascadeClassifier;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

