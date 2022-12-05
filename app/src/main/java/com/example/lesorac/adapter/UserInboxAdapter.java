package com.example.lesorac.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lesorac.R;
import com.example.lesorac.databinding.InboxItemContainerUserBinding;
import com.example.lesorac.listeners.UserListener;
import com.example.lesorac.model.User;

import java.net.MalformedURLException;
import java.util.List;

public class UserInboxAdapter extends RecyclerView.Adapter<UserInboxAdapter.UserViewHolder> {

    private final List<User> userList;
    private final UserListener userListener;

    public UserInboxAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InboxItemContainerUserBinding itemContainerUserBinding = InboxItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        try {
            holder.setUserData(userList.get(position));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        InboxItemContainerUserBinding binding;
        UserViewHolder(InboxItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        };
        void setUserData(User user) throws MalformedURLException {
            binding.textName.setText(user.getName());
            Glide
                    .with(binding.imageProfile.getContext())
                    .load(user.getPhoto())
                    .placeholder(R.color.white)
                    .into(binding.imageProfile);
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }
}
