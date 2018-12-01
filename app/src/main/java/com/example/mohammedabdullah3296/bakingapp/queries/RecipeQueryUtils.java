package com.example.mohammedabdullah3296.bakingapp.queries;

import android.text.TextUtils;
import android.util.Log;

import com.example.mohammedabdullah3296.bakingapp.models.Ingredient;
import com.example.mohammedabdullah3296.bakingapp.models.Recipe;
import com.example.mohammedabdullah3296.bakingapp.models.Step;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohammed Abdullah on 9/10/2017.
 */

public final class RecipeQueryUtils {
    public static final String LOG_TAG = RecipeQueryUtils.class.getSimpleName();

    private RecipeQueryUtils() {
    }

    public static List<Recipe> fetchRecipeData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Recipe} object
        List<Recipe> Recipes = extractFeatureFromJson(jsonResponse);

        // Return the {@link Recipe}
        return Recipes;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Recipe JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an {@link Recipe} object by parsing out information
     * about the first Recipe from the input RecipeJSON string.
     */
    private static List<Recipe> extractFeatureFromJson(String RecipeJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(RecipeJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding Recipes to
        List<Recipe> Recipes = new ArrayList<>();
        try {
            // Create a JSONOArray from the JSON response string
            JSONArray baseJsonResponse = new JSONArray(RecipeJSON);
            if (baseJsonResponse.length() != 0) {
                for (int i = 0; i < baseJsonResponse.length(); i++) {
                    List<Ingredient> ingredientsList = new ArrayList<>();
                    List<Step> stepsList = new ArrayList<>();
                    // Get a single Recipe at position i within the list of Recipes
                    JSONObject currentRecipe = baseJsonResponse.getJSONObject(i);
                    int recipeId = currentRecipe.getInt("id");
                    String recipeName = currentRecipe.getString("name");
                    JSONArray ingredients = currentRecipe.getJSONArray("ingredients");
                    if (ingredients.length() != 0) {
                        for (int k = 0; k < ingredients.length(); k++) {
                            JSONObject currentIngredient = ingredients.getJSONObject(k);
                            double quantity = currentIngredient.getDouble("quantity");
                            String measure = currentIngredient.getString("measure");
                            String ingredientElement = currentIngredient.getString("ingredient");
                            Ingredient ingredient1 = new Ingredient(quantity, measure, ingredientElement);
                            Log.i("OBGECT" + i, ingredient1.toString());
                            // Add the new {@link Recipe} to the list of Recipes.
                            ingredientsList.add(ingredient1);
                        }
                    }

                    JSONArray steps = currentRecipe.getJSONArray("steps");
                    if (steps.length() != 0) {
                        for (int k = 0; k < steps.length(); k++) {
                            JSONObject currentStep = steps.getJSONObject(k);
                            int stepId = currentStep.getInt("id");
                            String shortDescription = currentStep.getString("shortDescription");
                            String description = currentStep.getString("description");
                            String videoURL = currentStep.getString("videoURL");
                            String thumbnailURL = currentStep.getString("thumbnailURL");
                            Step step1 = new Step(stepId, shortDescription, description, videoURL, thumbnailURL);
                            Log.i("OBGECT" + i, step1.toString());
                            // Add the new {@link Recipe} to the list of Recipes.
                            stepsList.add(step1);
                        }
                    }
                    int servings = currentRecipe.getInt("servings");
                    String image = currentRecipe.getString("image");
                    Recipe recipe = new Recipe(recipeId, recipeName, ingredientsList, stepsList, servings, image);
                    Log.i("OBGECT" + i, recipe.toString());
                    // Add the new {@link Recipe} to the list of Recipes.
                    Recipes.add(recipe);
                }
            } else {
                return null;
            }
        } catch (JSONException e)

        {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("RecipeQueryUtils", "Problem parsing the Recipe JSON results", e);
        }

        // Return the list of Recipes
        return Recipes;
    }
}