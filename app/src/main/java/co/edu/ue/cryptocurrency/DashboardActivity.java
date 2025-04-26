package co.edu.ue.cryptocurrency;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
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
        // Modifica la URL para obtener los precios de las 10 principales criptomonedas
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1";

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
                    final String responseData = response.body() != null ? response.body().string() : "[]";
                    JSONArray jsonArray = new JSONArray(responseData);

                    runOnUiThread(() -> {
                        StringBuilder cryptoPricesText = new StringBuilder("Precios Actuales:\n\n");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject crypto = null;
                            try {
                                crypto = jsonArray.getJSONObject(i);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            String name = null;
                            try {
                                name = crypto.getString("name");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            double price = 0;
                            try {
                                price = crypto.getDouble("current_price");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                            cryptoPricesText.append(name)
                                    .append(": $")
                                    .append(price)
                                    .append("\n");
                        }

                        tvCryptoPrices.setText(cryptoPricesText.toString());
                    });
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error parsing JSON", e);
                    runOnUiThread(() ->
                            Toast.makeText(DashboardActivity.this, "Error procesando datos", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
