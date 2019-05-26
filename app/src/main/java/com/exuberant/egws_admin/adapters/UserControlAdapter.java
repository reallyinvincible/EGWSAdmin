package com.exuberant.egws_admin.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.activities.UserDetailActivity;
import com.exuberant.egws_admin.interfaces.UserSwitchInterface;
import com.exuberant.egws_admin.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import static maes.tech.intentanim.CustomIntent.customType;

public class UserControlAdapter extends RecyclerView.Adapter<UserControlAdapter.UserControlViewHolder> {

    List<User> userList;
    UserSwitchInterface userSwitchInterface;

    public UserControlAdapter(List<User> userList, UserSwitchInterface userSwitchInterface) {
        this.userList = userList;
        this.userSwitchInterface = userSwitchInterface;
    }

    @NonNull
    @Override
    public UserControlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_user_control, parent, false);
        return new UserControlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserControlViewHolder holder, final int position) {
        final User user = userList.get(position);
        holder.nameTextView.setText(user.getDisplayName());
        holder.emailTextView.setText(user.getEmail());
        holder.checkReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.checkReportsButton.getContext(), UserDetailActivity.class);
                intent.putExtra("UserId", user.getUserId());
                holder.checkReportsButton.getContext().startActivity(intent);
                customType(holder.checkReportsButton.getContext(), "bottom-to-up");
            }
        });
        holder.isAllowedSwitch.setChecked(user.getAllowedToApp());
        holder.isAllowedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.isAllowedSwitch.setChecked(isChecked);
                userSwitchInterface.allowedSwitch(position, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserControlViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView, emailTextView;
        MaterialButton checkReportsButton;
        Switch isAllowedSwitch;

        public UserControlViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_user_name);
            emailTextView = itemView.findViewById(R.id.tv_user_email);
            checkReportsButton = itemView.findViewById(R.id.btn_user_reports);
            isAllowedSwitch = itemView.findViewById(R.id.sw_allowed_switch);
        }
    }

}
