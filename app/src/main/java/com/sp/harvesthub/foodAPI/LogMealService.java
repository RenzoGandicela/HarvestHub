package com.sp.harvesthub.foodAPI;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogMealService {
    private static final String API_URL = "https://api.logmeal.com/v2/image/recognition/complete";
    private static final String API_KEY = "ce5a3d19a9f7c7736261cc1e3295f2f1f079bf37"; // Replace with actual API key

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void analyzeFoodImage(File imageFile, ApiCallback callback) {
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("LogMealService", "Response received: " + responseBody);
                    callback.onSuccess(formatApiResponse(responseBody));
                } else {
                    Log.e("LogMealService", "API call failed. Response code: " + response.code());
                    callback.onFailure("Error: " + response.code());
                }
            } catch (IOException e) {
                Log.e("LogMealService", "Error in API call: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            }
        });
    }

    private String formatApiResponse(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray foodArray = jsonObject.getAsJsonArray("recognition_results");

        StringBuilder formattedResult = new StringBuilder();
        formattedResult.append("Detected Food:\n");

        List<String> detectedDishes = new ArrayList<>();
        List<String> otherPossibleFoods = new ArrayList<>();
        Set<String> detectedIngredients = new HashSet<>(); // Store ingredients only

        for (int i = 0; i < foodArray.size(); i++) {
            JsonObject foodItem = foodArray.get(i).getAsJsonObject();
            String foodName = foodItem.get("name").getAsString();
            double probability = foodItem.get("prob").getAsDouble() * 100;

            // ✅ Store detected dishes
            if (i == 0) {
                detectedDishes.add(foodName + " (" + String.format("%.2f", probability) + "% probability)");
            } else {
                otherPossibleFoods.add(foodName + " (" + String.format("%.2f", probability) + "% probability)");
            }
        }

        // ✅ Format detected food
        for (int i = 0; i < detectedDishes.size(); i++) {
            formattedResult.append((i + 1)).append(". ").append(detectedDishes.get(i)).append("\n");
        }

        // ✅ Format other possible foods
        if (!otherPossibleFoods.isEmpty()) {
            formattedResult.append("\nOther Possible Food:\n");
            for (int i = 0; i < otherPossibleFoods.size(); i++) {
                formattedResult.append((i + 1)).append(". ").append(otherPossibleFoods.get(i)).append("\n");
            }
        }

        return formattedResult.toString();
    }
}