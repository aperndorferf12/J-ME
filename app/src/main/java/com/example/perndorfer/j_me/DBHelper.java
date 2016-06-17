package com.example.perndorfer.j_me;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by Perndorfer on 07.04.2016.
 */
public class DBHelper extends SQLiteOpenHelper implements Serializable
{
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "data.db";
    Context context;
    public DBHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TblChatRecords.SQL_CREATE);
        //db.execSQL(TblChats.SQL_CREATE);
        db.execSQL(TblContacts.SQL_CREATE);
        seed(db);
    }

    public void seed(SQLiteDatabase db)
    {
        Log.w("SEED", "seed:=========================================");
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET + "<> 'com.whatsapp' AND "+ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET+" <> 'com.google'", null, null);
        //Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null,null,null);
        while (c.moveToNext())
        {
            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            int id = c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = parsePhoneNumber(phone);
            Log.w("Contacts", id+" Name: "+name+" Phone: "+phone );
            db.execSQL("INSERT INTO contacts (_id, name, number, haschat) VALUES ("+id+",'"+name+"','"+phone+"','false')");
            //db.execSQL("INSERT INTO chats (_id,name) VALUES (" + id + ",'" + name + "')");
        }
    }

    private String parsePhoneNumber(String number)
    {
        if(!number.startsWith("+"))
        {
            StringBuffer sb = new StringBuffer(number);
            sb = sb.delete(0,1);
            number = sb.toString();
            number = "+43"+number;
        }
        if(number.contains(" "))
        {
            number = number.replace(" ","");
        }

        return number;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
