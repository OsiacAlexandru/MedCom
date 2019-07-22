package osiac.ase.ro.medcom.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import osiac.ase.ro.medcom.Classes.Message;
import osiac.ase.ro.medcom.Classes.MessageAdapter;
import osiac.ase.ro.medcom.Classes.ProfilePicture;
import osiac.ase.ro.medcom.R;

public class PatientMessageActivity extends AppCompatActivity {

    private CircleImageView image;
    private TextView tvUsername;
    private Intent intent;
    private String email;
    private FirebaseAuth mAuth;
    private DatabaseReference mReferenceName;
    private DatabaseReference mReferencePicture;
    private DatabaseReference mReferenceMessages;
    private DatabaseReference mReferenceSeen;
    private ValueEventListener seenListener;
    private FirebaseUser user;
    private ImageButton bttnSend;
    private EditText editText;
    private MessageAdapter adapter;
    private List<Message> messages;
    private RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_patient_message);

        image = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.username);
        editText = findViewById(R.id.text_send);
        bttnSend = findViewById(R.id.btn_send);
        intent = getIntent();
        email = intent.getStringExtra("email");
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view_patient_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        user = mAuth.getCurrentUser();
        mReferenceName = FirebaseDatabase.getInstance().getReference("Doctors");
        mReferenceName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("email").getValue(String.class).equals(email)) {
                        tvUsername.setText(ds.child("name").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mReferencePicture = FirebaseDatabase.getInstance().getReference("pictures");
        mReferencePicture.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ProfilePicture p = dataSnapshot.getValue(ProfilePicture.class);
                image.setImageResource(R.drawable.blanck_user);
                if (p.getEmail().equals(email)) {
                    Picasso.get().load(p.getImageUrl()).fit().into(image);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bttnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(user.getEmail(), email, msg);
                } else {
                    Toast.makeText(PatientMessageActivity.this, "You can't send an empty message.", Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }
        });
        readMessages(mAuth.getCurrentUser().getEmail(), email);
        seenMessage(email);
    }

    private void sendMessage(String sender, final String receiver, String text) {

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("text", text);
        hashMap.put("isSeen", false);

        mReference.child("Messages").push().setValue(hashMap);
    }

    private void readMessages(final String myEmail, final String otherEmail) {

        messages = new ArrayList<>();

        mReferenceMessages = FirebaseDatabase.getInstance().getReference("Messages");
        mReferenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String recv = snapshot.child("receiver").getValue(String.class);
                    boolean seen = snapshot.child("isSeen").getValue(Boolean.class);
                    String send = snapshot.child("sender").getValue(String.class);
                    String txt = snapshot.child("text").getValue(String.class);
                    Message mess = new Message(seen, send, recv, txt);
                    if (mess.getReceiver().equals(myEmail) && mess.getSender().equals(otherEmail) ||
                            mess.getReceiver().equals(otherEmail) && mess.getSender().equals(myEmail)) {
                        messages.add(mess);
                    }
                    adapter = new MessageAdapter(PatientMessageActivity.this, messages);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String email) {
        mReferenceSeen = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = mReferenceSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getReceiver().equals(mAuth.getCurrentUser().getEmail()) && message.getSender().equals(email)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        mReferenceSeen.removeEventListener(seenListener);
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active",false);
        ed.commit();
        finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active",false);
        ed.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active",true);
        ed.commit();

    }
}