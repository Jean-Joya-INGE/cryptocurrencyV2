package co.edu.ue.cryptocurrency.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import co.edu.ue.cryptocurrency.R;
import co.edu.ue.cryptocurrency.data.network.ApiClient;
import co.edu.ue.cryptocurrency.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private Button btnLogout;
    private TextView tvCryptoPrices;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Inicializamos los elementos de la vista
        btnLogout = findViewById(R.id.btnLogout);
        tvCryptoPrices = findViewById(R.id.tvCryptoPrices);

        // Llamamos a la función para traer los precios de criptomonedas
        fetchCryptoPrices();

        // Botón de cerrar sesión
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish(); // Cerramos esta Activity
        });

        // Botón para ver el portafolio del usuario
        Button btnViewPortfolio = findViewById(R.id.btnViewPortfolio);
        btnViewPortfolio.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, PortfolioActivity.class);
            intent.putExtra("USER_ID", sessionManager.getUserId()); // Usamos el ID del usuario en sesión
            startActivity(intent);
        });
    }

    /**
     * Función para obtener precios de las principales criptomonedas
     */
    private void fetchCryptoPrices() {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1";

        // Ejecutamos la conexión en un hilo aparte para no bloquear la interfaz
        new Thread(() -> {
            try {
                // Pedimos los datos al servidor usando ApiClient
                String responseData = ApiClient.getCryptoData(url);

                // Actualizamos la UI en el hilo principal
                runOnUiThread(() -> processCryptoData(responseData));

            } catch (Exception e) {
                runOnUiThread(() -> Toast
                        .makeText(DashboardActivity.this, "Error al cargar precios", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Función para procesar el JSON recibido y mostrarlo en pantalla
     */
    private void processCryptoData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            StringBuilder cryptoPricesText = new StringBuilder("Precios Actuales:\n\n");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject crypto = jsonArray.getJSONObject(i);
                String name = crypto.getString("name");
                double price = crypto.getDouble("current_price");

                cryptoPricesText.append(name).append(": $").append(price).append("\n");
            }

            tvCryptoPrices.setText(cryptoPricesText.toString());

        } catch (Exception e) {
            Toast.makeText(this, "Error procesando datos", Toast.LENGTH_SHORT).show();
        }
    }
}
