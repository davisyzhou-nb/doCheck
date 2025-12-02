package zlian.netgap.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;

import zlian.netgap.R;
import zlian.netgap.data.TempData;
import zlian.netgap.data.in.VersionInfo;
import zlian.netgap.mtool.BtService;
import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;
import zlian.netgap.mtool.RecvStatus;
import zlian.netgap.mtool.UpdateManager;
import zlian.netgap.support.DownLoadFile;
import zlian.netgap.util.CheckData;
import zlian.netgap.util.CommonFun;

public class SplashActivity extends BaseActivity {
    public final static int DIALOG_PROGRESS = 0;
    public final static int DIALOG_UPDATEAPK = 2;
    protected int mVersionCode;
    protected ProgressDialog pd;
    private VersionInfo versionInfo;
    private boolean mupdateapk;
    private String mupdateurl;
    private Context mContext;
    private TextView tv_status_desc, tv_version;

    private static final int WAIT_RECV_DATA_MAX_TIME = 4000;
    private RecvStatus recvstatus = RecvStatus.NORECV;
    //检查更新类
    private UpdateManager updateManager;

    private final Runnable mTimeRcvChk = new Runnable() {

        @Override
        public void run() {
            switch (recvstatus) {
                case NORECV:
                    LoginActivity.startLoginActivity(mContext);
                    finish();
                    break;
                case RECVNG:
                    break;
                case RECVOK:
                    break;
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BtService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BtService.STATE_CONNECTED: {
                            BtService mChatService = TempData.getIns().getBtService();
                            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                            String token = sp.getString("token", "");
                            mChatService.querytate(token);
                            // 发送命令后一段时间内硬件没有回复，就跳转到登陆界面
                            recvstatus = RecvStatus.NORECV;
                            postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                        }
                        break;
                        case BtService.STATE_CONNECTING:
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            break;
                        case BtService.STATE_CONNECT_ERR:
                        case BtService.STATE_CONNECT_LOST: {
                            LoginActivity.startLoginActivity(mContext);
                            finish();
                        }
                        break;
                    }
                    break;
                case BtService.MESSAGE_UPDATE:
                    break;
                case BtService.MESSAGE_DEVICE_NAME:
                    break;
                case BtService.MESSAGE_RECV_DATA: {
                    // 进入 dashbord
                    Data data = (Data) msg.obj;
                    byte[] buf = data.getData();
                    if (CheckData.getChkdat().validateData(data) == DataStatus.OK) {
                        Data newData;
                        if (CheckData.getChkdat().getBufData() != null) {
                            newData = CheckData.getChkdat().getBufData();
                            CheckData.getChkdat().setBufData(null);
                        } else {
                            newData = data;
                        }
                        String info;
                        try {
                            info = new String(newData.getData(), 0, newData.getData().length, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return;
                        }
                        try {
                            JSONTokener jsonParser = new JSONTokener(info);
                            JSONObject response = (JSONObject) jsonParser.nextValue();
                            JSONObject body = response.getJSONObject("response");
                            String cmd = body.getString("cmd");
                            if (body.getInt("code") == 212) { // 当前token 有效，还未登出
                                recvstatus = RecvStatus.RECVOK;
                                mHandler.removeCallbacks(mTimeRcvChk);

                                RecordActivity.startRecordActivity(mContext);
                                finish();
                            } else {
                                LoginActivity.startLoginActivity(mContext);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            }
        }
    };

    private Runnable startlogin = new Runnable() {
        @Override
        public void run() {

            //doGetVersionUpdate();
            // 检查网络是否连接
            if (isNetworkConnected() == false) {
                LoginActivity.startLoginActivity(mContext);
                finish();
            }
            else {
                //检查更新类
                updateManager = new UpdateManager(SplashActivity.this);
                updateManager.checkUpdate();
            }

//            // 如果连接上回记住的设备，有可能导致同一时刻另外一个用户连接不上
//            // 启动画面直接跳转到登陆界面
//            LoginActivity.startLoginActivity(mContext);
//            finish();

//            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//            String token = sp.getString("token", "");
//            if (token.isEmpty()) {
//                LoginActivity.startLoginActivity(mContext);
//                finish();
//            } else {
//                if (TempData.getIns().getChoosedAddress().length() > 10) {
//                    CommonFun.enable_bluethooth();
//                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//                        LoginActivity.startLoginActivity(mContext);
//                        finish();
//                    } else {
//                        BtService mChatService = TempData.getIns().getBtService();
//                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(TempData.getIns().getChoosedAddress());
//                        mChatService.connect(device, false);
//                    }
//                } else {
//                    LoginActivity.startLoginActivity(mContext);
//                    finish();
//                }
//            }
        }
    };

    private boolean isNetworkConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    private void blueToothOper() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, R.string.not_found_bt, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void LoadSetting() {
        ObjectInputStream in = null;
        try {
            FileInputStream is = openFileInput("setting.obj");
            in = new ObjectInputStream(is);
            // device name
            TempData.getIns().setDeviceName(in.readUTF());

            // device address
            TempData.getIns().setChoosedAddress(in.readUTF());
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_splash);
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            mVersionCode = info.versionCode;
            mupdateapk = false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        long timestamp = System.currentTimeMillis();
//        Date aa = new Date(timestamp);

        tv_status_desc = (TextView) findViewById(R.id.tv_status_desc);
        tv_version = (TextView) findViewById(R.id.version);
        tv_version.setText(CommonFun.getLocalVersionName(SplashActivity.this));

        blueToothOper();
        LoadSetting();
        TempData.getIns().getBtService().setHandle(mHandler);

        mHandler.postDelayed(startlogin, 1000);
    }

//    private void doGetVersionUpdate() {
//        final String url = "http://app.ejustcn.com/tm/api/v1/ejuser/appversion";
//        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String json) {
//
//                Gson gson = new GsonBuilder().create();
//                VersionInfo versionInfo = gson.fromJson(json, VersionInfo.class);
//                serviceCode = versionInfo.getVersionCode();
//                mupdateurl = versionInfo.getUrl();
//
//                // 获取当前软件版本
//                int versionCode = getVersionCode(mContext);
//
//                new AlertDialog.Builder(SplashActivity.this)
//                        .setTitle("提示")
//                        .setMessage("发现新版本,是否下载更新?")
//                        .setPositiveButton("是",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int whichButton) {
//                                        tv_status_desc.setText("正在更新版本,请稍候...");
//                                        updateapk();
//                                    }
//                                })
//                        .setNegativeButton("否",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int whichButton) {
//                                        //tv_status_desc.setText("页面跳转中...");
//                                        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//                                        String token = sp.getString("token", "");
//                                        if (token.isEmpty()) {
//                                            LoginActivity.startLoginActivity(mContext);
//                                            finish();
//                                        } else {
//                                            if (TempData.getIns().getChoosedAddress().length() > 10) {
//                                                CommonFun.enable_bluethooth();
//                                                BtService mChatService = TempData.getIns().getBtService();
//                                                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(TempData.getIns().getChoosedAddress());
//                                                mChatService.connect(device, false);
//                                            } else {
//                                                LoginActivity.startLoginActivity(mContext);
//                                                finish();
//                                            }
//                                        }
//                                    }
//                                })
//                        .setCancelable(false)
//                        .show();
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
//    }

//    @Override
//    protected Dialog onCreateDialog(int id) {
//        switch (id) {
//            case DIALOG_PROGRESS:
//                pd = new ProgressDialog(this);
//                pd.setTitle("提示");
//                pd.setMessage("正在检测版本更新...");
//                pd.setIndeterminate(true);
//                pd.setCancelable(true);
//                return pd;
//            case DIALOG_UPDATEAPK:
//                return new AlertDialog.Builder(SplashActivity.this)
//                        .setTitle("提示")
//                        .setMessage("发现新版本,是否下载更新?")
//                        .setPositiveButton("是",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int whichButton) {
//                                        tv_status_desc.setText("正在更新版本,请稍候...");
//                                        updateapk();
//                                    }
//                                })
//                        .setNegativeButton("否",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int whichButton) {
//                                        //tv_status_desc.setText("页面跳转中...");
//                                        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//                                        String token = sp.getString("token", "");
//                                        if (token.isEmpty()) {
//                                            LoginActivity.startLoginActivity(mContext);
//                                            finish();
//                                        } else {
//                                            if (TempData.getIns().getChoosedAddress().length() > 10) {
//                                                CommonFun.enable_bluethooth();
//                                                BtService mChatService = TempData.getIns().getBtService();
//                                                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(TempData.getIns().getChoosedAddress());
//                                                mChatService.connect(device, false);
//                                            } else {
//                                                LoginActivity.startLoginActivity(mContext);
//                                                finish();
//                                            }
//                                        }
//                                    }
//                                })
//                        .setCancelable(false)
//                        .show();
//        }
//
//        return null;
//    }

    public String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    private void updateapk() {
        DownLoadFile downLoadFile = new DownLoadFile();
        downLoadFile.downLoadFile(SplashActivity.this, mupdateurl, ".apk");
    }
}
