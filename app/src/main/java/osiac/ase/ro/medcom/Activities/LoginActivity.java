package osiac.ase.ro.medcom.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import osiac.ase.ro.medcom.Classes.Doctor;
import osiac.ase.ro.medcom.Classes.Patient;
import osiac.ase.ro.medcom.R;

public class LoginActivity extends AppCompatActivity {

    private TextView tvEmail,tvPass;
    private Button bttnLogin,bttnBack;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_login);
        // TV
        tvEmail=findViewById(R.id.inputLoginEmail);
        tvPass=findViewById(R.id.inputLoginPass);
        // BUTTONS
        bttnLogin=findViewById(R.id.logBttnLogin);
        // PROGRESS BAR
        progressBar = findViewById(R.id.pbLogLoad);
        progressBar.setVisibility(View.INVISIBLE);
        // FIRE BASE
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();


        bttnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputValidation()!=true) {
                    displayToast(getString(R.string.regInputErrors));
                } else {
                    mAuth.signInWithEmailAndPassword(tvEmail.getText().toString(), tvPass.getText().toString())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    if(!task.isSuccessful()){
                                        progressBar.setVisibility(View.GONE);
                                       displayToast(getString(R.string.logInputErrors));
                                    } else {
                                        displayToast(getString(R.string.successfullySignedIn));
                                        searchDB();
                                    }
                                }
                            });
                }
            }
        });
    }

    // INPUT VALIDATION
    private Boolean inputValidation() {
        if (tvPass.getText().length() <= 5) {
            tvPass.setError(getString(R.string.logPassError));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tvEmail.getText()).matches()) {
            tvEmail.setError(getString(R.string.logEmailError));
            return false;
        }
        return true;
    }

    // METHOD FOR DISPLAYING A TEXT MESSAGE THROUGH TOAST
    private void displayToast(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    private void searchDB()
    {
        FirebaseUser user =  mAuth.getCurrentUser();
        String userId = user.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("Patients").orderByKey().equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent intent = new Intent(getApplicationContext(),PatientPageActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(),DoctorPageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

}
