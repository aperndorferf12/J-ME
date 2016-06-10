package com.example.perndorfer.j_me;

/**
 * Created by Perndorfer on 07.04.2016.
 */
@Deprecated
/**
 * @deprecated _id references now chatrecord's chat_id
 */
public class TblChats
{
    public static final String TABLE_NAME = "chats";
    public static final String ID = "_id";
    public static final String NAME = "name";

    public static String[] ALL_COLUMNS = new String[]{ID,NAME};

    public static String SQL_CREATE = "CREATE TABLE "+TABLE_NAME+
            "( "+
            ID+" INTEGER PRIMARY KEY,"+
            NAME+" TEXT NOT NULL,"+
            "FOREIGN KEY(_id) REFERENCES chatrecords(chat_id)"+
            ")";


}
