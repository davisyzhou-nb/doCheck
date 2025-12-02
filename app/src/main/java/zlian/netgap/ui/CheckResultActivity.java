package zlian.netgap.ui;

import static zlian.netgap.util.CommonFun.comm_disconnectbluetooth;
import static zlian.netgap.util.HttpUtils.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import zlian.netgap.R;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.data.TempData;
import zlian.netgap.mtool.BtService;
import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;
import zlian.netgap.util.CheckData;
import zlian.netgap.util.CommonFun;

@SuppressLint({"SdCardPath", "HandlerLeak"})
public class CheckResultActivity extends CheckBaseActivity {

    private static final int RECONNECT_DELAY = 1000; // 1秒
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BtService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BtService.STATE_CONNECTED:
                            break;
                        case BtService.STATE_CONNECTING:
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            break;
                        case BtService.STATE_CONNECT_ERR:
                            break;
                        case BtService.STATE_CONNECT_LOST: {
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    break;
                case BtService.MESSAGE_UPDATE:
                    break;
                case BtService.MESSAGE_DEVICE_NAME:
                    break;
                case BtService.MESSAGE_RECV_DATA: {
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
                            if (cmd.equalsIgnoreCase("inspect_finish")) {
//                                if (body.getInt("code") == 200) {
//                                    // 点检失败的情况下
//                                    if (CheckPoint.isKeyPass() == 0) {
//                                        // 登出
//                                        BtService mChatService = TempData.getIns().getBtService();
//                                        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//                                        String token = sp.getString("token", "");
//                                        mChatService.logout(token);
//                                    }
//                                    else {
//                                        mHandler.removeCallbacks(mTimeReconnect);
//                                        if (SummaryActivity.instance != null) {
//                                            SummaryActivity.instance.finish();
//                                        }
//                                        TempData.getIns().getBtService().setHandle(null);
//                                        RecordActivity.startRecordActivity(mContext);
//                                        finish();
//                                    }

//                                    //是否拍照
//                                    File file = new File(FileUtil.filePath + TempData.uuid + "/");
//                                    if (file.exists() && file.isDirectory()) {
//                                        if (file.list().length > 0) {
//                                            // 上传图片
//                                            UploadActivity.startUpload(mContext, 0);
//                                            finish();
//                                        }else {
//                                            mHandler.removeCallbacks(mTimeReconnect);
//                                            if (SummaryActivity.instance != null) {
//                                                SummaryActivity.instance.finish();
//                                            }
//                                            TempData.getIns().getBtService().setHandle(null);
//                                            RecordActivity.startRecordActivity(mContext);
//                                            finish();
//                                        }
//                                    }
//                                } else
                                if (body.getInt("code") == 207) {
                                    // Token无效
                                    new AlertDialog.Builder(CheckResultActivity.this)
                                            .setTitle(R.string.exit_tip)
                                            .setMessage(R.string.login_has_expired)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            mHandler.removeCallbacks(mTimeReconnect);
                                                            if (SummaryActivity.instance != null) {
                                                                SummaryActivity.instance.finish();
                                                                SummaryActivity.instance = null;
                                                            }
                                                            TempData.getIns().getBtService().setHandle(null);
                                                            // 断开蓝牙连接
                                                            comm_disconnectbluetooth();
                                                            LoginActivity.startLoginActivity(mContext);
                                                            finish();
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                } else {
                                    mHandler.removeCallbacks(mTimeReconnect);
                                    if (SummaryActivity.instance != null) {
                                        SummaryActivity.instance.finish();
                                        SummaryActivity.instance = null;
                                    }
                                    RecordActivity.startRecordActivity(mContext);
                                    finish();
                                }
                            } else if (cmd.equalsIgnoreCase("logout")) {
                                mHandler.removeCallbacks(mTimeReconnect);
                                if (SummaryActivity.instance != null) {
                                    SummaryActivity.instance.finish();
                                    SummaryActivity.instance = null;
                                }

                                // clear token
                                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", "").commit();

                                LoginActivity.startLoginActivity(mContext);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.device_exception, Toast.LENGTH_LONG).show();
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

    private boolean isPass;
    private ImageView iv_checkresult;
    private Button iv_confirm;
    private Button iv_no;
    private TextView tv_result;
    private int reConnectCount;
    private BluetoothAdapter mBluetoothAdapter = null;
    // 连接重试定时器
    private final Runnable mTimeReconnect = new Runnable() {

        @Override
        public void run() {
            // 每隔两秒重试一次，一共重试三次
            BtService mChatService = TempData.getIns().getBtService();
            if (mChatService.getState() != mChatService.STATE_CONNECTED &&
                    mChatService.getState() != mChatService.STATE_CONNECTING) {
                // 连接不上，重试
                reConnectCount--;
                if (reConnectCount > 0) {
                    //Toast.makeText(getApplicationContext(),"连接失败，重试中",Toast.LENGTH_LONG).show();
                    mHandler.postDelayed(this, RECONNECT_DELAY);
                    if (mBluetoothAdapter != null) {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                        mChatService.OPT_TYPE = mChatService.OPT_24;
                        mChatService.connect(device, false);
                    }
                } else {
                    // 已重试三次连接失败
                    Toast.makeText(getApplicationContext(), R.string.connect_err, Toast.LENGTH_LONG).show();
                    iv_confirm.setClickable(true);
                    iv_no.setClickable(true);
                    iv_confirm.setEnabled(true);
                    iv_no.setEnabled(true);
                }
            } else {
                reConnectCount--;
                if (reConnectCount > 0) {
                    mHandler.postDelayed(this, RECONNECT_DELAY);
                } else {
                    // 未收到控制器回复
                    Toast.makeText(getApplicationContext(), R.string.connect_err, Toast.LENGTH_LONG).show();
                    iv_confirm.setClickable(true);
                    iv_no.setClickable(true);
                    iv_confirm.setEnabled(true);
                    iv_no.setEnabled(true);
                }
            }
        }
    };

    public static void startCheckResultActivity(Context mContext, boolean isPass) {
        Intent intent = new Intent(mContext, CheckResultActivity.class);
        intent.putExtra("isPass", isPass);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_check_result);
            setFinishOnTouchOutside(false);
            isPass = getIntent().getBooleanExtra("isPass", false);
            iv_checkresult = (ImageView) findViewById(R.id.iv_checkresult);
            iv_confirm = (Button) findViewById(R.id.iv_confirm);
            iv_confirm.setText(R.string.yes);
            iv_no = (Button) findViewById(R.id.iv_no);
            iv_no.setText(R.string.no);
            tv_result = (TextView) findViewById(R.id.tv_result);
            iv_confirm.setOnClickListener(this);
            iv_no.setOnClickListener(this);
            if (isPass) {
                iv_checkresult.setBackgroundResource(R.drawable.result_pass);
                tv_result.setText(R.string.check_passed);
            } else {
                iv_checkresult.setBackgroundResource(R.drawable.result_failure);
                tv_result.setText(R.string.check_failed);
            }
            TempData.getIns().getBtService().setHandle(mHandler);
            reConnectCount = 3;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mTimeReconnect);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TempData.getIns().getBtService().setHandle(mHandler);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int viewId = v.getId();
        if (viewId == R.id.iv_confirm) {
            // 2023todo
            String transactionId = UUID.randomUUID().toString().replace("-", "");

            // 发送点检结果给控制器
            // 是和否按钮变灰
            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
            String token = sp.getString("token", "");
            BtService mChatService = TempData.getIns().getBtService();
            if (mChatService.getState() == mChatService.STATE_CONNECTED ||
                    mChatService.getState() == mChatService.STATE_CONNECTING) {
                if (mChatService.getState() == mChatService.STATE_CONNECTING) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            } else {
                // 判断蓝牙是否支持和开启
                // 如果没有开启，就不连接和登陆
                CommonFun.enable_bluethooth();
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    TempData.getIns().reNewBtService();
                    mChatService = TempData.getIns().getBtService();
                    TempData.getIns().getBtService().setHandle(mHandler);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                    mChatService.OPT_TYPE = mChatService.OPT_24;
                    mChatService.connect(device, false);
                    while (mChatService.getState() != mChatService.STATE_CONNECTED) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                    }
                }
            }

