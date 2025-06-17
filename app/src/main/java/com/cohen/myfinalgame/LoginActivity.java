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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button loginButton;
    private TextView signUpText;
    private FirebaseAuth auth;
    private AlertDialog noInternetDialog;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // יוזר-אינפוט
        editTextEmail    = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginButton      = findViewById(R.id.loginButton);
        signUpText       = findViewById(R.id.signUpText);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // כפתור התחברות
        loginButton.setOnClickListener(v -> loginUser());

        // ניווט לרישום
        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        // אם כבר מחובר
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) navigateToMain();
    }

    private void loginUser() {
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // אימות תווים
        if (!isValidInput(email, password)) return;

        // בדיקת זמינות רשת
        if (!isInternetAvailable()) {
            showNoInternetDialog();
            return;
        }

        // ניסיון התחברות
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // התחבר בהצלחה
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    // אם אין רשת
                    if (e instanceof FirebaseNetworkException || !isInternetAvailable()) {
                        showNoInternetDialog();
                    }
                    // אם סיסמה/אימייל שגויים
                    else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        showAlert("ההתחברות נכשלה", "בדוק את כתובת האימייל והסיסמה ונסה שוב.");
                    }
                    // שגיאה אחרת
                    else {
                        showAlert("שגיאה", e.getLocalizedMessage());
                    }
                });
    }

    private boolean isValidInput(String email, String password) {
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
                .setMessage("לא ניתן להתחבר ללא אינטרנט. בדוק את החיבור ונסה שנית.")
                .setCancelable(true)
                .setPositiveButton("אישור", (d, w) -> d.dismiss())
                .show();
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("אישור", null)
                .show();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
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

    /** מקשיב לשינויים ברשת בזמן אמת **/
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
