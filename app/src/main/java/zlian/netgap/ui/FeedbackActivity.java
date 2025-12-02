package zlian.netgap.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import zlian.netgap.R;
import zlian.netgap.util.HttpUtils;

/***********************************************************************************************************************
 * Create        : 2017/2/21
 * Author        : zhangyiming
 * Description   : 建议和反馈界面
 * Maintenance   :
 ***********************************************************************************************************************/
public class FeedbackActivity extends AppCompatActivity {

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Toast.makeText(FeedbackActivity.this, R.string.submit_success, Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(FeedbackActivity.this, R.string.submit_failed, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private ImageView iv_back;
    private EditText et_feedback;
    private Button submit;
    private String feedback;

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            // 在这里进行 http request.网络请求相关操作
            Map<String, String> params = new HashMap<String, String>();
            params.put("comments", feedback);
            if (HttpUtils.submitPostData("http://usdata.teknect.net/tm/api/v1/ejuser/feedback", params, "utf-8")) {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        et_feedback = (EditText) findViewById(R.id.et_feedback);
        submit = (Button) findViewById(R.id.submit);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedback = et_feedback.getText().toString();
                if (feedback.equals("")) {
                    Toast.makeText(FeedbackActivity.this, R.string.enter_the_content, Toast.LENGTH_LONG).show();
                } else {
                    new Thread(networkTask).start();
                }
            }
        });
    }

}