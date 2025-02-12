package com.sp.harvesthub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.sp.harvesthub.activities.LoginActivity;
import com.sp.harvesthub.nav_fragment.AnnouncementFragment;
import com.sp.harvesthub.nav_fragment.BookmarkFragment;
import com.sp.harvesthub.nav_fragment.CalendarFragment;
import com.sp.harvesthub.nav_fragment.SettingFragment;
import com.sp.harvesthub.nav_fragment.SocialFragment;
import com.sp.harvesthub.nav_fragment.MapFragment;
import com.sp.harvesthub.nav_fragment.FavouritesFragment;
import com.sp.harvesthub.nav_fragment.ProfileFragment;
import com.sp.harvesthub.nav_fragment.LogMealFragment;
import com.sp.harvesthub.foodListings.FoodFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private TextView userNameTxt, emailTxt;
    private ImageView profileImg;
    private FirebaseAuth auth;
    private NavigationUpdateReceiver navigationReceiver;

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onBackPressedMethod();
        }
    };

    private void onBackPressedMethod() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    public void updateNavigationToHome() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        auth = FirebaseAuth.getInstance();
        
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Initialize views
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up header view
        View headerView = nav_view.getHeaderView(0);
        userNameTxt = headerView.findViewById(R.id.userNameTxt);
        emailTxt = headerView.findViewById(R.id.emailTxt);
        profileImg = headerView.findViewById(R.id.profileImg);

        nav_view.setNavigationItemSelectedListener(this);

        // Set default fragment to FoodFragment after login
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FoodFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new FoodFragment();
                setTitle(getString(R.string.home));
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
                setTitle(getString(R.string.map));
            } else if (itemId == R.id.nav_add) {
                selectedFragment = new LogMealFragment();
                setTitle("Add Listing");
            } else if (itemId == R.id.nav_favorites) {
                selectedFragment = new FavouritesFragment();
                setTitle(getString(R.string.favorites));
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                setTitle(getString(R.string.profile));
            } else if (itemId == R.id.nav_announcement) {
                selectedFragment = new AnnouncementFragment();
                setTitle(getString(R.string.announcement));
            } else if (itemId == R.id.nav_bookmark) {
                selectedFragment = new BookmarkFragment();
                setTitle(getString(R.string.bookmark));
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
                setTitle(getString(R.string.calendar));
            } else if (itemId == R.id.nav_setting) {
                selectedFragment = new SettingFragment();
                setTitle(getString(R.string.setting));
            } else if (itemId == R.id.nav_social) {
                selectedFragment = new SocialFragment();
                setTitle(getString(R.string.social));
            } else if (itemId == R.id.nav_share) {
                Toast.makeText(this, getString(R.string.share), Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_logout) {
                auth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default selected item to home
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Initialize and register the receiver
        navigationReceiver = new NavigationUpdateReceiver();
        registerReceiver(navigationReceiver, 
            new IntentFilter("com.sp.harvesthub.SELECT_HOME"),
            Context.RECEIVER_NOT_EXPORTED);

        // Check user role and update menu visibility
        checkUserRoleAndUpdateMenu();
    }

    private void checkUserRoleAndUpdateMenu() {
        if (auth.getCurrentUser() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("Users")
                    .child(auth.getCurrentUser().getUid());
            
            userRef.child("role").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String role = task.getResult().getValue(String.class);

                    // Hide add listing option for non-sellers
                    MenuItem addItem = bottomNavigationView.getMenu().findItem(R.id.nav_add);
                    if (addItem != null) {
                        boolean isSeller = "seller".equalsIgnoreCase(role);
                        addItem.setVisible(isSeller);
                        
                        // If current fragment is LogMealFragment and user is not a seller,
                        // switch to home fragment
                        if (!isSeller && bottomNavigationView.getSelectedItemId() == R.id.nav_add) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check role again when activity resumes
        checkUserRoleAndUpdateMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigationReceiver != null) {
            unregisterReceiver(navigationReceiver);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FoodFragment())
                    .commit();
        } else if (id == R.id.nav_announcement) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AnnouncementFragment())
                    .commit();
        } else if (id == R.id.nav_bookmark) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookmarkFragment())
                    .commit();
        } else if (id == R.id.nav_calendar) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CalendarFragment())
                    .commit();
        } else if (id == R.id.nav_setting) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingFragment())
                    .commit();
        } else if (id == R.id.nav_social) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SocialFragment())
                    .commit();
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, getString(R.string.share), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
