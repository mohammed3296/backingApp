package com.example.mohammedabdullah3296.bakingapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.R;
import com.example.mohammedabdullah3296.bakingapp.interfaces.ListItemClickListener;
import com.example.mohammedabdullah3296.bakingapp.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by Mohammed Abdullah on 11/27/2017.
 */

public class RecipesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater inflater;
    public List<Recipe> data = Collections.emptyList();
    Recipe current;
    int currentPos = 0;
    final private ListItemClickListener lOnClickListener;

    public RecipesAdapter(ListItemClickListener listener) {
        lOnClickListener = listener;
    }

    public void setRecipeData(List<Recipe> recipesIn, Context context) {
        data = recipesIn;
        mContext = context;
        notifyDataSetChanged();
    }

    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.recipe_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, parent, false);
        MyHolder viewHolder = new MyHolder(view);

        return viewHolder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        current = data.get(position);
        String imageUrl = current.getImage().toString();
        if (imageUrl != "") {
            Uri builtUri = Uri.parse(imageUrl).buildUpon().build();
            Picasso.with(mContext).load(builtUri).placeholder(R.drawable.sdf).into(((MyHolder) holder).recipeImage);
        }

        myHolder.recipeName.setText(current.getName().toString());
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView recipeName;
        ImageView recipeImage;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            recipeName = (TextView) itemView.findViewById(R.id.recipeName);
            recipeImage = (ImageView) itemView.findViewById(R.id.recipeImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            lOnClickListener.onListItemClick(data.get(clickedPosition));
        }
    }
}