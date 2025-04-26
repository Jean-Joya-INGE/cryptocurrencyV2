package co.edu.ue.cryptocurrency;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private Button btnLogout;
    private TextView tvCryptoPrices;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnLogout = findViewById(R.id.btnLogout);
        tvCryptoPrices = findViewById(R.id.tvCryptoPrices);

        client = new OkHttpClient();

        fetchCryptoPrices();

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        });
    }

    private void fetchCryptoPrices() {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana&vs_currencies=usd";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Error al obtener datos: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(DashboardActivity.this, "Error al cargar precios", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(DashboardActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    final String responseData = response.body() != null ? response.body().string() : "{}";
                    JSONObject json = new JSONObject(responseData);

                    runOnUiThread(() -> {
                        try {
                            double btcPrice = json.getJSONObject("bitcoin").getDouble("usd");
                            double ethPrice = json.getJSONObject("ethereum").getDouble("usd");
                            double solPrice = json.getJSONObject("solana").getDouble("usd");

                            String text = "Precios Actuales:\n\n" +
                                    "Bitcoin (BTC): $" + btcPrice + "\n" +
                                    "Ethereum (ETH): $" + ethPrice + "\n" +
                                    "Solana (SOL): $" + solPrice;

                            tvCryptoPrices.setText(text);
                        } catch (Exception e) {
                            Log.e("DashboardActivity", "Error parsing JSON", e);
                            tvCryptoPrices.setText("Error al mostrar precios");
                        }
                    });
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error processing response", e);
                    runOnUiThread(() ->
                            Toast.makeText(DashboardActivity.this, "Error procesando datos", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}