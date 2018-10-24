package com.whut.ein3614.customviewtest;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity implements VerticalScrollView.OnGiveUpTouchEventListener {

    private VerticalScrollView mVerticalScrollView;
    private ListView listView;
    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_2);
        Log.d(TAG, "onCreate");
        initView();
    }

    private void initView() {
        mVerticalScrollView = findViewById(R.id.container);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.content_layout, mVerticalScrollView, false);
        TextView textView = layout.findViewById(R.id.title);
        textView.setText("page 1");
        layout.setBackgroundColor(Color.rgb(255, 255, 0));
        createList(layout);
        mVerticalScrollView.addView(layout);
        mVerticalScrollView.setOnGiveUpTouchEventListener(this);
    }

    private void createList(ViewGroup layout) {
        listView = layout.findViewById(R.id.list);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name " + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.content_list_item, R.id.name, datas);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        if (listView.getFirstVisiblePosition() == 0) {
            View view = listView.getChildAt(0);
            if (view != null && view.getTop() >= 0) {
                Log.d(TAG, "it is top");
                return true;
            }
        }
        return false;
    }
}
