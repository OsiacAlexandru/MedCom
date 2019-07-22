package osiac.ase.ro.medcom.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import osiac.ase.ro.medcom.Classes.Patient;
import osiac.ase.ro.medcom.Classes.PatientAdapter;
import osiac.ase.ro.medcom.R;

public class PatientListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ProgressBar pb ;

    String mEmail;

    private Menu menu;

    private PatientAdapter adapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_patient_list);

        pb = findViewById(R.id.pbLoad);

        pb.setVisibility(View.VISIBLE);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = currentFirebaseUser.getUid();
        final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference ref1 = mDatabase.getReference().child("Doctors");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    DatabaseReference ref2 = mDatabase.getReference("Doctors").child(uid).child("accessCode");
                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final Long code = dataSnapshot.getValue(Long.class);
                            DatabaseReference ref3 = mDatabase.getReference().child("Patients");
                            ValueEventListener valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ArrayList<Patient> list = new ArrayList<>();
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String id = ds.child("healthSecurityCode").getValue(String.class);
                                        String name = ds.child("name").getValue(String.class);
                                        String pass = ds.child("password").getValue(String.class);
                                        Integer doc = ds.child("doctor").getValue(Integer.class);
                                        String email = ds.child("email").getValue(String.class);
                                        Patient patient = new Patient(id,name,pass,email,doc);
                                        Long checkCode = Long.valueOf(doc);
                                        if(Objects.equals(code,checkCode)) {
                                            list.add(patient);
                                        }
                                    }
                                    final ListView lv = findViewById(R.id.lvPatients);
                                    try {
                                        adapter = new PatientAdapter(getApplicationContext(),list);
                                    } catch (CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                    lv.setAdapter(adapter);
                                    pb.setVisibility(View.INVISIBLE);
                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Patient pat =  (Patient)lv.getItemAtPosition(position);
                                            mEmail=pat.getEmail();
                                            documentsDialog(mEmail);
                                        }
                                    });
                                }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                };
                            ref3.addValueEventListener(valueEventListener);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void documentsDialog(String email) {

        TextView title = new TextView(this);
        title.setText("PATIENT DOCUMENTS");
        int color =  ResourcesCompat.getColor(getResources(), R.color.colorPrimary,getTheme());
        title.setBackgroundColor(color);
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextSize(20);

        Context context = getApplicationContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(getApplicationContext());
        tv.setText(email);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(title.getTypeface(), Typeface.BOLD);
        tv.setTextSize(16);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(10, 10, 10, 10);
        layout.addView(tv);

        Button button1 = new Button(getApplicationContext());
        button1.setText("VIEW DOCUMENTS");
        button1.setTextSize(18);
        button1.setTypeface(title.getTypeface(), Typeface.BOLD);
        button1.setTextColor(Color.WHITE);
        button1.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0,30,0,10);
        button1.setLayoutParams(params);
        button1.setPadding(15, 15, 15, 15);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.dialog_button);
        button1.setBackground(drawable);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ViewDocumentsActivity.class);
                intent.putExtra("email",mEmail);
                startActivity(intent);
            }
        });

        layout.addView(button1);

        Button button2 = new Button(getApplicationContext());
        button2.setText("ADD DOCUMENTS");
        button2.setTextSize(18);
        button2.setTypeface(title.getTypeface(), Typeface.BOLD);
        button2.setTextColor(Color.WHITE);
        button2.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.setMargins(0,30,0,0);
        button2.setLayoutParams(params2);
        button2.setPadding(15, 15, 15, 15);
        button2.setBackground(drawable);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(),AddDocumentsActivity.class);
               intent.putExtra("email",mEmail);
               startActivity(intent);
            }
        });

        layout.addView(button2);

        Button button3 = new Button(getApplicationContext());
        button3.setText("SEND MESSAGE");
        button3.setTextSize(18);
        button3.setTypeface(title.getTypeface(), Typeface.BOLD);
        button3.setTextColor(Color.WHITE);
        button3.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        params3.setMargins(0,30,0,0);
        button3.setLayoutParams(params3);
        button3.setPadding(15, 15, 15, 15);
        button3.setBackground(drawable);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DoctorMessageActivity.class);
                intent.putExtra("email",mEmail);
                startActivity(intent);
            }
        });

        layout.addView(button3);


        layout.setGravity(Gravity.CENTER);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(title);
        dialog.setView(layout);

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().getAttributes().windowAnimations=R.style.DialogTheme;
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.patient_list_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.id_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Patients");
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Query Inserted", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return true;
    }
}


