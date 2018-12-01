package com.example.mohammedabdullah3296.bakingapp.ui;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.R;
import com.example.mohammedabdullah3296.bakingapp.models.Ingredient;

import java.util.ArrayList;

import static android.R.attr.data;

public class IngredientsActivity extends AppCompatActivity {
private ArrayList<Ingredient> ingredients ;
    TextView textView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        textView = (TextView)findViewById(R.id.inininininininin);
        Bundle data = getIntent().getExtras();
        ingredients  = data.getParcelableArrayList("ingredients787878");
        Log.e("ccvfc" , ingredients.get(0).toString());
        for(int i = 0 ; i < ingredients.size() ; i++){
            textView.append(ingredients.get(i).toString() + "\n\n");
        }
    }
}