            mChatService.startingspectFinish(CheckPoint.isKeyPass(), token, "", transactionId);

            /*2023todo*/
            // 上传图片
            for (CheckPoint checkPoint : TempData.getIns().getCheckPointList()) {
                if (checkPoint.getImgFile() != null) {
                    // 图片质量压缩
                    qualityCompress(checkPoint.getImgBitmap(), checkPoint.getImgFile());
                    // 参数
                    final Map<String, String> params = new HashMap<>();
                    params.put("transactionId", transactionId);
                    params.put("itemId", checkPoint.getId());
                    final Map<String, File> files = new HashMap<>();
                    files.put("file", checkPoint.getImgFile());
                    // 上传图片
                    new Thread(() -> uploadInspectImage(params, files)).start();
                }
            }
            /*2023todo*/

//                if (mChatService.getState() != BtService.STATE_CONNECTED) {
//                    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(TempData.getIns().getChoosedAddress());
//                    mChatService.connect(device, false);
//                    // 等待1s
//                    try {
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                    }
//                }
//                if (mChatService.getState() == BtService.STATE_CONNECTED) {
//
//                    //判断是否有拍照
//                    File file = new File(FileUtil.filePath + TempData.uuid + "/");
//                    if (file.exists() && file.isDirectory()) {
//                        if (file.list().length > 0) {
//                            mChatService.startingspectFinish(CheckPoint.isKeyPass(), token, TempData.uuid + ".zip");
//                        }
//                        else {
//                            mChatService.startingspectFinish(CheckPoint.isKeyPass(), token, "");
//                        }
//                    }
//                    else {
//                        mChatService.startingspectFinish(CheckPoint.isKeyPass(), token, "");
//                    }
//                }

            iv_confirm.setClickable(false);
            iv_no.setClickable(false);
            iv_confirm.setEnabled(false);
            iv_no.setEnabled(false);
            // 启动定时器，重新连接
            reConnectCount = 3;
            mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);

        } else if (viewId == R.id.iv_no) {
                finish();
        }
    }
}
