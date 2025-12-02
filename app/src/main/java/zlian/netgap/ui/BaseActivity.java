package zlian.netgap.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import zlian.netgap.app.Constants;
import zlian.netgap.util.ActivityCollector;


public class BaseActivity extends AppCompatActivity implements View.OnClickListener,Constants {

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onClick(View v) {

    }

    public void initViews(){

    }

    public boolean checkForm(){
        return false;
    }
}
