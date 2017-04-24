package com.example.zwan.a4;

import android.content.SharedPreferences;
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
    private long currentTime;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    Log.e("response", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int thisuid = jsonObject.getInt("UId");
                            String content = jsonObject.getString("Content");
                            currentTime = jsonObject.getLong("Time");
                            Msg message = new Msg(content, Msg.TYPE_RECEIVED);
                            msgList.add(message);
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
        currentTime = System.currentTimeMillis();
        uid = pref.getInt("uid", 0);
        fid = pref.getInt("fid", 0);

        adapter = new MsgAdapter(GroupChat.this, R.layout.msg_item_layout, msgList);
        inputText = (EditText)findViewById(R.id.input_text);
        send = (Button)findViewById(R.id.send_button);
        msgLisView = (ListView)findViewById(R.id.list_view);
        msgLisView.setAdapter(adapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = inputText.getText().toString();
                if(!"".equals(content)){
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
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
                                currentTime = System.currentTimeMillis();
                                multipart.addFormField("time", Long.toString(currentTime));

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

        final Handler handlerdelay=new Handler();
        handlerdelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                handlerdelay.postDelayed(this, 3000);
            }
        }, 1500);
    }
}
