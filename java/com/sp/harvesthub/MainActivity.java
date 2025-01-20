package com.sp.harvesthub;

import android.os.Bundle;
import android.view.MenuItem;
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
import com.sp.harvesthub.nav_fragment.AnnouncementFragment;
import com.sp.harvesthub.nav_fragment.BookmarkFragment;
import com.sp.harvesthub.nav_fragment.CalendarFragment;
import com.sp.harvesthub.nav_fragment.SettingFragment;
import com.sp.harvesthub.nav_fragment.SocialFragment;
import com.sp.harvesthub.nav_fragment.HomeFragment;
import com.sp.harvesthub.nav_fragment.MapFragment;
import com.sp.harvesthub.nav_fragment.FavouritesFragment;
import com.sp.harvesthub.nav_fragment.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onBackPressedMethod();
        }
    };

    private void onBackPressedMethod() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up header view
        TextView userNameTxt = navigationView.getHeaderView(0).findViewById(R.id.userNameTxt);
        TextView emailTxt = navigationView.getHeaderView(0).findViewById(R.id.emailTxt);
        ImageView profileImg = navigationView.getHeaderView(0).findViewById(R.id.profileImg);

        navigationView.setNavigationItemSelectedListener(this);

        // Initialize bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.nav_map) {
                replaceFragment(new MapFragment());
            } else if (itemId == R.id.nav_favorites) {
                replaceFragment(new FavouritesFragment());
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
            }
            
            return true;
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navFragment, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_setting) {
            replaceFragment(new SettingFragment());
            setTitle(getString(R.string.setting));
        } else if (itemId == R.id.nav_announcement) {
            replaceFragment(new AnnouncementFragment());
            setTitle(getString(R.string.announcement));
        } else if (itemId == R.id.nav_bookmark) {
            replaceFragment(new BookmarkFragment());
            setTitle(getString(R.string.bookmark));
        } else if (itemId == R.id.nav_calendar) {
            replaceFragment(new CalendarFragment());
            setTitle(getString(R.string.calendar));
        } else if (itemId == R.id.nav_social) {
            replaceFragment(new SocialFragment());
            setTitle(getString(R.string.social));
        } else if (itemId == R.id.nav_share) {
            Toast.makeText(this, getString(R.string.share), Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, getString(R.string.logout), Toast.LENGTH_LONG).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
