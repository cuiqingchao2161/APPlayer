package com.ap.player;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.ap.live.LiveManager;
import com.ap.live.list.LiveList;
import com.ap.live.room.Room;
import com.ap.live.room.Videoinfo;
import com.google.gson.Gson;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class MainActivity extends RxAppCompatActivity implements TabLayout
        .BaseOnTabSelectedListener, LiveAdapter.OnItemClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private LiveAdapter liveAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 配置recycleview
        recyclerView = findViewById(R.id.recycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        liveAdapter = new LiveAdapter(this);
        liveAdapter.setItemClickListener(this);
        recyclerView.setAdapter(liveAdapter);

        //配置tab
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);
        //添加标签
        addTabs();
        checkPermssion();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        LiveManager.getInstance()
                .getLiveList(tab.getTag().toString())
                .compose(this.<LiveList>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<LiveList>() {

                    @Override
                    public void onNext(LiveList liveList) {
                        Log.i("onTabSelected data", new Gson().toJson(liveList));
                        liveAdapter.setLiveList(liveList);
                        liveAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tabLayout.removeOnTabSelectedListener(this);
    }


    @Override
    public void onItemClick(String id) {
        LiveManager.getInstance()
                .getLiveRoom(id)
                .compose(this.<Room>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<Room>() {
                    @Override
                    public void onNext(Room room) {
                        Videoinfo info = room.getData().getInfo().getVideoinfo();
                        String[] plflags = info.getPlflag().split("_");
                        String room_key = info.getRoom_key();
                        String sign = info.getSign();
                        String ts = info.getTs();
                        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                        String v = "3";
                        if (null != plflags && plflags.length > 0) {
                            v = plflags[plflags.length - 1];
                        }
                        intent.putExtra("url", "http://pl" + v + ".live" +
                                ".panda.tv/live_panda/" + room_key
                                + "_mid" +
                                ".flv?sign=" + sign +
                                "&time=" + ts);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void addTabs() {
        addTab("lol", "英雄联盟");
        addTab("acg", "二次元");
        addTab("food", "美食");
    }


    private void addTab(String tag, String title) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setTag(tag);
        tab.setText(title);
        tabLayout.addTab(tab);
    }

    public void toLocalPlayer(View view) {
        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
        startActivity(intent);
    }
    private long PERMISSION_TIME = 0;
    boolean isPermissionFinish = true;
    boolean isPermissionFirst = true;

    /**
     * READ_PHONE_STATE 读取手机状态信息权限
     * ACCESS_COARSE_LOCATION 蓝牙扫描权限
     */
    private void checkPermssion() {
        if (!PermissionUtil.isHasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (isPermissionFinish && ((System.currentTimeMillis() - PERMISSION_TIME) > 500 || isPermissionFirst)) {
                isPermissionFirst = false;
                isPermissionFinish = false;
                PermissionUtil.requestPermissions(this, PermissionUtil.PERMISSION_READ_PHONE_STATE_CODE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }


    /**
     * 处理权限请求结果
     *
     * @param requestCode  请求权限时传入的请求码，用于区别是哪一次请求的
     * @param permissions  所请求的所有权限的数组
     * @param grantResults 权限授予结果，和 permissions 数组参数中的权限一一对应，元素值为两种情况，如下:
     *                     授予: PackageManager.PERMISSION_GRANTED
     *                     拒绝: PackageManager.PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_READ_PHONE_STATE_CODE:
                boolean isAllGranted = true;

                // 判断是否所有的权限都已经授予了
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }

                if (isAllGranted) {
                    // 如果所有的权限都授予了
                    isPermissionFinish = true;
                    PERMISSION_TIME = System.currentTimeMillis();
                } else {
                    isPermissionFinish = false;
                    // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                    PermissionUtil.openAppDetails(this, "需要使用“存储”权限，请到 “设置 -> 安全 -> 应用权限” 中授予！",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isPermissionFinish = true;
                                    PERMISSION_TIME = System.currentTimeMillis();
                                }
                            });
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
