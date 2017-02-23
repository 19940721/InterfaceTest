package com.gionee.autotest.interfacetest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.gionee.auto.auto1salarm.R;

public class Result extends Activity {
    private TextView count,succf;
    private int times,succ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        count = (TextView)findViewById(R.id.times_all);
        succf = (TextView)findViewById(R.id.times_succ);
        Intent mIntent = getIntent();
        times = (mIntent.getExtras().getInt("times_all"));
        succ = mIntent.getExtras().getInt("succ");
        String all = String.valueOf(times);
        String succfull = String.valueOf(succ);
        count.setText("测试总数："+all);
        succf.setText("成功总数："+succfull);
    }

}