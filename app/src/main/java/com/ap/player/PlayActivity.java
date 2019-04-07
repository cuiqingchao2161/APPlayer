package com.ap.player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;

public class PlayActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private APlayer aPlayer;
    public String url;
    private SeekBar seekBar;

    private int progress;
    private boolean isTouch;
    private boolean isSeek;
    private EditText editText_url;
//    private Button button_play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_play);
//        button_play = findViewById(R.id.play_btn);
        editText_url = findViewById(R.id.edit_url);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        aPlayer = new APlayer();
        aPlayer.setSurfaceView(surfaceView);
       /* File file = new File(Environment.getDataDirectory(), "123.mp4");
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG);
            return;
        }*/
        //aPlayer.setDataSource("/data/123.mp4");
        aPlayer.setOnPrepareListener(new APlayer.OnPrepareListener() {
            /**
             * 视频信息获取完成 随时可以播放的时候回调
             */
            @Override
            public void onPrepared() {
                // TODO: 2018/12/15  时间设置
               /* //获得时间
                int duration = aPlayer.getDuration();
                //直播： 时间就是0
                if (duration != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //显示进度条
                            seekBar.setVisibility(View.VISIBLE);
                        }
                    });
                }*/
                aPlayer.start();
            }
        });

        aPlayer.setOnErrorListener(new APlayer.OnErrorListener() {
            @Override
            public void onError(int error) {
                Log.e("PlayActivity onError", error + "");
            }
        });

        aPlayer.setOnProgressListener(new APlayer.OnProgressListener() {

            @Override
            public void onProgress(final int progress2) {
                /*if (!isTouch) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int duration = aPlayer.getDuration();
                            //如果是直播
                            if (duration != 0) {
                                if (isSeek){
                                    isSeek = false;
                                    return;
                                }
                                //更新进度 计算比例
                                seekBar.setProgress(progress2 * 100 / duration);
                            }
                        }
                    });
                }*/
            }
        });
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        url = getIntent().getStringExtra("url");
       /* File file = new File(Environment.getExternalStorageDirectory(), "123.mp4");
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG);
            return;
        }*/
        // aPlayer.setDataSource(file.getAbsolutePath());
        //  aPlayer.setDataSource(url);
        aPlayer.setDataSource("/storage/emulated/0/qfmidea/test.mp4");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        aPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        aPlayer.release();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void startPlay(View view) {
        String path = editText_url.getText().toString();
        aPlayer.setDataSource("/storage/emulated/0/qfmidea/"+path);
        aPlayer.prepare();
    }

    public void startPlayOnline(View view) {

        String path = editText_url.getText().toString();
//        aPlayer.setDataSource("http://luyin....com/luyin/2018/201809/20180921/90190/"+path);
        aPlayer.setDataSource("http://vjs.zencdn.net/v/oceans.mp4 ");
        aPlayer.prepare();
    }
}
