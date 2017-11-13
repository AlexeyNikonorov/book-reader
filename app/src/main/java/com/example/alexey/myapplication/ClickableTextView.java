package com.example.alexey.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ClickableTextView extends android.support.v7.widget.AppCompatTextView
                                implements View.OnTouchListener {
    public static OnWordSelectedListener onWordSelectedListener;

    public ClickableTextView(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public ClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public ClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    public static void setOnWordSelectedListener(OnWordSelectedListener listener) {
        onWordSelectedListener = listener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            CharSequence text = getText();
            int offset = getOffsetForPosition(event.getX(), event.getY());
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
                onWordSelectedListener.onWordSelected(word);
            }
        }
        return true;
    }

    public interface OnWordSelectedListener {
        void onWordSelected(String word);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-';
    }
}
