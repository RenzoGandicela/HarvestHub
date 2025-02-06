package com.sp.harvesthub;

import android.content.Intent;
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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private TextView userNameTxt, emailTxt;
    private ImageView profileImg;
    private FirebaseAuth auth;

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

        // Initialize bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                replaceFragment(new LogMealFragment());
                setTitle(getString(R.string.home));
            } else if (itemId == R.id.nav_map) {
                replaceFragment(new MapFragment());
                setTitle(getString(R.string.map));
            } else if (itemId == R.id.nav_favorites) {
                replaceFragment(new FavouritesFragment());
                setTitle(getString(R.string.favorites));
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                setTitle(getString(R.string.profile));
            }
            
            return true;
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LogMealFragment()).commit();
        } else if (id == R.id.nav_announcement) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AnnouncementFragment()).commit();
        } else if (id == R.id.nav_bookmark) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BookmarkFragment()).commit();
        } else if (id == R.id.nav_calendar) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalendarFragment()).commit();
        } else if (id == R.id.nav_setting) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingFragment()).commit();
        } else if (id == R.id.nav_social) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SocialFragment()).commit();
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
