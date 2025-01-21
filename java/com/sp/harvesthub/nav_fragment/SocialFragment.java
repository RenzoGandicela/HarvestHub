package com.sp.harvesthub.nav_fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.BlinkAdapter;
import com.sp.harvesthub.adapters.ServerAdapter;
import com.sp.harvesthub.models.Blink;
import com.sp.harvesthub.models.Server;
import com.sp.harvesthub.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SocialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView blinksRecyclerView;
    private RecyclerView serversRecyclerView;
    private BlinkAdapter blinkAdapter;
    private ServerAdapter serverAdapter;
    private List<Blink> blinksList;
    private List<Server> serversList;

    public SocialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SocialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SocialFragment newInstance(String param1, String param2) {
        SocialFragment fragment = new SocialFragment();
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
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        
        // Initialize RecyclerViews
        blinksRecyclerView = view.findViewById(R.id.blinksRecyclerView);
        serversRecyclerView = view.findViewById(R.id.serversRecyclerView);
        
        // Setup horizontal layout for blinks
        blinksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Setup vertical layout for servers
        serversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize lists and adapters
        blinksList = new ArrayList<>();
        serversList = new ArrayList<>();
        blinkAdapter = new BlinkAdapter(getContext(), blinksList);
        serverAdapter = new ServerAdapter(getContext(), serversList);
        
        // Set adapters
        blinksRecyclerView.setAdapter(blinkAdapter);
        serversRecyclerView.setAdapter(serverAdapter);
        
        // Load data
        loadBlinks();
        loadServers();
        
        return view;
    }

    private void loadBlinks() {
        Log.d("SocialFragment", "Starting to load blinks");
        FirebaseHelper.getStatusesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blinksList.clear();
                Log.d("SocialFragment", "Blinks snapshot exists: " + snapshot.exists());
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        String imageURL = userSnapshot.child("imageURL").getValue(String.class);
                        String userId = userSnapshot.getKey(); // Get the UserID from the key
                        String name = userSnapshot.child("name").getValue(String.class);
                        Long timestamp = userSnapshot.child("timestamp").getValue(Long.class);
                        
                        Log.d("SocialFragment", "Loading blink - UserID: " + userId);
                        Log.d("SocialFragment", "Name: " + name);
                        Log.d("SocialFragment", "Image URL: " + imageURL);
                        
                        if (imageURL != null && !imageURL.isEmpty()) {
                            Blink blink = new Blink(userId, imageURL, name, timestamp != null ? timestamp : System.currentTimeMillis());
                            blinksList.add(blink);
                            Log.d("SocialFragment", "Added blink to list");
                        }
                    } catch (Exception e) {
                        Log.e("SocialFragment", "Error loading blink: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                Log.d("SocialFragment", "Final blinks list size: " + blinksList.size());
                blinkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SocialFragment", "Error loading blinks: " + error.getMessage());
                Toast.makeText(getContext(), "Error loading blinks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServers() {
        Log.d("SocialFragment", "Starting to load servers");
        FirebaseHelper.getServersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serversList.clear();
                Log.d("SocialFragment", "Servers snapshot exists: " + snapshot.exists());
                
                for (DataSnapshot serverSnapshot : snapshot.getChildren()) {
                    try {
                        String id = serverSnapshot.getKey(); // Get ServerID from the key
                        String name = serverSnapshot.child("name").getValue(String.class);
                        String description = serverSnapshot.child("description").getValue(String.class);
                        String iconURL = serverSnapshot.child("iconURL").getValue(String.class);
                        
                        Log.d("SocialFragment", "Loading server - ID: " + id + ", Name: " + name + ", Desc: " + description);
                        
                        if (name != null) {
                            Server server = new Server(id, name, description, iconURL);
                            serversList.add(server);
                            Log.d("SocialFragment", "Added server to list");
                        }
                    } catch (Exception e) {
                        Log.e("SocialFragment", "Error loading server: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                Log.d("SocialFragment", "Final servers list size: " + serversList.size());
                serverAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SocialFragment", "Error loading servers: " + error.getMessage());
                Toast.makeText(getContext(), "Error loading servers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}