package com.example.mohammedabdullah3296.bakingapp.loders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.example.mohammedabdullah3296.bakingapp.models.Recipe;
import com.example.mohammedabdullah3296.bakingapp.queries.RecipeQueryUtils;

import android.util.Log;


import java.util.List;

/**
 * Created by Mohammed Abdullah on 10/1/2017.
 */


public class RecipeLoader extends AsyncTaskLoader<List<Recipe>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = RecipeLoader.class.getName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Constructs a new {@link RecipeLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public RecipeLoader(Context context, String url) {
        super(context);
        mUrl = url;
        Log.w(LOG_TAG, "RecipeLoader consrructor");
    }

    @Override
    protected void onStartLoading() {
        Log.w(LOG_TAG, "onStartLoading forceLoad");
        forceLoad();

    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Recipe> loadInBackground() {
        Log.w(LOG_TAG, "loadInBackground loadInBackground");

        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of Recipes.
        List<Recipe> Recipes = RecipeQueryUtils.fetchRecipeData(mUrl);
        return Recipes;
    }
}
