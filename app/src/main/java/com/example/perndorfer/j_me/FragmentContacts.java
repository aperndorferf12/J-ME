package com.example.perndorfer.j_me;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
public class FragmentContacts extends Fragment
{
    private String title;
    private int page;
    private View view;
    private ListView contacts;
    private SQLiteDatabase db;
    private ArrayList<Contact> contactArrayList = new ArrayList<>();

    public static FragmentContacts newInstance(String title) {
        
        Bundle args = new Bundle();
        FragmentContacts fragment = new FragmentContacts();
        args.putString("title",title);
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
        view = inflater.inflate(R.layout.fragment_contacts,container,false);
        contacts = (ListView) view.findViewById(R.id.contactsList);
        db = MainActivity.getDb();
        onCreateStuff();
        return view;
    }

    private void onCreateStuff()
    {
        contactArrayList = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM contacts ORDER BY name;",null);
        while ((c.moveToNext())) {
            String name = c.getString(c.getColumnIndex(TblContacts.NAME));
            int id = c.getInt(c.getColumnIndex(TblContacts.ID));
            String number = c.getString(c.getColumnIndex(TblContacts.NUMBER));
            Contact k = new Contact(id, name, number);
            contactArrayList.add(k);
            Log.d("*===FragmentCONTACTS===",id+", "+ name + " NUMBER: " + number);
        }

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.two_line_list_item,c,new String[]{TblContacts.NAME,TblContacts.NUMBER},new int[]{android.R.id.text1,android.R.id.text2});
        contacts.setAdapter(adapter);

        contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickContact(position);
            }
        });
    }

    private void onClickContact(final int pos)
    {
        Toast.makeText(getActivity(), "Contacts", Toast.LENGTH_LONG).show();
        Contact c = contactArrayList.get(pos);
        db.execSQL("UPDATE contacts SET hasChat = 'true' WHERE _id = "+c.getId()+";");
        FragmentChats.onCreateStuffAndUpdate();
        Intent intent = new Intent(getActivity(), ChatAct.class);
        intent.putExtra("CONTACT", c);
        intent.putExtra("CHATID", c.getChat_id());
        startActivity(intent);
    }
}
