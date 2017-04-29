package com.example.zwan.a4;

/**
 * Created by sersh on 16/3/12.
 */
public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    private String content;
    private int type;
    private String image;

    public Msg(String content, int type, String image){
        this.content = content;
        this.type = type;
        this.image = image;
    }

    public String getContent(){
        return content;
    }

    public int getType(){
        return type;
    }

    public String getImage(){
        return image;
    }
}
