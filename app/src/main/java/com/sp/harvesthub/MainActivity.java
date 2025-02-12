package com.sp.harvesthub;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.util.Log;
import android.widget.Button;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.sp.harvesthub.activities.LoginActivity;
import com.sp.harvesthub.activities.ChatActivity;
import com.sp.harvesthub.nav_fragment.AnnouncementFragment;
import com.sp.harvesthub.nav_fragment.BookmarkFragment;
import com.sp.harvesthub.nav_fragment.CalendarFragment;
import com.sp.harvesthub.nav_fragment.SettingFragment;
import com.sp.harvesthub.nav_fragment.SocialFragment;
import com.sp.harvesthub.nav_fragment.LogMealFragment;
import com.sp.harvesthub.nav_fragment.MapFragment;
import com.sp.harvesthub.nav_fragment.FavouritesFragment;
import com.sp.harvesthub.nav_fragment.ProfileFragment;
import com.sp.harvesthub.foodListings.FoodFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout;
    private NavigationView nav_view;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private TextView userNameTxt, emailTxt;
    private CircleImageView profileImg;
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

        // Load user data
        loadUserData();

        nav_view.setNavigationItemSelectedListener(this);

        // Initialize bottom navigation with updated listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // Clear drawer selection
            nav_view.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < nav_view.getMenu().size(); i++) {
                nav_view.getMenu().getItem(i).setChecked(false);
            }
            nav_view.getMenu().setGroupCheckable(0, true, true);
            
            if (itemId == R.id.nav_home) {
                replaceFragment(new FoodFragment());
                setTitle(getString(R.string.home));
            } else if (itemId == R.id.nav_map) {
                replaceFragment(new MapFragment());
                setTitle(getString(R.string.map));
            } else if (itemId == R.id.nav_add) {
                replaceFragment(new LogMealFragment());
                setTitle("Add Food");
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
        updateNavigationToHome();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String profilePicture = snapshot.child("profilePicture").getValue(String.class);

                        // Update UI
                        if (username != null) userNameTxt.setText(username);
                        if (email != null) emailTxt.setText(email);
                        
                        // Load profile picture
                        if (profilePicture != null && !profilePicture.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(profilePicture)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImg);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error loading user data: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        // Clear selection of navigation drawer
        nav_view.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < nav_view.getMenu().size(); i++) {
            nav_view.getMenu().getItem(i).setChecked(false);
        }
        nav_view.getMenu().setGroupCheckable(0, true, true);
        
        // Clear selection of bottom navigation if not a main navigation item
        if (!(fragment instanceof FoodFragment) && 
            !(fragment instanceof LogMealFragment) && 
            !(fragment instanceof MapFragment) && 
            !(fragment instanceof FavouritesFragment) && 
            !(fragment instanceof ProfileFragment)) {
            bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
        }
        
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Clear bottom navigation selection when using drawer
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        if (id == R.id.nav_home) {
            replaceFragment(new FoodFragment());
            setTitle(getString(R.string.home));
        } else if (id == R.id.nav_announcement) {
            replaceFragment(new AnnouncementFragment());
            setTitle(getString(R.string.announcement));
        } else if (id == R.id.nav_bookmark) {
            replaceFragment(new BookmarkFragment());
            setTitle(getString(R.string.bookmark));
        } else if (id == R.id.nav_calendar) {
            replaceFragment(new CalendarFragment());
            setTitle(getString(R.string.calendar));
        } else if (id == R.id.nav_setting) {
            replaceFragment(new SettingFragment());
            setTitle(getString(R.string.setting));
        } else if (id == R.id.nav_social) {
            replaceFragment(new SocialFragment());
            setTitle(getString(R.string.social));
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, getString(R.string.share), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_chat) {
            // Open ChatActivity
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateNavigationToHome() {
        // Set the home fragment as default
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        replaceFragment(new FoodFragment());
        setTitle(getString(R.string.home));
        
        // Clear drawer selection
        nav_view.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < nav_view.getMenu().size(); i++) {
            nav_view.getMenu().getItem(i).setChecked(false);
        }
        nav_view.getMenu().setGroupCheckable(0, true, true);
    }
}
