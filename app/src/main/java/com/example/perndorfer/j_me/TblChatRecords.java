package com.example.perndorfer.j_me;

/**
 * Created by Perndorfer/Huemer on 07.04.2016.
 */
public class TblChatRecords
{
    public static final String TABLE_NAME = "chatrecords";
    public static final String ID = "_id";
    public static final String WHO = "who";
    public static final String FLAG ="flag";
    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String CHAT_ID = "chat_id";

    public static String[] ALL_COLUMNS = new String[]{ID,TEXT, CHAT_ID,FLAG,DATE};

    public static String SQL_CREATE = "CREATE TABLE "+TABLE_NAME+
            "( "+
            ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            WHO+" TEXT NOT NULL,"+
            FLAG+" TEXT NOT NULL,"+
            TEXT+" TEXT NOT NULL,"+
            DATE+" TEXT NOT NULL,"+
            CHAT_ID+" INTEGER NOT NULL"+
            ")";


}
