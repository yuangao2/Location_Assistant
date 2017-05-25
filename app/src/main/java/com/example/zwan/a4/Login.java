package com.example.zwan.a4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Login extends AppCompatActivity {
    public static final int SHOW_RESPONSE = 0;
    private EditText emailEdit;
    private EditText passwordEdit;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    if(response.equals("fail")){
                        Toast.makeText(getApplicationContext(), "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int uid = jsonObject.getInt("uid");
                            int fid = jsonObject.getInt("fid");
                            // String username = jsonObject.getString("");
                            String invitation = jsonObject.getString("invitation");
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            //intent.putExtra("uid", uid);
                            //intent.putExtra("fid", fid);
                            //intent.putExtra("invitation", invitation);
                            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                            editor.putInt("uid", uid);
                            editor.putInt("fid", fid);
                            // editor.putString("username", username);
                            editor.putString("invitation", invitation);
                            editor.commit();
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    };

    public void register(View view){
        Intent intent = new Intent(Login.this, Registration.class);
        startActivity(intent);
        finish();
    }

    public void login(View view){
        if(emailEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type email.", Toast.LENGTH_LONG).show();
        }
        else if(passwordEdit.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(), "Please type password.", Toast.LENGTH_LONG).show();
        }
        else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String requestURL = "https://people.cs.clemson.edu/~zwan/android/login.php";
                    String charset = "UTF-8";
                    try {
                        FileUploader multipart = new FileUploader(requestURL, charset);
                        multipart.addHeaderField("User-Agent", "CodeJava");
                        multipart.addHeaderField("Test-Header", "Header-Value");
                        multipart.addFormField("email", emailEdit.getText().toString());
                        multipart.addFormField("password", passwordEdit.getText().toString());

                        List<String> response = multipart.finish();

                        System.out.println("SERVER REPLIED:");
                        System.out.println(response.get(1));
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response.get(1);
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEdit = (EditText) findViewById(R.id.editText4);
        passwordEdit = (EditText) findViewById(R.id.editText5);
    }
}
