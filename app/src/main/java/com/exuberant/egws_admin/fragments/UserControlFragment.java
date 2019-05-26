package com.exuberant.egws_admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonAdapter;
import com.ethanhua.skeleton.SkeletonScreen;
import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.adapters.UserControlAdapter;
import com.exuberant.egws_admin.interfaces.UserSwitchInterface;
import com.exuberant.egws_admin.models.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserControlFragment extends Fragment {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserReference;
    private static UserSwitchInterface userSwitchInterface;
    private List<User> users;
    private SkeletonScreen skeletonScreen;

    private RecyclerView userControlRecyclerview;
    private TextView totalUserTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_control, container, false);
        initialize(view);

        SkeletonAdapter skeletonAdapter = new SkeletonAdapter();
        skeletonScreen = Skeleton.bind(userControlRecyclerview)
                .adapter(skeletonAdapter)
                .shimmer(true)
                .angle(20)
                .duration(1200)
                .load(R.layout.skeleton_item_user_control)
                .count(10)
                .show();

        mUserReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);
                users.add(user);
                loadUsers();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                users.remove(user);
                loadUsers();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    void initialize(View view){
        userControlRecyclerview = view.findViewById(R.id.rv_user_control);
        users = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        mUserReference = mDatabase.getReference().child("users");
        userSwitchInterface = new UserSwitchInterface() {
            @Override
            public void allowedSwitch(int position, boolean isAllowed) {
                switchAllowed(position, isAllowed);
            }
        };
        totalUserTextView = view.findViewById(R.id.tv_total_user_count);
    }

    void loadUsers(){
        totalUserTextView.setText(String.valueOf(users.size()));
        UserControlAdapter adapter = new UserControlAdapter(users, userSwitchInterface);
        userControlRecyclerview.setAdapter(adapter);
    }

    void switchAllowed(int position, boolean isAllowed){
        User user = users.get(position);
        user.setAllowedToApp(isAllowed);
        mUserReference.child(user.getUserId()).setValue(user);
    }

}
