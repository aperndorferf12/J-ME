package com.example.perndorfer.j_me;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static String ipString = "10.0.2.244";
    FragmentPagerAdapter fragmentPagerAdapter;
    private DBHelper dbHelper;
    static Socket s;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private NotificationManager notificationManager;
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;
    private static BufferedWriter bw = null;
    private static BufferedReader br = null;
    private Thread t;
    private AppCompatActivity thisActivity=this;
    private Vibrator vibrator;
    private Ringtone r;


    public static SQLiteDatabase getDb() {
        return db;
    }

    private static SQLiteDatabase db;
    private static String mPhoneNumber;
    private File j_meFiles;
    private String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<Fragment> fragments = getFragments();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        fragmentPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(1);
        PagerTabStrip pts = (PagerTabStrip) findViewById(R.id.pagerHeader);
        pts.setTabIndicatorColor(getResources().getColor(android.R.color.darker_gray));
        pts.buildLayer();
        pts.forceLayout();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        File extStore = Environment.getExternalStorageDirectory();
        j_meFiles = new File(extStore.getPath() + "/J_ME_Files");
        Log.d("*===Files===", extStore.getPath() + " " + j_meFiles.getPath());
        File f = new File(j_meFiles.getAbsolutePath());
        f.mkdirs();
        new File(f.getPath() + "/Images").mkdirs();
        new File(f.getPath() + "/Videos").mkdirs();
        new File(f.getPath() + "/Audio").mkdirs();
        new File(f.getPath() + "/Files").mkdirs();


        mPhoneNumber = sp.getString("myNumber", null);
        if (mPhoneNumber == null) {
            askForNumber();
        }

        dbHelper = new DBHelper(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        db = dbHelper.getWritableDatabase();


        connectAndListen();

    }

    private OutputStream fileOut;

    public void connectAndListen() {
        Thread t = new Thread() {


            @Override
            public void run() {
                Log.d("*===THREAD RUN====", "run: ");
                setConnectionAndStreams();

                try {
                    Log.w("*===IPOFTHREAD===", s.getInetAddress().toString() + "");

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        Log.d("*===LINE===1", line);
                        String[] split = line.split(";");
                        final String number = split[0];
                        final String flag = split[1];
                        final String text = URLDecoder.decode(split[2], "UTF-8");
                        final String date = split[3];
                        Log.d("*===FLAG===2", flag);
                        Cursor c = db.rawQuery("SELECT _id, name FROM contacts WHERE number = '" + number + "';", null);
                        c.moveToFirst();
                        Log.w("*===NUMBER===3", number);
                        final int id = c.getInt(c.getColumnIndex(TblContacts.ID));
                        final String name = c.getString(c.getColumnIndex(TblContacts.NAME));
                        Contact contact = new Contact(id,name,number);
                        NotificationCompat.Builder mBuilder = null;

                        try {
                            fileOut = null;
                            Intent i;
                            PendingIntent resultPendingIntent;
                            switch (flag) {
                                case "msg":
                                    db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES('remote','" + flag + "','" + text + "','" + date + "'," + id + ");");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentChats.onCreateStuffAndUpdate();
                                            ChatAct.insertReceivedMessage(text, date, id);
                                        }
                                    });
                                    mBuilder =
                                            new NotificationCompat.Builder(getBaseContext())
                                                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                                                    .setContentTitle(name)
                                                    .setContentText(text);
                                    i = new Intent(thisActivity,ChatAct.class);
                                    i.putExtra("CONTACT",contact);
                                    i.putExtra("CHATID", contact.getChat_id());

                                    resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    thisActivity,
                                                    0,
                                                    i,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    mBuilder.setContentIntent(resultPendingIntent);
                                    notificationManager.notify(1, mBuilder.build());
                                    notifyMessage();
                                    break;

                                case "audio":
                                    filePath = j_meFiles.getPath() + "/Audio/" + text;
                                    fileOut = new FileOutputStream(filePath);
                                    db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES('remote','" + flag + "','" + filePath + "','" + date + "'," + id + ");");

                                    writeFile(fileOut);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentChats.onCreateStuffAndUpdate();
                                            ChatAct.insertReceivedAudio(filePath, date, id);
                                        }
                                    });


                                    mBuilder =
                                            new NotificationCompat.Builder(getBaseContext())
                                                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                                                    .setContentTitle(name)
                                                    .setContentText(URLDecoder.decode("%F0%9F%8E%B5", "UTF-8") + " Audio");

                                    i = new Intent(thisActivity,ChatAct.class);
                                    i.putExtra("CONTACT", contact);
                                    i.putExtra("CHATID", contact.getChat_id());

                                    resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    thisActivity,
                                                    0,
                                                    i,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    mBuilder.setContentIntent(resultPendingIntent);
                                    notificationManager.notify(1, mBuilder.build());
                                    notifyMessage();
                                    break;

                                case "image":

                                    Log.d("*===IMAGE===4", "");
                                    filePath = j_meFiles.getPath() + "/Images/" + text;
                                    fileOut = new FileOutputStream(filePath);
                                    db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES ('remote','image','" + filePath + "','" + date + "'," + id + ");");

                                    writeFile(fileOut);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentChats.onCreateStuffAndUpdate();
                                            ChatAct.insertReceivedImage(filePath, date, id);
                                        }
                                    });


                                    mBuilder =
                                            new NotificationCompat.Builder(getBaseContext())
                                                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                                                    .setContentTitle(name)
                                                    .setContentText(URLDecoder.decode("%F0%9F%93%B7", "UTF-8") + " Bild");

                                    i = new Intent(thisActivity,ChatAct.class);
                                    i.putExtra("CONTACT", contact);
                                    i.putExtra("CHATID", contact.getChat_id());

                                    resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    thisActivity,
                                                    0,
                                                    i,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    mBuilder.setContentIntent(resultPendingIntent);
                                    notificationManager.notify(1, mBuilder.build());
                                    notifyMessage();
                                    break;

                                case "video":

                                    filePath = j_meFiles.getPath() + "/Videos/" + text;
                                    fileOut = new FileOutputStream(filePath);
                                    db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES ('remote','video','" + filePath + "','" + date + "'," + id + ");");

                                    writeFile(fileOut);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentChats.onCreateStuffAndUpdate();
                                            ChatAct.insertReceivedVideo(filePath, date, id);
                                        }
                                    });


                                    mBuilder =
                                            new NotificationCompat.Builder(getBaseContext())
                                                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                                                    .setContentTitle(name)
                                                    .setContentText(URLDecoder.decode("%F0%9F%93%B9", "UTF-8") + " Video");

                                    i = new Intent(thisActivity,ChatAct.class);
                                    i.putExtra("CONTACT", contact);
                                    i.putExtra("CHATID", contact.getChat_id());

                                    resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    thisActivity,
                                                    0,
                                                    i,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    mBuilder.setContentIntent(resultPendingIntent);
                                    notificationManager.notify(1, mBuilder.build());
                                    notifyMessage();
                                    break;

                                case "file":

                                    filePath = j_meFiles.getPath() + "/Files/" + text;
                                    fileOut = new FileOutputStream(filePath);
                                    db.execSQL("INSERT INTO chatrecords(who,flag,text,date,chat_id) VALUES ('remote','file','" + filePath + "','" + date + "'," + id + ");");

                                    writeFile(fileOut);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FragmentChats.onCreateStuffAndUpdate();
                                            ChatAct.insertReceivedFile(filePath, date, id);
                                        }
                                    });


                                    mBuilder =
                                            new NotificationCompat.Builder(getBaseContext())
                                                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                                                    .setContentTitle(name)
                                                    .setContentText(URLDecoder.decode("%F0%9F%93%82", "UTF-8") + " Datei");

                                    i = new Intent(thisActivity,ChatAct.class);
                                    i.putExtra("CONTACT", contact);
                                    i.putExtra("CHATID", contact.getChat_id());

                                    resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    thisActivity,
                                                    0,
                                                    i,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    mBuilder.setContentIntent(resultPendingIntent);
                                    notificationManager.notify(1, mBuilder.build());
                                    notifyMessage();
                                    break;

                                default:
                                    System.exit(0);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        c.close();

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
        t.start();
    }

    private void writeFile(OutputStream fileOut) {
        try {
            byte[] bytes = new byte[16*1024];

            int count;

            while ((count = inputStream.read(bytes)) > 0) {
                fileOut.write(bytes, 0, count);
            }

            fileOut.close();
            inputStream.close();
            outputStream.close();
            s.close();
            setConnectionAndStreams();
            Log.w("*===WRITE FINISHED===5", "writeFile: FINISHED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsePhoneNumber() {
        if (!mPhoneNumber.startsWith("+")) {
            StringBuffer sb = new StringBuffer(mPhoneNumber);
            sb = sb.delete(0, 1);
            mPhoneNumber = sb.toString();
            mPhoneNumber = "+43" + mPhoneNumber;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sp.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                preferencesChanged(sharedPreferences, key);
                Log.d("*===PREFS===", "onSharedPreferenceChanged: ");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sp.unregisterOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                preferencesChanged(sharedPreferences, key);
            }
        });
    }

    private void preferencesChanged(SharedPreferences sharedPreferences, String key) {
        mPhoneNumber = sharedPreferences.getString(key, null);
        setConnectionAndStreams();
    }

    private void askForNumber() {
        final AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Geben Sie Ihre Nummer ein:");
        final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ip_dialog, null);
        final EditText iped = (EditText) ll.findViewById(R.id.ip);

        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPhoneNumber = iped.getText().toString();
                parsePhoneNumber();
                sped.putString("myNumber", mPhoneNumber);
                sped.commit();
            }
        });
        ab.setCancelable(false);
        ab.setView(ll);
        ab.show();
        Log.d("*===Ask", mPhoneNumber + "");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            startActivity(new Intent(this, Preferences.class));
        }

        if (id == R.id.exit) {
            Log.e("EXIT", "*======EXIT======");
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle("Beenden");
            ab.setMessage("Wenn Sie beenden wird alles geschlossen. Dadurch können Sie keine Nachrichten mehr empfangen.\nFortfahren?");
            ab.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        inputStream.close();
                        outputStream.close();
                        s.close();
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            ab.setNegativeButton("Nein",null);
            ab.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void setConnectionAndStreams() {
        try {
            if (s != null) {
                inputStream.close();
                outputStream.close();
                s.close();
                Log.e("*===ISDOWN===", s.isInputShutdown() + " and closed");
            }

            s = new Socket(ipString, 1234);
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
            Log.w("*===ISDOWN===", s.isInputShutdown()+"" );
            Log.d("*===THREAD STARTED====", outputStream.toString() + "");
            br = new BufferedReader(new InputStreamReader(inputStream));
            bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(mPhoneNumber + "\r\n");
            bw.flush();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setWritersAndReaders() {
        Log.w("*===SetWritersAn", "setWritersAndReaders");
        try {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        br = new BufferedReader(new InputStreamReader(inputStream));
        bw = new BufferedWriter(new OutputStreamWriter(outputStream));

    }

    public static OutputStream getOutputStream() {
        return outputStream;
    }

    public static InputStream getInputStream() {
        return inputStream;
    }

    public static BufferedWriter getBw() {
        return bw;
    }

    public static String getmPhoneNumber() {
        return mPhoneNumber;
    }

    private void notifyMessage()
    {
        vibrator.vibrate(150);
        r.play();
    }

    private ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(FragmentContacts.newInstance("Kontakte"));
        fragments.add(FragmentChats.newInstance("Chats"));
        return fragments;
    }
}
