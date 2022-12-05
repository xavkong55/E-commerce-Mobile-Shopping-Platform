package com.example.lesorac.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.databinding.InboxItemContainerRecentConversionBinding;
import com.example.lesorac.databinding.ItemContainerSentMessageBinding;
import com.example.lesorac.listeners.ConversationListener;
import com.example.lesorac.model.ChatMessage;
import com.example.lesorac.model.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessageList, ConversationListener conversationListener) {
        this.chatMessageList = chatMessageList;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                InboxItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        InboxItemContainerRecentConversionBinding binding;

        ConversionViewHolder(InboxItemContainerRecentConversionBinding
                                     inboxItemContainerRecentConversionBinding){
            super(inboxItemContainerRecentConversionBinding.getRoot());
            binding =inboxItemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            Glide
                    .with(binding.imageProfile.getContext())
                    .load(chatMessage.conversionImage)
                    .placeholder(R.color.white)
                    .into(binding.imageProfile);
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.setUid(chatMessage.conversionId);
                user.setName(chatMessage.conversionName);
                user.setPhoto(chatMessage.conversionImage);
                conversationListener.OnConversationClicked(user);
            });
        }
    }
}
