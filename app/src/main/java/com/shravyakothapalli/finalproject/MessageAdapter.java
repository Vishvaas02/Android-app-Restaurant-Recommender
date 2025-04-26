package com.shravyakothapalli.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{

    List<Message> list;

    public MessageAdapter(List<Message> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chat = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat, null);
        MyViewHolder viewHolder = new MyViewHolder((chat));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = list.get(position);
        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightChatMessageView.setText(message.getMessage());
        } else {
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatMessageView.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatView, rightChatView;
        TextView leftChatMessageView, rightChatMessageView;
        public MyViewHolder(View view) {
            super(view);
            leftChatView = view.findViewById(R.id.left_chat_view);
            rightChatView = view.findViewById(R.id.right_chat_view);
            leftChatMessageView = view.findViewById(R.id.left_chat_text_view);
            rightChatMessageView = view.findViewById(R.id.right_chat_text_view);
        }
    }

}
