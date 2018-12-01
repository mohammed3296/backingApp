package com.example.mohammedabdullah3296.bakingapp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.adapters.RecipesAdapter;
import com.example.mohammedabdullah3296.bakingapp.interfaces.ListItemClickListener;
import com.example.mohammedabdullah3296.bakingapp.loders.RecipeLoader;
import com.example.mohammedabdullah3296.bakingapp.models.Ingredient;
import com.example.mohammedabdullah3296.bakingapp.models.Recipe;

import java.util.List;

import static com.example.mohammedabdullah3296.bakingapp.queries.RecipeQueryUtils.LOG_TAG;

/**
 * The configuration screen for the {@link RecipesWidget RecipesWidget} AppWidget.
 */
public class RecipesWidgetConfigureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Recipe>>, ListItemClickListener {

    private RecyclerView mRecyclerView;
    private RecipesAdapter mRecipestAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    private static final String url = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";
    private static final int RECIPE_LOADER_ID = 1;

    private static final String PREFS_NAME = "com.example.mohammedabdullah3296.bakingapp.RecipesWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public RecipesWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.recipes_widget_configure);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recipes);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.empty_view);


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);
        mRecipestAdapter = new RecipesAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mRecipestAdapter);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        callRecipesLoderAsyncTask();

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }

    @Override
    public Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        return new RecipeLoader(this, url);
    }

    @Override
    public void onLoadFinished(Loader<List<Recipe>> loader, List<Recipe> data) {

        mLoadingIndicator.setVisibility(View.GONE);
        mRecipestAdapter.data.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        if (data != null && !data.isEmpty()) {
            showRecipeDataView();
            mRecipestAdapter.setRecipeData(data, this);
        } else {
            mErrorMessageDisplay.setText("No Recipes");
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Recipe>> loader) {

        Log.w(LOG_TAG, "onLoaderReset");
// Loader reset, so we can clear out our existing data.
        mRecipestAdapter.data.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(Recipe clickedItemIndex) {
        final Context context = RecipesWidgetConfigureActivity.this;

        // When the button is clicked, store the string locally
        //String widgetText = mAppWidgetText.getText().toString();
        String widgetText = "" ;
        List<Ingredient> ingredients = clickedItemIndex.getIngredients();
        for(int i = 0 ; i < ingredients.size() ; i++){
            widgetText += ingredients.get(i).toString() + "\n";
        }
        saveTitlePref(context, mAppWidgetId, widgetText);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RecipesWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();

    }
    public void callRecipesLoderAsyncTask() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(RECIPE_LOADER_ID, null, this);
        } else {
            mLoadingIndicator.setVisibility(View.GONE);
            mErrorMessageDisplay.setText("No internet Connection");
        }}
    private void showRecipeDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }
}

