package osiac.ase.ro.medcom.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import osiac.ase.ro.medcom.Classes.Doctor;
import osiac.ase.ro.medcom.Classes.Message;
import osiac.ase.ro.medcom.Classes.Patient;
import osiac.ase.ro.medcom.R;

public class RegisterScreenActivity extends AppCompatActivity {

    private Button bttnCancel,bttnRegister;

    private TextView tvName,tvEmail,tvPass,tvPassC,tvHealthCode,tvDoctorId;

    private ProgressBar progressBar;

    //FIRE BASE
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_register_screen);

        bttnRegister=findViewById(R.id.bttnRegister);

        tvName=findViewById(R.id.inputRegisterName);
        tvPass=findViewById(R.id.inputRegisterPass);
        tvPassC=findViewById(R.id.inputRegisterPassConfirm);
        tvEmail=findViewById(R.id.inputLoginEmail);
        tvHealthCode=findViewById(R.id.inputHealthSecurityCode);
        tvDoctorId=findViewById(R.id.inputDoctorId);

        // PROGRESS BAR
        progressBar = findViewById(R.id.pbRegLoad);
        progressBar.setVisibility(View.INVISIBLE);

        // FIRE BASE
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // BUTTONS
        bttnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });

    }


    // METHOD FOR VALIDATING USER INPUT
    private void Register() {
        final FirebaseAuth mAuth2 = FirebaseAuth.getInstance();
        mAuth2.signInWithEmailAndPassword("email.proba.cv@ceva.com","29091969");
        final ArrayList<String> idList = new ArrayList<>();
        DatabaseReference mCheck = FirebaseDatabase.getInstance().getReference("Doctors");
        mCheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long access = snapshot.child("accessCode").getValue(Long.class);
                    String accessCode = access.toString();
                    idList.add(accessCode);
                }
                boolean check = checkID(idList);
                if(!check) {
                    displayToast(getString(R.string.regInputErrors));
                } else {
                    String email = tvEmail.getText().toString();
                    String password = tvPass.getText().toString();
                    String healthCode = tvHealthCode.getText().toString();
                    Integer doctorId=Integer.parseInt(tvDoctorId.getText().toString());
                    final Patient patient = new Patient(healthCode,tvName.getText().toString(), password, email, doctorId);

                    progressBar.setVisibility(View.VISIBLE);
                    // FIRE BASE AUTHENTICATION
                    mAuth.createUserWithEmailAndPassword(patient.getEmail(),patient.getPassword())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        addToTheDatabase(patient);
                                        sendVerificationEmail();
                                        displayToast(getString(R.string.fireBaseAuthSuccess));
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        mAuth2.signOut();
                                        progressBar.setVisibility(View.GONE);
                                        finish();
                                    } else{
                                        displayToast(getString(R.string.fireBaseAuthError));
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    boolean checkID(ArrayList<String> idList){
        int count = 0;
        for(String s:idList){
            if(s.equals(tvDoctorId.getText().toString())){
                count++;
            }
        }
        if (tvName.getText().length() <= 5) {
            tvName.setError(getString(R.string.regNameError));
            return false;
        } else if (tvPass.getText().length() <= 5) {
            tvPass.setError(getString(R.string.regPassError));
            return false;
        } else if (!tvPassC.getText().toString().equals(tvPass.getText().toString())) {
            tvPassC.setError(getString(R.string.regPassConfirmError));
            return false;
        }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tvEmail.getText()).matches()) {
            tvEmail.setError(getString(R.string.regEmailError));
            return false;
        }else if (tvHealthCode.getText().length() != 20 || tvHealthCode.getText().length() < 0) {
            tvHealthCode.setError(getString(R.string.regHealthCodeError));
            return false;
        } else if (tvDoctorId.getText().length() < 1 || count == 0) {
            tvDoctorId.setError(getString(R.string.regDoctorIdError));
            return false;
        }
        return true;
    }

    // METHOD FOR DISPLAYING A TEXT MESSAGE THROUGH TOAST
    private void displayToast(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    //VERIFICATION EMAIL SENDER (DEFAULT BY FIRE BASE)
    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            // email not sent
                            displayToast(getString(R.string.authEmailError));
                        }
                    }
                });
    }

    // ADD DATA TO THE DATABASE OF THE USER
    private void addToTheDatabase(Patient patient) {
        try {
            FirebaseUser user =  mAuth.getCurrentUser();
            String userId = user.getUid();
            mDatabase.child("Patients").child(userId).setValue(patient);
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(),getString(R.string.errorRes)+e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
