package com.sp.harvesthub.foodListings;

import java.io.Serializable;
import java.util.List;

public class FoodItem implements Serializable {
    private String dishName;
    private boolean halal;
    private boolean spicy;
    private List<String> ingredients;
    private String imageURL;

    public FoodItem() {}
    public FoodItem(String dishName, boolean halal, boolean spicy, List<String> ingredients) {
        this(dishName, halal, spicy, ingredients, null);
    }

    public FoodItem(String dishName, boolean halal, boolean spicy, List<String> ingredients, String imageURL) {
        this.dishName = dishName;
        this.halal = halal;
        this.spicy = spicy;
        this.ingredients = ingredients;
        this.imageURL = imageURL;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public boolean isHalal() {
        return halal;
    }

    public boolean isSpicy() {
        return spicy;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}