package com.example.lesorac.activity;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.lesorac.adapter.ChatAdapter;
import com.example.lesorac.databinding.ActivityChatBinding;
import com.example.lesorac.databinding.ItemContainerReceiverMessageBinding;
import com.example.lesorac.model.ChatMessage;
import com.example.lesorac.model.User;
import com.example.lesorac.network.ApiClient;
import com.example.lesorac.network.ApiServices;
import com.example.lesorac.util.Constants;
import com.example.lesorac.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private User receiverUser;
    private FirebaseFirestore mFirebase;
    private String uid, name, imgUrl, token;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;
    private PreferenceManager preferenceManager;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        uid = getIntent().getStringExtra("User_Id");
        text = getIntent().getStringExtra("message");

        setListener();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,
                receiverUser.getPhoto(),
                uid
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        mFirebase = FirebaseFirestore.getInstance();
        DocumentReference docRef = mFirebase.collection(Constants.KEY_COLLECTION_USER).document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getData().get("name").toString();
                        imgUrl = document.getData().get("photo").toString();
                    }
                }
            }
        });
        if (!text.equals("")) {
            binding.inputMessage.setText(text);
        }
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, uid);
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getUid());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        mFirebase.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversions = new HashMap<>();
            conversions.put(Constants.KEY_SENDER_ID, uid);
            conversions.put(Constants.KEY_SENDER_NAME, name);
            conversions.put(Constants.KEY_SENDER_IMAGE, imgUrl);
            conversions.put(Constants.KEY_RECEIVER_ID, receiverUser.getUid());
            conversions.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            conversions.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getPhoto());
            conversions.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversions.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversions);
        }
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverUser.getfcmToken());

            JSONObject data = new JSONObject();
            data.put(Constants.KEY_UID, uid);
            data.put(Constants.KEY_USER_NAME, name);
            data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
            data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendNotification(body.toString());

        } catch (Exception e) {
            showToast(e.getMessage());
        }
        binding.inputMessage.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiServices.class).sendMessages(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("Error: "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        mFirebase.collection(Constants.KEY_COLLECTION_USER)
                .document(receiverUser.getUid())
                .addSnapshotListener(ChatActivity.this, (value, error) ->
                {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(
                                    value.getLong(Constants.KEY_AVAILABILITY)
                            ).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.setfcmToken(value.getString(Constants.KEY_FCM_TOKEN));
                        if (receiverUser.getPhoto() == null) {
                            receiverUser.setPhoto(value.getString(Constants.KEY_PHOTO_URL));
                            chatAdapter.setImage(receiverUser.getPhoto());
                            chatAdapter.notifyItemRangeChanged(0, chatMessageList.size());
                        }
                    }
                    if (isReceiverAvailable) {
                        binding.imageAvailability.setVisibility(View.VISIBLE);
                    } else {
                        binding.imageAvailability.setVisibility(View.GONE);
                    }
                });
    }

    private void listenMessages() {
        mFirebase.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, uid)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getUid())
                .addSnapshotListener(eventListener);
        mFirebase.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getUid())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, uid)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversion();
        }
    };

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra("user");
        binding.textName.setText(receiverUser.getName());
    }

    private void setListener() {
        if(!text.equals("")) {
            binding.imageBack.setOnClickListener(v -> onBackPressed());
        }
        else
            binding.imageBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, InboxActivity.class);
                    intent.putExtra("User_Id", uid);
                    intent.putExtra("Username", name);
                    startActivity(intent);
                }
            });
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.imageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("User_Id", receiverUser.getUid());
                intent.putExtra("Username", receiverUser.getName());
                startActivity(intent);
            }
        });
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        mFirebase.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference =
                mFirebase.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_RECEIVER_ID, receiverUser.getUid(),
                Constants.KEY_RECEIVER_NAME, receiverUser.getName(),
                Constants.KEY_RECEIVER_IMAGE, receiverUser.getPhoto(),
                Constants.KEY_TIMESTAMP, new Date(),
                Constants.KEY_SENDER_ID, uid,
                Constants.KEY_SENDER_IMAGE, imgUrl,
                Constants.KEY_SENDER_NAME, name
        );
    }

    private void checkForConversion() {
        if (chatMessageList.size() != 0) {
            checkForConversionRemotely(
                    uid,
                    receiverUser.getUid()
            );
            checkForConversionRemotely(
                    receiverUser.getUid(),
                    uid
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        mFirebase.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}