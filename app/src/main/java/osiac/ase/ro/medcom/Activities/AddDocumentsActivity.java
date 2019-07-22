package osiac.ase.ro.medcom.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import osiac.ase.ro.medcom.Classes.Document;
import osiac.ase.ro.medcom.R;

public class AddDocumentsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    //Variables
    private ImageView image;
    private EditText imageName;
    private EditText imageDesc;
    private Button btnUpload;
    private Button btnChoose;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    private ProgressBar mProgressBar;
    private Uri mImageUri;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private StorageTask<UploadTask.TaskSnapshot> mUploadTask;

    private String patientEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_add_documents);

        image = findViewById(R.id.image_view);

        btnUpload = findViewById(R.id.bttnUp);
        btnChoose = findViewById(R.id.bttnChoose);
        imageName = findViewById(R.id.tvFileName);
        imageDesc = findViewById(R.id.tvDesc);

        Intent intent = getIntent();
        if(intent!=null) {
            patientEmail = intent.getStringExtra("email");
        }

        mAuth = FirebaseAuth.getInstance();


        mStorageReference = FirebaseStorage.getInstance().getReference("documents");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("documents");

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
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
            Picasso.get().load(mImageUri).into(image);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference imgReference = mStorageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

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
                        Document doc = new Document(imageName.getText().toString().trim(),downloadUri.toString(),patientEmail,imageDesc.getText().toString());
                        String uploadId = mDatabaseReference.push().getKey();
                        mDatabaseReference.child(uploadId).setValue(doc);
                        Toast.makeText(AddDocumentsActivity.this, "Uploaded successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddDocumentsActivity.this, "Upload failed:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            Toast.makeText(getApplicationContext(),"Please select an image first!",Toast.LENGTH_LONG).show();
        }
}
}
