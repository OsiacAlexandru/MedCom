package osiac.ase.ro.medcom.Activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import osiac.ase.ro.medcom.Classes.ProfilePicture;
import osiac.ase.ro.medcom.R;

public class ManageAccountActivity extends AppCompatActivity {

    private Button bttnChangeEmail;
    private Button bttnChangePass;
    private Button bttnInfo;
    private Button bttnDelete;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase1;
    private FirebaseDatabase mDatabase2;
    private FirebaseUser user;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView image;

    // profile pic

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private StorageTask<UploadTask.TaskSnapshot> mUploadTask;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_manage_account);

        image=findViewById(R.id.imageViewUser);

        registerForContextMenu(image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContextMenu(image);
            }
        });

        bttnChangeEmail = findViewById(R.id.bttnChangeEmail);
        bttnChangePass = findViewById(R.id.bttnChangePass);
        bttnInfo = findViewById(R.id.bttnInstructions);
        bttnDelete = findViewById(R.id.bttnDeleteAccount);


        bttnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogEmail();
            }
        });

        bttnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });

        bttnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UsageInfoActivity.class);
                startActivity(intent);
            }
        });

        bttnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDelete();
            }
        });

        // profile pic

        mStorageReference = FirebaseStorage.getInstance().getReference("pictures");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("pictures");

        mAuth=FirebaseAuth.getInstance();

        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    ProfilePicture p = dataSnapshot.getValue(ProfilePicture.class);
                    if (p.getUid().equals(mAuth.getUid()))
                        Picasso.get().load(p.getImageUrl()).into(image);
                }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ProfilePicture p = dataSnapshot.getValue(ProfilePicture.class);
                if (p.getEmail().equals(mAuth.getUid()))
                    Picasso.get().load(p.getImageUrl()).into(image);
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
    }

    private void alertDialog() {

        TextView title = new TextView(this);
        title.setText(R.string.changePassTitleRes);
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

        TextView editTextOldPass = new TextView(context);
        editTextOldPass.setText("Confirmation for the old password");
        editTextOldPass.setTypeface(editTextOldPass.getTypeface(), Typeface.BOLD);
        editTextOldPass.setTextSize(18);
        editTextOldPass.setTextColor(Color.BLACK);
        editTextOldPass.setGravity(Gravity.LEFT);
        editTextOldPass.setPadding(15, 30, 15, 15);
        layout.addView(editTextOldPass);

        final EditText oldPass = new EditText(context);
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPass.setTextSize(18);
        layout.addView(oldPass);

        TextView editTextNewPass = new TextView(context);
        editTextNewPass.setText("Enter the new desired password");
        editTextNewPass.setTypeface(editTextOldPass.getTypeface(), Typeface.BOLD);
        editTextNewPass.setTextSize(18);
        editTextNewPass.setTextColor(Color.BLACK);
        editTextNewPass.setGravity(Gravity.LEFT);
        editTextNewPass.setPadding(15, 30, 15, 15);
        layout.addView(editTextNewPass);

        final EditText newPass = new EditText(context);
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPass.setTextSize(18);
        layout.addView(newPass);

        TextView editTextInfo = new TextView(context);
        editTextInfo.setText("You will be automatically signed out after this operation.");
        editTextInfo.setTypeface(editTextInfo.getTypeface(), Typeface.ITALIC);
        editTextInfo.setPadding(15, 60, 15, 30);
        editTextInfo.setTextSize(16);
        editTextInfo.setTextColor(Color.DKGRAY);
        editTextInfo.setGravity(Gravity.CENTER);
        layout.addView(editTextInfo);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(title);
        dialog.setView(layout);
        dialog.setPositiveButton("OK",null);

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().getAttributes().windowAnimations=R.style.DialogTheme;

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth = FirebaseAuth.getInstance();
                        user = mAuth.getCurrentUser();
                        final String userId = user.getUid();

                        mDatabase1 = FirebaseDatabase.getInstance();

                        DatabaseReference ref1check = mDatabase1.getReference("Doctors");

                        ref1check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(userId)) {
                                    DatabaseReference ref1 = mDatabase1.getReference("Doctors").child(userId).child("password");

                                    ref1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String pass = dataSnapshot.getValue(String.class);
                                            if (!check(pass, oldPass, newPass) == Boolean.FALSE)
                                            {
                                                changePassAuth(newPass.getText().toString());
                                                DatabaseReference ref1 = mDatabase1.getReference("Doctors");
                                                ref1.child(userId).child("password").setValue(newPass.getText().toString());
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                        });

                        mDatabase2 = FirebaseDatabase.getInstance();

                        DatabaseReference ref2check = mDatabase2.getReference("Patients");

                        ref2check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(userId)) {
                                    DatabaseReference ref2 = mDatabase2.getReference("Patients").child(userId).child("password");
                                    ref2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String pass = dataSnapshot.getValue(String.class);
                                            if (!check(pass, oldPass, newPass) == Boolean.FALSE)
                                            {
                                                changePassAuth(newPass.getText().toString());
                                                DatabaseReference ref2 = mDatabase2.getReference("Patients");
                                                ref2.child(userId).child("password").setValue(newPass.getText().toString());
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                dialog.dismiss();
                                                finish();
                                            }
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
                });
            }
        });
        alertDialog.show();
    }

    void alertDialogDelete() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("ACCOUNT DELETION");
        int color =  ResourcesCompat.getColor(getResources(), R.color.colorPrimary,getTheme());
        title.setBackgroundColor(color);
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextSize(20);

        alertDialogBuilder.setCustomTitle(title);
        alertDialogBuilder.setMessage("Are you sure you wanna delete your account?").setCancelable(false);

        alertDialogBuilder.setPositiveButton("YES",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                DatabaseReference delete = FirebaseDatabase.getInstance().getReference("Patients").child(uid);
                delete.removeValue();
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        Toast.makeText(getApplicationContext(), "Your profile is deleted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                finish();
            }});

        alertDialogBuilder.setNegativeButton("NO",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations=R.style.DialogTheme;
        alertDialog.show();
    }

    private void alertDialogEmail() {

        TextView title = new TextView(this);
        title.setText("EMAIL CHANGER");
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

        TextView editTextNewEmail = new TextView(context);
        editTextNewEmail.setText("Enter the new desired email");
        editTextNewEmail.setTypeface(editTextNewEmail.getTypeface(), Typeface.BOLD);
        editTextNewEmail.setTextColor(Color.BLACK);
        editTextNewEmail.setTextSize(18);
        editTextNewEmail.setGravity(Gravity.CENTER);
        editTextNewEmail.setPadding(15, 30, 15, 15);
        layout.addView(editTextNewEmail);

        final EditText newEmail = new EditText(context);
        newEmail.setTextSize(18);
        layout.addView(newEmail);

        TextView editTextInfo = new TextView(context);
        editTextInfo.setText("You will be automatically signed out after this operation.");
        editTextInfo.setTypeface(editTextInfo.getTypeface(), Typeface.ITALIC);
        editTextInfo.setPadding(15, 60, 15, 30);
        editTextInfo.setTextSize(18);
        editTextInfo.setTextColor(Color.DKGRAY);
        editTextInfo.setGravity(Gravity.CENTER);
        layout.addView(editTextInfo);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(title);
        dialog.setView(layout);
        dialog.setPositiveButton("OK",null);

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.cancelPassRes), Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().getAttributes().windowAnimations=R.style.DialogTheme;

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth = FirebaseAuth.getInstance();
                        user = mAuth.getCurrentUser();
                        final String userId = user.getUid();

                        mDatabase1 = FirebaseDatabase.getInstance();

                        DatabaseReference ref1check = mDatabase1.getReference("Doctors");

                        ref1check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(userId)) {
                                    DatabaseReference ref1 = mDatabase1.getReference("Doctors").child(userId).child("email");

                                    ref1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String email = dataSnapshot.getValue(String.class);
                                            if (!checkEmail(email,newEmail) == Boolean.FALSE)
                                            {
                                                changeEmailAuth(newEmail.getText().toString());
                                                DatabaseReference ref1 = mDatabase1.getReference("Doctors");
                                                ref1.child(userId).child("email").setValue(newEmail.getText().toString());
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                        });

                        mDatabase2 = FirebaseDatabase.getInstance();

                        DatabaseReference ref2check = mDatabase2.getReference("Patients");

                        ref2check.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(userId)) {
                                    DatabaseReference ref2 = mDatabase2.getReference("Patients").child(userId).child("email");
                                    ref2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String email = dataSnapshot.getValue(String.class);
                                            if (!checkEmail(email,newEmail) == Boolean.FALSE)
                                            {
                                                changeEmailAuth(newEmail.getText().toString());
                                                DatabaseReference ref2 = mDatabase2.getReference("Patients");
                                                ref2.child(userId).child("email").setValue(newEmail.getText().toString());
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                dialog.dismiss();
                                                finish();
                                            }
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
                });
            }
        });
        alertDialog.show();
    }

    Boolean check(String pass,TextView tvOldPass,TextView tvNewPass) {
        if (!tvOldPass.getText().toString().equals(pass)) {
            tvOldPass.setError(getString(R.string.oldPassError));
            return false;
        } else if (!(tvNewPass.getText().toString().length() > 6)) {
            tvNewPass.setError(getString(R.string.newPassError));
            return false;
        } else {
            return true;
        }
    }

    void changePassAuth(String newPass) {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.passsssres), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    Boolean checkEmail(String pass,TextView tvNewEmail) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tvNewEmail.getText()).matches()){
            tvNewEmail.setError("The email format is wrong!");
            return false;
        } else {
            return true;
        }
    }

    void changeEmailAuth(String newEmail) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user!=null) {
            user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Email address is updated.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.manage_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_option:
                method();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mAuth=FirebaseAuth.getInstance();
            if (mImageUri != null) {
                final StorageReference imgReference = mStorageReference.child(mAuth.getCurrentUser().getEmail());

                mUploadTask = imgReference.putFile(mImageUri);

                mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imgReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            ProfilePicture pic = new ProfilePicture(downloadUri.toString(),mAuth.getUid(),mAuth.getCurrentUser().getEmail());
                            mDatabaseReference.child(mAuth.getUid()).setValue(pic);
                            Picasso.get().load(downloadUri).into(image);
                            Toast.makeText(getApplicationContext(), "Updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Update failed:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {

    }

    void method(){
        openFileChooser();
    }
}
