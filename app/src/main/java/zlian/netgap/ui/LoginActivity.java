package zlian.netgap.ui;

import static zlian.netgap.util.CommonFun.comm_disconnectbluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import zlian.netgap.R;
import zlian.netgap.app.MyApplication;
import zlian.netgap.data.TempData;
import zlian.netgap.mtool.BtService;
import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;
import zlian.netgap.mtool.DeviceListActivity;
import zlian.netgap.mtool.RecvStatus;
import zlian.netgap.util.CheckData;
import zlian.netgap.util.CommonFun;
import zlian.netgap.util.StringUtils;

public class LoginActivity extends BaseActivity {

    /**
     * 蓝牙部分
     */
    private static final boolean D = true;
    private static final int BLUETOOTH_REQUEST = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int WAIT_RECV_DATA_MAX_TIME = 2000;
    /**
     * 选中的设备的地址
     */
    //private String choosedAddress = "";
    private final UUID MY_UUID = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    private final String NAME = "Bluetooth_Socket";
    private ProgressDialog mProgressDialog;
    /*
     * NFC
     * */
    private Button iv_login;
    private EditText et_device_info;
    private EditText et_customer, et_userName, et_password;
    private Handler handler;
    private boolean NFCScanFirstTime = true;
    private long NFCScanFirstTimestamp = 0;
    private AlertDialog mDialog;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    /**
     * 选中的设备的名称
     */
    private String choosedName = "";
    private String choosenAddress = "";
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream os;

