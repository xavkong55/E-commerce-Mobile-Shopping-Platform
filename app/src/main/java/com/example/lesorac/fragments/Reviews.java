package com.example.lesorac.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lesorac.R;
import com.example.lesorac.adapter.RatingAdapter;
import com.example.lesorac.model.Rating;
import com.example.lesorac.model.User;
import com.example.lesorac.util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.text.DecimalFormat;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Reviews#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Reviews extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private DocumentReference mUserRef;
    private RatingAdapter mRatingAdapter;

    private RecyclerView mRatingRecycler;
    private LinearLayout linlayout_write_review;
    private ViewGroup mEmptyView;
    private Button btn_submit;
    private MaterialRatingBar mRatingBar;
    private EditText mRatingText;
    private TextView tv_rating;

    private DecimalFormat df = new DecimalFormat("0.00");
    private Double newAvgRating;
    private String currentUserName, currentUserID;


//    private ArrayList<> mSnapshot;

//    private SharedPreferences sharedPref;
//    private SharedPreferences.Editor editor;

    public Reviews() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Two.
     */
    // TODO: Rename and change types and number of parameters
    public static Reviews newInstance(String param1, String param2) {
        Reviews fragment = new Reviews();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        mRatingRecycler = view.findViewById(R.id.recycler_ratings);
        mEmptyView = view.findViewById(R.id.view_empty_ratings);
        btn_submit = view.findViewById(R.id.fragment_review_submit_button);
        mRatingBar = view.findViewById(R.id.fragment_reviews_rating_bar);
        mRatingText = view.findViewById(R.id.fragment_review_text);
        tv_rating = getActivity().findViewById(R.id.profile_activity_tv_avg_rating);
        linlayout_write_review = view.findViewById(R.id.linlay_write_review);


        String userId = getActivity().getIntent().getExtras().getString("User_Id");

        mFirestore = FirebaseUtil.getFirestore();
        mAuth = FirebaseUtil.getAuth();
        mUserRef = mFirestore.collection("users").document(userId);
        Query query = mUserRef.collection("ratings").orderBy("timestamp", Query.Direction.ASCENDING);

        currentUserID = mAuth.getCurrentUser().getUid();
        if (!currentUserID.equals(userId)) {
            linlayout_write_review.setVisibility(View.VISIBLE);
            initInfo();
        }

        mRatingAdapter = new RatingAdapter(query);

        mRatingRecycler.setAdapter(mRatingAdapter);
        mRatingRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Rating ratings = new Rating(
                        currentUserName,
                        userId,
                        mRatingBar.getRating(),
                        mRatingText.getText().toString()
                );
                addRating(mUserRef, ratings).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(view.getContext(), "Review added", Toast.LENGTH_SHORT).show();
                        mRatingText.setText("");
                        mRatingBar.setRating(0);
                        tv_rating.setText(Double.toString(newAvgRating));
                        mRatingAdapter.updateData();
                    }
                });

            }
        });
        return view;
    }


    private Task<Void> addRating(final DocumentReference userRef, final Rating rating) {

        final DocumentReference ratingRef = userRef.collection("ratings")
                .document();

        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                User user = transaction.get(userRef).toObject(User.class);

                // Compute new number of ratings
                int newNumRatings = user.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = user.getAvgRating() *
                        user.getNumRatings();
                newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                newAvgRating = Double.parseDouble(df.format(newAvgRating));

                // Set new user info
                user.setNumRatings(newNumRatings);
                user.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(userRef, user);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    private void initInfo() {
        DocumentReference docRef = mFirestore.collection("users").document(currentUserID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentUserName = document.getData().get("name").toString();
                    }
                }
            }
        });
    }
}