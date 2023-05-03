package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {

    private Button buttonEnglish;
    private Button buttonHindi;
    private Button buttonMarathi;
    // Add more language buttons here as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        buttonEnglish = findViewById(R.id.buttonEnglish);
        buttonHindi = findViewById(R.id.buttonHindi);
        buttonMarathi = findViewById(R.id.buttonMarathi);
        // Initialize more language buttons here as needed

        buttonEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivityWithLanguage("English");
            }
        });

        buttonHindi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivityWithLanguage("Hindi");
            }
        });

        buttonMarathi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivityWithLanguage("Marathi");
            }
        });

        // Set click listeners for more language buttons here as needed
    }

    private void startMainActivityWithLanguage(String language) {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        intent.putExtra("language", language);
        startActivity(intent);
    }
}
