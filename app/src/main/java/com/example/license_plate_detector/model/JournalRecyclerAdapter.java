package com.example.license_plate_detector.model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mastercoding.license_plate_detector.R;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<LicensePlateData> journalList;


    public JournalRecyclerAdapter(Context context, List<LicensePlateData> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, viewGroup, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LicensePlateData journal = journalList.get(position);
        String imageUrl;

        holder.title.setText(journal.getCaseNumber());
        holder.thoughts.setText(journal.getLicensePlateNumber());
       // holder.name.setText(journal.getUserName());
        if (journal.getImageBase64() != null) {
            byte[] imageData = Base64.decode(journal.getImageBase64(), Base64.DEFAULT);
            holder.image.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
        }
        if (journal.getImageBase64() != null) {
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                journal.getTimeAdded()
                .getSeconds()*1000);
        holder.dateAdded.setText(timeAgo);
}
        /**
         *  Using Glide Library to Display the images
         * */

//        Glide.with(context)
//                .load(imageUrl)
//                //.placeholder()
//                .fitCenter()
//                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }


    // View Holder:
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title, thoughts, dateAdded, name;
        public ImageView image;
        public ImageView shareButton;
        String userId;
        String username;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            // These widgets: Belongs to Journal_row.xml

            // Let's create this row layout
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);

            image = itemView.findViewById(R.id.journal_image_list);



        }
    }




}
