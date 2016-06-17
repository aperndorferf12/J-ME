package com.example.perndorfer.j_me;

import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Perndorfer on 07.04.2016.
 */
public class ChatAct extends ActionBarActivity {
    private BufferedWriter bw;
    private SQLiteDatabase db;
    private static LinearLayout ausgabe;
    private static Contact c;
    private EditText eingabe;
    private static int chatId;
    private String destPhoneNumber;
    private String mPhoneNumber;
    private static Activity context;
    private static int black, white, red;
    private static ScrollView sv;
    private OutputStream outputStream;
    private String selectedPath;
    private static View selectedView;
    private static String selectedText="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_act);

        sv = (ScrollView) findViewById(R.id.sv);
        outputStream = MainActivity.getOutputStream();
        bw = MainActivity.getBw();
        mPhoneNumber = MainActivity.getmPhoneNumber();

        context = this;
        black = getResources().getColor(android.R.color.black);
        white = getResources().getColor(android.R.color.white);
        red = getResources().getColor(R.color.red);

        ausgabe = (LinearLayout) findViewById(R.id.ausgabe);
        eingabe = (EditText) findViewById(R.id.eingabe);
        c = (Contact) getIntent().getSerializableExtra("CONTACT");
        this.setTitle(c.getName());
        db = MainActivity.getDb();
        chatId = getIntent().getIntExtra("CHATID", 0);
        destPhoneNumber = c.getNumber();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(c.getNumber());
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Cursor cu = db.rawQuery("SELECT * FROM chatrecords WHERE chat_id = " + chatId + " ORDER BY _id;", null);
        while (cu.moveToNext()) {
            String who = cu.getString(cu.getColumnIndex(TblChatRecords.WHO));
            String msg = cu.getString(cu.getColumnIndex(TblChatRecords.TEXT));
            String date = cu.getString(cu.getColumnIndex(TblChatRecords.DATE));
            String flag = cu.getString(cu.getColumnIndex(TblChatRecords.FLAG));
            Log.d("*===MESSAGE===", who + " " + msg + " " + date);
            if (who.equals("me")) {

                switch (flag)
                {
                    case "msg":insertMeMessage(msg, date);
                        break;
                    case "image": insertMeImage(msg, date);
                        break;
                    case "video":
                        insertMeVideo(msg, date);
                        break;
                    case  "audio":
                        insertMeAudio(msg, date);
                        break;
                    case  "file":
                        insertMeFile(msg, date);
                        break;
                }
            } else
            {
                switch (flag)
                {
                    case "msg":insertRemoteMessage(msg, date);
                        break;
                    case "image": insertReceivedImage(msg, date, chatId);
                        break;
                    case "video":
                        insertReceivedVideo(msg, date, chatId);
                        break;
                    case  "audio":
                        insertReceivedAudio(msg, date, chatId);
                        break;
                    case  "file":
                        insertReceivedFile(msg, date, chatId);
                        break;
                }

            }
        }

        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.contextmenu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        db.execSQL("DELETE FROM chatrecords WHERE text = '"+selectedText+"' AND chat_id = "+chatId+";");
        ausgabe.removeView(selectedView);
        FragmentChats.onCreateStuffAndUpdate();
        return super.onContextItemSelected(item);
    }

    private void insertMeImage(final String path, String date)
    {
        ImageView iv = new ImageView(context);
        registerForContextMenu(iv);
        iv.setPadding(5,5,5,5);
        iv.setBackgroundColor(red);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 0);
        iv.setLayoutParams(llp);
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        Bitmap image = BitmapFactory.decodeFile(path, options);
        iv.setImageBitmap(image);*/
        iv.setImageDrawable(Drawable.createFromPath(path));
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("*===IMAGEVIEW===", "onClickImageView");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path), "image/*");
                v.getContext().startActivity(intent);
            }
        });
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked(v,path);
                return false;
            }
        });

        ausgabe.addView(iv);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void insertMeVideo(final String path, String date)
    {
        final VideoView vv = new VideoView(context);
        vv.setPadding(5,5,5,5);
        registerForContextMenu(vv);
        vv.setVideoPath(path);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(550,550);
        vv.setLayoutParams(llp);
        llp.setMargins(0, 10, 0, 0);
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
                vv.start();
            }
        });

        vv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path), "video/*");
                v.getContext().startActivity(intent);
                onLongClicked(v,path);
                return false;
            }
        });

        ausgabe.addView(vv);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public static void insertReceivedMessage(final String msg, String date, int id) {
        if (id == chatId) {
            TextView tv = new TextView(context);
            context.registerForContextMenu(tv);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 10, 0, 0);
            tv.setLayoutParams(llp);
            tv.setTextSize(16);
            tv.setTextColor(black);
            tv.setClickable(true);
            //tv.setBackgroundColor(white);
            tv.setBackgroundResource(R.drawable.remote);
            tv.setPadding(30, 10, 30, 10);
            try {
                tv.setText(c.getName() + ":\n" + URLDecoder.decode(msg, "UTF-8") + "\n" + date);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClicked(v,msg);
                    return false;
                }
            });

            ausgabe.addView(tv);
            sv.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public static void insertReceivedImage(final String path, String date, int id) {
        if (id == chatId) {
            Log.w("*===INSERT IMAGE===6", id + "");

            ImageView iv = new ImageView(context);
            context.registerForContextMenu(iv);
            iv.setPadding(5,5,5,5);
            iv.setBackgroundColor(white);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 10, 0, 0);
            iv.setLayoutParams(llp);
            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            Bitmap image = BitmapFactory.decodeFile(path, options);
            iv.setImageBitmap(image);*/
            iv.setImageDrawable(Drawable.createFromPath(path));
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("*===IMAGEVIEW===", "onClickImageView");
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + path), "image/*");
                    v.getContext().startActivity(intent);
                }
            });

            iv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClicked(v,path);
                    return false;
                }
            });

            ausgabe.addView(iv);
            sv.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public static void insertReceivedVideo(final String path, String date, int id)
    {
        if(id == chatId)
        {
            final VideoView vv = new VideoView(context);
            vv.setPadding(5,5,5,5);
            vv.setVideoPath(path);
            context.registerForContextMenu(vv);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(550,550);
            llp.setMargins(0, 10, 0, 0);
            vv.setLayoutParams(llp);
            vv.setClickable(true);
            vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0f, 0f);
                    vv.start();
                }
            });

            vv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + path), "video/*");
                    v.getContext().startActivity(intent);
                    onLongClicked(v,path);
                    return false;
                }
            });

            ausgabe.addView(vv);
            sv.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

    private void insertMeMessage(final String msg, String date)
    {
        TextView tv = new TextView(this);
        registerForContextMenu(tv);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 0);
        tv.setLayoutParams(llp);
        tv.setTextSize(16);
        tv.setTextColor(white);
        tv.setClickable(true);
        //tv.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        tv.setBackgroundResource(R.drawable.me);
        tv.setPadding(30, 10, 30, 10);
        tv.setText(eingabe.getText() + msg + "\n" + date);

        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked(v,msg);
                return false;
            }
        });
        ausgabe.addView(tv);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void insertRemoteMessage(final String msg, String date) {
        TextView tv = new TextView(this);
        registerForContextMenu(tv);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 0);
        tv.setLayoutParams(llp);
        tv.setTextSize(16);
        tv.setTextColor(black);
        tv.setClickable(true);
        //tv.setBackgroundColor(white);
        tv.setBackgroundResource(R.drawable.remote);
        tv.setPadding(30, 10, 30, 10);
        tv.setText(c.getName() + ":\n" + msg + "\n" + date);
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked(v,msg);
                return false;
            }
        });

        ausgabe.addView(tv);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void insertMeAudio(final String path, String date)
    {
        TextView music = new TextView(context);
        registerForContextMenu(music);
        music.setPadding(5,5,5,5);
        music.setBackgroundColor(red);
        music.setBackgroundResource(R.drawable.music);
        music.setTextColor(black);
        music.setText(new File(path).getName() + "\n" + date);
        music.setHeight(53);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llp.setMargins(0, 10, 0, 0);
        music.setLayoutParams(llp);

        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path), "audio/*");
                v.getContext().startActivity(intent);
            }
        });

        music.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked(v,path);
                return false;
            }
        });

        ausgabe.addView(music);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public static void insertReceivedAudio(final String path, String date,int id)
    {
        if(id==chatId)
        {
            TextView music = new TextView(context);
            context.registerForContextMenu(music);
            music.setPadding(5,5,5,5);
            music.setBackgroundColor(white);
            music.setBackgroundResource(R.drawable.music);
            music.setTextColor(black);
            music.setText(new File(path).getName() + "\n" + date);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            llp.setMargins(0,10,0,0);
            music.setLayoutParams(llp);

            music.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + path), "audio/*");
                    v.getContext().startActivity(intent);
                }
            });

            music.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClicked(v,path);
                    return false;
                }
            });

            ausgabe.addView(music);
            sv.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public static void insertReceivedFile(final String msg, String date, int id)
    {
        if(id==chatId)
        {
            final LinearLayout ll = (LinearLayout)context.getLayoutInflater().inflate(R.layout.fc_item,null);
            context.registerForContextMenu(ll);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 10, 0, 0);
            ll.setLayoutParams(llp);
            TextView tv = (TextView)ll.findViewById(R.id.folderName);
            tv.setText(msg);
            tv.setClickable(true);
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClicked(ll,msg);
                    return false;
                }
            });
            ausgabe.addView(ll);
            ((ImageView)ll.findViewById(R.id.pic)).setImageDrawable(context.getResources().getDrawable(R.drawable.file));
        }
    }

    private void insertMeFile(final String msg, String date)
    {
        final LinearLayout ll = (LinearLayout)context.getLayoutInflater().inflate(R.layout.fc_item,null);
        ll.setBackgroundColor(red);
        registerForContextMenu(ll);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 0);
        ll.setLayoutParams(llp);
        TextView tv = (TextView)ll.findViewById(R.id.folderName);
        tv.setText(msg);
        tv.setTextColor(white);
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked(ll,msg);
                return false;
            }
        });
        ausgabe.addView(ll);
        ((ImageView)ll.findViewById(R.id.pic)).setImageDrawable(context.getResources().getDrawable(R.drawable.file));
    }

    private static void onLongClicked(View v, String s)
    {
        selectedView = v;
        selectedText = s;
    }

    public void send(final View src) {
        Log.w("*===SEND===", "Sending");
        String msg = eingabe.getText().toString();
        bw = new BufferedWriter(new OutputStreamWriter(MainActivity.getOutputStream()));

        try
        {
            //textAusgabe.setText(textAusgabe.getText() + "\r\n Du: " + eingabe.getText());
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 10, 0, 0);
            tv.setLayoutParams(llp);
            tv.setTextSize(16);
            tv.setTextColor(white);
            //tv.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            tv.setBackgroundResource(R.drawable.me);
            tv.setPadding(30, 10, 30, 10);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = sdf.format(new Date());
            tv.setText(eingabe.getText() + "\n" + date);
            ausgabe.addView(tv);
            sv.fullScroll(ScrollView.FOCUS_DOWN);
            Log.e("*===WROTE===", "WROTE");
            bw.write(destPhoneNumber + ";" + mPhoneNumber + ";msg;" + URLEncoder.encode(msg, "UTF-8") + ";" + date + "\r\n");
            bw.flush();
            db.execSQL("INSERT INTO chatrecords (who,flag,text,chat_id, date) VALUES ('me','msg','" + msg + "'," + chatId + ",'" + date + "')");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentChats.onCreateStuffAndUpdate();
                }
            });
        }
        catch (Exception e)
        {
            Log.w("", "Fehler beim Senden");
            e.printStackTrace();
        }
        eingabe.setText("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (Integer.parseInt(Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            Log.e("*===BACK=====, ", "onKeyDown: BACK");
            //FragmentChats.onCreateStuffAndUpdate();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatactmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i = null;
        switch (id) {
            case R.id.bild:
                i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
                break;

            case R.id.video:
                i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 2);
                break;

            case R.id.musik:
                i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 3);
                break;

            case R.id.datei:
                i = new Intent(this,FileChooserAct.class);
                startActivityForResult(i, 4);
                break;

            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        selectedPath = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    selectedPath = getImagePath(data.getData());
                    sendFile("image");
                    insertMeImage(selectedPath, null);

                    Log.d("*===SENT===", "onActivityResult: ");
                    break;

                case 2:
                    selectedPath = getVideoPath(data.getData());
                    sendFile("video");
                    insertMeVideo(selectedPath, null);

                    break;

                case 3:
                    selectedPath = getMusicPath(data.getData());
                    sendFile("audio");
                    insertMeAudio(selectedPath,new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
                    Log.d("*===PATH===", selectedPath + "");
                    break;

                case 4:
                    selectedPath = data.getStringExtra("path");
                    Log.w("*===FILEPATH===", selectedPath + "");
                    insertMeFile(selectedPath,null);
                    sendFile("file");
                    break;
            }
        }
        selectedPath = null;
        super.onActivityResult(requestCode, resultCode, data);
    }


    private String getImagePath(Uri uri)
    {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return null;
        }
    }

    private String getVideoPath(Uri uri)
    {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return null;
        }
    }


    private String getMusicPath(Uri uri)
    {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return null;
        }
    }

    private ProgressDialog pd;
    private long totalBytes = 0l;
    private long copiedBytes = 0l;

    private void sendFile(String flag)
    {
        Log.e("*===sendfile===", "sendFile: ");

        if (selectedPath != null) {

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String date = sdf.format(new Date());
                final File file = new File(selectedPath);
                db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES ('me','"+flag+"','" + file.getAbsolutePath() + "','" + date + "'," + chatId + ");");
                int index = file.getName().lastIndexOf('.');
                String fileNameExtension = file.getName().substring(index);
                Log.wtf("*===FileName===",fileNameExtension);

                final byte[] bytes = new byte[16 * 1024];

                final InputStream fileIn = new FileInputStream(file);
                bw.write(destPhoneNumber + ";" + mPhoneNumber + ";" + flag + ";" + System.currentTimeMillis() + fileNameExtension + ";" + date + "\r\n");
                bw.flush();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FragmentChats.onCreateStuffAndUpdate();
                    }
                });

                pd = new ProgressDialog(this);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle("Datei wird gesendet...");
                pd.setMessage("Versende Datei an " + c.getName() + ".");
                pd.show();
                pd.setProgress(0);
                pd.setCancelable(false);
                totalBytes = file.length();
                Toast.makeText(this,totalBytes+"",Toast.LENGTH_LONG).show();
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {

                            int count;
                            while ((count = fileIn.read(bytes)) > 0) {
                                outputStream.write(bytes, 0, count);
                                pd.setProgress((int) ((copiedBytes+= 16*1024) * 100 / totalBytes));
                                Log.d("*===LOOP===", count + "");
                            }
                            pd.dismiss();
                            outputStream.close();
                            Log.w("*===SENT===", "whileFin");
                            fileIn.close();

                            MainActivity.setConnectionAndStreams();
                            bw = MainActivity.getBw();
                            outputStream = MainActivity.getOutputStream();
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }


                /*outputStream.write(bytes, 0, bytes.length);
                outputStream.flush();*/


                };
                t.start();
            }
            catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


}



