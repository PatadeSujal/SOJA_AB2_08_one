package com.soja.farmerseller;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private RecyclerView messageRecycler;
    private ChatAdapter chatAdapter;
    private ArrayList<MessageManager> chatList;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize RecyclerView
        messageRecycler = view.findViewById(R.id.messageRecycler);
        messageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize List and Adapter
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList);
        messageRecycler.setAdapter(chatAdapter);

        // Fetch chat messages
        fetchChatMessages("SellerEmailxUserEmail");

        return view;
    }

    private void fetchChatMessages(String userEmail) {
        db.collection("Chat").document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        chatList.clear();
                        Map<String, Object> chatData = documentSnapshot.getData();
                        if (chatData != null) {
                            for (Map.Entry<String, Object> entry : chatData.entrySet()) {
                                // Ensure the field contains a valid Map
                                if (entry.getValue() instanceof Map) {
                                    Map<String, Object> messageData = (Map<String, Object>) entry.getValue();

                                    String sender = (String) messageData.get("sender");
                                    String msg = (String) messageData.get("msg");
                                    Timestamp timestamp = (Timestamp) messageData.get("Timestamp");
                                    MessageManager message = new MessageManager(sender,msg,timestamp);
                                    chatList.add(message);
                                }
                            }
                        }

                        // Sort messages by timestamp if needed
                        chatList.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

                        chatAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(), "ddf", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching messages: " + e.getMessage()));
    }




}
