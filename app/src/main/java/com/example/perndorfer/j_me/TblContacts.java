package com.example.perndorfer.j_me;

/**
 * Created by Perndorfer/Huemer on 07.04.2016.
 */
public class TblContacts
{
    public static final String TABLE_NAME = "contacts";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String NUMBER = "number";
    public static final String HASCHAT = "haschat";

    public static String[] ALL_COLUMNS = new String[]{ID,NAME,NUMBER};

    public static String SQL_CREATE = "CREATE TABLE "+TABLE_NAME+
            "( "+
            ID+" INTEGER PRIMARY KEY,"+
            NAME+" TEXT NOT NULL,"+
            NUMBER+" TEXT NOT NULL,"+
            HASCHAT+ " BOOLEAN NOT NULL,"+
            "FOREIGN KEY(_id) REFERENCES chatrecords(chat_id)"+
            ")";
}
