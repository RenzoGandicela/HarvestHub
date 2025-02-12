package com.sp.harvesthub.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.sp.harvesthub.foodListings.FoodItemExtended;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookmarks.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_DISH_NAME = "dish_name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_SELLER_ID = "seller_id";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_IS_HALAL = "is_halal";
    public static final String COLUMN_IS_SPICY = "is_spicy";
    public static final String COLUMN_LIKES = "likes";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT NOT NULL, " +
                    COLUMN_ITEM_ID + " TEXT NOT NULL, " +
                    COLUMN_DISH_NAME + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_EXPIRY_DATE + " TEXT, " +
                    COLUMN_QUANTITY + " TEXT, " +
                    COLUMN_SELLER_ID + " TEXT, " +
                    COLUMN_INGREDIENTS + " TEXT, " +
                    COLUMN_IS_HALAL + " INTEGER, " +
                    COLUMN_IS_SPICY + " INTEGER, " +
                    COLUMN_LIKES + " INTEGER, " +
                    COLUMN_CREATED_AT + " TEXT, " +
                    "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_ITEM_ID + "))";

    private final FirebaseAuth auth;

    public BookmarkDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // For existing tables, add the user_id column
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKMARKS +
                        " ADD COLUMN " + COLUMN_USER_ID + " TEXT");

                // If there are existing bookmarks, you might want to associate them with a default user
                // or clear them out
                String currentUserId = getCurrentUserId();
                if (currentUserId != null) {
                    db.execSQL("UPDATE " + TABLE_BOOKMARKS +
                                    " SET " + COLUMN_USER_ID + " = ?",
                            new String[]{currentUserId});
                } else {
                    // If no user is logged in, might as well clear the table
                    db.execSQL("DELETE FROM " + TABLE_BOOKMARKS);
                }
            } catch (Exception e) {
                // If anything goes wrong during upgrade, recreate the table
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
                onCreate(db);
            }
        }
    }

    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public long addBookmark(FoodItemExtended foodItem) {
        String userId = getCurrentUserId();
        if (userId == null) return -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ITEM_ID, foodItem.getItemId());
        values.put(COLUMN_DISH_NAME, foodItem.getDishName());
        values.put(COLUMN_DESCRIPTION, foodItem.getDescription());
        values.put(COLUMN_IMAGE_URL, foodItem.getImageUrl());
        values.put(COLUMN_LOCATION, foodItem.getLocation());
        values.put(COLUMN_EXPIRY_DATE, foodItem.getExpirationDate());
        values.put(COLUMN_QUANTITY, foodItem.getQuantity());
        values.put(COLUMN_SELLER_ID, foodItem.getSellerId());
        values.put(COLUMN_INGREDIENTS, String.join(",", foodItem.getIngredients()));
        values.put(COLUMN_IS_HALAL, foodItem.isHalal() ? 1 : 0);
        values.put(COLUMN_IS_SPICY, foodItem.isSpicy() ? 1 : 0);
        values.put(COLUMN_LIKES, foodItem.getLikesCount());
        values.put(COLUMN_CREATED_AT, foodItem.getCreatedAt());

        return db.insertWithOnConflict(TABLE_BOOKMARKS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeBookmark(String itemId) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKMARKS,
                COLUMN_ITEM_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{itemId, userId});
    }

    public boolean isBookmarked(String itemId) {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKMARKS, new String[]{COLUMN_ITEM_ID},
                COLUMN_ITEM_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{itemId, userId},
                null, null, null);
        boolean isBookmarked = cursor.getCount() > 0;
        cursor.close();
        return isBookmarked;
    }

    public List<FoodItemExtended> getAllBookmarks() {
        String userId = getCurrentUserId();
        if (userId == null) return new ArrayList<>();

        List<FoodItemExtended> bookmarks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKMARKS, null,
                COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                FoodItemExtended item = new FoodItemExtended();
                item.setItemId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
                item.setDishName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_NAME)));
                item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                item.setExpirationDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE)));
                item.setQuantity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)));
                item.setSellerId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID)));

                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
                item.setIngredients(Arrays.asList(ingredients.split(",")));

                item.setHalal(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_HALAL)) == 1);
                item.setSpicy(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SPICY)) == 1);
                item.setLikesCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)));
                item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));

                bookmarks.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarks;
    }
}