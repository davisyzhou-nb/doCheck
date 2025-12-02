package zlian.netgap.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import zlian.netgap.R;
import zlian.netgap.ShareCallBack;

public class MainActivity extends AppCompatActivity {

//    private UpdateManager updateManager;

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_exit) {
                // 发送状态查询命令
                int a = 0;
            } else if (itemId == R.id.action_settings) {
            }

            return true;
        }
    };

    public static void startMainActivity(Context mContext) {
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        updateManager = new UpdateManager(MainActivity.this);

        //设置ToolBar

        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.vehicle_information);

        // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有作用
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);

        //设置抽屉DrawerLayout
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
//        mDrawerToggle.setDrawerIndicatorEnabled(false);
//        mDrawerToggle.setHomeAsUpIndicator(R.mipmap.ic_action_search);
        mDrawerToggle.syncState();//初始化状态
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //设置导航栏NavigationView的点击事件
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.item_feedback) {
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_content,new FragmentOne()).commit();
//                        mToolbar.setTitle("建议和反馈");
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, FeedbackActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.item_share) {
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_content,new FragmentTwo()).commit();
//                        mToolbar.setTitle("分享给好友");
                    showShareDialog();
                } else if (itemId == R.id.item_update) {
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_content,new FragmentThree()).commit();
//                        mToolbar.setTitle("检查更新");
//                        updateManager.checkUpdate();//点击检查更新；可放到oncreate方法中，则为自动检测更新
                } else if (itemId == R.id.item_three) {
                        Intent intent1 = new Intent();
                        intent1.setClass(MainActivity.this, AboutActivity.class);
                        startActivity(intent1);
                }
//                menuItem.setChecked(true);//点击了把它设为选中状态
                mDrawerLayout.closeDrawers();//关闭抽屉
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //设置右上角的填充菜单 刷新
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

}
