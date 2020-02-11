package com.medha.avinder.uietianshub.puWifi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.utils.RecyclerTouchListener;

import java.util.ArrayList;

public class ActivityPuWifi extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;

    private DatabaseHelper db;

    private ArrayList<String> usersList;
    private AdapterWifi adapterWifi;

    private TextView tvNoUsers, tvSwitchStatus;
    public static TextView tvStatus;
    private TextInputLayout tilPassword;
    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        init();
    }

    private void init() {
        setContentView(R.layout.activity_pu_login);

        recyclerView = findViewById(R.id.recyclerView);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnLogout = findViewById(R.id.btnLogout);
        tvSwitchStatus = findViewById(R.id.tvSwitchStatus);
        tvNoUsers = findViewById(R.id.tvNoUsers);
        etName = findViewById(R.id.etName);
        tilPassword = findViewById(R.id.textInputLayout);
        TextView btnSave = findViewById(R.id.btnSave);
        Switch swAutoLogin = findViewById(R.id.swAutoLogin);

        usersList = new ArrayList<>();

        adapterWifi = new AdapterWifi(this, usersList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterWifi);

        usersList.addAll(db.getAllWifiUsers());
        adapterWifi.notifyDataSetChanged();

        if (usersList.isEmpty()) {
            tvNoUsers.setVisibility(View.VISIBLE);
        }

        if (Functions.getStatus(getApplicationContext()).equals("Logged In")) {
            tvStatus.setText("Logged In as " + Functions.getActiveUserName(getApplicationContext()));
        } else {
            tvStatus.setText(Functions.getStatus(getApplicationContext()));
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etName.getText().toString();
                String password = tilPassword.getEditText().getText().toString();

                if (username.isEmpty()) {
                    etName.setError("This field cannot be empty");
                    return;
                }
                if (password.isEmpty()) {
                    tilPassword.getEditText().setError("This field cannot be empty");
                    return;
                }

                if (usersList.isEmpty()) {
                    Functions.initialise(ActivityPuWifi.this);
                    Functions.setActiveUser(ActivityPuWifi.this, username);
                }

                db.insertWifiUser(username, password);
                Toast.makeText(ActivityPuWifi.this, "User Added Successfully", Toast.LENGTH_SHORT).show();

                usersList.add(username);
                adapterWifi.notifyDataSetChanged();

                tvNoUsers.setVisibility(View.GONE);

                etName.setText("");
                tilPassword.getEditText().setText("");
            }
        });

        if (Functions.isAutoLoginEnabled(getApplicationContext())){
            swAutoLogin.setChecked(true);
            tvSwitchStatus.setText("Enabled");
        } else {
            swAutoLogin.setChecked(false);
            tvSwitchStatus.setText("Disabled");
        }

        swAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Functions.setAutoLoginEnabled(getApplicationContext(), true);
                    tvSwitchStatus.setText("Enabled");
                } else {
                    Functions.setAutoLoginEnabled(getApplicationContext(),false);
                    tvSwitchStatus.setText("Disabled");
                }
            }
        });

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Functions.setActiveUser(ActivityPuWifi.this, usersList.get(position));
                adapterWifi.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {
                showEditDialog(usersList.get(position));
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                if (usersList.isEmpty()) {
                    Toast.makeText(this, "Enter at least one user", Toast.LENGTH_SHORT).show();
                } else {
                    new LoginTask(ActivityPuWifi.this, false).execute();
                }
                break;
            case R.id.btnLogout:
                if (usersList.isEmpty()) {
                    Toast.makeText(this, "No users added", Toast.LENGTH_SHORT).show();
                } else {
                    new LogoutTask(ActivityPuWifi.this).execute();
                }
                break;
        }
    }

    private void showEditDialog(final String oldUser) {
        final Dialog dialog = new Dialog(ActivityPuWifi.this);
        dialog.setContentView(R.layout.dialog_edit_user);
        TextView save = dialog.findViewById(R.id.btn_save);
        TextView delete = dialog.findViewById(R.id.btn_delete);
        TextView cancel = dialog.findViewById(R.id.btn_cancel);
        final EditText username = dialog.findViewById(R.id.etEditName);
        final TextInputLayout password = dialog.findViewById(R.id.textInputLayout);
        username.setText(oldUser);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = username.getText().toString();
                if (newUsername.isEmpty()) {
                    username.setError("This field cannot be empty");
                    return;
                }

                String pwd = password.getEditText().getText().toString();
                if (pwd.isEmpty()) {
                    pwd = db.getPasswordFromUsername(oldUser);
                }
                db.updateWifiUser(oldUser, newUsername, pwd);

                onEditUserConformation(oldUser, newUsername);
                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ActivityPuWifi.this)
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog1, int which) {
                                db.deleteWifiUser(username.getText().toString());
                                onDeleteUserConformation(username.getText().toString());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = getResources().getDisplayMetrics().widthPixels / 10 * 9;
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    private void onEditUserConformation(String oldUsername, String newUsername) {
        int x = usersList.indexOf(oldUsername);
        usersList.remove(oldUsername);
        usersList.add(x, newUsername);
        if (Functions.getActiveUserName(this).equals(oldUsername))
            Functions.setActiveUser(this, newUsername);
        adapterWifi.notifyDataSetChanged();
        Toast.makeText(this, "Edit Successful", Toast.LENGTH_SHORT).show();
    }

    private void onDeleteUserConformation(String username) {
        usersList.remove(username);
        if (usersList.isEmpty()) {
            Functions.disable(this);
            tvNoUsers.setVisibility(View.VISIBLE);
        } else {
            if (Functions.getActiveUserName(this).equals(username))
                Functions.setActiveUser(this, usersList.get(0));
        }
        adapterWifi.notifyDataSetChanged();
        Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT).show();
    }

    public class AdapterWifi extends RecyclerView.Adapter<AdapterWifi.MyViewHolder> {
        private Context context;
        private ArrayList<String> usersList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbActiveUser;
            TextView tvUser;
            LinearLayout linearLayout;

            MyViewHolder(View view) {
                super(view);
                cbActiveUser = view.findViewById(R.id.cbActiveUser);
                tvUser = view.findViewById(R.id.tvUser);
                linearLayout = view.findViewById(R.id.linearLayout);
            }
        }


        public AdapterWifi(Context context, ArrayList<String> usersList) {
            this.context = context;
            this.usersList = usersList;
        }

        @NonNull
        @Override
        public AdapterWifi.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_user, parent, false);
            return new AdapterWifi.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdapterWifi.MyViewHolder holder, int position) {
            final String user = usersList.get(position);
            holder.tvUser.setText(user);

            if (Functions.getActiveUserName(context).equals(user)) {
                holder.cbActiveUser.setChecked(true);
            } else {
                holder.cbActiveUser.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return usersList.size();
        }
    }
}