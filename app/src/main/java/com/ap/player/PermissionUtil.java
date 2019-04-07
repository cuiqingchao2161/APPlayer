package com.ap.player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * PackageName: com.sybercare.thermometer.util
 * ClassName: PermissionUtil
 * Author: cuiqingchao
 * CreateDate: 2018/6/19 19:43
 * Description:
 *
 * -----------------------------------Version Info----------------------------------------------
 * Version: V0.1    UpdateUser: cuiqingchao    UpdateDate: 2018/6/19 19:43   UpdateRemark:
 *
 *
 * ---------------------------------------------------------------------------------------------
 */
public class PermissionUtil {
    /**
     * group:android.permission-group.CONTACTS
     * permission:android.permission.GET_ACCOUNTS
     * permission:android.permission.READ_CONTACTS
     * permission:android.permission.WRITE_CONTACTS
     */
    public static int PERMISSION_WRITE_CONTACTS_CODE = 0;

    /**
     * group:android.permission-group.PHONE
     * permission:android.permission.READ_CALL_LOG
     * permission:android.permission.READ_PHONE_STATE
     * permission:android.permission.CALL_PHONE
     * permission:android.permission.WRITE_CALL_LOG
     * permission:android.permission.USE_SIP
     * permission:android.permission.PROCESS_OUTGOING_CALLS
     * permission:com.android.voicemail.permission.ADD_VOICEMAIL
     */
    public final static int PERMISSION_READ_PHONE_STATE_CODE = 100;

    /**
     * group:android.permission-group.CALENDAR
     * permission:android.permission.WRITE_CALENDAR
     * permission:android.permission.WRITE_CALENDAR
     */
    public static int PERMISSION_READ_CALENDAR_CODE = 200;

    /**
     * group:android.permission-group.CAMERA
     * permission:android.permission.CAMERA
     */
    public static int PERMISSION_CAMERA_CODE = 300;

    /**
     * group:android.permission-group.SENSORS
     * permission:android.permission.BODY_SENSORS
     */
    public static int PERMISSION_BODY_SENSORS_CODE = 400;

    /**
     * group:android.permission-group.LOCATION
     * permission:android.permission.ACCESS_FINE_LOCATION
     * permission:android.permission.ACCESS_COARSE_LOCATION
     */

    public static int PERMISSION_ACCESS_FINE_LOCATION_CODE = 500;

    /**
     * group:android.permission-group.STORAGE
     * permission:android.permission.READ_EXTERNAL_STORAGE
     * permission:android.permission.WRITE_EXTERNAL_STORAGE
     */
    public static int PERMISSION_READ_EXTERNAL_STORAGE_CODE = 600;

    /**
     * group:android.permission-group.MICROPHONE
     * permission:android.permission.RECORD_AUDIO
     */
    public static int PERMISSION_RECORD_AUDIO_CODE = 700;

    /**
     * group:android.permission-group.SMS
     * permission:android.permission.READ_SMS
     * permission:android.permission.RECEIVE_WAP_PUSH
     * permission:android.permission.RECEIVE_MMS
     * permission:android.permission.RECEIVE_SMS
     * permission:android.permission.SEND_SMS
     * permission:android.permission.READ_CELL_BROADCASTS
     */
    public static int PERMISSION_READ_SMS_CODE = 800;

    /**
     * android.permission.REQUEST_INSTALL_PACKAGES
     */
    public static int PERMISSION_INSTALL_PACKAGES_CODE = 900;

    /**
     * all project need permission group
     */
    public final static int PERMISSION_INIT_CODE = 1000;


    public static String[] initPermission = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

