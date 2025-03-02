package com.soja.farmerseller;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private RecyclerView messageRecycler;
    private ChatAdapter chatAdapter;
    private ArrayList<MessageManager> chatList;
    private FirebaseFirestore db;
    private EditText messageInput;
    private Button sendButton;
    private String senderEmail;
    private String chatDocId;
    private LinearLayout layoutContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize SharedPreferences correctly
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        senderEmail = sharedPreferences.getString("user_email", "default_email@gmail.com");
        String receiverEmail = "ab@gmail.com"; // Change this dynamically if needed
        chatDocId = senderEmail + "x" + receiverEmail;

        // Inflate message layout dynamically
        View messageLayout = inflater.inflate(R.layout.message_layout, container, false);

        // Initialize UI components
        messageRecycler = view.findViewById(R.id.messageRecycler);
        messageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        messageInput = messageLayout.findViewById(R.id.messageInput);
        sendButton = messageLayout.findViewById(R.id.sendButton);

        // Ensure layoutContainer exists in XML
        layoutContainer = view.findViewById(R.id.layoutContainer);
        if (layoutContainer != null) {
            layoutContainer.addView(messageLayout);
        } else {
            Log.e("NotificationsFragment", "layoutContainer not found in fragment_notifications.xml");
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize chat list and adapter
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList);
        messageRecycler.setAdapter(chatAdapter);

        // Start real-time listener for messages
        listenForChatUpdates();

        // Send button listener
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            } else {
                Toast.makeText(getContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // 🔥 Real-time Firestore listener for chat messages
    private void listenForChatUpdates() {
        db.collection("Chat").document(chatDocId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Firestore", "Error listening to messages: " + e.getMessage());
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            chatList.clear();
                            Map<String, Object> chatData = documentSnapshot.getData();
                            if (chatData != null) {
                                for (Map.Entry<String, Object> entry : chatData.entrySet()) {
                                    Toast.makeText(getContext(), chatDocId, Toast.LENGTH_SHORT).show();
                                    if (entry.getValue() instanceof Map) {
                                        Map<String, Object> messageData = (Map<String, Object>) entry.getValue();
                                        if (messageData.get("sender") != null) {
                                            String sender = messageData.get("sender").toString();
                                            String msg = messageData.get("msg") != null ? messageData.get("msg").toString() : "";
                                            Timestamp timestamp = messageData.get("Timestamp") instanceof Timestamp ? (Timestamp) messageData.get("Timestamp") : null;

                                            chatList.add(new MessageManager(timestamp, msg, sender));
                                        }
                                    }

                                }
                            }

                            // Sort messages by timestamp
                            chatList.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

                            chatAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No messages found");
                        }
                    }
                });
    }

    // ✅ Send message to Firestore
    private void sendMessage(String messageText) {
        db.collection("Chat").document(chatDocId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> chatData = documentSnapshot.exists() ? documentSnapshot.getData() : new HashMap<>();

                    // Determine the next message ID
                    int nextMessageId = 1; // Default to 1 if no messages exist
                    for (String key : chatData.keySet()) {
                        try {
                            int messageId = Integer.parseInt(key);
                            if (messageId >= nextMessageId) {
                                nextMessageId = messageId + 1; // Increment the highest found ID
                            }
                        } catch (NumberFormatException ignored) {
                            // Ignore non-numeric keys
                        }
                    }

                    // Create new message
                    Map<String, Object> newMessage = new HashMap<>();
                    newMessage.put("sender", senderEmail);
                    newMessage.put("msg", messageText);
                    newMessage.put("Timestamp", Timestamp.now());

                    chatData.put(String.valueOf(nextMessageId), newMessage); // Store message with numeric key

                    // Update Firestore
                    db.collection("Chat").document(chatDocId)
                            .set(chatData)
                            .addOnSuccessListener(aVoid -> {
                                messageInput.setText(""); // Clear input field
                                Toast.makeText(getContext(), "Message Sent!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error sending message: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching chat: " + e.getMessage()));
    }

}
