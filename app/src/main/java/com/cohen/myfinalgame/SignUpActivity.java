package com.cohen.myfinalgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button signUpButton, backButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    private AlertDialog noInternetDialog;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // --- אתחול UI ו־Firebase ---
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail    = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signUpButton     = findViewById(R.id.signUpButton);
        backButton       = findViewById(R.id.backButton);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // לחיצה על הרשמה
        signUpButton.setOnClickListener(v -> signUpUser());

        // כפתור חזרה
        backButton.setOnClickListener(v -> {
            // חוזרים למסך הראשי (או ל־LoginActivity אם תרצה)
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signUpUser() {
        String username = editTextUsername.getText().toString().trim();
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // בדיקת שדות
        if (!isValidInput(username, email, password)) return;

        // בדיקה: האם יש אינטרנט?
        if (!isInternetAvailable()) {
            showNoInternetDialog();
            return;
        }

        // יצירת משתמש בפיירבייס
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        showAlert("שגיאה", "משתמש לא זוהה.");
                        return;
                    }

                    // עדכון הפרופיל עם שם המשתמש
                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                    user.updateProfile(profileUpdates)
                            .addOnSuccessListener(aVoid -> {
                                // שמירת המשתמש בבסיס הנתונים
                                HashMap<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                databaseRef.child(user.getUid()).setValue(userData);

                                // מעבר למסך הראשי
                                navigateToMain();
                            })
                            .addOnFailureListener(e -> {
                                if (e instanceof FirebaseNetworkException || !isInternetAvailable()) {
                                    showNoInternetDialog();
                                } else {
                                    showAlert("שגיאת עדכון פרופיל", e.getLocalizedMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || !isInternetAvailable()) {
                        showNoInternetDialog();
                    }
                    else if (e instanceof FirebaseAuthWeakPasswordException) {
                        showAlert("סיסמה חלשה", "הסיסמה חייבת להכיל לפחות 6 תווים.");
                    }
                    else if (e instanceof FirebaseAuthUserCollisionException) {
                        showAlert("משתמש קיים", "כתובת המייל כבר נמצאת בשימוש.");
                    }
                    else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        showAlert("כתובת מייל לא חוקית", "בדוק את כתובת המייל ונסה שוב.");
                    }
                    else {
                        showAlert("ההרשמה נכשלה", e.getLocalizedMessage());
                    }
                });
    }

    private boolean isValidInput(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("יש להזין שם משתמש");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("כתובת מייל לא חוקית");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTextPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            return false;
        }
        return true;
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnectedOrConnecting();
        }
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;

        noInternetDialog = new AlertDialog.Builder(this)
                .setTitle("אין חיבור לאינטרנט")
                .setMessage("לא ניתן להירשם ללא אינטרנט. בדוק את החיבור ונסה שוב.")
                .setCancelable(true)
                .setPositiveButton("אישור", (d, w) -> d.dismiss())
                .show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("אישור", null)
                .show();
    }

    private void navigateToMain() {
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(
                networkChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    // --- Receiver לשינויים ברשת ---
    public class NetworkChangeReceiver extends android.content.BroadcastReceiver {
        private AlertDialog dialog;

        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (!isInternetAvailable()) {
                if (dialog == null || !dialog.isShowing()) {
                    dialog = new AlertDialog.Builder(ctx)
                            .setTitle("אין חיבור לאינטרנט")
                            .setMessage("החיבור נותק – חלק מהפונקציות לא יהיו זמינות.")
                            .setCancelable(true)
                            .setPositiveButton("הבנתי", (d, w) -> d.dismiss())
                            .show();
                }
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }
}
