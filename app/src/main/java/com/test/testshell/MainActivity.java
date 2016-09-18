package com.test.testshell;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Application app = this.getApplication();

        JniUtils jni = new JniUtils();

        Log.i("demo", this.getClass().getName() + "onCreate : " + jni.getCLanguageString());
    }

    public static String Hanzi(String n) {//完成该函数功能，不需要写main()调用函数，系统会自动调用
        char[] t1 = null;
        t1 = n.toCharArray();
        int t0 = t1.length;
        int count = 0;
        for (int i = 0; i < t0; i++) {
            if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                count++;

            }
        }

        return "汉字个数为" + count;
    }
}
