package co.edu.ue.cryptocurrency.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.cryptocurrency.data.db.DatabaseHelper;
import co.edu.ue.cryptocurrency.utils.HashUtils;
import co.edu.ue.cryptocurrency.utils.SessionManager;
import co.edu.ue.cryptocurrency.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private DatabaseHelper db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Depuración del email
            Log.d("RegisterActivity", "Email a validar: '" + email + "'");
            boolean isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
            Log.d("RegisterActivity", "¿Es email válido?: " + isValidEmail);

            // Verificación manual alternativa del formato de correo
            boolean hasAtSign = email.contains("@");
            boolean hasDomain = false;
            if (hasAtSign) {
                String[] parts = email.split("@");
                if (parts.length > 1) {
                    hasDomain = parts[1].contains(".");
                }
            }
            Log.d("RegisterActivity", "¿Tiene @?: " + hasAtSign + ", ¿Tiene dominio válido?: " + hasDomain);

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail) {
                etEmail.setError("Email inválido");
                String errorMsg = "Formato de email incorrecto. Debe contener @ y un dominio válido (ejemplo: usuario@dominio.com)";
                if (!hasAtSign) {
                    errorMsg = "El email debe contener el símbolo @";
                } else if (!hasDomain) {
                    errorMsg = "El email debe tener un dominio válido después del @ (ejemplo: dominio.com)";
                }
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("La contraseña debe tener al menos 6 caracteres");
                return;
            }

            try {
                String hashedPassword = HashUtils.sha256(password);

                if (db.isEmailExists(email)) {
                    Toast.makeText(RegisterActivity.this, "El email ya está registrado", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean insert = db.registerUser(username, email, hashedPassword);
                if (insert) {
                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    int userId = db.getUserId(email);
                    Log.d("RegisterActivity", "ID de usuario obtenido: " + userId);

                    if (userId != -1) {
                        sessionManager.createLoginSession(userId);
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Log.e("RegisterActivity", "No se pudo obtener el ID del usuario recién registrado");
                        Toast.makeText(RegisterActivity.this, "Error al iniciar sesión, intente nuevamente",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("RegisterActivity", "Falla al insertar en la base de datos");
                    Toast.makeText(RegisterActivity.this,
                            "Error al registrar. Verifique que el correo no esté ya registrado.", Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception e) {
                Log.e("RegisterActivity", "Error en registro", e);
                Toast.makeText(RegisterActivity.this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}