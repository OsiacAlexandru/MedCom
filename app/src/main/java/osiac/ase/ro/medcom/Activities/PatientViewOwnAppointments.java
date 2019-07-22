package osiac.ase.ro.medcom.Activities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import osiac.ase.ro.medcom.Classes.Appointment;
import osiac.ase.ro.medcom.Classes.PatientAppointmentAdapter;
import osiac.ase.ro.medcom.R;

public class PatientViewOwnAppointments extends AppCompatActivity  {
    private static final String TAG = ViewDocumentsActivity.class.getSimpleName();
    private RecyclerView rv;
    private PatientAppointmentAdapter adapter;
    private ProgressBar pb;
    private ArrayList<Appointment> dateAppointments;
    private TextView tv;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ChildEventListener mDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        setContentView(R.layout.activity_patient_view_own_appointments);

        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");
        mAuth = FirebaseAuth.getInstance();
        final String email = mAuth.getCurrentUser().getEmail();

        tv = findViewById(R.id.tvDate_own);
        tv.setText("PERSONAL APPOINTMENT LIST");
        rv = findViewById(R.id.recycler_view_app_own);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        pb = findViewById(R.id.pb_app_own);
        dateAppointments = new ArrayList<>();

        adapter = new PatientAppointmentAdapter(getApplicationContext(),dateAppointments);

        rv.setAdapter(adapter);


        mDBListener = databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Appointment appointment = dataSnapshot.getValue(Appointment.class);
                if (appointment.getPatientEmail().equals(email))
                    dateAppointments.add(appointment);
                adapter.notifyDataSetChanged();
                pb.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Appointment appointment = dataSnapshot.getValue(Appointment.class);
                String id = appointment.getUniqueId();
                Integer position = searchById(dateAppointments,id);
                if(!appointment.getPatientEmail().equals(email)) {
                    dateAppointments.remove(dateAppointments.get(position));
                    adapter.notifyDataSetChanged();
                    pb.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Appointment appointment = dataSnapshot.getValue(Appointment.class);
                String id = appointment.getUniqueId();
                Integer position = searchById(dateAppointments,id);
                dateAppointments.remove(dateAppointments.get(position));
                adapter.notifyDataSetChanged();
                pb.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mDBListener);
    }

    Integer searchById(ArrayList<Appointment> appointments,String id) {
        for(int i=0;i<appointments.size();i++) {
            if(appointments.get(i).getUniqueId().equals(id))
                return i;
        }
        return 0;
    }
}
