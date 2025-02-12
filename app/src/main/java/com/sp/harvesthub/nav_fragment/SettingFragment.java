package com.sp.harvesthub.nav_fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sp.harvesthub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "HarvestHubPrefs";
    private static final String THEME_PREF = "theme_preference";
    private static final String NOTIFICATIONS_PREF = "notifications_preference";

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Theme Switch
        SwitchMaterial themeSwitch = view.findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(isDarkThemeEnabled());
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
            // Apply theme without recreating the activity
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            // Apply the theme to the current activity without recreation
            AppCompatActivity activity = (AppCompatActivity) requireActivity();
            activity.getDelegate().applyDayNight();
        });

        // Notifications Switch
        SwitchMaterial notificationSwitch = view.findViewById(R.id.notificationSwitch);
        notificationSwitch.setChecked(isNotificationsEnabled());
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationsPreference(isChecked);
        });

        return view;
    }

    private boolean isDarkThemeEnabled() {
        return sharedPreferences.getBoolean(THEME_PREF, false);
    }

    private void saveThemePreference(boolean isDarkTheme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(THEME_PREF, isDarkTheme);
        editor.apply();
    }

    private boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(NOTIFICATIONS_PREF, true);
    }

    private void saveNotificationsPreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOTIFICATIONS_PREF, enabled);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update switch state when returning to the fragment
        if (getView() != null) {
            SwitchMaterial themeSwitch = getView().findViewById(R.id.themeSwitch);
            themeSwitch.setChecked(isDarkThemeEnabled());
        }
    }
}