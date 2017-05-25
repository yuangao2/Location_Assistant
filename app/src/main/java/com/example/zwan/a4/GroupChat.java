package com.example.zwan.a4;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupChat extends AppCompatActivity {
    public static final int SHOW_RESPONSE = 0;
    private ListView msgLisView;
    private EditText inputText;
    private Button send;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<Msg>();

    private int uid;
    private int fid;
    private String pic;
    private String userName;
    private long currentTime;

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    Log.e("message", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int thisuid = jsonObject.getInt("UId");
                            int mid = jsonObject.getInt("MId");
                            Log.e("mid", String.valueOf(mid));
                            String content = jsonObject.getString("Content");
                            currentTime = jsonObject.getLong("4");
                            String picture = jsonObject.getString("Picture");
                            Log.e("content", content );
                            Log.e("time", String.valueOf(currentTime) );
                            Log.e("pic", picture );
                            ContentValues values = new ContentValues();
                            //values.put("id", mid);
                            values.put("content", content);
                            values.put("picture", picture);
                            values.put("time", currentTime);
                            if(thisuid==uid){
                                values.put("type", 1);
                            }
                            else{
                                values.put("type", 0);
                            }
                            db.insert("message", null, values);
                            if(thisuid!=uid) {
                                Msg message = new Msg(content, Msg.TYPE_RECEIVED, picture);
                                msgList.add(message);
                            }
                            //refresh adapter when there is a new message
                            adapter.notifyDataSetChanged();
                            //scroll ListView to last roll
                            msgLisView.setSelection(msgList.size());

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        currentTime = 0;
        uid = pref.getInt("uid", 0);
        fid = pref.getInt("fid", 0);
        pic = pref.getString("pic", "");
        userName = pref.getString("name", "y");

        adapter = new MsgAdapter(GroupChat.this, R.layout.msg_item_layout, msgList);
        inputText = (EditText)findViewById(R.id.input_text);
        send = (Button)findViewById(R.id.send_button);
        msgLisView = (ListView)findViewById(R.id.list_view);
        msgLisView.setAdapter(adapter);

        dbHelper = new MyDatabaseHelper(this, "Message.db", null, 1);
        db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("message", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String picture = cursor.getString(cursor.getColumnIndex("picture"));
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                Msg msg = new Msg(content, type, picture);
                msgList.add(msg);
                if(time>currentTime){
                    currentTime=time;
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        msgLisView.setSelection(msgList.size());

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = inputText.getText().toString();
                if(!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT, pic);
                    msgList.add(msg);
                    //refresh adapter when there is a new message
                    adapter.notifyDataSetChanged();
                    //scroll ListView to last roll
                    msgLisView.setSelection(msgList.size());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String requestURL = "https://people.cs.clemson.edu/~zwan/android/message.php";
                            String charset = "UTF-8";
                            try {
                                FileUploader multipart = new FileUploader(requestURL, charset);
                                multipart.addHeaderField("User-Agent", "CodeJava");
                                multipart.addHeaderField("Test-Header", "Header-Value");
                                multipart.addFormField("content", content);
                                multipart.addFormField("uid", String.valueOf(uid));
                                multipart.addFormField("fid", String.valueOf(fid));
                                //currentTime = System.currentTimeMillis();
                                multipart.addFormField("time", Long.toString(System.currentTimeMillis()));

                                List<String> response = multipart.finish();

                                System.out.println("SERVER REPLIED:");
                                //System.out.println(response.get(1));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String requestURL = "https://people.cs.clemson.edu/~yuang/cpsc6820/Project/group_push.php";
                            String charset = "UTF-8";
                            try {
                                FileUploader multipart = new FileUploader(requestURL, charset);
                                multipart.addHeaderField("User-Agent", "CodeJava");
                                multipart.addHeaderField("Test-Header", "Header-Value");
                                multipart.addFormField("title", "New message");
                                multipart.addFormField("body", userName+" send you a message.");

                                List<String> response = multipart.finish();

                                System.out.println("SERVER REPLIED:");
                                //System.out.println(response.get(1));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                    inputText.setText("");
                }
            }
        });

        final Handler handlebar=new Handler();
        handlebar.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.query("message", null, null, null, null, null, "time desc");
                        if (cursor.moveToFirst()) {
                            currentTime = cursor.getLong(cursor.getColumnIndex("time"));
                        }
                        Log.e("current", String.valueOf(currentTime));
                        cursor.close();
                        String requestURL = "https://people.cs.clemson.edu/~zwan/android/getmessage.php";
                        String charset = "UTF-8";
                        try {
                            FileUploader multipart = new FileUploader(requestURL, charset);
                            multipart.addHeaderField("User-Agent", "CodeJava");
                            multipart.addHeaderField("Test-Header", "Header-Value");
                            multipart.addFormField("fid", String.valueOf(fid));
                            multipart.addFormField("time", Long.toString(currentTime));

                            List<String> response = multipart.finish();

                            System.out.println("SERVER REPLIED:");
                            /*
                            for (String line : response) {
                                Log.e("response", line);
                            }*/
                            System.out.println(response.get(1));
                            //Log.e("response", response.get(1));
                            Message message = new Message();
                            message.what = SHOW_RESPONSE;
                            message.obj = response.get(1);
                            handler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.e("delay", "delay");
                handlebar.postDelayed(this, 3000);
            }
        }, 1500);
    }
}
