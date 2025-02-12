package com.sp.harvesthub.foodAPI;

public interface ApiCallback {
    void onSuccess(String result);
    void onFailure(String error);
}