package com.stomas.conectamobile20;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private MessagesAdapter adapter;

    private List<Map<String, String>> messagesList = new ArrayList<>();
    private String currentUserId, otherUserId, chatId;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(messagesList);
        messagesRecyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("userId");

        // Crear un ID único para el chat basado en los IDs de usuario
        chatId = currentUserId.compareTo(otherUserId) < 0 ? currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;
        messagesRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);

        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Object value = snapshot.getValue();
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) value;
                    messagesList.add(message);
                    adapter.notifyItemInserted(messagesList.size() - 1);
                    messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Object value = snapshot.getValue();
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> updatedMessage = (Map<String, String>) value;

                    for (int i = 0; i < messagesList.size(); i++) {
                        if (Objects.requireNonNull(snapshot.getKey()).equals(messagesList.get(i).get("key"))) {
                            messagesList.set(i, updatedMessage);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String messageKey = snapshot.getKey();
                for (int i = 0; i < messagesList.size(); i++) {
                    if (Objects.requireNonNull(snapshot.getKey()).equals(messagesList.get(i).get("key"))) {
                        messagesList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Manejo opcional si los mensajes cambian de orden
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar errores si es necesario
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        Map<String, String> message = new HashMap<>();
        message.put("sender", currentUserId);
        message.put("receiver", otherUserId);
        message.put("message", messageText);

        messagesRef.push().setValue(message);
        messageEditText.setText(""); // Limpiar el campo de texto
    }
}
