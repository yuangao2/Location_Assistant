package com.example.zwan.a4;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;


public class Registration extends AppCompatActivity {
    private ImageView imageView;
    private EditText usernameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private Switch aSwitch;
    private Uri imageUri;

    private EditText familyEdit;
    private EditText invitationEdit;


    public void join(View view){
        if(imageUri == null){
            Toast.makeText(getApplicationContext(), "Please select a profile picture.", Toast.LENGTH_LONG).show();
        }
        else if(usernameEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your username.", Toast.LENGTH_LONG).show();
        }
        else if(emailEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your email.", Toast.LENGTH_LONG).show();
        }
        else if(passwordEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your password.", Toast.LENGTH_LONG).show();
        }
        else {
            AlertDialog.Builder joinDialog = new AlertDialog.Builder(Registration.this);
            joinDialog.setTitle("Please type your invitation code");
            View dialogView = LayoutInflater.from(Registration.this).inflate(R.layout.join_dialog, null);
            joinDialog.setView(dialogView);
            invitationEdit = (EditText) dialogView.findViewById(R.id.invitation_code);
            joinDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (invitationEdit.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Please type your invitation code.", Toast.LENGTH_LONG).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String requestURL = "https://people.cs.clemson.edu/~zwan/android/joinfamily.php";
                                String charset = "UTF-8";
                                File file = new File(imageUri.getPath());
                                try {
                                    FileUploader multipart = new FileUploader(requestURL, charset);
                                    multipart.addHeaderField("User-Agent", "CodeJava");
                                    multipart.addHeaderField("Test-Header", "Header-Value");
                                    multipart.addFormField("username", usernameEdit.getText().toString());
                                    multipart.addFormField("email", emailEdit.getText().toString());
                                    multipart.addFormField("password", passwordEdit.getText().toString());
                                    multipart.addFormField("guardian", String.valueOf(aSwitch.isChecked()));

                                    multipart.addFormField("invitation", invitationEdit.getText().toString());

                                    multipart.addFilePart("fileUpload", file);

                                    List<String> response = multipart.finish();

                                    System.out.println("SERVER REPLIED:");

                                    for (String line : response) {
                                        System.out.println(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }).start();
                    }
                }
            });
            joinDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            joinDialog.show();
        }
    }

    public void create(View view){
        if(imageUri == null){
            Toast.makeText(getApplicationContext(), "Please select a profile picture.", Toast.LENGTH_LONG).show();
        }
        else if(usernameEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your username.", Toast.LENGTH_LONG).show();
        }
        else if(emailEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your email.", Toast.LENGTH_LONG).show();
        }
        else if(passwordEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type your password.", Toast.LENGTH_LONG).show();
        }
        else {
            AlertDialog.Builder createDialog = new AlertDialog.Builder(Registration.this);
            createDialog.setTitle("Please create a name for your family");
            View dialogView = LayoutInflater.from(Registration.this).inflate(R.layout.create_dialog, null);
            createDialog.setView(dialogView);
            familyEdit = (EditText) dialogView.findViewById(R.id.family_name);
            createDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (familyEdit.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Please type your family name.", Toast.LENGTH_LONG).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String requestURL = "https://people.cs.clemson.edu/~zwan/android/createfamily.php";
                                String charset = "UTF-8";
                                /*
                                imageView.setDrawingCacheEnabled(true);
                                Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
                                imageView.setDrawingCacheEnabled(false);
                                File file = new File("path");
                                OutputStream os = null;
                                try {
                                    os = new BufferedOutputStream(new FileOutputStream(file));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                                File file = new File(imageUri.getPath());
                                try {
                                    FileUploader multipart = new FileUploader(requestURL, charset);
                                    multipart.addHeaderField("User-Agent", "CodeJava");
                                    multipart.addHeaderField("Test-Header", "Header-Value");
                                    multipart.addFormField("username", usernameEdit.getText().toString());
                                    multipart.addFormField("email", emailEdit.getText().toString());
                                    multipart.addFormField("password", passwordEdit.getText().toString());
                                    multipart.addFormField("guardian", String.valueOf(aSwitch.isChecked()));

                                    multipart.addFormField("family", familyEdit.getText().toString());
                                    String invitation = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                                    //System.out.println(invitation);
                                    multipart.addFormField("invitation", invitation);

                                    multipart.addFilePart("fileUpload", file);

                                    List<String> response = multipart.finish();

                                    System.out.println("SERVER REPLIED:");

                                    for (String line : response) {
                                        System.out.println(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }).start();
                    }
                }
            });
            createDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            createDialog.show();
        }
    }

    public void choosePhoto(View view){
        /*
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
        */
        CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    /*
                    Bundle extras = data.getExtras();
                    // get the cropped bitmap
                    Bitmap thePic = extras.getParcelable("data");
                    imageView.setImageBitmap(thePic);
                    */
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUri = result.getUri();
                    imageView.setImageURI(imageUri);
                    //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        imageView = (ImageView) findViewById(R.id.imageView);
        usernameEdit = (EditText) findViewById(R.id.editText);
        emailEdit = (EditText) findViewById(R.id.editText2);
        passwordEdit = (EditText) findViewById(R.id.editText3);
        aSwitch = (Switch) findViewById(R.id.switch1);
    }
}
