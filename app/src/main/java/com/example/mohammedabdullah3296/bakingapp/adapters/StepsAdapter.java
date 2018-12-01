package com.example.mohammedabdullah3296.bakingapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mohammedabdullah3296.bakingapp.R;
import com.example.mohammedabdullah3296.bakingapp.models.Step;

import java.util.Collections;
import java.util.List;

/**
 * Created by Mohammed Abdullah on 11/27/2017.
 */

public class StepsAdapter extends ArrayAdapter<Step> {
    private static final String LOG_TAG = StepsAdapter.class.getSimpleName();
    private Context mContext;
    public List<Step> data = Collections.emptyList();
    Step current;

    public StepsAdapter(Activity context, List<Step> steps) {
        super(context, 0, steps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.step_item, parent, false);
        }
        Step currentStep = getItem(position);
        TextView stepshortDescription = (TextView) listItemView.findViewById(R.id.step_shortDescription_oooooooo);
        stepshortDescription.setText(currentStep.getShortDescription());
        return listItemView;
    }
}