package com.example.alexey.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private static final int SELECTED_FILE_RESULT_CODE = 1;
    private RecyclerView recyclerView;
    private TextView textView;
    private int currentPage = 0;
    private int pageCount = 0;
    private Toast pageToast;
    private Toast translationToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        textView = (TextView) findViewById(R.id.textView);

        pageToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
        pageToast.setGravity(Gravity.LEFT|Gravity.TOP, 0, 0);

        translationToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new PageSeparator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                View child = recyclerView.findChildViewUnder(50, 50);
                int page = 1 + recyclerView.getChildAdapterPosition(child);
                if (page != currentPage) {
                    currentPage = page;
                    pageToast.setText(currentPage + " / " + pageCount);
                    pageToast.show();
                }
            }
        });

        ClickableTextView.setOnWordSelectedListener(new ClickableTextView.OnWordSelectedListener() {
            @Override
            public void onWordSelected(String word) {
                translationToast.setText(word);
                translationToast.show();
            }
        });

        openBook(new File("/sdcard/Download/lovecraft.fb2"));
    }

    public void openBook(File file) {
        FBParserSpanned2 p = new FBParserSpanned2();
        long t = System.currentTimeMillis();
        p.parse(file);
        t = System.currentTimeMillis() - t;
        Log.d("MyLog", "parse time " + t);

        final CharSequence text = p.text;
        final long time = System.currentTimeMillis();

        textView.setText(p.text);
        textView.post(new Runnable() {
            @Override
            public void run() {
                List<CharSequence> pages = new ArrayList<>();
                Layout layout = textView.getLayout();
                int lineCount = layout.getLineCount();
                int pageHeight = textView.getHeight();
                int currentPageHeight = 0;
                int start = 0;
                for (int line = 0; line < lineCount; line++) {
                    int lineHeight = layout.getLineBottom(line) - layout.getLineTop(line);
                    currentPageHeight += lineHeight;
                    if (currentPageHeight > pageHeight) {
                        int end = layout.getLineEnd(line);
                        pages.add(text.subSequence(start, end));
                        start = end;
                        currentPageHeight = 0;
                    }
                }
                pages.add(text.subSequence(start, text.length()));
                pageCount = pages.size();
                textView.setText(null);
                recyclerView.setAdapter(new TestAdapter(TestActivity.this, pages));
                long finishTime = System.currentTimeMillis() - time;
                Log.d("MyLog", "render time " + finishTime);
            }
        });
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
                String filePath = data.getStringExtra(MainActivity.SELECTED_FILE_PATH);
                openBook(new File(filePath));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class PageSeparator extends RecyclerView.ItemDecoration {
        private Paint paint;
        private int strokeWidth;

        PageSeparator() {
            strokeWidth = 2;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.DKGRAY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, 0, strokeWidth);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                c.drawLine(
                        layoutManager.getDecoratedLeft(child),
                        layoutManager.getDecoratedBottom(child),
                        layoutManager.getDecoratedRight(child),
                        layoutManager.getDecoratedBottom(child),
                        paint);
            }
        }
    }

    class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
        private LayoutInflater inflater;
        private List<CharSequence> pages;

        TestAdapter(Context context, List<CharSequence> pages) {
            inflater = LayoutInflater.from(context);
            this.pages = pages;
        }

        @Override
        public TestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item, parent, false);
            return new TestAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TestAdapter.ViewHolder holder, int position) {
            holder.textView.setText(pages.get(position));
        }

        @Override
        public int getItemCount() {
            return pages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.pageText);
            }
        }
    }

    private static String loadFile(File file) {
        String text = "";
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
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
