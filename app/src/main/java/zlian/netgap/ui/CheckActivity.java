package zlian.netgap.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import zlian.netgap.R;
import zlian.netgap.app.MyApplication;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.data.TempData;
import zlian.netgap.mtool.BtService;
import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;
import zlian.netgap.util.CheckData;
import zlian.netgap.util.CommonFun;

public class CheckActivity extends BasePhotoActivity {

    private static final int REFRESH_DELAY = 1000; // 1秒
    private static final int RECONNECT_DELAY = 1000; // 1秒
    private static final int RESULT_CAPTURE_IMAGE = 1;// 照相的requestCode
    final int TAKE_PICTURE = 1;
    /**
     * index用于标示识第几个点检项目
     */
    private int index;

    // 最后一条点检后，发送锁车命令
    private boolean isLockingWhenFinishCheck = false;
    private int timeCount;
    private int maxTimeCount;
    private int reConnectCount;
    private EditText et_memoDesc;
    private TextView tv_category_value, tv_item_value, tv_desc_value,
            tv_time_count, tv_indexOfPage, tv_star, tv_version;

    private Button btn_yes, btn_no;
    private ImageButton btn_desc;
    private boolean isRecvResult = false;
    private boolean isTimeOver = false;
    private int retryConnectCount = 0;
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
                    // 判断蓝牙是否支持和开启
                    // 如果没有开启，就不连接和登陆
                    CommonFun.enable_bluethooth();
                    if (mBluetoothAdapter != null) {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                        mChatService.OPT_TYPE = mChatService.OPT_24;
                        mChatService.connect(device, false);
                    }
                } else {
                    // 已重试三次,并且未接收到回复
                    if (isRecvResult == false) {
                        if (isTimeOver == false) {
                            // 计时未结束
                            // 继续重试
                            // 判断蓝牙是否支持和开启
                            // 如果没有开启，就不连接和登陆
                            CommonFun.enable_bluethooth();
                            if (mBluetoothAdapter != null) {
                                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                                mChatService.OPT_TYPE = mChatService.OPT_24;
                                mChatService.connect(device, false);
                            }
                        } else {
                            // 计时已到
                            Toast.makeText(getApplicationContext(), R.string.connect_err, Toast.LENGTH_LONG).show();
                            btn_yes.setClickable(true);
                            btn_no.setClickable(true);
                            btn_yes.setBackgroundResource(R.drawable.btn_yes);
                            btn_no.setBackgroundResource(R.drawable.btn_no);
                        }
                    }
                }
            } else {
                reConnectCount--;
                if (reConnectCount > 0) {
                    if (isRecvResult == false) {
                        mHandler.postDelayed(this, RECONNECT_DELAY);
                    }
                } else {
                    // 已重试3次，共计6秒
                    if (isRecvResult == false &&
                            isTimeOver == true) {
                        Toast.makeText(getApplicationContext(), R.string.connect_err, Toast.LENGTH_LONG).show();
                        btn_yes.setClickable(true);
                        btn_no.setClickable(true);
                        btn_yes.setBackgroundResource(R.drawable.btn_yes);
                        btn_no.setBackgroundResource(R.drawable.btn_no);
                    }
                }
            }
        }
    };
    // 定时器（按钮“是"与”否“的状态变更）
    private final Runnable mTimeRefresher = new Runnable() {

        @Override
        public void run() {
            timeCount--;
            tv_time_count.setText(String.valueOf(timeCount));
            if (timeCount != 0) {
                mHandler.postDelayed(this, REFRESH_DELAY);
                if (reConnectCount <= 0) {
                    mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);
                }
            } else {
                tv_time_count.setVisibility(View.INVISIBLE);
                if (isRecvResult) {
                    btn_yes.setClickable(true);
                    btn_no.setClickable(true);
                    btn_yes.setBackgroundResource(R.drawable.btn_yes);
                    btn_no.setBackgroundResource(R.drawable.btn_no);
                }
                isTimeOver = true;
            }
        }
    };
    /**
     * 是否是修改
     */
    private boolean isModify;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BtService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BtService.STATE_CONNECTED:
                            // 如果画面启动的时候，重新连接的话，在这里发送点检数据。
                            BtService mChatService = TempData.getIns().getBtService();
                            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                            String token = sp.getString("token", "");
                            mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
                            reConnectCount = 3;
                            mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);
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
                            //String info = new String(buf,"utf-8");
                            //Toast.makeText(getApplicationContext(),info,Toast.LENGTH_LONG).show();
                            JSONTokener jsonParser = new JSONTokener(info);
                            JSONObject response = (JSONObject) jsonParser.nextValue();
                            JSONObject body = response.getJSONObject("response");
                            String cmd = body.getString("cmd");
                            if (cmd.equalsIgnoreCase("inspect")) {
                                if (body.getInt("code") == 200) {
                                    if (isLockingWhenFinishCheck) {
                                        TempData.getIns().getBtService().setHandle(null);
                                        SummaryActivity.startSummaryActivity(mContext);
                                        finish();
                                    } else {
                                        //check ok
                                        isRecvResult = true;
                                        if (isTimeOver) {
                                            btn_yes.setClickable(true);
                                            btn_no.setClickable(true);
                                            btn_yes.setBackgroundResource(R.drawable.btn_yes);
                                            btn_no.setBackgroundResource(R.drawable.btn_no);
                                        }
                                    }
                                } else if (body.getInt("code") == 207) {
                                    // Token无效
                                    new AlertDialog.Builder(CheckActivity.this)
                                            .setTitle(R.string.exit_tip)
                                            .setMessage(R.string.login_has_expired)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            LoginActivity.startLoginActivity(mContext);
                                                            finish();
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.unlock_lock_failed, Toast.LENGTH_LONG).show();
                                    btn_yes.setClickable(true);
                                    btn_no.setClickable(true);
                                    btn_yes.setBackgroundResource(R.drawable.btn_yes);
                                    btn_no.setBackgroundResource(R.drawable.btn_no);
                                }
                            }
                            // 检测运动
                            else if (cmd.equalsIgnoreCase("query_md")) {
                                if (body.getInt("code") == 200) {
                                    String result = body.getString("result");
                                    int mdRes = Integer.parseInt(result);
                                    TempData.getIns().getCheckPoint(index).setMdResult(mdRes);

                                    if (isModify) {
                                        //setResult(RESULT_OK);
                                        //finish();
                                        isLockingWhenFinishCheck = true;
                                        BtService mChatService = TempData.getIns().getBtService();
                                        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                                        String token = sp.getString("token", "");
                                        mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), "0", token);
                                    } else {
                                        startNextPage();
                                    }
                                } else if (body.getInt("code") == 207) {
                                    // Token无效
                                    new AlertDialog.Builder(CheckActivity.this)
                                            .setTitle(R.string.exit_tip)
                                            .setMessage(R.string.login_has_expired)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            LoginActivity.startLoginActivity(mContext);
                                                            finish();
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.motion_detection_failed, Toast.LENGTH_LONG).show();
                                    btn_yes.setClickable(true);
                                    btn_no.setClickable(true);
                                    btn_yes.setBackgroundResource(R.drawable.btn_yes);
                                    btn_no.setBackgroundResource(R.drawable.btn_no);
                                }
                            } else if (cmd.equalsIgnoreCase("query_state")) {

                            } else {
                                Toast.makeText(getApplicationContext(), R.string.unlock_lock_failed, Toast.LENGTH_LONG).show();
                                btn_yes.setClickable(true);
                                btn_no.setClickable(true);
                                btn_yes.setBackgroundResource(R.drawable.btn_yes);
                                btn_no.setBackgroundResource(R.drawable.btn_no);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            btn_yes.setClickable(true);
                            btn_no.setClickable(true);
                            btn_yes.setBackgroundResource(R.drawable.btn_yes);
                            btn_no.setBackgroundResource(R.drawable.btn_no);
                        }
                    }
                }
                break;
            }
        }
    };
    // 定时器（最大点检时间确认）
    private final Runnable mMaxTimeRefresher = new Runnable() {

        @Override
        public void run() {
            maxTimeCount--;
            if (maxTimeCount != 0) {
                mHandler.postDelayed(this, REFRESH_DELAY);
            } else {
                // 超过最大点检时间，认为与期望值不同
                if (isRecvResult == true) {
                    if (TempData.getIns().getCheckPoint(index).getExpected().equalsIgnoreCase("N")) {
                        TempData.getIns().getCheckPoint(index).setResult("Y");
                    } else {
                        TempData.getIns().getCheckPoint(index).setResult("N");
                    }
                    //TempData.getIns().getCheckPoint(index).setResult(TempData.getIns().getCheckPoint(index).getExpected().equalsIgnoreCase("N")==true?1:0);
                    TempData.getIns().getCheckPoint(index).setMemo(String.valueOf(et_memoDesc.getText()));
                    if (isModify) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        startNextPage();
                    }
                }
            }
        }
    };

    private String strImgPath = "";// 照片文件绝对路径
    private RelativeLayout rl_photo;
    //    private View photoView;
    private int i = 0;

    // 2023todo
    private ImageView img_item;
    private Uri photoUri;
    private String cameraImagePath;
    private int TAKE_PHOTO_CODE = 7;
    private String imageName;

    public static void startCheckActivity(Context mContext, int page) {
        if (SummaryActivity.instance != null) {
            SummaryActivity.instance.finish();
            SummaryActivity.instance = null;
        }

        TempData.getIns().getBtService().setHandle(null);

        Intent intent = new Intent(mContext, CheckActivity.class);
        intent.putExtra(KEY_INDEX, page);
        mContext.startActivity(intent);
    }

    public static void startCheckActivityForResult(Context mContext, int page) {
        Intent intent = new Intent(mContext, CheckActivity.class);
        intent.putExtra(KEY_INDEX, page);
        intent.putExtra(KEY_ID_MODIFY, true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) mContext).startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
