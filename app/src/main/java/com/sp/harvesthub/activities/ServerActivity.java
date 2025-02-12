package com.sp.harvesthub.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.ChannelAdapter;
import com.sp.harvesthub.models.Channel;
import com.sp.harvesthub.utils.FirebaseHelper;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ServerActivity extends AppCompatActivity {
    private RecyclerView channelsRecyclerView;
    private ChannelAdapter channelAdapter;
    private List<Channel> channelsList;
    private String serverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        serverId = getIntent().getStringExtra("serverId");
        String serverName = getIntent().getStringExtra("serverName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(serverName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        channelsRecyclerView = findViewById(R.id.channelsRecyclerView);
        channelsList = new ArrayList<>();
        channelAdapter = new ChannelAdapter(this, channelsList, serverId);
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        channelsRecyclerView.setAdapter(channelAdapter);

        loadChannels();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChannels() {
        FirebaseHelper.getChannelsRef(serverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                channelsList.clear();
                for (DataSnapshot channelSnapshot : snapshot.getChildren()) {
                    try {
                        String id = channelSnapshot.getKey();
                        String name = channelSnapshot.child("name").getValue(String.class);
                        String description = channelSnapshot.child("description").getValue(String.class);
                        
                        Channel channel = new Channel(id, name);
                        channel.setDescription(description);
                        channelsList.add(channel);
                    } catch (Exception e) {
                        Toast.makeText(ServerActivity.this, 
                            "Error loading channel: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
                channelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServerActivity.this, "Error: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 