    /**
     * 判断是否具备所有权限
     *
     * @param permissions 所有权限
     * @return true 具有所有权限  false没有具有所有权限，此时包含未授予的权限
     */
    public static boolean isHasPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        for (String permission : permissions) {
            if (!isHasPermission(permission))
                return false;
        }
        return true;
    }

    /**
     * 判断该权限是否已经被授予
     *
     * @param permission
     * @return true 已经授予该权限 ，false未授予该权限
     */
    private static boolean isHasPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        boolean isHas = ContextCompat.checkSelfPermission(APlayerApp.getApp().getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        return isHas;
    }

    /**
     * 请求权限,经测试发现TabActivity管理Activity时，在Activity中请求权限时需要传入父Activity对象，即TabActivity对象
     * 并在TabActivity管理Activity中重写onRequestPermissionsResult并分发到子Activity，否则回调不执行  。TabActivity回调中  调用getLocalActivityManager().getCurrentActivity().onRequestPermissionsResult(requestCode, permissions, grantResults);分发到子Activity

     *
     *
     * @param object      Activity or Fragment
     * @param requestCode 请求码
     * @param permissions 请求权限
     */
    public static void requestPermissions(Object object, int requestCode, String... permissions) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isHasPermissions(permission)) {
                arrayList.add(permission);
            }
        }
        if (arrayList.size() > 0) {
            if (object instanceof Activity) {
                Activity activity = (Activity) object;
                Activity activity1 = activity.getParent() != null && activity.getParent() instanceof TabActivity ? activity.getParent() : activity;
                ActivityCompat.requestPermissions(activity1, arrayList.toArray(new String[] {}), requestCode);
            } else if (object instanceof Fragment) {
                Fragment fragment = (Fragment) object;
                //当Fragment嵌套Fragment时使用getParentFragment(),然后在父Fragment进行分发，否则回调不执行
                Fragment fragment1 = fragment.getParentFragment() != null ? fragment.getParentFragment() : fragment;
                fragment1.requestPermissions(arrayList.toArray(new String[]{}), requestCode);
            } else {
                throw new RuntimeException("the object must be Activity or Fragment");
            }
        }
    }


    /**
     * 打开 APP 的详情设置
     */
    public static void openAppDetails(final Context context,String message , final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //builder.setMessage("备份通讯录需要访问 “通讯录” 和 “外部存储器”，请到 “应用信息 -> 权限” 中授予！");
        builder.setMessage(message);
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog,which);
                jumpPermissionPage(context);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog,which);
            }
        });
        builder.show();
    }

    /**
     * 国产厂商跳转“设置”适配
     */
    public static void jumpPermissionPage(Context context) {
        String name = Build.MANUFACTURER;
        //L.e(TAG, "jumpPermissionPage --- name : " + name);
        switch (name) {
            case "HUAWEI":
                goHuaWeiMainager(context);
                break;
            case "vivo":
                goVivoMainager(context);
                break;
            case "OPPO":
                goOppoMainager(context);
                break;
            case "Coolpad":
                goCoolpadMainager(context);
                break;
            case "Meizu":
                goMeizuMainager(context);
                break;
            case "Xiaomi":
                goXiaoMiMainager(context);
                break;
            case "samsung":
                goSangXinMainager(context);
                break;
            case "Sony":
                goSonyMainager(context);
                break;
            case "LG":
                goLGMainager(context);
                break;
            default:
                goIntentSetting(context);
                break;
        }
    }

    private static void goLGMainager(Context context){
        try {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            intent.setData(Uri.parse("package:" + APlayerApp.getApp().getApplicationContext().getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "跳转失败", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            goIntentSetting(context);
        }
    }
    private static void goSonyMainager(Context context){
        try {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + APlayerApp.getApp().getApplicationContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "跳转失败", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            goIntentSetting(context);
        }
    }

    private static void goHuaWeiMainager(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName
                    comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + APlayerApp.getApp().getApplicationContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "跳转失败", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            goIntentSetting(context);
        }
    }

    private static String getMiuiVersion(Context context) {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    private static void goXiaoMiMainager(Context context) {
        String rom = getMiuiVersion(context);
        //L.e(TAG,"goMiaoMiMainager --- rom : "+rom);
        Intent intent=new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", APlayerApp.getApp().getApplicationContext().getPackageName());
        } else if ("V8".equals(rom) || "V9".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", APlayerApp.getApp().getApplicationContext().getPackageName());
        } else {
            goIntentSetting(context);
        }
        context.startActivity(intent);
    }

    private static void goMeizuMainager(Context context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("packageName", APlayerApp.getApp().getApplicationContext().getPackageName());
            context.startActivity(intent);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting(context);
        }
    }

    private static void goSangXinMainager(Context context) {
        //三星4.3可以直接跳转
        goIntentSetting(context);
    }

    private static void goIntentSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void goOppoMainager(Context context) {
        doStartApplicationWithPackageName(context,"com.coloros.safecenter");
    }

    /**
     * doStartApplicationWithPackageName("com.yulong.android.security:remote")
     * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
     * startActivity(open);
     * 本质上没有什么区别，通过Intent open...打开比调用doStartApplicationWithPackageName方法更快，也是android本身提供的方法
     */
    private static void goCoolpadMainager(Context context) {
        doStartApplicationWithPackageName(context,"com.yulong.android.security:remote");
      /*  Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
        startActivity(openQQ);*/
    }

    private static void goVivoMainager(Context context) {
        doStartApplicationWithPackageName(context,"com.bairenkeji.icaller");
     /*   Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.vivo.securedaemonservice");
        startActivity(openQQ);*/
    }

    /**
     * 此方法在手机各个机型设置中已经失效
     *
     * @return
     */
    private Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }

    private static void doStartApplicationWithPackageName(Context context, String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        Log.e("PermissionPageManager", "resolveinfoList" + resolveinfoList.size());
        for (int i = 0; i < resolveinfoList.size(); i++) {
            Log.e("PermissionPageManager", resolveinfoList.get(i).activityInfo.packageName + resolveinfoList.get(i).activityInfo.name);
        }
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packageName参数2 = 参数 packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packageName参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                goIntentSetting(context);
                e.printStackTrace();
            }
        }
    }

}
