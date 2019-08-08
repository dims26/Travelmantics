package com.dims.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by DIMS on 04/08/2019 for Travelmantics app.
 */
public class RecyclerDealAdapter extends RecyclerView.Adapter<RecyclerDealAdapter.DealViewHolder> {
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ImageView mImageViewDeal;

    public RecyclerDealAdapter(){
        mFirebaseDatabase = FirebaseUtils.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtils.mDatabaseReference;
        deals = FirebaseUtils.mDeals;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //convert the pulled data to TravelDeal class and add it too the ArrayList
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal title:", travelDeal.getTitle());
                travelDeal.setId(dataSnapshot.getKey());
                deals.add(travelDeal);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);

    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.recycler_view_item, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPrice;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            mImageViewDeal = itemView.findViewById(R.id.imageViewDeal);
            itemView.setOnClickListener(this);
        }
        //Bind deal information to a recycler item
        public void bind(TravelDeal deal){
            textViewTitle.setText(deal.getTitle());
            textViewDescription.setText(deal.getDescription());
            textViewPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d("Click", String.valueOf(pos));
            TravelDeal selectedDeal = deals.get(pos);
            Intent intent = new Intent(v.getContext(), DealActivity.class);
            intent.putExtra("selectedDeal", selectedDeal);
            v.getContext().startActivity(intent);
        }

        private void showImage(String url){
            if (url != null && !url.isEmpty()){
                Picasso.get()
                        .load(url)
                        .resize(80,80)
                        .centerCrop()
                        .into(mImageViewDeal);
            }
        }
    }
}
