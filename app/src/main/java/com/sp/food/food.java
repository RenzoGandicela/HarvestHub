package com.sp.food;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class food extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // UI Elements
        TextView resultTextView = findViewById(R.id.messageTextView);
        Button getButton = findViewById(R.id.getButton);

        // Volley RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // AstraDB REST API URL
        String astraDbUrl = "https://d0ffc4ba-e487-4bba-8059-132cc73f7ad6-eu-west-1.apps.astra.datastax.com/api/rest/v2/keyspaces/food/sgfood/rows";

        // Action for the Get Button
        getButton.setOnClickListener(v -> {
            // Create a GET request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    astraDbUrl,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Parse the response
                                JSONArray rows = response.getJSONArray("data");
                                StringBuilder result = new StringBuilder();
                                for (int i = 0; i < 1/*rows.length()*/; i++) {
                                    JSONObject row = rows.getJSONObject(i);
                                    result.append("Food: ").append(row.getString("Food"))
                                            .append(", Type: ").append(row.getString("Type"))
                                            .append(", Cuisine: ").append(row.getString("Cuisine"))
                                            .append("\n");
                                }
                                resultTextView.setText(result.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                resultTextView.setText("Error parsing data.");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            resultTextView.setText("Error fetching data: " + error.getMessage());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("x-cassandra-token", "AstraCS:MmMloKFGdiqYoqYPQcFECNsy:538fd1c71ed384be28a1888c07783c8f05b1e81da7e1439f14a6094709e7e714");
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            requestQueue.add(jsonObjectRequest);
        });
    }
}