//        boolean isCheck = true;
//        ((MyApplication) getApplication()).setIsCheck(isCheck);

        index = getIntent().getIntExtra(KEY_INDEX, 0);
        isModify = getIntent().getBooleanExtra(KEY_ID_MODIFY, false);
        initViews();
        checkPoint = TempData.getIns().getCheckPointList().get(index);
        timeCount = Integer.parseInt(checkPoint.getMin());
        maxTimeCount = Integer.parseInt(checkPoint.getMax());
        tv_category_value.setText(checkPoint.getTitle());
        tv_item_value.setText(checkPoint.getItem());
        tv_desc_value.setText(checkPoint.getContent());
        et_memoDesc.setText(checkPoint.getMemo());

        tv_version.setText(CommonFun.getLocalVersionName(CheckActivity.this));

        if ("0".equals(checkPoint.getMin())) {
            tv_time_count.setVisibility(View.INVISIBLE);
            isTimeOver = true;
            btn_yes.setClickable(true);
            btn_no.setClickable(true);
            btn_yes.setBackgroundResource(R.drawable.btn_yes);
            btn_no.setBackgroundResource(R.drawable.btn_no);
        } else {
            tv_time_count.setText(checkPoint.getMin());
            tv_time_count.setVisibility(View.VISIBLE);
            isTimeOver = false;
            mHandler.postDelayed(mTimeRefresher, REFRESH_DELAY);
            btn_yes.setClickable(false);
            btn_no.setClickable(false);
            btn_yes.setBackgroundResource(R.drawable.btn_yes_gray);
            btn_no.setBackgroundResource(R.drawable.btn_no_gray);
        }
        mHandler.postDelayed(mMaxTimeRefresher, REFRESH_DELAY);

        if ("1".equals(checkPoint.getKey())) {
            tv_star.setVisibility(View.VISIBLE);
        } else {
            tv_star.setVisibility(View.INVISIBLE);
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        TempData.getIns().getBtService().setHandle(mHandler);
        BtService mChatService = TempData.getIns().getBtService();
        mChatService.OPT_TYPE = mChatService.OPT_24;
        SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        String token = sp.getString("token", "");

        reConnectCount = 3;
        if (mChatService.getState() == BtService.STATE_CONNECTED) {
            mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
        } else {
            // 重新连接
            // 判断蓝牙是否支持和开启
            // 如果没有开启，就不连接和登陆
            CommonFun.enable_bluethooth();
            if (mBluetoothAdapter != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                mChatService.OPT_TYPE = mChatService.OPT_24;
                if (mChatService.getState() == BtService.STATE_CONNECTED) {
                    mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
                } else {
                    mChatService.connect(device, false);
                }
            }
        }

        // 拍照相关文件
//        if (isModify) {
//            do {
//                String filename = "";
//                filename = checkPoint.getId() + i + ".jpg";
//                String fullname = FileUtil.filePath + TempData.uuid + "/" + filename;
//                File photofile = new File(fullname);
//                if (photofile.exists()) {
//                    mPhotoController.addImage(photofile.getPath());
//                }
//                i++;
//            }
//            while ((mPhotoController.getCurrentSize() < COMMENT_MAX_IMAGE_NUM) && (i < TempData.getIns().getCheckPoint(index).getCommentPics()));
//        }

        mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCheckId(checkPoint.getId() + (++i));
        TempData.getIns().getCheckPoint(index).setCommentPics(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mTimeRefresher);
        mHandler.removeCallbacks(mMaxTimeRefresher);
        mHandler.removeCallbacks(mTimeReconnect);
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_category_value = (TextView) findViewById(R.id.tv_category_value);
        tv_item_value = (TextView) findViewById(R.id.tv_item_value);
        tv_desc_value = (TextView) findViewById(R.id.tv_desc_value);
//        top_right_count = (TextView) findViewById(R.id.top_right_count);
        tv_version = (TextView) findViewById(R.id.version);
        tv_time_count = (TextView) findViewById(R.id.tv_time_count);
        tv_indexOfPage = (TextView) findViewById(R.id.tv_indexOfPage);
        tv_star = (TextView) findViewById(R.id.tv_star);
        tv_indexOfPage.setText("(" + (index + 1) + "/" + TempData.getIns().size() + ")");
        et_memoDesc = (EditText) findViewById(R.id.tv_memo_desc);
        // 2023todo
        img_item = findViewById(R.id.img_item);
        if (TempData.getIns().getCheckPointList().get(index).getImgBitmap() != null)
            img_item.setImageBitmap(TempData.getIns().getCheckPointList().get(index).getImgBitmap());
        else
            img_item.setImageResource(R.drawable.selector_comment_camera);
//        rl_photo = (RelativeLayout) findViewById(R.id.rl_photo);
//        View photoView = createUploadPictureView();
//        rl_photo.addView(photoView);
        btn_yes = (Button) findViewById(R.id.btn_yes);
        btn_no = (Button) findViewById(R.id.btn_no);
        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        // 2024/1todo
        btn_desc = (ImageButton) findViewById(R.id.btn_desc);
        btn_desc.setOnClickListener(this);

        // 2023todo
        img_item.setOnClickListener(v -> takePhoto());
        img_item.setOnLongClickListener(v -> {
            if (TempData.getIns().getCheckPointList().get(index).getImgFile() != null)
                // 长按删除图片
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_the_photo)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            // 显示拍照按钮
                            img_item.setImageResource(R.drawable.selector_comment_camera);
                            // 删除图片
                            if (TempData.getIns().getCheckPointList().get(index).getImgFile().exists()
                                    && TempData.getIns().getCheckPointList().get(index).getImgFile().isFile())
                                TempData.getIns().getCheckPointList().get(index).getImgFile().delete();
                            TempData.getIns().getCheckPointList().get(index).setImgFile(null);
                        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            return true;
        });
    }

    /**
     * 拍照
     * 2023todo
     */
    private void takePhoto() {
        // 获取图片路径
        File photoFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String status = Environment.getExternalStorageState();
            if (Objects.equals(status, Environment.MEDIA_MOUNTED)) {
                photoUri = this.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues()
                );
            } else {
                photoUri = this.getContentResolver().insert(
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        new ContentValues()
                );
            }
        } else {
            try {
                photoFile = createImageFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                cameraImagePath = photoFile.getAbsolutePath();
                // 适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                photoUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", photoFile);
            }
        }

        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    /**
     * 创建保存图片的文件
     * 2023todo
     */
    private File createImageFile() {
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile)))
            return null;
        else
            return tempFile;
    }

    /**
     * 将Uri转换成File
     * 2023todo
     */
    private File uri2File(Uri uri) {
        String img_path;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualImageCursor = managedQuery(uri, proj, null,
                null, null);
        if (actualImageCursor == null) {
            img_path = uri.getPath();
        } else {
            int actual_image_column_index = actualImageCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualImageCursor.moveToFirst();
            img_path = actualImageCursor
                    .getString(actual_image_column_index);
        }
        return new File(img_path);
    }

    private void reConnect() {
        // 连接设备，发送点检结果
        BtService mChatService = TempData.getIns().getBtService();
        if (mChatService.getState() == mChatService.STATE_CONNECTED ||
                mChatService.getState() == mChatService.STATE_CONNECTING) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            if (isRecvResult == false) {
                // 重新发送点检结果
                reConnectCount = 3;
                CheckPoint checkPoint = TempData.getIns().getCheckPointList().get(index);
                timeCount = Integer.parseInt(checkPoint.getMin());
                tv_time_count.setText(checkPoint.getMin());
                tv_time_count.setVisibility(View.VISIBLE);
                isTimeOver = false;
                mHandler.postDelayed(mTimeRefresher, REFRESH_DELAY);
                btn_yes.setClickable(false);
                btn_no.setClickable(false);
                btn_yes.setBackgroundResource(R.drawable.btn_yes_gray);
                btn_no.setBackgroundResource(R.drawable.btn_no_gray);
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");
                // 检测运动
                mChatService.motionCheck(TempData.getIns().getCheckPoint(index).getId(), token);
                //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
                mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);
            }
        } else {
            // 连接失败
            // 重新连接
            // 判断蓝牙是否支持和开启
            // 如果没有开启，就不连接和登陆
            CommonFun.enable_bluethooth();
            if (mBluetoothAdapter != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TempData.getIns().getChoosedAddress());
                mChatService.OPT_TYPE = mChatService.OPT_24;
                mChatService.connect(device, false);
            }

            reConnectCount = 3;
            CheckPoint checkPoint = TempData.getIns().getCheckPointList().get(index);
            timeCount = Integer.parseInt(checkPoint.getMin());
            tv_time_count.setText(checkPoint.getMin());
            tv_time_count.setVisibility(View.VISIBLE);
            isTimeOver = false;
            mHandler.postDelayed(mTimeRefresher, REFRESH_DELAY);
            btn_yes.setClickable(false);
            btn_no.setClickable(false);
            btn_yes.setBackgroundResource(R.drawable.btn_yes_gray);
            btn_no.setBackgroundResource(R.drawable.btn_no_gray);
            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
            String token = sp.getString("token", "");
            // 检测运动
            mChatService.motionCheck(TempData.getIns().getCheckPoint(index).getId(), token);
            //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
            mHandler.postDelayed(mTimeReconnect, RECONNECT_DELAY);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int viewId = v.getId();
        if (viewId == R.id.btn_yes) {
            if (isRecvResult == true) {
                if (TempData.getIns() == null)
                    return;
                if (TempData.getIns().getCheckPoint(index) == null)
                    return;
                if (TempData.getIns().getBtService() == null)
                    return;
                btn_yes.setClickable(false);
                btn_no.setClickable(false);
                btn_yes.setBackgroundResource(R.drawable.btn_yes_gray);
                btn_no.setBackgroundResource(R.drawable.btn_no_gray);

                TempData.getIns().getCheckPoint(index).setResult("Y");
                TempData.getIns().getCheckPoint(index).setMemo(String.valueOf(et_memoDesc.getText()));
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
                    try {
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
                    } catch (Exception e) {
                    }
                }
                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");
                // 检测运动
                mChatService.motionCheck(TempData.getIns().getCheckPoint(index).getId(), token);
                //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), "0", token);
                //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
            } else {
                reConnect();
            }
        } else if (viewId == R.id.btn_no) {
            if (isRecvResult == true) {
                if (TempData.getIns() == null)
                    return;
                if (TempData.getIns().getCheckPoint(index) == null)
                    return;
                if (TempData.getIns().getBtService() == null)
                    return;
                btn_yes.setClickable(false);
                btn_no.setClickable(false);
                btn_yes.setBackgroundResource(R.drawable.btn_yes_gray);
                btn_no.setBackgroundResource(R.drawable.btn_no_gray);

                TempData.getIns().getCheckPoint(index).setResult("N");
                TempData.getIns().getCheckPoint(index).setMemo(String.valueOf(et_memoDesc.getText()));
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
                    try {
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
                    } catch (Exception e) {
                    }
                }

                SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
                String token = sp.getString("token", "");
                // 检测运动
                mChatService.motionCheck(TempData.getIns().getCheckPoint(index).getId(), token);
                //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), "0", token);
                //mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), TempData.getIns().getCheckPoint(index).getUnlock(), token);
            } else {
                reConnect();
            }
        } else if (viewId == R.id.btn_desc) {
            // 2024/1todo
            Intent intent = new Intent();
            intent.putExtra("imgUrl", checkPoint.getImg());
            intent.setClass(CheckActivity.this, DemoActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 判断是否是最后一个点检项目
     *
     * @return
     */
    private boolean isLastItem() {
        if (index == TempData.getIns().size() - 1) {
            return true;
        }
        return false;
    }

    private void startNextPage() {
        if (!isLastItem()) {
            Intent intent = new Intent(mContext, CheckActivity.class);
            intent.putExtra(KEY_INDEX, index + 1);
            mContext.startActivity(intent);
            finish();
        } else {
            //Toast.makeText(mContext,"点检完成",Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences("setting", Activity.MODE_PRIVATE);
            String token = sp.getString("token", "");
            BtService mChatService = TempData.getIns().getBtService();

            isLockingWhenFinishCheck = true;
            mChatService.startCheckItem(TempData.getIns().getCheckPoint(index).getId(), "0", token);
        }
    }

    private void cameraMethod2() {
        startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), TAKE_PICTURE);
    }

    private void cameraMethod() {
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        strImgPath = Environment.getExternalStorageDirectory().toString() + "/CONSDCGMPIC/";//存放照片的文件夹
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";//照片命名
        File out = new File(strImgPath);
        if (!out.exists()) {
            out.mkdirs();
        }
        out = new File(strImgPath, fileName);
        strImgPath = strImgPath + fileName;//该照片的绝对路径
        Uri uri = Uri.fromFile(out);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(imageCaptureIntent, RESULT_CAPTURE_IMAGE);

    }

    @Override
    public void setCheckId(String checkId) {
        this.checkId = checkId;
        // 2023todo
//        getPictureAdapter().setCheckId(checkId);
        imageName = checkId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_CODE) {
            // 获取图片
            Bitmap bitmap;
            File file;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                bitmap = BitmapFactory.decodeFile(cameraImagePath);
                file = new File(cameraImagePath);
            } else {
                try {
                    bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(photoUri));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                file = uri2File(photoUri);
            }
            TempData.getIns().getCheckPointList().get(index).setImgFile(file);
            TempData.getIns().getCheckPointList().get(index).setImgBitmap(bitmap);
            img_item.setImageBitmap(bitmap);
        }
//        switch (requestCode) {
//            case RESULT_CAPTURE_IMAGE://拍照
//                if (resultCode == RESULT_OK) {
//                    Bitmap bmp = BitmapFactory.decodeFile(strImgPath.toString());
//                    iv_take_photo.setImageBitmap(bmp);
//                }
//                break;
//
//        }
//        if (requestCode == TAKE_PICTURE) {
//            if (resultCode == RESULT_OK) {
//                Bitmap bm = (Bitmap) data.getExtras().get("data");
//                iv_take_photo.setImageBitmap(bm);//想图像显示在ImageView视图上，private ImageView img;
//                File myCaptureFile = new File(FileUtil.filePath+TempData.uuid+"/"+checkPoint.getId()+".jpg");
//                Log.e("12",FileUtil.filePath+TempData.uuid+"/"+checkPoint.getId()+".jpg");
//                try {
//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
//					/* 采用压缩转档方法 */
//                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//					/* 调用flush()方法，更新BufferStream */
//                    bos.flush();
//					/* 结束OutputStream */
//                    bos.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
