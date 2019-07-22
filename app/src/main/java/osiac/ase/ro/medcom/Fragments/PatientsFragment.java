package osiac.ase.ro.medcom.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import osiac.ase.ro.medcom.Classes.Patient;
import osiac.ase.ro.medcom.Classes.PatientForChatAdapter;
import osiac.ase.ro.medcom.Classes.ProfilePicture;
import osiac.ase.ro.medcom.R;


public class PatientsFragment extends Fragment{

    private RecyclerView recyclerView;
    private PatientForChatAdapter adapter;
    private List<Patient> patients;
    private List<ProfilePicture> pictures;

    private DatabaseReference mRefDoc;
    private DatabaseReference mRefPat;
    private DatabaseReference mReferencePictures;
    private FirebaseAuth mAuth;


    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        patients = new ArrayList<>();

        getPictures();

        return view;
    }

    void getPictures(){
        pictures = new ArrayList<>();
        mReferencePictures = FirebaseDatabase.getInstance().getReference("pictures");
        mReferencePictures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ProfilePicture p = ds.getValue(ProfilePicture.class);
                    pictures.add(p);
                }
                readPatients(pictures);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPatients(List<ProfilePicture> pictures) {

        adapter = new PatientForChatAdapter(getContext(),patients,pictures);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        mRefDoc = FirebaseDatabase.getInstance().getReference("Doctors").child(mAuth.getCurrentUser().getUid()).child("accessCode");
        mRefDoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long code = dataSnapshot.getValue(Long.class);
                mRefPat=FirebaseDatabase.getInstance().getReference("Patients");
                mRefPat.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                            String id = ds.child("healthSecurityCode").getValue(String.class);
                            String name = ds.child("name").getValue(String.class);
                            String pass = ds.child("password").getValue(String.class);
                            Integer doc = ds.child("doctor").getValue(Integer.class);
                            String email = ds.child("email").getValue(String.class);
                            Patient patient = new Patient(id,name,pass,email,doc);
                            Long checkCode = Long.valueOf(doc);
                            if(Objects.equals(code,checkCode)) {
                                patients.add(patient);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot ds) {
                        String id = ds.child("healthSecurityCode").getValue(String.class);
                        String name = ds.child("name").getValue(String.class);
                        String pass = ds.child("password").getValue(String.class);
                        Integer doc = ds.child("doctor").getValue(Integer.class);
                        String email = ds.child("email").getValue(String.class);
                        Patient patient = new Patient(id,name,pass,email,doc);
                        patients.remove(patient);
                        adapter.notifyDataSetChanged();
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
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
