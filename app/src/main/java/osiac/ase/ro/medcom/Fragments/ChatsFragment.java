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
import java.util.Collections;
import java.util.List;

import osiac.ase.ro.medcom.Classes.Message;
import osiac.ase.ro.medcom.Classes.Patient;
import osiac.ase.ro.medcom.Classes.PatientForChatAdapter2;
import osiac.ase.ro.medcom.Classes.ProfilePicture;
import osiac.ase.ro.medcom.R;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PatientForChatAdapter2 adapter;
    private List<Patient> patients;
    private FirebaseAuth mAuth;
    private DatabaseReference mReferenceList;
    private DatabaseReference mReferencePatients;
    private DatabaseReference mReferencePictures;
    private List<String> patientList;
    private List<ProfilePicture> pictures;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();

        patients = new ArrayList<>();
        patientList = new ArrayList<>();

        mReferenceList = FirebaseDatabase.getInstance().getReference("Messages");
        mReferenceList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                patientList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if(message.getSender().equals(mAuth.getCurrentUser().getEmail())) {
                        if(!patientList.contains(message.getReceiver()))
                            patientList.add(message.getReceiver());
                        if(patientList.contains(message.getReceiver())) {
                            patientList.remove(message.getReceiver());
                            patientList.add(message.getReceiver());
                        }
                    }
                    if(message.getReceiver().equals(mAuth.getCurrentUser().getEmail())) {
                        if(!patientList.contains(message.getSender()))
                            patientList.add(message.getSender());
                        if(patientList.contains(message.getSender())){
                            patientList.remove(message.getSender());
                            patientList.add(message.getSender());
                        }
                    }
                }

                getPictures(patientList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    void getPictures(final List<String> patientList){
        pictures = new ArrayList<>();
        mReferencePictures = FirebaseDatabase.getInstance().getReference("pictures");
        mReferencePictures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ProfilePicture p = ds.getValue(ProfilePicture.class);
                    pictures.add(p);
                }
                chatList(patientList,pictures);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chatList(List<String> patientList, final List<ProfilePicture> pictures) {
        patients = new ArrayList<>();
        mReferencePatients = FirebaseDatabase.getInstance().getReference("Patients");
        final List<String> patientList2;
        patientList2 = new ArrayList<>();
        if(patientList.size()>0){
            for(int i=0;i<patientList.size();i++) {
                String temp=patientList.get(i).replace(";", ".");
                patientList2.add(temp);
            }
            mReferencePatients.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                    String id = ds.child("healthSecurityCode").getValue(String.class);
                    String name = ds.child("name").getValue(String.class);
                    String pass = ds.child("password").getValue(String.class);
                    Integer doc = ds.child("doctor").getValue(Integer.class);
                    String email = ds.child("email").getValue(String.class);
                    Patient patient = new Patient(id, name, pass, email, doc);
                    Collections.reverse(patientList2);
                    for (String m : patientList2) {
                        if (patient.getEmail().equals(m)) {
                            patients.add(patient);
                        }
                        adapter = new PatientForChatAdapter2(getContext(), patients,pictures);
                        recyclerView.setAdapter(adapter);
                    }
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
                    adapter = new PatientForChatAdapter2(getContext(),patients,pictures);
                    recyclerView.setAdapter(adapter);
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

}
