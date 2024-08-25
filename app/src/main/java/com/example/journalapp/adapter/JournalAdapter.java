package com.example.journalapp.adapter;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journalapp.R;
import com.example.journalapp.model.Journal;
import com.google.firebase.Timestamp;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolderAdapter> {


        private Context context;
        private ArrayList<Journal> journalArrayList;

        public JournalAdapter(Context context, ArrayList<Journal> journalArrayList) {
            this.context = context;
            this.journalArrayList = journalArrayList;
        }

        @NonNull
        @Override
        public JournalViewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent,false);

            return new JournalViewHolderAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JournalViewHolderAdapter holder, int position) {
            Journal journal = journalArrayList.get(position);

            holder.noteTitle.setText(journal.getTitle());
            holder.noteDesc.setText(journal.getDescription());
            String imageUrl = journal.getImageUrl();
            ; // Assuming journal.getTimesAdd() returns a Timestamp object
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                    journal.getTimesAdd().getSeconds()*1000
            );
            holder.noteDate.setText(timeAgo);

            Glide.with(context).load(imageUrl)
                    .fitCenter()
                    .into(holder.noteImage);
        }

        @Override
        public int getItemCount() {
            return journalArrayList.size();
        }




        public static class JournalViewHolderAdapter extends RecyclerView.ViewHolder {
            TextView noteTitle, noteDesc, noteDate;
            RoundedImageView noteImage;
            public String userId, username;
            public JournalViewHolderAdapter(@NonNull View itemView) {
                super(itemView);
                noteTitle = itemView.findViewById(R.id.noteTitleTv);
                noteDesc = itemView.findViewById(R.id.noteDescTv);
                noteImage = itemView.findViewById(R.id.imageNoteTv);
                noteDate = itemView.findViewById(R.id.noteDateTv);
            }


        }


}
