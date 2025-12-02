package zlian.netgap.ui;

import static zlian.netgap.util.CommonFun.comm_disconnectbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.List;

import zlian.netgap.R;
import zlian.netgap.ShareCallBack;
import zlian.netgap.bean.CheckItemRng;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.data.TempData;
import zlian.netgap.mtool.BtService;
import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;
import zlian.netgap.mtool.RecvStatus;
import zlian.netgap.mtool.UpdateManager;
import zlian.netgap.util.Base64Util;
import zlian.netgap.util.CheckData;
import zlian.netgap.util.CommonFun;
import zlian.netgap.util.GZipUtils;

/**
 * Created by zhou on 2016/12/12.
 */
public class RecordActivity extends BaseActivity {

    private static final int WAIT_RECV_DATA_MAX_TIME = 2000; // 2秒
    private static final int UPDATE_INTER_TIME = 2000; // 2秒
    public static RecordActivity instance = null;
    private int btnBackgroundColor = 0;
    private int ukStatus = -1;
    private boolean onlyLogout = true; // true: logout false: logout and exit app

    // 定时器（状态更新）
    private final Runnable mTimeRefresher = new Runnable() {

        @Override
        public void run() {

            // 每隔2s获取一次车辆信息
            SendQueryCommand();
            mHandler.postDelayed(this, UPDATE_INTER_TIME);
        }
    };
    //检查更新类
    private UpdateManager updateManager;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_exit) {
            } else if (itemId == R.id.action_settings) {
            }

            return true;
        }
    };
    private Button btn_check;
    private Button btn_close;
    private Button btn_unlock;

    private RecvStatus recvstatus = RecvStatus.NORECV;
    private int count = 0; // retry times
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
                        //                        comm_disconnectbluetooth();
                        //                        CommonFun.enable_bluethooth();

                        //                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        //                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                        //                        BtService mChatService = TempData.getIns().getBtService();
                        //                        mChatService.OPT_TYPE = mChatService.OPT_24;
                        //                        mChatService.connect(device, false);

                        btn_check.setBackgroundResource(R.drawable.btn_bg);
                        btn_check.setClickable(true);
                        if (btn_close.getVisibility() == View.VISIBLE) {
                            btn_close.setBackgroundResource(R.drawable.btn_bg);
                            btn_close.setClickable(true);
                        }
                        if (btn_unlock.getVisibility() == View.VISIBLE) {
                            btn_unlock.setBackgroundResource(R.drawable.btn_bg);
                            btn_unlock.setClickable(true);
                        }
                    }
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
                        }
                        break;
                        case BtService.STATE_CONNECTING:
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            break;
                        case BtService.STATE_CONNECT_ERR: {
                            Toast.makeText(getApplicationContext(), getString(R.string.connect_error), Toast.LENGTH_SHORT).show();
                        }
                        break;
                        case BtService.STATE_CONNECT_LOST: {
                            recvstatus = RecvStatus.RECVNG;
                            mHandler.removeCallbacks(mTimeRcvChk);
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
                            btn_check.setBackgroundResource(R.drawable.btn_bg);
                            if (btn_close.getVisibility() == View.VISIBLE) {
                                btn_close.setBackgroundResource(R.drawable.btn_bg);
                                btn_close.setClickable(true);
                            }
                            if (btn_unlock.getVisibility() == View.VISIBLE) {
                                btn_unlock.setBackgroundResource(R.drawable.btn_bg);
                                btn_unlock.setClickable(true);
                            }
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
                            recvstatus = RecvStatus.RECVNG;
                            mHandler.removeCallbacks(mTimeRcvChk);
                            return;
                        }
                        try {
                            JSONTokener jsonParser = new JSONTokener(info);
                            JSONObject response = (JSONObject) jsonParser.nextValue();
                            JSONObject body = response.optJSONObject("response");
                            String cmd = body.getString("cmd");
                            // 接受点检数据列表
                            if (cmd.equalsIgnoreCase("checklist")) {
                                if (body.getInt("code") == 200 ||
                                        body.getInt("code") == 211) {
                                    Gson gson = new Gson();
                                    // get timestamp
                                    long timestamp = body.getLong("timestamp");
                                    if (TempData.getIns().getTimestamp() == 0) {
                                        TempData.getIns().setTimestamp(timestamp);
                                    } else if (TempData.getIns().getTimestamp() != timestamp) {
                                        return;
                                    }
                                    String itemStr = body.getString("item");
                                    CheckItemRng item = gson.fromJson(itemStr, CheckItemRng.class);
                                    //  stop 和 total 不一致的话，继续接收
                                    if (item.getStop() == item.getTotal()) {
                                        TempData.getIns().addZipCompressedChkList(body.getString("checklist"));
                                        String checkListStr = TempData.getIns().getZipCompressedChkList();
                                        //先base64解码
                                        byte[] bytes = Base64Util.decode(checkListStr.getBytes("utf-8"));
                                        //然后GZip解压缩
                                        byte[] gzipBytes = GZipUtils.decompress(bytes);
                                        checkListStr = new String(gzipBytes, "utf-8");
                                        List<CheckPoint> checkPoints = gson.fromJson(checkListStr, new TypeToken<List<CheckPoint>>() {
                                        }.getType());
                                        if (!checkPoints.isEmpty()) {
                                            mHandler.removeCallbacks(mTimeRefresher);
                                            mHandler.removeCallbacks(mTimeRcvChk);

                                            try {
                                                Thread.sleep(100);
                                            } catch (Exception e) {
                                            }
                                            TempData.getIns().setCheckPointList(checkPoints);
                                            CheckActivity.startCheckActivity(mContext, 0);
                                            finish();
                                        } else {
                                            Toast.makeText(RecordActivity.this, R.string.no_check_items, Toast.LENGTH_LONG).show();
                                            btn_check.setBackgroundResource(R.drawable.btn_bg);
                                            btn_close.setClickable(true);
                                        }
                                    } else {
                                        TempData.getIns().addZipCompressedChkList(body.getString("checklist"));
                                    }
                                    recvstatus = RecvStatus.RECVOK;
                                } else if (body.getInt("code") == 208) {
                                    Toast.makeText(RecordActivity.this, R.string.controller_updating, Toast.LENGTH_LONG).show();
                                    recvstatus = RecvStatus.RECVOK;

                                    //登出
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    mChatService.logout(token);
                                    try {
                                        Thread.sleep(100);
                                    } catch (Exception e) {
                                    }
                                } else if (body.getInt("code") == 209) {
                                    Toast.makeText(RecordActivity.this, R.string.check_function_not_enabled, Toast.LENGTH_LONG).show();
                                    recvstatus = RecvStatus.RECVOK;

                                    //登出
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    mChatService.logout(token);
                                    try {
                                        Thread.sleep(100);
                                    } catch (Exception e) {
                                    }
                                    //                                    new android.app.AlertDialog.Builder(RecordActivity.this)
                                    //                                            .setTitle("提示")
                                    //                                            .setMessage("点检功能未启用，程序退出")
                                    //                                            .setPositiveButton("确定",
                                    //                                                    new DialogInterface.OnClickListener() {
                                    //                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                    //                                                            // 断开连接
                                    //                                                            comm_disconnectbluetooth();
                                    //                                                            ActivityCollector.finishAll();
                                    //                                                            System.exit(0);
                                    //                                                        }
                                    //                                                    })
                                    //                                            .setCancelable(false)
                                    //                                            .show();
                                } else if (body.getInt("code") == 210) {
                                    Toast.makeText(RecordActivity.this, R.string.no_need_check, Toast.LENGTH_LONG).show();
                                    recvstatus = RecvStatus.RECVOK;
                                    //登出
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    mChatService.logout(token);
                                    try {
                                        Thread.sleep(100);
                                    } catch (Exception e) {
                                    }
                                } else if (body.getInt("code") == 215) {
                                    // 点检项过多
                                    Toast.makeText(RecordActivity.this, R.string.too_many_check_items, Toast.LENGTH_LONG).show();
                                    recvstatus = RecvStatus.RECVOK;

                                    //登出
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    mChatService.logout(token);
                                    try {
                                        Thread.sleep(100);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    BtService mChatService = TempData.getIns().getBtService();
                                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                    String token = sp.getString("token", "");
                                    Toast.makeText(RecordActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                                    recvstatus = RecvStatus.RECVNG;
                                    mHandler.removeCallbacks(mTimeRcvChk);
                                    btn_check.setBackgroundResource(R.drawable.btn_bg);
                                }
                            } else if (cmd.equalsIgnoreCase("query_state")) {

                                // Token无效 或 已登出，那退出dashboard返回登陆界面
                                if (body.getInt("code") == 213 ||
                                        body.getInt("code") == 207) {
                                    Toast.makeText(RecordActivity.this, R.string.token_invalid, Toast.LENGTH_LONG).show();
                                    TempData.getIns().getBtService().setHandle(null);
                                    // 断开蓝牙连接
                                    comm_disconnectbluetooth();
                                    LoginActivity.startLoginActivity(mContext);
                                    finish();
                                }
                                // 车辆名称
                                TextView tvCarName = (TextView) findViewById(R.id.tv_carname);
                                tvCarName.setText(TempData.getIns().getDeviceName());

                                int isAdmin = body.getInt("isAdmin");
                                // 显示驾驶员
                                String user = body.getString("user");
                                TextView tvUser = (TextView) findViewById(R.id.tv_user);
                                if (isAdmin == 1) {
                                    tvUser.setText(user + getString(R.string.admin));
                                } else {
                                    tvUser.setText(user + getString(R.string.ordinary));
                                }

                                // 显示点检状态
                                int ckStatus = body.getInt("checked");
                                TextView tvChkStatus = (TextView) findViewById(R.id.tv_checkstatus);
                                if (ckStatus == 1) {
                                    tvChkStatus.setText(R.string.checked);
                                } else {
                                    tvChkStatus.setText(R.string.not_checked);
                                }

                                // 故障状态
                                int bkStatus = body.getInt("broken");
                                TextView tvError = (TextView) findViewById(R.id.tv_error);
                                if (bkStatus == 1) {
                                    tvError.setText(R.string.failure);
                                    tvError.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                } else {
                                    tvError.setText(R.string.normal);
                                    tvError.setTextColor(Color.parseColor("#ff80cbc4"));
                                }

                                // 锁定状态
                                ukStatus = body.getInt("unlock");
                                TextView tvUnlock = (TextView) findViewById(R.id.tv_unlock);
                                if (ukStatus == 0) {
                                    tvUnlock.setText(R.string.locked);
                                } else {
                                    tvUnlock.setText(R.string.unlocked);
                                }

                                // 是否强制点检
                                int dcStatus = body.getInt("doCheck");
                                TextView tvNeedCheck = (TextView) findViewById(R.id.tv_needcheck);
                                if (dcStatus == 0) {
                                    tvNeedCheck.setText(R.string.not_mandatory_check);
                                } else if (dcStatus == 1) {
                                    tvNeedCheck.setText(R.string.forced_check);
                                } else if (dcStatus == 2) {
                                    tvNeedCheck.setText(R.string.change_driver_check);
                                }

                                // 控制"用车"按钮，"关闭车辆"按钮显示
                                if (isAdmin == 1) {
                                    // 管理员
                                    // 显示"关闭车辆"按钮，隐藏"用车"按钮
                                    btn_close.setVisibility(View.VISIBLE);
                                    btn_close.setClickable(true);
                                    btn_unlock.setVisibility(View.INVISIBLE);
                                    btn_unlock.setClickable(false);
                                } else {
                                    // 有故障，不可以用车
                                    if (bkStatus == 1) {
                                        // 显示"关闭车辆"按钮，隐藏"用车"按钮
                                        btn_close.setVisibility(View.VISIBLE);
                                        btn_close.setClickable(true);
                                        btn_unlock.setVisibility(View.INVISIBLE);
                                        btn_unlock.setClickable(false);
                                    } else {
                                        if (ukStatus == 1) {
                                            // 车辆已解锁，可以直接用车，不需要点击"用车"按钮
                                            // 显示"关闭车辆"按钮，隐藏"用车"按钮
                                            btn_close.setVisibility(View.VISIBLE);
                                            btn_close.setClickable(true);
                                            btn_unlock.setVisibility(View.INVISIBLE);
                                            btn_unlock.setClickable(false);
                                        } else {
                                            //  车辆锁车情况下
                                            if (dcStatus == 0) {
                                                // 非强制点检，可以用车
                                                btn_close.setVisibility(View.INVISIBLE);
                                                btn_close.setClickable(false);
                                                btn_unlock.setVisibility(View.VISIBLE);
                                                btn_unlock.setClickable(true);
                                            } else if (ckStatus == 1) {
                                                // 强制点检时，已点检通过，可以用车
                                                btn_close.setVisibility(View.INVISIBLE);
                                                btn_close.setClickable(false);
                                                btn_unlock.setVisibility(View.VISIBLE);
                                                btn_unlock.setClickable(true);
                                            } else {
                                                // 显示"关闭车辆"按钮，隐藏"用车"按钮
                                                btn_close.setVisibility(View.VISIBLE);
                                                btn_close.setClickable(true);
                                                btn_unlock.setVisibility(View.INVISIBLE);
                                                btn_unlock.setClickable(false);
                                            }
                                        }
                                    }
                                }
                            } else if (cmd.equalsIgnoreCase("logout")) {
                                recvstatus = RecvStatus.RECVOK;
                                mHandler.removeCallbacks(mTimeRcvChk);
                                mHandler.removeCallbacks(mTimeRefresher);

                                TempData.getIns().getBtService().setHandle(null);
                                // clear token
                                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", "").commit();
                                // 断开连接
                                comm_disconnectbluetooth();

                                if (onlyLogout) {
                                    // return back to login画面
                                    LoginActivity.startLoginActivity(mContext);
                                    finish();
                                } else {
                                    // 退出APP
                                    CommonFun.exitAPP(getApplicationContext());
                                }
                            } else if (cmd.equalsIgnoreCase("unlock")) {
                                recvstatus = RecvStatus.RECVOK;
                                mHandler.removeCallbacks(mTimeRcvChk);

                                if (body.getInt("code") == 200) {
                                    // 解锁成功
                                    Toast.makeText(RecordActivity.this, R.string.unlocked_success, Toast.LENGTH_LONG).show();
                                } else if (body.getInt("code") == 216) {
                                    // 柴油车解锁失败
                                    Toast.makeText(RecordActivity.this, R.string.unlock_failed_brush_battery_card, Toast.LENGTH_LONG).show();
                                    btn_unlock.setBackgroundResource(R.drawable.btn_bg);
                                    btn_unlock.setClickable(true);
                                } else {
                                    // 其他车解锁失败
                                    Toast.makeText(RecordActivity.this, R.string.unlock_failed, Toast.LENGTH_LONG).show();
                                    btn_unlock.setBackgroundResource(R.drawable.btn_bg);
                                    btn_unlock.setClickable(true);
                                }
                            } else {
                                Toast.makeText(RecordActivity.this, R.string.received_data_is_error, Toast.LENGTH_LONG).show();
                                recvstatus = RecvStatus.RECVNG;
                                mHandler.removeCallbacks(mTimeRcvChk);
                                btn_check.setBackgroundResource(R.drawable.btn_bg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }
    };

    public static void startRecordActivity(Context mContext) {
        TempData.getIns().getBtService().setHandle(null);

        Intent intent = new Intent(mContext, RecordActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        TextView header_tv;

        // 拍照相关文件
//        File file = new File(FileUtil.filePath);
//        if (file.isFile()) {
//            file.delete();
//            return;
//        }
//
//        if (file.isDirectory()) {
//            File[] childFiles = file.listFiles();
//            if (childFiles == null || childFiles.length == 0) {
//                file.delete();
//                return;
//            }
//            for (int i = 0; i < childFiles.length; i++) {
//                CommonFun.delete(childFiles[i]);
//            }
//            file.delete();
//        }

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        //getVersionData();

        //设置ToolBar
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        String strVer = CommonFun.getLocalVersionName(RecordActivity.this);
        getSupportActionBar().setTitle(getString(R.string.vehicle_information) + "    " + strVer);

        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);

        //设置抽屉DrawerLayout
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();//初始化状态
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //设置导航栏NavigationView的点击事件
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.item_feedback) {
                    Intent intent = new Intent();
                    intent.setClass(RecordActivity.this, FeedbackActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.item_share) {
                    showShareDialog();
                } else if (itemId == R.id.item_update) {
                    //检查更新类
                    //updateManager = new UpdateManager(RecordActivity.this);
                    //updateManager.checkUpdate();
                } else if (itemId == R.id.item_three) {
                        Intent intent1 = new Intent();
                        intent1.setClass(RecordActivity.this, AboutActivity.class);
                        startActivity(intent1);
                }
                //点击了把它设为选中状态
//                menuItem.setChecked(true);
                //关闭抽屉
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        //退出登录
        header_tv = mNavigationView.getHeaderView(0).findViewById(R.id.header_tv);
        header_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //登出
                BtService mChatService = TempData.getIns().getBtService();
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");
                mChatService.logout(token);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        });

        //退出APP事件
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_exit) {
                    TextView title = new TextView(RecordActivity.this);
                    title.setText(R.string.exit_tip);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextSize(20);
                    title.setTextColor(Color.BLUE);
                    title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    if (ukStatus == 0) { // 已锁定
                        AlertDialog dialog = new AlertDialog.Builder(RecordActivity.this)
                                .setCustomTitle(title)
                                .setMessage(R.string.locked_exit_prompt)
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // 断开连接
                                                //comm_disconnectbluetooth();
                                                //CommonFun.exitAPP(getApplicationContext());

                                                BtService mChatService = TempData.getIns().getBtService();
                                                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                                String token = sp.getString("token", "");

                                                recvstatus = RecvStatus.NORECV;
                                                count = 3;
                                                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);

                                                mChatService.logout(token);
                                                onlyLogout = false;
                                                try {
                                                    Thread.sleep(100);
                                                } catch (Exception e) {
                                                }
                                            }
                                        })
                                .setNegativeButton(R.string.soft_update_cancel, null)
                                .setCancelable(false)
                                .show();

                        // align button to center
                        Button b = (Button) dialog.findViewById(android.R.id.button1);
                        b.setGravity(Gravity.CENTER_HORIZONTAL);
                    } else if (ukStatus == 1) { // 已解锁
                        AlertDialog dialog = new AlertDialog.Builder(RecordActivity.this)
                                .setCustomTitle(title)
                                .setMessage(R.string.unlocked_exit_prompt)
                                .setPositiveButton(R.string.logout_and_close_the_app,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                BtService mChatService = TempData.getIns().getBtService();
                                                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                                String token = sp.getString("token", "");

                                                recvstatus = RecvStatus.NORECV;
                                                count = 3;
                                                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);

                                                mChatService.logout(token);
                                                onlyLogout = false;
                                                try {
                                                    Thread.sleep(100);
                                                } catch (Exception e) {
                                                }
                                            }
                                        })
                                .setNeutralButton(R.string.close_the_app,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // 断开连接
                                                comm_disconnectbluetooth();
                                                CommonFun.exitAPP(getApplicationContext());
                                            }
                                        })
                                .setNegativeButton(R.string.soft_update_cancel, null)
                                .setCancelable(false)
                                .show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(12);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(12);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(12);
                    }
                }
                return true;
            }
        });

        btn_check = (Button) findViewById(R.id.btn_check);
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_unlock = (Button) findViewById(R.id.btn_unlock);

        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 发送命令获取点检列表
                    BtService mChatService = TempData.getIns().getBtService();
                    if (mChatService != null) {
                        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                        if (sp != null) {
                            Log.v("RecordActivity", "change color of check button");
                            btn_check.setBackgroundColor(0xFFC3C3C3);
                            btn_close.setClickable(false);
                            Log.v("RecordActivity", "get token");
                            String token = sp.getString("token", "");
                            recvstatus = RecvStatus.NORECV;
                            count = 3;
                            mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);
                            mChatService.startChecklist(token);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_unlock.setBackgroundColor(0xFFC3C3C3);
                btn_unlock.setClickable(false);

                //解锁车辆
                BtService mChatService = TempData.getIns().getBtService();
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");

                recvstatus = RecvStatus.NORECV;
                count = 3;
                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);

                mChatService.unlock(token);
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_close.setBackgroundColor(0xFFC3C3C3);
                btn_check.setClickable(false);

                //关闭车辆
                BtService mChatService = TempData.getIns().getBtService();
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");

                recvstatus = RecvStatus.NORECV;
                count = 3;
                mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);

                mChatService.logout(token);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        });

        TempData.getIns().getBtService().setHandle(mHandler);
        mHandler.postDelayed(mTimeRefresher, UPDATE_INTER_TIME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //设置右上角的填充菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 使用统一数据结构
     */
    public void showShareDialog() {
//        ShareEntity testBean = new ShareEntity(getString(R.string.title), getString(R.string.content));
//        testBean.setUrl("https://www.baidu.com"); //分享链接
//        testBean.setImgUrl("https://www.baidu.com/img/bd_logo1.png");
//        ShareUtil.showShareDialog(this, testBean, ShareConstant.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 分享回调处理
         */
//        if (requestCode == ShareConstant.REQUEST_CODE) {
//            if (data != null) {
//                int channel = data.getIntExtra(ShareConstant.EXTRA_SHARE_CHANNEL, -1);
//                int status = data.getIntExtra(ShareConstant.EXTRA_SHARE_STATUS, -1);
//                onShareCallback(channel, status);
//            }
//        }
    }

    /**
     * 分享回调处理
     *
     * @param channel
     * @param status
     */
    private void onShareCallback(int channel, int status) {
//        new ShareCallBack().onShareCallback(channel, status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendQueryCommand();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mTimeRcvChk);
        mHandler.removeCallbacks(mTimeRefresher);
    }

    private void SendQueryCommand() {
        try {
            // 是否连接断开，断开的话，重新连接
            BtService mChatService = TempData.getIns().getBtService();
            if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                // 连接上的话，发送查询命令
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");
                mChatService.querytate(token);
            } else if (mChatService.getState() == mChatService.STATE_CONNECTING) {
                try {
                    // 连接中的话，等两秒
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                if (mChatService.getState() == mChatService.STATE_CONNECTED) {
                    // 连接上的话，发送查询命令
                    SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                    String token = sp.getString("token", "");
                    mChatService.querytate(token);
                }
            }

            if (mChatService.getState() != mChatService.STATE_CONNECTED) {
                // 断开的话，重新连接
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                mChatService.OPT_TYPE = mChatService.OPT_24;
                mChatService.connect(device, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.back_keycode_prompt)
                    .setNegativeButton(R.string.exit_app, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 断开连接
                            comm_disconnectbluetooth();

                            CommonFun.exitAPP(getApplicationContext());
                        }
                    })
                    .setPositiveButton(R.string.close_vehicle, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //关闭车辆
                            BtService mChatService = TempData.getIns().getBtService();
                            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                            String token = sp.getString("token", "");

                            recvstatus = RecvStatus.NORECV;
                            count = 3;
                            mHandler.postDelayed(mTimeRcvChk, WAIT_RECV_DATA_MAX_TIME);

                            mChatService.logout(token);
                            onlyLogout = true;
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                        }
                    }).show();

            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    //    private int getVersionData(){
    //        final String url = "http://app.ejustcn.com/tm/api/v1/ejuser/appversion";
    //        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
    //            @Override
    //            public void onResponse(String json) {
    //
    //                Gson gson = new GsonBuilder().create();
    //                VersionInfo versionInfo = gson.fromJson(json, VersionInfo.class);
    //                serviceCode = versionInfo.getVersionCode();
    //                mUrl = versionInfo.getUrl();
    //
    //            }
    //        }, new Response.ErrorListener() {
    //            @Override
    //            public void onErrorResponse(VolleyError error) {
    //
    //                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    //            }
    //        });
    //        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    //        return serviceCode;
    //    }

}
