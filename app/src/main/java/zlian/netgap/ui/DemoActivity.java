package zlian.netgap.ui;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import zlian.netgap.R;

/**
 * 2024/1 Create
 */

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        ImageButton backBtn = (ImageButton) findViewById(R.id.backBtn);
        WebView imgWebView = (WebView) findViewById(R.id.imgWebView);

        // 获取图片URL
        Intent intent = getIntent();
        String imgUrl = intent.getStringExtra("imgUrl");

        WebSettings settings = imgWebView.getSettings();
        settings.setUseWideViewPort(true);  // 启用视口适配
        settings.setLoadWithOverviewMode(true); // 缩放内容至屏幕宽度
        settings.setSupportZoom(false);     // 禁用缩放（可选）
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // URL不为空则加载
        if (imgUrl != null) {
            imgWebView.loadUrl(imgUrl);
        }

        // 返回键
        backBtn.setOnClickListener(view -> finish());

    }

}
