package co.edu.ue.cryptocurrency.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.cryptocurrency.data.db.DatabaseHelper;
import co.edu.ue.cryptocurrency.utils.HashUtils;
import co.edu.ue.cryptocurrency.utils.SessionManager;
import co.edu.ue.cryptocurrency.R;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoToRegister;
    DatabaseHelper db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Verificar si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        this.begin();
        this.loginAPP();
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    private void loginAPP() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                String hashedPassword = HashUtils.sha256(password);
                boolean exists = db.checkUser(email, hashedPassword);

                if (exists) {
                    int userId = db.getUserId(email);
                    if (userId != -1) {
                        sessionManager.createLoginSession(userId);
                        Toast.makeText(MainActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void begin() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
    }
}
