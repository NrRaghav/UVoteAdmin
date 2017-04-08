package nraghav.example.com.uvoteadmin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikelau.croperino.Croperino;
import com.mikelau.croperino.CroperinoConfig;
import com.mikelau.croperino.CroperinoFileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference ref;
    private StorageReference imagesRef;
    private  ImageButton mProfile;
    long esttime;
    private long estimatedServerTimeMs;
    private String photoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://u-vote-1f66c.appspot.com");
        ref = FirebaseDatabase.getInstance().getReference();

        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", Environment.getExternalStorageDirectory().getPath());
        CroperinoFileUtil.setupDirectory(MainActivity.this);
        mProfile = (ImageButton)findViewById(R.id.picture);

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEstimatedServerTimeMs();

                imagesRef = storageRef.child("images").child("parties");

                if (CroperinoFileUtil.verifyStoragePermissions(MainActivity.this))
                    prepareChooser();
            }
        });
        Button confirm = (Button)findViewById(R.id.confirm);
        final TextView text = (TextView)findViewById(R.id.text);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String a = text.getText().toString();
                if (isEmpty(a)) {
                    Toast.makeText(MainActivity.this, "Enter description first!",
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                    offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
//                            long offset = snapshot.getValue(Long.class);
//                            estimatedServerTimeMs = System.currentTimeMillis() + offset;
//
//                            DateFormat dfm = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.UK);
//
//                            long unixtime=estimatedServerTimeMs;
//                            try {
//                                unixtime = dfm.parse(b+" "+c).getTime();
//                                unixtime=unixtime/1000;
//                            } catch (ParseException e1) {
//                                e1.printStackTrace();
//                            }
                            //DatabaseReference mDatabase = ref.child("surveys").child(String.valueOf(unixtime));
                            DatabaseReference mDatabase = ref.child("surveys");
                            survey p = new survey( photoUrl,a);
                            mDatabase.setValue(p);
                            Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
                            startActivity(myIntent);
                            finish();

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.err.println("Listener was cancelled");
                        }
                    });
                }
            }
        });
    }
    private void prepareChooser() {
        Croperino.prepareChooser(MainActivity.this, "Change Picture", ContextCompat.getColor(MainActivity.this, android.R.color.background_dark));
    }

    private void prepareCamera() {
        Croperino.prepareCamera(MainActivity.this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CroperinoConfig.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), MainActivity.this, true, 1, 1, 0, 0);
                }
                break;
            case CroperinoConfig.REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    CroperinoFileUtil.newGalleryFile(data, MainActivity.this);
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), MainActivity.this, true, 1, 1, 0, 0);
                }
                break;
            case CroperinoConfig.REQUEST_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getmFileTemp());
                    mProfile.setImageURI(i);
                    UploadTask uploadTask = imagesRef.putFile(i);

// Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            photoUrl = downloadUrl.toString();
                        }
                    });
                    //Do saving / uploading of photo method here.
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CroperinoFileUtil.REQUEST_CAMERA) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(android.Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        prepareCamera();
                    }
                }
            }
        } else if (requestCode == CroperinoFileUtil.REQUEST_EXTERNAL_STORAGE) {
            boolean wasReadGranted = false;
            boolean wasWriteGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasReadGranted = true;
                    }
                }
                if (permission.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasWriteGranted = true;
                    }
                }
            }

            if (wasReadGranted && wasWriteGranted) {
                prepareChooser();
            }
        }

    }



    private void getEstimatedServerTimeMs() {

        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                esttime = System.currentTimeMillis() + offset;
                imagesRef = storageRef.child("images").child("parties").child(String.valueOf(esttime));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

    }
}