    private TextView tv_version;
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * 蓝牙部分
     */
    private RecvStatus recvstatus = RecvStatus.NORECV;
    private int count = 0; // retry times
    private String nfcCardId = "";
    private int loginaproch = 0; // 1: account, 2:nfc
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BtService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BtService.STATE_CONNECTED:
                            TempData.getIns().setTimestamp(0);
                            BtService mChatService = TempData.getIns().getBtService();
                            // 启动定时器，检查控制器是否有应答，如果3秒之内没有应答，提示用户
                            recvstatus = RecvStatus.NORECV;
                            postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                            if (loginaproch == 1) {
                                mChatService.startLogin(String.valueOf(et_userName.getText()), String.valueOf(et_password.getText()));
                                SaveSetting();
                            } else if (loginaproch == 2) {
                                mChatService.startLoginbyNFC(nfcCardId);
                            }
                            break;
                        case BtService.STATE_CONNECTING:
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            //Toast.makeText(getApplicationContext(), "Connect Error.", Toast.LENGTH_SHORT).show();
                            //btnConnect.setText("Connect");
                            break;
                        case BtService.STATE_CONNECT_ERR:
                            Toast.makeText(getApplicationContext(), getString(R.string.connect_error), Toast.LENGTH_SHORT).show();
                            iv_login.setClickable(true);
                            iv_login.setBackgroundResource(R.drawable.login_button_normal);
                            break;
                        case BtService.STATE_CONNECT_LOST:
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
                            iv_login.setClickable(true);
                            iv_login.setBackgroundResource(R.drawable.login_button_normal);
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
                    //TempData.getIns().addZipCompressedChkList(body.getString("checklist"));
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
                            Toast.makeText(LoginActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            recvstatus = RecvStatus.RECVNG;
                            mHandler.removeCallbacks(mTimeRcvChk);
                            iv_login.setClickable(true);
//                            iv_login.setImageResource(R.drawable.btn_login);
                            iv_login.setBackgroundResource(R.drawable.login_button_normal);
                            return;
                        }
                        try {
//                            String info = new String(buf,"utf-8");
                            //Toast.makeText(getApplicationContext(),info,Toast.LENGTH_LONG).show();
                            mHandler.removeCallbacks(mTimeRcvChk);
                            JSONTokener jsonParser = new JSONTokener(info);
                            JSONObject response = (JSONObject) jsonParser.nextValue();
                            JSONObject body = response.optJSONObject("response");
                            String cmd = body.getString("cmd");
                            //if (cmd.equalsIgnoreCase("login") && body.getInt("code")==200){
                            if (cmd.equalsIgnoreCase("login")) {
                                if (body.getInt("code") == 200) {
                                    String token = body.getString("token");
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("token", token).commit();
                                    editor.putString("customer", String.valueOf(et_customer.getText())).commit();
                                    editor.putString("account", String.valueOf(et_userName.getText())).commit();
                                    editor.putString("password", String.valueOf(et_password.getText())).commit();

                                    // 检测运动
                                    String did = body.getString("did");
                                    editor.putString("did", did).commit();
                                    String hname = body.getString("cname");
                                    editor.putString("hname", hname).commit();
                                    String appn = body.getString("appn");
                                    editor.putString("appn", appn).commit();
                                    String cname = body.getString("cname");
                                    editor.putString("cname", cname).commit();

                                    RecordActivity.startRecordActivity(mContext);
                                    finish();
                                } else if (body.getInt("code") == 201 || body.getInt("code") == 202) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                                } else if (body.getInt("code") == 203) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.key_is_close, Toast.LENGTH_LONG).show();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("customer", String.valueOf(et_customer.getText())).commit();
                                    editor.putString("account", String.valueOf(et_userName.getText())).commit();
                                    editor.putString("password", String.valueOf(et_password.getText())).commit();
                                } else if (body.getInt("code") == 204) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.Driver_license_expired, Toast.LENGTH_LONG).show();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("customer", String.valueOf(et_customer.getText())).commit();
                                    editor.putString("account", String.valueOf(et_userName.getText())).commit();
                                    editor.putString("password", String.valueOf(et_password.getText())).commit();
                                } else if (body.getInt("code") == 205) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.used_by_other_users, Toast.LENGTH_LONG).show();
                                } else if (body.getInt("code") == 214) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.forklift_failure, Toast.LENGTH_LONG).show();
                                } else if (body.getInt("code") == 219) {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.card_number_err, Toast.LENGTH_LONG).show();
                                } else {
                                    iv_login.setClickable(true);
                                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                                    Toast.makeText(LoginActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                                }
                                recvstatus = RecvStatus.RECVOK;
                                return;
                            }
                            if (cmd.equalsIgnoreCase("unlock")) {
                                if (body.getInt("code") == 200) {
                                    // 解锁成功
                                    //Toast.makeText(LoginActivity.this,"解锁成功",Toast.LENGTH_LONG).show();
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    mChatService.querytate(token);
                                } else if (body.getInt("code") == 216) {
                                    // 柴油车解锁失败
                                    Toast.makeText(LoginActivity.this, R.string.unlock_failed_brush_battery_card, Toast.LENGTH_LONG).show();
                                } else {
                                    // 其他车解锁失败
                                    Toast.makeText(LoginActivity.this, R.string.unlock_failed, Toast.LENGTH_LONG).show();
                                }
                                return;
                            }
                            if (cmd.equalsIgnoreCase("logout")) {
                                recvstatus = RecvStatus.RECVOK;
                                iv_login.setClickable(true);
                                iv_login.setBackgroundResource(R.drawable.login_button_normal);
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                                recvstatus = RecvStatus.RECVNG;
                                mHandler.removeCallbacks(mTimeRcvChk);
                                iv_login.setClickable(true);
                                iv_login.setBackgroundResource(R.drawable.login_button_normal);
                            }
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            recvstatus = RecvStatus.RECVNG;
                            mHandler.removeCallbacks(mTimeRcvChk);
                            iv_login.setClickable(true);
                            iv_login.setBackgroundResource(R.drawable.login_button_normal);
                        }
                    }
                }
                break;
            }
        }
    };
    private final Runnable mTimeRcvChk = new Runnable() {

        @Override
        public void run() {
            switch (recvstatus) {
                case NORECV:
                    count--;
                    if (count > 0) {
                        mHandler.postDelayed(this, WAIT_RECV_DATA_MAX_TIME);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.device_no_answer, Toast.LENGTH_SHORT).show();
                        iv_login.setClickable(true);
//                        iv_login.setImageResource(R.drawable.btn_login);
                        iv_login.setBackgroundResource(R.drawable.login_button_normal);
                    }
                    break;
                case RECVNG:
                    Toast.makeText(getApplicationContext(), R.string.device_response_incorrect, Toast.LENGTH_SHORT).show();
                    iv_login.setClickable(true);
//                    iv_login.setImageResource(R.drawable.btn_login);
                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                    break;
                case RECVOK:
                    iv_login.setClickable(true);
//                    iv_login.setImageResource(R.drawable.btn_login);
                    iv_login.setBackgroundResource(R.drawable.login_button_normal);
                    break;
            }
        }
    };

    public static void startLoginActivity(Context mContext) {
        TempData.getIns().getBtService().setHandle(null);
        comm_disconnectbluetooth();

        Intent intent = new Intent();
        intent.setClass(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        verifyStoragePermissions();
//        SysApplication.getInstance().addActivity(this);
        initViews();

        tv_version.setText(CommonFun.getLocalVersionName(LoginActivity.this));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        LoadSetting();
        et_device_info.setText(choosedName + "     " + TempData.getIns().getChoosedAddress());

        TempData.getIns().getBtService().setHandle(mHandler);
        ((MyApplication) getApplication()).setBt_name(choosedName);
        mDialog = new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null).create();
        /*
         * NFC刷卡登录
         * */
        resolveIntent(getIntent());
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                getString(R.string.nfc_message), Locale.ENGLISH, true)});
    }


    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(mAdapter != null && mAdapter.isEnabled()){
//            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//        }
//        else {
//            Toast.makeText(getApplicationContext(), "该设备NFC未打开，如果选择刷卡登录请在设置中开启", Toast.LENGTH_LONG).show();
//            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
//        }
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);升级到targetSdk 34报错
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
//            mAdapter.disableForegroundNdefPush(this);升级到targetSdk 34报错
        }
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(R.string.open_nfc, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.nfc_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
//                finish();
            }
        });
        builder.create().show();
        return;
    }

    /**
     * 获取和分析卡的信息
     */
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                long timestamp = System.currentTimeMillis() / 1000;
                if (NFCScanFirstTime) {
                    NFCScanFirstTimestamp = timestamp;
                    NFCScanFirstTime = false;

                    byte[] empty = new byte[0];
                    byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                    Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    //byte[] payload = dumpTagData(tag).getBytes();

                    String info = et_device_info.getText().toString();
                    if (info.isEmpty() || info.length() < 17) {
                        Toast.makeText(LoginActivity.this, R.string.choose_forklift, Toast.LENGTH_LONG).show();
                        return;
                    }
                    String address = info.substring(info.length() - 17);
                    if (address.length() > 10) {
                        //连接中，禁止再次点击登录按钮
                        iv_login.setClickable(false);
                        iv_login.setBackgroundColor(0xffcccccc);
                        BtService mChatService = TempData.getIns().getBtService();
                        if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                            mChatService.startLoginbyNFC(dumpTagData(tag));
                            //SaveSetting();
                            recvstatus = RecvStatus.NORECV;
                            count = 3;
                            mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                            return;
                        } else if (mChatService.getState() == mChatService.STATE_CONNECTING) {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                            }
                            if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                                mChatService.startLoginbyNFC(dumpTagData(tag));
                                //SaveSetting();
                                recvstatus = RecvStatus.NORECV;
                                count = 3;
                                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                                return;
                            }
                        }

                        loginaproch = 2;
                        nfcCardId = dumpTagData(tag);
                        if (mBluetoothAdapter != null) {
                            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                            mChatService.OPT_TYPE = mChatService.OPT_24;
                            mChatService.connect(device, false);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.choose_forklift, Toast.LENGTH_LONG).show();
                    }
                } else {
                    if ((timestamp - NFCScanFirstTimestamp) < 5) {
                        Toast.makeText(getApplicationContext(), R.string.card_too_frequent, Toast.LENGTH_LONG).show();
                    } else {
                        NFCScanFirstTimestamp = timestamp;
                        NFCScanFirstTime = false;

                        byte[] empty = new byte[0];
                        byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                        Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                        //byte[] payload = dumpTagData(tag).getBytes();

                        String info = et_device_info.getText().toString();
                        if (info.isEmpty() || info.length() < 17) {
                            Toast.makeText(LoginActivity.this, R.string.choose_forklift, Toast.LENGTH_LONG).show();
                            return;
                        }
                        String address = info.substring(info.length() - 17);
                        if (address.length() > 10) {
                            //连接中，禁止再次点击登录按钮
                            iv_login.setClickable(false);
                            iv_login.setBackgroundColor(0xffcccccc);
                            BtService mChatService = TempData.getIns().getBtService();
                            if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                                mChatService.startLoginbyNFC(dumpTagData(tag));
                                //SaveSetting();
                                recvstatus = RecvStatus.NORECV;
                                count = 3;
                                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                                return;
                            } else if (mChatService.getState() == mChatService.STATE_CONNECTING) {
                                try {
                                    Thread.sleep(2000);
                                } catch (Exception e) {
                                }
                                if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                                    mChatService.startLoginbyNFC(dumpTagData(tag));
                                    //SaveSetting();
                                    recvstatus = RecvStatus.NORECV;
                                    count = 3;
                                    mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                                    return;
                                }
                            }

                            loginaproch = 2;
                            nfcCardId = dumpTagData(tag);
                            if (mBluetoothAdapter != null) {
                                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                                mChatService.OPT_TYPE = mChatService.OPT_24;
                                mChatService.connect(device, false);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.choose_forklift, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }


    /**
     * 检测到有NFC卡或设备
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);

    }


    private String dumpTagData(Parcelable p) {
        //StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        //sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        //sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        //sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

        String idStr = String.valueOf(getDec(id));
        while (idStr.length() < 10) {
            idStr = "0" + idStr;
        }

        /*
         * NFC刷卡获取到的信息值
         * */
        //new  AlertDialog.Builder(this)
        //        .setTitle("Information" )
        //        .setMessage(idStr)
        //        .show();
        //return sb.toString();
        return idStr;
    }


    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mTimeRcvChk);
        super.onDestroy();
    }

    @Override
    public void initViews() {
        super.initViews();
        mProgressDialog = new ProgressDialog(this);
        iv_login = (Button) findViewById(R.id.btn_login);
        iv_login.setOnClickListener(this);
        et_customer = (EditText) findViewById(R.id.et_customer_info);
        et_device_info = (EditText) findViewById(R.id.et_device_info);
        et_userName = (EditText) findViewById(R.id.et_userName);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_version = (TextView) findViewById(R.id.version);
        TextView tv_customer = (TextView) findViewById(R.id.tv_customer);
        Drawable drawableCustomer = getResources().getDrawable(R.drawable.login_customer);
        drawableCustomer.setBounds(0, 0, 55, 55);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        tv_customer.setCompoundDrawables(drawableCustomer, null, null, null);//只放左边
        tv_customer.setCompoundDrawablePadding(15);

        TextView tv_device = (TextView) findViewById(R.id.tv_device);
        Drawable drawableDevice = getResources().getDrawable(R.drawable.login_bluetooth);
        drawableDevice.setBounds(0, 0, 55, 55);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        tv_device.setCompoundDrawables(drawableDevice, null, null, null);//只放左边
        tv_device.setCompoundDrawablePadding(15);

        TextView tv_account = (TextView) findViewById(R.id.tv_account);
        Drawable drawableAccount = getResources().getDrawable(R.drawable.login_account);
        drawableAccount.setBounds(0, 0, 55, 55);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        tv_account.setCompoundDrawables(drawableAccount, null, null, null);//只放左边
        tv_account.setCompoundDrawablePadding(15);

        TextView tv_password = (TextView) findViewById(R.id.tv_password);
        Drawable drawablePassword = getResources().getDrawable(R.drawable.login_password);
        drawablePassword.setBounds(0, 0, 55, 55);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
        tv_password.setCompoundDrawables(drawablePassword, null, null, null);//只放左边
        tv_password.setCompoundDrawablePadding(15);

        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        String customer = sp.getString("customer", "");
        String account = sp.getString("account", "");
        String password = sp.getString("password", "");
        //et_customer.setText(customer);
        et_customer.setText(R.string.ejustcn);
//        et_userName.setText(account);
//        et_password.setText(password);

        et_customer.setSelection(et_customer.getText().length());
        et_device_info.setOnClickListener(this);
    }

    @Override
    public boolean checkForm() {
        if (StringUtils.isEmpty(et_userName.getText()) || StringUtils.isEmpty(et_password.getText())) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int viewId = v.getId();
        if (viewId == R.id.et_device_info) {
            Intent serverIntent = new Intent(LoginActivity.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
        } else if (viewId == R.id.btn_login) {
            //获取当前时间
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
            Date curDate = new Date(System.currentTimeMillis());
            String login_time = formatter.format(curDate);
            ((MyApplication) getApplication()).setLogin_time(login_time);
            long time_accumulator = curDate.getTime();
            ((MyApplication) getApplication()).setTime_accumulator(time_accumulator);

            if (checkForm()) {
                String info = et_device_info.getText().toString();
                if (info.isEmpty() || info.length() < 17) {
                    Toast.makeText(LoginActivity.this, R.string.choose_forklift, Toast.LENGTH_LONG).show();
                    return;
                }
                String address = info.substring(info.length() - 17);
                if (address.length() > 10) {
                    //连接中，禁止再次点击登录按钮
                    iv_login.setClickable(false);

                    // 判断蓝牙是否支持和开启
                    // 如果没有开启，就不连接和登陆
                    CommonFun.enable_bluethooth();
                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                        Toast.makeText(LoginActivity.this, R.string.please_turn_on_bt, Toast.LENGTH_LONG).show();
                        iv_login.setClickable(true);
                        return;
                    }
                    //iv_login.setBackgroundResource(R.drawable.btn_login_gray);
//                        iv_login.setImageResource(R.drawable.btn_login_gray_normal);
                    iv_login.setBackgroundColor(0xffcccccc);
                    BtService mChatService = TempData.getIns().getBtService();
                    if (!choosenAddress.isEmpty() && !address.equals(choosenAddress)) {
                        mChatService.stop();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                    }
                    if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                        mChatService.startLogin(String.valueOf(et_userName.getText()), String.valueOf(et_password.getText()));
                        SaveSetting();
                        recvstatus = RecvStatus.NORECV;
                        count = 3;
                        mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                        return;
                    } else if (mChatService.getState() == mChatService.STATE_CONNECTING) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                        if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                            mChatService.startLogin(String.valueOf(et_userName.getText()), String.valueOf(et_password.getText()));
                            SaveSetting();
                            recvstatus = RecvStatus.NORECV;
                            count = 3;
                            mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                            return;
                        }
                    }

                    loginaproch = 1;
                    if (mBluetoothAdapter != null) {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        mChatService.OPT_TYPE = mChatService.OPT_24;
                        mChatService.connect(device, false);
                    }
                    choosenAddress = address;
                }
            } else {
                //Crouton.showText(this, "信息不全!", Style.INFO);
                Toast.makeText(LoginActivity.this, R.string.please_enter_username_and_password, Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLUETOOTH_REQUEST:
                if (resultCode == RESULT_OK) {
                    //Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.not_allow_bt_open, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    void LoadSetting() {
        ObjectInputStream in = null;
        try {
            FileInputStream is = openFileInput("setting.obj");
            in = new ObjectInputStream(is);
            choosedName = in.readUTF();
            TempData.getIns().setDeviceName(choosedName);
            TempData.getIns().setChoosedAddress(in.readUTF());
            //choosedAddress = in.readUTF();
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

    void SaveSetting() {
        ObjectOutputStream out = null;
        try {
            FileOutputStream os = openFileOutput("setting.obj", MODE_PRIVATE);
            out = new ObjectOutputStream(os);
            out.writeUTF(choosedName);
            out.writeUTF(TempData.getIns().getChoosedAddress());
            //out.writeUTF(choosedAddress);
        } catch (Exception e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        choosedName = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
        TempData.getIns().setDeviceName(choosedName);
        TempData.getIns().setChoosedAddress(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS));
        et_device_info.setText(choosedName + "     " + data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS));

    }

    class ConnectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (TempData.getIns().getChoosedAddress().length() > 10) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                if (mBluetoothAdapter != null) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                    BtService mChatService = TempData.getIns().getBtService();
                    mChatService.OPT_TYPE = mChatService.OPT_24;
                    mChatService.connect(device, false);
                }
            }
        }
    }

    /**
     * 权限申请
     * 2024/1修改
     */
    public Boolean verifyStoragePermissions() {
        String[] PERMISSIONS_STORAGE;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            // Android 12.0 以下
            PERMISSIONS_STORAGE = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
        else /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)*/
            // Android 12.0 以上
            PERMISSIONS_STORAGE = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        try {
            // 判断是否有权限
            List<String> permissionDeniedList = new ArrayList<>();
            for (String permission : PERMISSIONS_STORAGE) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    permissionDeniedList.add(permission);
                }
            }
            // 无权限、请求权限
            if (!permissionDeniedList.isEmpty()) {
                String[] deniedPermissions = permissionDeniedList.toArray(new String[0]);
                ActivityCompat.requestPermissions(this, deniedPermissions, 1);
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
