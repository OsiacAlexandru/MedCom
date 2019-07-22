package osiac.ase.ro.medcom.Activities;

import android.app.DownloadManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import osiac.ase.ro.medcom.Classes.Document;
import osiac.ase.ro.medcom.Classes.DocumentAdapter;
import osiac.ase.ro.medcom.Classes.ZoomImageView;
import osiac.ase.ro.medcom.R;

public class ViewDocumentsActivity extends AppCompatActivity implements DocumentAdapter.OnItemClickListener {

    private static final String TAG = ViewDocumentsActivity.class.getSimpleName();
    private RecyclerView rv;
    private DocumentAdapter adapter;
    private ProgressBar pb;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private List<Document> documents;
    private long downloadID;
    private static final int uniId = 123;
    Notification.Builder builder;

    private ValueEventListener mDBListener;

    private String patientEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_view_documents);

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        Intent intent = getIntent();
        if (intent != null) {
            patientEmail = intent.getStringExtra("email");
        }

        builder = new Notification.Builder(this);
        builder.setAutoCancel(true);

        rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        pb = findViewById(R.id.pb_doc);
        documents = new ArrayList<>();

        adapter = new DocumentAdapter(getApplicationContext(), documents);

        rv.setAdapter(adapter);

        storage = FirebaseStorage.getInstance();
        adapter.setOnItemClickListener(ViewDocumentsActivity.this);

        databaseReference = FirebaseDatabase.getInstance().getReference("documents");
        mDBListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                documents.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Document doc = postSnapshot.getValue(Document.class);
                    doc.setmKey(postSnapshot.getKey());
                    if (doc.getmPatientEmail().equals(patientEmail))
                        documents.add(doc);
                }
                adapter.notifyDataSetChanged();
                pb.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                pb.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onWhatEverClick(int position) {
        fileDownload(documents.get(position).getImageUrl(), position);
    }

    @Override
    public void onDeleteClick(int position) {
        Document selectedItem = documents.get(position);
        final String selectedKey = selectedItem.getmKey();
        StorageReference imageRef = storage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference ref = databaseReference.child(selectedKey);
                ref.removeValue();
                Toast.makeText(getApplicationContext(), "Item deleted" + selectedKey, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void fileDownload(final String url, final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_DialogWhenLarge_NoActionBar);

        Context context = getApplicationContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final ZoomImageView imageView = new ZoomImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(params);
        imageView.setPadding(15, 15, 15, 15);
        Picasso.get().load(url).into(imageView);
        layout.addView(imageView);

        alertDialogBuilder.setView(layout);

        alertDialogBuilder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                download2(url, documents.get(position).getName());
                Toast.makeText(getApplicationContext(),"Successfully saved file.\nCheck Medcom folder in Gallery.",Toast.LENGTH_LONG).show();
            }
        });

        alertDialogBuilder.setNegativeButton("BACK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mDBListener);
        unregisterReceiver(onDownloadComplete);
    }

    void download1(ZoomImageView imageView) {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/Medcom");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);
    }

    void download2(String url, String name) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(name + ".jpg");
        request.setDescription("Downloading " + name + ".jpg");
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Medcom/" + "/" + name + ".png");
        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
        Log.e(TAG, "" + downloadID);
    }



    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           // Toast.makeText(getApplicationContext(),"Successfully saved file.\nCheck Medcom folder in Gallery.",Toast.LENGTH_LONG).show();
        }
    };
}

