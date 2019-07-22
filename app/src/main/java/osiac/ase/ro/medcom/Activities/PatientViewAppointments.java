package osiac.ase.ro.medcom.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import osiac.ase.ro.medcom.Classes.Appointment;
import osiac.ase.ro.medcom.Classes.PatientBookAppointmentAdapter;
import osiac.ase.ro.medcom.R;

public class PatientViewAppointments extends AppCompatActivity implements PatientBookAppointmentAdapter.OnItemClickListener {
    private static final String TAG = ViewDocumentsActivity.class.getSimpleName();
    private RecyclerView rv;
    private PatientBookAppointmentAdapter adapter;
    private ProgressBar pb;
    private ArrayList<Appointment> dateAppointments;
    private TextView tv;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ChildEventListener mDBListener;

    private String docEmail;
    private String selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        setContentView(R.layout.activity_patient_view_appointments);

        Intent intent = getIntent();
        if (intent != null) {
            docEmail = intent.getStringExtra("email");
            selectedDate = intent.getStringExtra("date");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");
        mAuth = FirebaseAuth.getInstance();

        tv = findViewById(R.id.tvDate);
        tv.setText(selectedDate);
        rv = findViewById(R.id.recycler_view_app);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        pb = findViewById(R.id.pb_app);
        dateAppointments = new ArrayList<>();

        adapter = new PatientBookAppointmentAdapter(getApplicationContext(),dateAppointments);

        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(PatientViewAppointments.this);

        mDBListener = databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Appointment appointment = dataSnapshot.getValue(Appointment.class);
                if (appointment.getCalendarDate().equals(selectedDate)&&appointment.getDoctorEmail().equals(docEmail)
                        && appointment.getPatientEmail().equals("null"))
                    dateAppointments.add(appointment);
                adapter.notifyDataSetChanged();
                pb.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Appointment appointment = dataSnapshot.getValue(Appointment.class);
                String id = appointment.getUniqueId();
                Integer position = searchById(dateAppointments,id);
                if(!appointment.getPatientEmail().equals("null")) {
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
    public void onItemClick(int position) {

    }

    @Override
    public void onBookClick(int position) {

        String selectedKey = dateAppointments.get(position).getUniqueId();
        String email = mAuth.getCurrentUser().getEmail();
        DatabaseReference refEmail = databaseReference.child(selectedKey).child("patientEmail");
        refEmail.setValue(email);
        Toast.makeText(getApplicationContext(),"Appointment booked!",Toast.LENGTH_LONG).show();
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
