package zlian.netgap.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import zlian.netgap.R;
import zlian.netgap.adapter.CheckPointAdapter;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.data.TempData;
import zlian.netgap.util.CommonFun;

/**
 * 点检结果一览画面
 */
public class SummaryActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private Button mBtn;
    private CheckPointAdapter checkPointAdapter;
    public int chkposition;
    public static SummaryActivity instance = null;
    private TextView tv_version;

    public static void startSummaryActivity(Context mContext){
        Intent intent = new Intent(mContext,SummaryActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
//        SysApplication.getInstance().addActivity(this);
        initViews();
        instance = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initViews() {
        super.initViews();
        mListView = (ListView) findViewById(R.id.mListView);
        mBtn = (Button) findViewById(R.id.btn_confirm);
        mBtn.setOnClickListener(this);
        checkPointAdapter = new CheckPointAdapter(mContext,BasePhotoActivity.MAX_IMAGE_NUM, (ArrayList<CheckPoint>) TempData.getIns().getCheckPointList());
        mListView.setAdapter(checkPointAdapter);
        mListView.setOnItemClickListener(this);

        // get version
        tv_version = (TextView) findViewById(R.id.version);
        tv_version.setText(CommonFun.getLocalVersionName(SummaryActivity.this));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int itemId = v.getId();
        if (itemId == R.id.btn_confirm) {
            // 检测运动
            // 统计运动检测结果
            int index = 0;
            StringBuilder disp = new StringBuilder();
            boolean firstMotionChk = true;
            int motionChkPosition = 0;
            List<CheckPoint> checkPoints = TempData.getIns().getCheckPointList();
            for(CheckPoint checkPoint : checkPoints){
                index++;
                if (checkPoint.getMd().equals("1") && checkPoint.getMdResult() != 1) {
                    if (firstMotionChk) {
                        motionChkPosition = index - 1;
                        firstMotionChk = false;
                    } else {
                        disp.append(",");
                    }
                    disp.append(index);
                }
            }
            if (disp.length() > 0) {
                final int finalMotionChkPosition = motionChkPosition;
                new AlertDialog.Builder(SummaryActivity.this)
                        .setTitle(R.string.exit_tip)
                        .setMessage(String.format(getString(R.string.no_exercise_testing), disp.toString()))
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int whichButton)
                                    {
                                        chkposition = finalMotionChkPosition;
                                        CheckActivity.startCheckActivityForResult(mContext, finalMotionChkPosition);
                                    }
                                })
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton)
                                    {

                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
            else {
                CheckResultActivity.startCheckResultActivity(mContext, CheckPoint.isKeyPass() == 1);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.chkposition = position;
        CheckActivity.startCheckActivityForResult(mContext, position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1){
                //CheckPointAdapter checkPointAdapter = new CheckPointAdapter(mContext, (ArrayList<CheckPoint>) TempData.getIns().getCheckPointList());
                //mListView.setAdapter(checkPointAdapter);
                if (checkPointAdapter != null) {
                    checkPointAdapter.notifyDataSetChanged();
                    mListView.setSelection(chkposition);
                }
            }
        }
    }
}
