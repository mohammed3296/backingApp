package com.example.mohammedabdullah3296.bakingapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.R;
import com.example.mohammedabdullah3296.bakingapp.adapters.StepsAdapter;
import com.example.mohammedabdullah3296.bakingapp.models.Ingredient;
import com.example.mohammedabdullah3296.bakingapp.models.Recipe;
import com.example.mohammedabdullah3296.bakingapp.ui.IngredientsActivity;
import com.example.mohammedabdullah3296.bakingapp.ui.RecipeDetailsActivity;

import java.util.ArrayList;

public class StepsListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private StepsAdapter mStepestAdapter;
    OnStepClickListener mCallback;

    public interface OnStepClickListener {
        void OnStepClickListener(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (OnStepClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnStepClickListener");
        }
    }

    // Mandatory empty constructor
    public StepsListFragment() {
        Log.e(">>>>>" , "Hello mohammed !");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_master_list_recipe_details, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.detail_steps);
        StepsAdapter mAdapter = new StepsAdapter(getActivity(), RecipeDetailsActivity.steps);
        listView.setAdapter(mAdapter);
        Log.e(">>>>>>>>>>>>>>>>>>" , RecipeDetailsActivity.steps.get(0).toString());
        // Set a click listener on the gridView and trigger the callback onImageSelected when an item is clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Trigger the callback method and pass in the position that was clicked
                mCallback.OnStepClickListener(position);
            }
        });

        TextView ingredients_id_text = (TextView) rootView.findViewById(R.id.ingredients_id_text);
        ingredients_id_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                ArrayList<Ingredient> selectedRecipe = new ArrayList<>();
                selectedRecipe.addAll(RecipeDetailsActivity.ingredients);
                bundle.putParcelableArrayList("ingredients787878", selectedRecipe);

                final Intent recipeDetails = new Intent(getContext(), IngredientsActivity.class);
                recipeDetails.putExtras(bundle);
                startActivity(recipeDetails);
            }
        });
        // Return the root view
        return rootView;
    }
}
