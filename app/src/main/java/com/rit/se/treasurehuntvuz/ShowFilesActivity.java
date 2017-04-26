package com.rit.se.treasurehuntvuz;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowFilesActivity extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/");

        fill(currentDir);
    }

    @Override
    public void onBackPressed() {
        try {
            Intent fileOpenActivityIntent = new Intent(ShowFilesActivity.this, StartGameActivity.class);
            startActivity(fileOpenActivityIntent);
            finish();
        }
        catch(Exception exception) {
            Log.e("ShowFilesActivity", exception.getMessage());
        }
    }

    private void fill(File f)
    {
        File[]dirs = f.listFiles();
        this.setTitle("Current Dir: "+f.getName());
        List<Option> dir = new ArrayList<Option>();
        List<Option>fls = new ArrayList<Option>();
        adapter = new FileArrayAdapter(ShowFilesActivity.this,R.layout.file_view,dir);
        this.setListAdapter(adapter);

        try{
            for(File ff: dirs)
            {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
            }
        }catch(Exception e)
        {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0,new Option("..","Parent Directory",f.getParent()));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Option o = adapter.getItem(position);
        if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        }
        else
        {
            onFileClick(o);
        }
    }

    private void onFileClick(Option o)
    {
        File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();

        File file = new File(dir, o.getName());
        if(file.exists())   // check if file exist
        {

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                line = br.readLine();
                Toast.makeText(this, "Info in File: "+line, Toast.LENGTH_SHORT).show();

            }
            catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(this, "Sorry, File doesn't exist!", Toast.LENGTH_SHORT).show();
        }

    }

}
