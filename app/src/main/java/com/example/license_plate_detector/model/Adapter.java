package com.example.license_plate_detector.model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mastercoding.license_plate_detector.LpdActivity;
import com.mastercoding.license_plate_detector.R;

import java.util.ArrayList;


public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference reference = firestore.collection("Users");
Context context;
ArrayList<LicensePlateData> licenseplatelist ;
LpdActivity mainActivity;

    public Adapter(Context context, ArrayList<LicensePlateData> contactlist, LpdActivity mainActivity) {
        this.context = context;
        this.licenseplatelist = contactlist;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
          LicensePlateData data = licenseplatelist.get(position);
             // reference.add(data);
          holder.case_number.setText(data.getCaseNumber());
          holder.license_number.setText(data.getLicensePlateNumber());
         if (data.getImageBase64() != null) {
             byte[] imageData = Base64.decode(data.getImageBase64(), Base64.DEFAULT);
             holder.img.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
         }


          holder.itemView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  mainActivity.editandsave(data,position);
              }
          });
    }

    @Override
    public int getItemCount() {
        return licenseplatelist.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
     private final TextView case_number;
     private final TextView license_number;
     private final ImageView img;




    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        this.case_number = itemView.findViewById(R.id.tx1);
        this.license_number = itemView.findViewById(R.id.tx2);
        this.img = itemView.findViewById(R.id.imgbt);
    }
}



}
