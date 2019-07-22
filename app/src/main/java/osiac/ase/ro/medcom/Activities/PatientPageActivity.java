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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import osiac.ase.ro.medcom.Classes.Appointment;
import osiac.ase.ro.medcom.Classes.Document;
import osiac.ase.ro.medcom.Classes.Message;
import osiac.ase.ro.medcom.Classes.MessageAdapter;
import osiac.ase.ro.medcom.R;

public class PatientPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private Button manageAccount;
    private Button bttnBack;
    private Button bttnViewDocuments;
    private Button bttnCalendar;
    private Button bttnChat;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceMessages;
    private DatabaseReference mReferenceDocuments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_patient_page);

        mAuth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference();
        manageAccount=findViewById(R.id.bttnManageAccount);
        bttnBack = findViewById(R.id.bttnBack12);
        bttnViewDocuments = findViewById(R.id.bttnViewDocuments);
        bttnCalendar = findViewById(R.id.bttnPatientCalendar);
        bttnChat = findViewById(R.id.bttnPatientChat);

        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active",false);
        ed.commit();

        mDatabase = FirebaseDatabase.getInstance();

        // Message Notification

        mReferenceMessages = FirebaseDatabase.getInstance().getReference("Messages");
        mReferenceMessages.addValueEventListener(new ValueEventListener() {
            boolean showNotif = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(showNotif){
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        String recv = snapshot.child("receiver").getValue(String.class);
                        boolean seen = snapshot.child("isSeen").getValue(Boolean.class);
                        String send = snapshot.child("sender").getValue(String.class);
                        String txt = snapshot.child("text").getValue(String.class);
                        Message mess = new Message(seen, send, recv, txt);

                        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
                        boolean check = sp.getBoolean("active", true);
                        if (mess.getReceiver().equals(mAuth.getCurrentUser().getEmail()) && check == false) {
                            addNotification(send);
                        }
                    }
                }
                showNotif = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // -----------------------------------------------------------------------------------------------

        // CALENDAR NOTIFICATION

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        final String currentDate = sdf.format(date);
        final Query check = databaseReference.child("Appointments").orderByChild("calendarDate").equalTo(currentDate);
        check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // TAKE THE DATA OUT OF THE DATABASE
                for (DataSnapshot datasnapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = datasnapshot.getValue(Appointment.class);
                    if(appointment.getCalendarDate().equals(currentDate)
                            && appointment.getPatientEmail().equals(mAuth.getCurrentUser().getEmail()))
                        addNotificationAppointment();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // --------------------------------------------------------------------------------

        // DOCUMENTS NOTIFICATION

        mReferenceDocuments = FirebaseDatabase.getInstance().getReference("documents");
        mReferenceDocuments.addValueEventListener(new ValueEventListener() {
            boolean showNotif = false;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(showNotif) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Document doc = postSnapshot.getValue(Document.class);
                        if (doc.getmPatientEmail().equals(mAuth.getCurrentUser().getEmail()))
                            addNotificationDocuments();
                    }
                }
                showNotif = true;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        manageAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ManageAccountActivity.class);
                startActivity(intent);
            }
        });

        bttnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),getString(R.string.successfullySignedOut),Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        });

        bttnViewDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mAuth.getCurrentUser().getEmail();
                Intent intent = new Intent(getApplicationContext(),ViewDocumentsActivity.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });

        bttnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PatientCalendar.class);
                startActivity(intent);
            }
        });

        bttnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = mAuth.getCurrentUser().getUid();
                DatabaseReference ref1 = mDatabase.getReference().child("Patients").child(id).child("doctor");
                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Integer docI = dataSnapshot.getValue(Integer.class);
                        Query ref2 = mDatabase.getReference().child("Doctors").orderByChild("accessCode");
                        ref2.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Long acc = ds.child("accessCode").getValue(Long.class);
                                    Integer accI = Math.toIntExact(acc);
                                    if (accI.equals(docI)) {
                                        String docEmail = ds.child("email").getValue(String.class);
                                        Intent intent = new Intent(getApplicationContext(),PatientMessageActivity.class);
                                        intent.putExtra("email",docEmail);
                                        startActivity(intent);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.user_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.mSignOut:
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),getString(R.string.successfullySignedOut),Toast.LENGTH_LONG).show();
                finish();
                break;
        }
        return true;
    }

    private void addNotification(String senderName) {

        String CHANNEL_ID = "my_channel_message_notification";
        CharSequence name = "my_channel";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.caduceus4)
                .setContentTitle("You have a new message!")
                .setContentText("Message from " + senderName);

        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        manager.notify((int)(System.currentTimeMillis()/1000), builder.build());
    }

    private void addNotificationAppointment() {

        String CHANNEL_ID = "my_channel_message_appointment";
        CharSequence name = "my_channel_02";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.caduceus4)
                .setContentTitle("You have appointments today!")
                .setContentText("Please check your appointments list");

        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        manager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE), builder.build());
    }

    private void addNotificationDocuments() {

        String CHANNEL_ID = "my_channel_message_document";
        CharSequence name = "my_channel_02";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.caduceus4)
                .setContentTitle("You received a new document")
                .setContentText("Please check your documents list");

        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        manager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE), builder.build());
    }




    @Override
    public void onBackPressed()
    {
        mAuth.signOut();
        Toast.makeText(getApplicationContext(),getString(R.string.successfullySignedOut),Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("ok_notification", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active",false);
        ed.commit();
    }

}
