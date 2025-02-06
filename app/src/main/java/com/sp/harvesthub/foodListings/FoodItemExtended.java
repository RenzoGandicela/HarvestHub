package com.sp.harvesthub.foodListings;

import com.sp.harvesthub.foodListings.FoodItem;

import java.util.List;

public class FoodItemExtended extends FoodItem {
    private String expirationDate;
    private String location;
    private String quantity;
    private boolean availability;

    public FoodItemExtended() {
        // Default constructor required for calls to DataSnapshot.getValue(FoodItemExtended.class)
    }

    public FoodItemExtended(String dishName, boolean isHalal, boolean isSpicy, List<String> ingredients,
                            String expirationDate, String location, String quantity, boolean availability) {
        super(dishName, isHalal, isSpicy, ingredients);
        this.expirationDate = expirationDate;
        this.location = location;
        this.quantity = quantity;
        this.availability = availability;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}