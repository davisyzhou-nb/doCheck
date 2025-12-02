package zlian.netgap.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.io.File;

import zlian.netgap.R;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.photo.ImageLoader;
import zlian.netgap.photo.PhotoController;
import zlian.netgap.photo.PictureAdapter;
import zlian.netgap.view.NoScrollGridView;

public abstract class BasePhotoActivity extends BaseActivity {

    public ImageLoader imageLoader;

    private int mGridItemWidth;

    protected CheckPoint checkPoint;

    /**
     * 拍照的最大张数
     */
    public final static int COMMENT_MAX_IMAGE_NUM = 5;

    /**
     * 单次最大上传数量限制
     */
    public static final int MAX_IMAGE_NUM = 5;

    /**
     * 这个界面拍照的requestCode
     */
    public static final int CAMERA_PHOTO = 103;

    // 照相机拍照存放的photo
    //private File mPhotoFile;

    private PictureAdapter mPictureAdapter;

    // photopick的控制数据
    public PhotoController mPhotoController;

    private LayoutInflater mInflater;

    public String checkId;

    /**
     * 点击basephotoupload中选中图片--->图片预览photoviewactivity.java
     */
    protected OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(BasePhotoActivity.this, PhotoViewerActivity.class);
            intent.putExtra("position", position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("PhotoController", getPhotoController());
            intent.putExtras(bundle);
            intent.putExtra("PhotoController", getPhotoController());
            startActivity(intent);
        }
    };

    /**
     * 长按basephotoupload中的选中图片--->删除photocontrol中的数据
     */
    protected OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BasePhotoActivity.this).setMessage(R.string.delete_the_photo);
            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String filename = removeImage(position);
                    File photofile = new File(filename);
                    if (photofile.exists()) {
                        photofile.delete();
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
            return true;

        }
    };


    /**
     * 根据position进行删除photocontrol的选中数据
     *
     * @param position
     */
    public String removeImage(final int position) {
        String photoFile = mPhotoController.removeImage(position);
        mPictureAdapter.notifyDataSetChanged();
        return photoFile;
    }


    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(this);
        imageLoader = ImageLoader.getInstance();
        initPhotoController();
    }


    public abstract void setCheckId(String checkId);

    /**
     * 调用照相机拍照
     * 将相机照相的图片的URL返回给调用者
     *
     * @param requestCode 请求code
     */
//    public void takePhoto(int requestCode) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        mPhotoFile = PhotoUpHelper.getCameraPhotoFile(checkId);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
//        startActivityForResult(takePictureIntent, requestCode);
//    }

    /**
     * 返回相机拍照图片的file
     *
     * @return
     */
//    public File getTakePhotoFile() {
//        return mPhotoFile;
//    }

    public View createUploadPictureView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mGridItemWidth = (int) ((dm.widthPixels - dm.density * (16 + 16 + 8 * 3)) / 4);
        View view = LayoutInflater.from(this).inflate(R.layout.comment_picture_grid, null);
        NoScrollGridView gridView = (NoScrollGridView) view.findViewById(R.id.gv);
        mPictureAdapter = new PictureAdapter(mInflater,getPhotoController(),imageLoader,mGridItemWidth,this.checkId,this);
        gridView.setAdapter(mPictureAdapter);
        gridView.setOnItemLongClickListener(longClickListener);
        gridView.setOnItemClickListener(itemClickListener);
        return view;
    }

//    public class UploadPictureAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            int size = getPhotoController().getCurrentSize();
//            return size < COMMENT_MAX_IMAGE_NUM ? size + 1 : size;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            if (position == getPhotoController().getCurrentSize()
//                    && getPhotoController().getCurrentSize() < MAX_IMAGE_NUM) {
//                View view = mInflater.inflate(R.layout.image_grid_item, null);
//                GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, mGridItemWidth);
//                view.setLayoutParams(mImageViewLayoutParams);
//                final ImageView image_item = (ImageView) view.findViewById(R.id.image_item);
//                TextView tv = (TextView) view.findViewById(R.id.upload_state);
//                tv.setVisibility(View.GONE);
//                image_item.setScaleType(ImageView.ScaleType.FIT_XY);
//                image_item.setImageResource(R.drawable.selector_comment_camera);
//                image_item.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        takePhoto(CAMERA_PHOTO);
//                    }
//                });
//                return view;
//            } else {
//                String photoFile = getPhotoController().getselectedPhotoList().get(position);
//
//                System.out.print("photoFile:" + photoFile);
//
//                View view = mInflater.inflate(R.layout.image_grid_item, null);
//                GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, mGridItemWidth);
//                view.setLayoutParams(mImageViewLayoutParams);
//                ImageView image_item = (ImageView) view.findViewById(R.id.image_item);
//                TextView tv = (TextView) view.findViewById(R.id.upload_state);
//                tv.setVisibility(View.GONE);
//
//                tv.setText(getPhotoController().getPhotoUpStateTxt(photoFile));
//                imageLoader.displayImageFileFitView(new File(photoFile), image_item);
//                return view;
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PHOTO) {
            if (null != mPictureAdapter.getTakePhotoFile()) {
                if (resultCode == Activity.RESULT_OK) {
                    mPhotoController.addImage(mPictureAdapter.getTakePhotoFile().getPath());
                    mPictureAdapter.notifyDataSetChanged();

                } else {
                    mPictureAdapter.getTakePhotoFile().delete();
                }
                mPictureAdapter.setTakePhotoFile(null);
            }
        }
    }

    /**
     * 返回photopick的图片选中的control
     *
     * @return
     */
    public PhotoController getPhotoController() {
        return mPhotoController;
    }

    public PictureAdapter getPictureAdapter() {
        return mPictureAdapter;
    }

    /**
     * 初始化photopick control
     */
    public void initPhotoController() {
        initPhotoController(MAX_IMAGE_NUM);
    }

    /**
     * 初始化photopick control
     *
     * @param maxNum
     */
    public void initPhotoController(int maxNum) {
        mPhotoController = new PhotoController(maxNum);
    }
}
