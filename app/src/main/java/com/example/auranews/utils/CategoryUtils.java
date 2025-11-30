package com.example.auranews.utils;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class CategoryUtils {

    public interface CategoryCallback {
        void onCategorySelected(String category);
    }

    public static void setupCategorySpinner(Context context,
                                            Spinner spinner,
                                            String[] categories,
                                            CategoryCallback callback) {
        ArrayAdapter<String> adapterSpinner =
                new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);
        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null) callback.onCategorySelected(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
