package zlian.netgap.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.List;

import zlian.netgap.data.TempData;
import zlian.netgap.mtool.BtService;

public class CommonFun {
        public static void comm_logout(Context context) {
        BtService mChatService = TempData.getIns().getBtService();
        SharedPreferences sp = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
        String token = sp.getString("token", "");
        mChatService.logout(token);
        // clear token
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", "").commit();
        // clear checklist
        // 断开连接
        mChatService.powerOff();
        mChatService.stop();
    }

    public static void comm_disconnectbluetooth()  {
        BtService mChatService = TempData.getIns().getBtService();
        if (mChatService != null) {
            mChatService.powerOff();
            mChatService.stop();
        }
    }

    public static void enable_bluethooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            int retry = 0;
            while (!mBluetoothAdapter.isEnabled()){
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                retry++;
                if (retry >= 30) {
                    return;
                }
            }
        }
    }

    /**
     * 删除目录及目录下文件
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = "v" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void exitAPP(Context pContext) {
        ActivityManager activityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }

        System.exit(0);
    }
}
