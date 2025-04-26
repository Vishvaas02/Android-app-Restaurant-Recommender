package com.shravyakothapalli.finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

public class Adapter extends androidx.recyclerview.widget.RecyclerView.Adapter<Adapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    Data[] data;
    Context context;

    Adapter(Data[] data, Context context){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @androidx.annotation.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.card_view,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Data dataList = data[i];
        viewHolder.placeName.setText(dataList.getPlaceName());
        viewHolder.placeDesc.setText(dataList.getPlaceDesc());
        viewHolder.placeImage.setImageBitmap(dataList.getPlaceImage());
        viewHolder.placeRating.setRating(dataList.getPlaceRating());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),MapsActivity.class);
                i.putExtra("Place ID", dataList.getPlaceID());
                i.putExtra("Lat", dataList.getLat());
                i.putExtra("Lon", dataList.getLon());
                i.putExtra("Place Name", dataList.getPlaceName());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder{
        TextView placeName;
        TextView placeDesc;
        ImageView placeImage;
        RatingBar placeRating;
        public ViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            placeName = itemView.findViewById(R.id.placeName);
            placeDesc = itemView.findViewById(R.id.placeDesc);
            placeImage = itemView.findViewById(R.id.imageView);
            placeRating = itemView.findViewById(R.id.rating);
        }
    }
}