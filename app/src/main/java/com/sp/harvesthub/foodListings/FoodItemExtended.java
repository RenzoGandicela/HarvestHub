package com.sp.harvesthub.foodListings;

import com.sp.harvesthub.foodListings.FoodItem;

import java.io.Serializable;
import java.util.List;

public class FoodItemExtended extends FoodItem implements Serializable {
    private String expirationDate;
    private String location;
    private String quantity;
    private boolean availability;
    private String sellerId;
    private String status = "available";
    private String createdAt;
    private String updatedAt;
    private String description;
    private String imageUrl;
    private String itemId;
    private int likesCount;
    private String originalSellerId;

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
        this.status = availability ? "available" : "unavailable";
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

    public void setQuantity(Object quantityObj) {
        if (quantityObj instanceof Long) {
            this.quantity = String.valueOf(quantityObj);
        } else if (quantityObj instanceof String) {
            this.quantity = (String) quantityObj;
        } else {
            this.quantity = "0";
        }
    }

    public String getQuantity() {
        return quantity;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
        this.status = availability ? "available" : "unavailable";
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.availability = "available".equals(status);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getOriginalSellerId() {
        return originalSellerId;
    }

    public void setOriginalSellerId(String originalSellerId) {
        this.originalSellerId = originalSellerId;
    }
}