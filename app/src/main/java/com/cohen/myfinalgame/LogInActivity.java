package com.cohen.myfinalgame;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Check if fields are empty
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LogInActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Retrieve saved credentials from SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
                    String savedUsername = sharedPreferences.getString("username", "");
                    String savedPassword = sharedPreferences.getString("password", "");

                    // Validate the entered username and password
                    if (username.equals(savedUsername) && password.equals(savedPassword)) {
                        // If valid, redirect to MainActivity (GameActivity)
                        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If invalid, show error
                        Toast.makeText(LogInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}