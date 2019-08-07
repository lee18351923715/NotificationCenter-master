package com.sample.notificationcenter;

import android.content.ContentValues;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static ContentValues set(String title, String message, int flag, int type){
        ContentValues values = new ContentValues();
        values.put("title",title);
        values.put("message",message);
        values.put("flag", flag);
        values.put("time",new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date()).toString());
        values.put("type",type);
        return values;
    }

}
