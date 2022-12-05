package com.example.lesorac.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.databinding.ItemContainerReceiverMessageBinding;
import com.example.lesorac.databinding.ItemContainerSentMessageBinding;
import com.example.lesorac.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final String senderId;
    private String imgUrl;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessageList,String imgUrl, String senderId) {
        this.chatMessageList = chatMessageList;
        this.imgUrl = imgUrl;
        this.senderId = senderId;
    }
    public void setImage(String imgUrl){
        this.imgUrl = imgUrl;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        else{
            return new ReceivedMessageViewHolder(
                    ItemContainerReceiverMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        }
        else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position),imgUrl);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(@NonNull ItemContainerSentMessageBinding ItemContainerSentMessageBinding){
            super(ItemContainerSentMessageBinding.getRoot());
            binding =ItemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceiverMessageBinding Binding;

        ReceivedMessageViewHolder(ItemContainerReceiverMessageBinding itemContainerReceiverMessagingBinding){
            super(itemContainerReceiverMessagingBinding.getRoot());
            Binding = itemContainerReceiverMessagingBinding;
        }
        void setData(ChatMessage chatMessage, String imgUrl){
            Binding.textMessage.setText(chatMessage.message);
            Binding.textDateTime.setText(chatMessage.dateTime);
            if(imgUrl != null) {
                Glide
                        .with(Binding.imageProfile.getContext())
                        .load(imgUrl)
                        .placeholder(R.color.white)
                        .into(Binding.imageProfile);
            }
        }
    }
}
