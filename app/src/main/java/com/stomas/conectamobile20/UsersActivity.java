package com.stomas.conectamobile20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UsersAdapter adapter;
    private List<Map<String, String>> userList = new ArrayList<>();
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();

        adapter = new UsersAdapter(userList, user -> {
            // Al hacer clic en un usuario, abrir ChatActivity
            Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
            intent.putExtra("userId", user.get("userId"));
            intent.putExtra("userName", user.get("name"));
            startActivity(intent);
        });

        usersRecyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseUser currentUser = auth.getCurrentUser();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    HashMap<String, String> user = (HashMap<String, String>) userSnapshot.getValue();

                    if (!userId.equals(currentUser.getUid())) { // Evitar incluir al usuario actual
                        user.put("userId", userId);
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
