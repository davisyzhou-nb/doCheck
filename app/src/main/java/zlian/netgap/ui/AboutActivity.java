package zlian.netgap.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import zlian.netgap.R;
import zlian.netgap.util.CommonFun;

/***********************************************************************************************************************
 * Create        : 2017/2/22
 * Author        : zhangyiming
 * Description   : 关于界面
 * Maintenance   :
 ***********************************************************************************************************************/
public class AboutActivity extends AppCompatActivity {

    private TextView version;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        version = (TextView) findViewById(R.id.version);
        iv_back = (ImageView) findViewById(R.id.iv_back);

        //version.setText("版本号：" + getVersionName());
        version.setText(getString(R.string.version) + CommonFun.getLocalVersionName(AboutActivity.this));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void showActivities(ActivityInfo[] activities) {
        for (ActivityInfo activity : activities) {
            Log.i("activity=========", activity.name);
        }
    }
}