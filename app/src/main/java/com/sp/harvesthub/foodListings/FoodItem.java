package com.sp.harvesthub.foodListings;

import java.io.Serializable;
import java.util.List;

public class FoodItem implements Serializable {
    private String dishName;
    private boolean halal;
    private boolean spicy;
    private List<String> ingredients;
    private String imageURL;
    private String location;

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

    public void setHalal(boolean halal) {
        this.halal = halal;
    }

    public boolean isSpicy() {
        return spicy;
    }

    public void setSpicy(boolean spicy) {
        this.spicy = spicy;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}