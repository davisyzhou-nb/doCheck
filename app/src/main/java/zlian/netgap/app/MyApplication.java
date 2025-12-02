package zlian.netgap.app;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import zlian.netgap.util.CommonFun;

public class MyApplication extends Application {

    protected static MyApplication mInstance;
    int nTest = 0;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 要做的事情
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        mInstance = this;
        super.onCreate();

        ForegroundCallbacks callbacks = ForegroundCallbacks.get(this);
        callbacks.addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                handler.removeCallbacks(mMaxConnectKeep);
            }

            @Override
            public void onBecameBackground() {
                //handler.postDelayed(mMaxConnectKeep, 1000*60*15);
                handler.postDelayed(mMaxConnectKeep, 1000*60*5);
            }
        });
    }

    private final Runnable mMaxConnectKeep = new Runnable() {

        @Override
        public void run() {
            // 断开连接
            CommonFun.comm_disconnectbluetooth();
        }
    };

    public static MyApplication getInstance() {

        return mInstance;
    }

    private String bt_name;
    public String getBt_name() {
        return bt_name;
    }

    public void setBt_name(String bt_name) {
        this.bt_name = bt_name;
    }

    private String login_time;
    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }

    private long time_accumulator;
    public long getTime_accumulator() {
        return time_accumulator;
    }

    public void setTime_accumulator(long time_accumulator) {
        this.time_accumulator = time_accumulator;
    }

//    private boolean isCheck;
//    public boolean getIsCheck() {
//        return isCheck;
//    }

//    public void setIsCheck(boolean isCheck) {
//        this.isCheck = isCheck;
//    }

}
