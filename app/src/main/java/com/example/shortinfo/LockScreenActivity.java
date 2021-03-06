package com.example.shortinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {
    public String str = "";
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;
    TextView textView8;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.btn_layout);


        final Bundle bundle = new Bundle();

        textView1 = findViewById(R.id.value1);
        textView2 = findViewById(R.id.value2);
        textView3 = findViewById(R.id.value3);
        textView4 = findViewById(R.id.value4);
        textView5 = findViewById(R.id.value5);
        textView6 = findViewById(R.id.value6);
        textView7 = findViewById(R.id.value7);
        textView8 = findViewById(R.id.value8);
        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%BD%94%EB%A1%9C%EB%82%98").get();
                    Log.d("signal","first");

                    Element contents = doc.select("div.status_info li.info_01").select(".info_num").first();
                    Log.d("?????????",contents.text());
                    bundle.putString("confirmed",contents.text());

                    contents = doc.select("div.status_info li.info_01").select("em.info_variation").first();
                    Log.d("???",contents.text());
                    bundle.putString("confirmed_var",contents.text());

                    contents = doc.select("div.status_info li.info_02").select(".info_num").first();
                    Log.d("?????????",contents.text());
                    bundle.putString("inspection",contents.text());

                    contents = doc.select("div.status_info li.info_02").select("em.info_variation").first();
                    Log.d("???",contents.text());
                    bundle.putString("inspection_var",contents.text());


                    contents = doc.select("div.status_info li.info_03").select(".info_num").first();
                    Log.d("????????????",contents.text());
                    bundle.putString("release",contents.text());

                    contents = doc.select("div.status_info li.info_03").select("em.info_variation").first();
                    Log.d("???",contents.text());
                    bundle.putString("release_var",contents.text());


                    contents = doc.select("div.status_info li.info_04").select(".info_num").first();
                    Log.d("?????????",contents.text());
                    bundle.putString("dead",contents.text());


                    contents = doc.select("div.status_info li.info_04").select("em.info_variation").first();
                    Log.d("???",contents.text());
                    bundle.putString("dead_var",contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
                    Log.d("??? ?????? ?????????",contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
                    Log.d("???",contents.text());

                    bundle.putString("numbers",str);
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
                catch (Exception e){
                    Log.e("error",e.getMessage());
                }
            }
        }.start();
    }
    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg){
            textView1.setText("????????? : "+msg.getData().getString("confirmed"));
            textView2.setText("???"+msg.getData().getString("confirmed_var"));
            textView3.setText("????????? : "+msg.getData().getString("inspection"));
            textView4.setText("???"+msg.getData().getString("inspection_var"));
            textView5.setText("???????????? : "+msg.getData().getString("release"));
            textView6.setText("???"+msg.getData().getString("release_var"));
            textView7.setText("????????? : "+msg.getData().getString("dead"));
            textView8.setText("???"+msg.getData().getString("dead_var"));





        }
    };
}
