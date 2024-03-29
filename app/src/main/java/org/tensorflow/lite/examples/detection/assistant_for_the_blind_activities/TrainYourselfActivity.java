package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import static com.google.android.gms.tasks.Tasks.await;

import static org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.CropperActivity.RESULT_OK_CROP_IMAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.adapters.TrainYourselfObjectAdapter;
import org.tensorflow.lite.examples.detection.helpers.RotateImage;
import org.tensorflow.lite.examples.detection.models.SelectedCoordinates;
import org.tensorflow.lite.examples.detection.models.TrainYourselfDbObject;
import org.tensorflow.lite.examples.detection.models.TrainYourselfDbRequestObject;
import org.tensorflow.lite.examples.detection.models.TrainYourselfObject;
import org.tensorflow.lite.examples.detection.utils.PathUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrainYourselfActivity extends AppCompatActivity
        implements View.OnClickListener {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    public static final int REQUEST_TAKE_PHOTO = 102;
    public static final int REQUEST_TAKE_FROM_GALLERY = 103;
    public static final int REQUEST_CROP_IMAGE = 104;

    private AppCompatButton addObjectButton;
    private AppCompatButton trainYourselfButton;
    private RecyclerView objectsRecyclerview;

    private List<TrainYourselfObject> trainYourselfObjects;
    private int currentObjectIndex;
    private Uri addedImageUri;
    private TrainYourselfObjectAdapter adapter;
    private Dialog dialog;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference mDatabase;
    private FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_yourself);

        addObjectButton = findViewById(R.id.add_object_button);
        trainYourselfButton = findViewById(R.id.train_yourself_button);
        objectsRecyclerview = findViewById(R.id.objects_recyclerView);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        fStore = FirebaseFirestore.getInstance();

        trainYourselfButton.setOnClickListener(this);
        addObjectButton.setOnClickListener(this);
        trainYourselfObjects = new LinkedList<>();

        dialog = new Dialog(TrainYourselfActivity.this);
        adapter = new TrainYourselfObjectAdapter(trainYourselfObjects, TrainYourselfActivity.this, dialog);
        objectsRecyclerview.setAdapter(adapter);
        objectsRecyclerview.setLayoutManager(new LinearLayoutManager(TrainYourselfActivity.this));
    }


    // function to check permission
    public static boolean checkAndRequestPermissions(final Activity context) {
        int WExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);  // TODO: Write istendi, Read istenmedi?
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                                     REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    // Handled permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(TrainYourselfActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Kamera erişimi gereklidir.", Toast.LENGTH_SHORT)
                            .show();
                } else if (ContextCompat.checkSelfPermission(TrainYourselfActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Storage erişimi gereklidir.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // function to let's the user to choose image from camera or gallery
    public void chooseImage(Context context, int position){
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Exit" }; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        currentObjectIndex = position;
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionsMenu[i].equals("Take Photo")){
                    // Open the camera and get the photo
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
                    addedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, addedImageUri);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
                else if(optionsMenu[i].equals("Choose from Gallery")){
                    // choose from  external storage
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(pickPhoto , REQUEST_TAKE_FROM_GALLERY);
                }
                else if (optionsMenu[i].equals("Exit")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:  // Take photo result
                    if (resultCode == RESULT_OK) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), addedImageUri);
                            String filePath = PathUtil.getPath(getApplicationContext(), addedImageUri);
                            bitmap = RotateImage.getCorrectlyOrientedImage(TrainYourselfActivity.this, bitmap, filePath);
                            // bitmap = RotateImage.getCorrectlyOrientedImage(TrainYourselfActivity.this, addedImageUri, 10000);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }

                        trainYourselfObjects.get(currentObjectIndex).addImageToImageList(bitmap);
                        trainYourselfObjects.get(currentObjectIndex).addImageUriToImageUrisList(addedImageUri);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case REQUEST_TAKE_FROM_GALLERY:  // Get from gallery result
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            // Get the Image from data
                            String[] filePathColumn = { MediaStore.Images.Media.DATA };
                            if(data.getData() !=null){

                                Uri mImageUri = data.getData();
                                Log.i("uri_test", mImageUri.toString());

                                // Get the cursor
                                Cursor cursor = getContentResolver().query(mImageUri,
                                        filePathColumn, null, null, null);
                                // Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);

                                // Exif information
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                bitmap = RotateImage.getCorrectlyOrientedImage(TrainYourselfActivity.this, bitmap, picturePath);

                                trainYourselfObjects.get(currentObjectIndex).addImageToImageList(bitmap);
                                trainYourselfObjects.get(currentObjectIndex).addImageUriToImageUrisList(mImageUri);
                                adapter.notifyDataSetChanged();
                                cursor.close();

                            }
                            else {
                                if (data.getClipData() != null) {
                                    ClipData mClipData = data.getClipData();
                                    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                                        ClipData.Item item = mClipData.getItemAt(i);
                                        Uri uri = item.getUri();
                                        mArrayUri.add(uri);
                                        // Get the cursor
                                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                        // Move to first row
                                        cursor.moveToFirst();

                                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                        String picturePath = cursor.getString(columnIndex);
                                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                        bitmap = RotateImage.getCorrectlyOrientedImage(TrainYourselfActivity.this, bitmap, picturePath);
                                        trainYourselfObjects.get(currentObjectIndex).addImageToImageList(bitmap);
                                        trainYourselfObjects.get(currentObjectIndex).addImageUriToImageUrisList(uri);
                                        cursor.close();
                                    }
                                    adapter.notifyDataSetChanged();
                                    Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                    break;
                case REQUEST_CROP_IMAGE:
                    if(resultCode == RESULT_OK_CROP_IMAGE){
                         String result = data.getStringExtra("result");
                         int objectIndex = data.getIntExtra("objectIndex", -1);
                         int imageIndex = data.getIntExtra("imageIndex", -1);
                         int minX = data.getIntExtra("minX", -1);
                         int maxX = data.getIntExtra("maxX", -1);
                         int minY = data.getIntExtra("minY", -1);
                         int maxY = data.getIntExtra("maxY", -1);
                         Uri resultUri = null;
                         if(result != null){
                             resultUri = Uri.parse(result);
                         }

                         // Seçilmiş alanı kaydet.
                        trainYourselfObjects.get(objectIndex).getSelectedCoordinatesList().
                                set(imageIndex, new SelectedCoordinates(minX, minY, maxX, maxY));
                         adapter.notifyDataSetChanged();

                         // IMP: Sadece crop edilmiş kısım tutuluyordu, şimdi tüm resim ve seçilmiş alan tutulacak.
                         /*
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                            trainYourselfObjects.get(objectIndex).getImagesList().set(imageIndex, bitmap);
                            trainYourselfObjects.get(objectIndex).getImageUrisList().set(imageIndex, resultUri);
                            adapter.notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        */
                    }
            }
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.train_yourself_button){
            /*
            TrainRequest
                Objects[]
                    Object1
                            storageLink1
                            storageLink2
                            storageLink3
                    Object2
                            storageLink1
                            storageLink2
                 IsCompleted: boolean
             */

            // Seçilen tüm image'lar Firebase'e upload edilir ve linkleri bir listeye eklenir.
            try {
                uploadAllImagesAndCreateTrainRequestInDb();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


        }
        else if(v.getId() == R.id.add_object_button){
            showNewObjectButton();
        }

    }

    interface Callback {
        void onSuccess(String value);
    }

    private void uploadAllImagesAndCreateTrainRequestInDb() throws IOException, URISyntaxException {
        int trainYourselfObjectIndex = 0;
        for(TrainYourselfObject trainYourselfObject : trainYourselfObjects){
            int imageIndex = 0;
            for(Uri uri : trainYourselfObject.getImageUrisList()){

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                String filePath = PathUtil.getPath(getApplicationContext(), uri);
                bitmap = RotateImage.getCorrectlyOrientedImage(TrainYourselfActivity.this, bitmap, filePath);

                int finalTrainYourselfObjectIndex = trainYourselfObjectIndex;
                int finalImageIndex = imageIndex;
                uploadImage(bitmap, new Callback() {
                    @Override
                    public void onSuccess(String value) {
                        trainYourselfObject.addNewLinkToStorageLinksList(value);
                        // Finish
                        if(finalTrainYourselfObjectIndex == trainYourselfObjects.size() - 1
                            && finalImageIndex == trainYourselfObject.getImageUrisList().size() - 1){

                            List<TrainYourselfDbObject> trainYourselfDbObjects = new ArrayList<>();
                            for(TrainYourselfObject object : trainYourselfObjects){
                                trainYourselfDbObjects.add(
                                        new TrainYourselfDbObject(
                                                object.getObjectName(),
                                                object.getStorageLinksList(),
                                                object.getSelectedCoordinatesList()
                                        )
                                );
                            }

                            TrainYourselfDbRequestObject requestObject =
                                    new TrainYourselfDbRequestObject(trainYourselfDbObjects, false);

                            //final DocumentReference documentReference = fStore.collection("TrainRequests").document(mAuth.getCurrentUser().getUid());
                            //documentReference.set(requestObject);

                            mDatabase.child("TrainRequests").child(mAuth.getCurrentUser().getUid()).setValue(requestObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(TrainYourselfActivity.this, "Eğitim isteği kaydedildi! Model hazır olduğunda indirilecek.", Toast.LENGTH_LONG).show();
                                    trainYourselfObjects.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TrainYourselfActivity.this, "Eğitim isteğinin kaydedilmesinde problem oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                });
                imageIndex++;
            }

            trainYourselfObjectIndex++;
        }

    }

    private void uploadImage(Bitmap bitmap, Callback callback) {

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                progressDialog.dismiss();
                Toast.makeText(TrainYourselfActivity.this, "Failed " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onSuccess(uri.toString());
                    }
                } ).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i("TrainYourselfActivity", "Error while getting download url: " + exception.getMessage());
                    }
                } );

                progressDialog.dismiss();
                Toast.makeText(TrainYourselfActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int)progress + "%");
            }
        });

    }

    private void showNewObjectButton(){
        dialog.setContentView(R.layout.popup_new_object);

        ImageView closeIcon = dialog.findViewById(R.id.close_icon_username_change);
        final EditText etNewObject = dialog.findViewById(R.id.et_new_object_name);
        Button acceptUsernameButton = dialog.findViewById(R.id.popup_add_object_button);

        closeIcon.setOnClickListener(v -> dialog.dismiss());

        acceptUsernameButton.setOnClickListener(v -> {
            String newObjectName = etNewObject.getText().toString().trim();
            trainYourselfObjects.add(new TrainYourselfObject(newObjectName));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }




}