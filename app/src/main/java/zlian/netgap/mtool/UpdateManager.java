package zlian.netgap.mtool;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import zlian.netgap.R;
import zlian.netgap.data.in.VersionInfo;
import zlian.netgap.ui.LoginActivity;
import zlian.netgap.ui.SplashActivity;

/***********************************************************************************************************************
 * Create        : 2017/2/23
 * Author        : zhangyiming
 * Description   : 检查更新
 * Maintenance   :
 ***********************************************************************************************************************/

public class UpdateManager {
    private static final int DOWNLOAD = 0;//下载
    private static final int DOWNLOAD_FINISH = 1;//下载完成
    private static final int START_APK = 2;//启动APP
    private Context mContext;
    private int serviceCode;
    private String mUrl;

    private String mSavePath;//保存路径
    private int progress;//进度值
    private ProgressBar mProgress;//进度条
    private Dialog mDownloadDialog;//更新窗口
    private boolean cancelUpdate = false;//取消更新
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                case START_APK:
                    // 启动
                    apkInstalled("zlian.netgap");
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    private void getVersionData(Context context){
        final String url = "http://app.ejustcn.com/tm/api/v1/ejuser/appversion";
        StringRequest stringRequest = new StringRequest(url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String json) {

                Gson gson = new GsonBuilder().create();
                VersionInfo versionInfo = gson.fromJson(json, VersionInfo.class);
                serviceCode = versionInfo.getVersionCode();
                mUrl = versionInfo.getUrl();
                //mUrl = "http://app.ejustcn.com/doCheck20180516.apk";
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        Volley.newRequestQueue(context).add(stringRequest);
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        getVersionData(this.mContext);

        mHandler.postDelayed(versionUpdate, 2000);
    }

    private Runnable versionUpdate = new Runnable() {
        @Override
        public void run() {
            if (isUpdate()) {
                // 显示提示对话框
                showNoticeDialog();
            } else {
                //Toast.makeText(mContext, "没有新版", Toast.LENGTH_LONG).show();
                LoginActivity.startLoginActivity(mContext);
                SplashActivity activity = (SplashActivity) mContext;
                activity.finish();
            }
        }
    };

    /**
     * 判断是否有更新，需要跟后台产生信息交互
     *
     * @return
     */
    private boolean isUpdate() {

        //调用方法获取服务器可用版本信息，此处模拟为大于当前版本的定值SplashActivity VersionInfo RestClient
        //serviceCode = 11;
        // 版本判断
        if (serviceCode > getVersionCode(mContext)) {
            return true;
        }
        return false;
    }

    /**
     * 获取本地软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        builder.setNegativeButton(R.string.remind_later, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginActivity.startLoginActivity(mContext);
                SplashActivity activity = (SplashActivity) mContext;
                activity.finish();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    private void installApk() {
//        改为doCheck
        File apkfile = new File(mSavePath, "doCheckTemp.apk");
        if (!apkfile.exists()) {
            return;
        }
        //校验MD5

        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    private boolean apkInstalled(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.putExtra("type", "110");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }

        return true;
    }

    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            boolean downloadFailed = false;

            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
//                    改为此软件下载地址即可
                    URL url = new URL(mUrl);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    long length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, "doCheckTemp.apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    long count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    int i = 0;
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((double) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
//                        if (numread <= 0) {
//                            // 下载完成
//                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
//                            break;
//                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                        if (count == length) {
                            break;
                        }
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                    mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                downloadFailed = true;
            } catch (IOException e) {
                e.printStackTrace();
                downloadFailed = true;
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();

            if (downloadFailed) {
                Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
            }

            if (downloadFailed) {
                // 启动画面直接跳转到登陆界面
                LoginActivity.startLoginActivity(mContext);
                SplashActivity activity = (SplashActivity) mContext;
                activity.finish();
            }
        }
    }
}
