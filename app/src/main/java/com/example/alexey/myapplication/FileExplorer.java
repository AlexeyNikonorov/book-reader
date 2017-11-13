package com.example.alexey.myapplication;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class FileExplorer extends ListActivity {
    private static final String CURRENT_FILE = "CURRENT_FILE";
    private File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        if (savedInstanceState == null) {
            listDirectory(new File("/"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_FILE, currentFile.getPath());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String currentFilePath = savedInstanceState.getString(CURRENT_FILE);
        listDirectory(new File(currentFilePath));
    }

    private void listDirectory(File nextFile) {
        currentFile = nextFile;
        ArrayAdapter<String> fileList = new ArrayAdapter<>(this, R.layout.explorer_item);
        if (currentFile.getParent() != null) {
            fileList.add("../");
        }
        File[] paths = currentFile.listFiles();
        for (File file: paths) {
            if (file.isDirectory()) {
                fileList.add(file.getName() + "/");
            } else {
                fileList.add(file.getName());
            }
        }
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = currentFile.getParentFile();
        if (file == null) {
            file = currentFile.listFiles()[position];
        } else if (position > 0) {
            file = currentFile.listFiles()[position-1];
        }

        if (file.isDirectory()) {
            if (file.canRead()) {
                listDirectory(file);
            } else {
                String message = "Can not open " + file.getName();
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } else if (file.isFile()) {
            Intent data = new Intent();
            data.putExtra(MainActivity.SELECTED_FILE_PATH, file.getPath());
            setResult(RESULT_OK, data);
            finish();
        }
    }
}