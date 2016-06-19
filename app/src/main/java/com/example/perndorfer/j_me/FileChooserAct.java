package com.example.perndorfer.j_me;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Alexander on 10.06.2016.
 */
public class FileChooserAct extends AppCompatActivity
{
    private LinearLayout layout;
    private String selectedFile = "";
    private AppCompatActivity activity = this;
    private File previousFile;
    private File root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_chooser);
        layout = (LinearLayout)findViewById(R.id.rootView);
        setTitle("Datei auswählen...");

        root = new File("/");
        previousFile = root;
        listFiles(root);
    }

    private void listFiles(File file)
    {
        if(!file.equals(root))
        {
            if(file.getParentFile().equals(root))
            {
                previousFile=root;
            }
            else
            {
                previousFile=file.getParentFile();
            }
        }

        final File [] files = file.listFiles();
        for(int i = 0; i<files.length; i++)
        {
            final int z = i;
            if(files[i].isDirectory())
            {
                LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.fc_item,null);
                TextView tv = (TextView)ll.findViewById(R.id.folderName);
                tv.setText(files[i].getName());
                layout.addView(ll);
                ((ImageView)ll.findViewById(R.id.pic)).setImageDrawable(getResources().getDrawable(R.drawable.folder));
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("*=SelFile", "onClick " + selectedFile);
                        layout.removeAllViews();
                        listFiles(files[z]);
                    }
                });
            }
            else
            {
                LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.fc_item,null);
                TextView tv = (TextView)ll.findViewById(R.id.folderName);
                tv.setText(files[i].getName());
                layout.addView(ll);
                ((ImageView)ll.findViewById(R.id.pic)).setImageDrawable(getResources().getDrawable(R.drawable.file));
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFile = files[z].getAbsolutePath();
                        Log.w("*===ONCLICKFILe", selectedFile);
                        Intent i = new Intent();
                        i.putExtra("path", selectedFile);
                        activity.setResult(RESULT_OK, i);
                        activity.finish();
                    }
                });

            }
        }
    }

    public void onBackClicked(final View src)
    {
        listFiles(previousFile);
    }
}
