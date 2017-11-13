package com.example.alexey.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int SELECTED_FILE_RESULT_CODE = 1;
    public static final String SELECTED_FILE_PATH = "SELECTED_FILE_PATH";
    private Dictionary dictionary = new Dictionary();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        recyclerView = (RecyclerView) findViewById(R.id.list);

        startActivity(new Intent(this, TestActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_file:
                Intent intent = new Intent(this, FileExplorer.class);
                startActivityForResult(intent, SELECTED_FILE_RESULT_CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTED_FILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getStringExtra(SELECTED_FILE_PATH);
                recyclerView.setAdapter(new Adapter(this, filePath));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class Loader extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... string) {
            String word = string[0];
            Article article = dictionary.getArticle(word);
            if (article == null && word.endsWith("s")) {
                article = dictionary.getArticle(word.substring(0, word.length()-1));
            }
            return article == null ? "Unknown word: "+word : article.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            print(result);
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private LayoutInflater inflater;
        private FBParser fbParser;

        Adapter(Context context, String path) {
            inflater = LayoutInflater.from(context);
            fbParser = new FBParser();
            fbParser.parse(new File(path));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FBParser.Element element = fbParser.items.get(position);
            TextView textView = holder.textView;
            textView.setText("\t" + element.content);
            if (element.isTitle()) {
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
                textView.setTypeface(null, Typeface.BOLD);
                textView.setPadding(5, 0, 5, 0);
            } else if (element.isPoem()) {
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                textView.setTypeface(null, Typeface.ITALIC);
                textView.setPadding(5, 0, 5, 0);
            } else if (element.isEpigraph()) {
                textView.setGravity(Gravity.END);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                textView.setTypeface(null, Typeface.ITALIC);
                textView.setPadding(5, 0, 5, 0);
            } else if (element.isCite()) {
                textView.setGravity(Gravity.START);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                textView.setTypeface(null, Typeface.ITALIC);
                int padding = 0;
                if ((position > 0 && !fbParser.items.get(position-1).isCite()) ||
                        (position < fbParser.items.size()-1 && !fbParser.items.get(position+1).isCite())) {
                    padding = 10;
                }
                textView.setPadding(15, padding, 15, padding);
            } else {
                textView.setGravity(Gravity.START);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setPadding(5, 0, 5, 0);
            }
        }

        @Override
        public int getItemCount() {
            return fbParser.items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.pageText);
                textView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            CharSequence text = textView.getText();
                            int offset = textView.getOffsetForPosition(event.getX(), event.getY());
                            int start = offset;
                            int end = offset;
                            while (start > 0 && isWordChar(text.charAt(start - 1))) {
                                start--;
                            }
                            while (end < text.length() && isWordChar(text.charAt(end))) {
                                end++;
                            }
                            if (start < end) {
                                String word = text.subSequence(start, end).toString().toLowerCase();
                                new Loader().execute(word);
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void print(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-';
    }

    private static String loadFile(String path) {
        String text = "";
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
            int bytesAvailable = stream.available();
            byte[] bytes = new byte[bytesAvailable];
            stream.read(bytes);
            text = new String(bytes);
        } catch (IOException exception) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException exception) {}
            }
        }
        return text;
    }
}