package com.soja.farmerseller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageManager> chatList;

    public ChatAdapter(Context context, ArrayList<MessageManager> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageManager message = chatList.get(position);
        holder.senderText.setText(message.getSender());
        holder.messageText.setText(message.getMsg());
        holder.timeStampText.setText(message.getTimestamp().toDate().toString());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, messageText,timeStampText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderMessage);
            messageText = itemView.findViewById(R.id.senderTextView);
            timeStampText = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
