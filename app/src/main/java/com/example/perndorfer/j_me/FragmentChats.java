package com.example.perndorfer.j_me;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Alexander on 03.06.2016.
 */
public class FragmentChats extends Fragment
{
    private String title;
    private int page;
    private View view;
    private static SQLiteDatabase db;
    private static ArrayList<Contact> contactsWithChat = new ArrayList<Contact>();
    private static ListView chats;
    private static Activity activity;
    private static FragmentChats thisFragment;
    private static int selectedItem = 0;

    public static FragmentChats newInstance(String title) {

        Bundle args = new Bundle();
        FragmentChats fragment = new FragmentChats();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString("title");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chats, container, false);
        chats = (ListView) view.findViewById(R.id.chatsList);
        db = MainActivity.getDb();
        thisFragment = this;
        registerForContextMenu(chats);
        onCreateStuffAndUpdate();
        return view;
    }

    public static void onCreateStuffAndUpdate()
    {
        contactsWithChat = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT c._id, c.name, c.number, c.hasChat, r.text FROM contacts c JOIN chatrecords r ON (r._id = (SELECT _id FROM chatrecords WHERE _id = (SELECT MAX(_id) FROM chatrecords WHERE chat_id = c._id))) WHERE hasChat = 'true' ORDER BY name;", null);
        /*String s = "SELECT c._id, c.name, c.number, c.hasChat, r.text FROM contacts c \n" +
                "JOIN chatrecords r \n" +
                "ON (r._id = (SELECT _id FROM chatrecords \n" +
                "WHERE _id = (SELECT MAX(_id) FROM chatrecords WHERE chat_id = c._id)))\n" +
                "WHERE hasChat <> 'false' ORDER BY name;";*/

        while ((c.moveToNext())) {
            String name = c.getString(c.getColumnIndex(TblContacts.NAME));
            int id = c.getInt(c.getColumnIndex(TblContacts.ID));
            String number = c.getString(c.getColumnIndex(TblContacts.NUMBER));
            Contact k = new Contact(id, name, number);
            contactsWithChat.add(k);
            Log.d("*===FragmentCHATS===", id+", "+name + " NUMBER: " + number);
        }

        if(c.moveToFirst())
        {
            String[] cols = {TblContacts.NAME, TblChatRecords.TEXT};
            int[] lay = {android.R.id.text1, android.R.id.text2};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(thisFragment.getActivity(), android.R.layout.two_line_list_item, c, cols, lay);
            chats.setAdapter(adapter);

            chats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onClickChat(position);
                }
            });
            chats.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedItem = position;
                    return false;
                }
            });
        }
        else {
            chats.setAdapter(new ArrayAdapter<Contact>(thisFragment.getActivity(),android.R.layout.simple_list_item_1,contactsWithChat));
        }
    }

    private static void onClickChat(final int pos) {
        Toast.makeText(thisFragment.getActivity(),"Chats",Toast.LENGTH_LONG).show();
        Contact c = contactsWithChat.get(pos);
        Intent intent = new Intent(thisFragment.getActivity(), ChatAct.class);
        intent.putExtra("CONTACT", c);
        intent.putExtra("CHATID", c.getChat_id());
        thisFragment.startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.contextmenu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = contactsWithChat.get(selectedItem).getChat_id();
        db.execSQL("DELETE FROM chatrecords WHERE chat_id = "+id+";");
        db.execSQL("UPDATE contacts SET hasChat = 'false' WHERE _id = " + id + ";");
        Log.w("*===Delete===", id + "");
        onCreateStuffAndUpdate();
        return super.onContextItemSelected(item);
    }
}
