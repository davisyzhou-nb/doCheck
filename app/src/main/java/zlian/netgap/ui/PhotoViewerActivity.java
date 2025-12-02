package zlian.netgap.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.File;

import zlian.netgap.R;
import zlian.netgap.photo.PhotoController;


/**
 * Created by shiwanhui on 2016/9/20.
 */
public class PhotoViewerActivity extends Activity implements
        GestureDetector.OnGestureListener {
// 手势事件监控接口

    Bitmap bp = null;
    float scaleWidth;
    float scaleHeight;
    private int verticalMinDistance = 20;
    private int minVelocity = 0;
    private int position = 0;
    private File mPhotoFile;
    private String picPath;
    PhotoController mPhotoController;

    GestureDetector gestureDetector = new GestureDetector(this); // 声明检测手势事件

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_viewer);
        //跨界面获取参数值(对象)
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        mPhotoController = (PhotoController) intent.getSerializableExtra("PhotoController");

        scale();

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return gestureDetector.onTouchEvent(event); // 注册手势事件
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
            //向左的手势
            position++;
            if (position >= mPhotoController.getselectedPhotoList().size()) {
                position = mPhotoController.getselectedPhotoList().size() - 1;

            }

            scale();
        } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
            //向右的手势
            position--;
            if (position < 0) {
                position = 0;

            }
            scale();
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    public void scale() {
        ImageView test_gallery = (ImageView) findViewById(R.id.test_gallery);

        picPath = mPhotoController.getselectedPhotoList().get(position);
        Display display = getWindowManager().getDefaultDisplay();

        //异常处理，防止内存溢出
        try {
            if (mPhotoFile != null) {
                //容器置空
                mPhotoFile = null;
                bp = BitmapFactory.decodeFile(picPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bp = BitmapFactory.decodeFile(picPath);
        int width = bp.getWidth();
        int height = bp.getHeight();
        int w = display.getWidth();
        int h = display.getHeight();
        scaleWidth = ((float) w) / width;
        scaleHeight = ((float) h) / height;
        test_gallery.setImageBitmap(bp);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
        test_gallery.setImageBitmap(newBitmap);
        return;
    }


}

