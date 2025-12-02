package zlian.netgap.photo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import zlian.netgap.R;
import zlian.netgap.ui.BasePhotoActivity;

/**
 * Created by david on 2016/12/9.
 */
public class PictureAdapter extends BaseAdapter {

    private PhotoController mPhotoController;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mGridItemWidth;
    private String mCheckId;
    private File mPhotoFile;
    private Activity mActivity;

    public PictureAdapter(LayoutInflater inflater,PhotoController photoController,ImageLoader imageLoader,int gridItemWidth,String checkId,Activity activity) {
        this.mInflater = inflater;
        this.mPhotoController = photoController;
        this.mImageLoader = imageLoader;
        this.mGridItemWidth = gridItemWidth;
        this.mCheckId = checkId;
        this.mActivity = activity;
    }

    public File getTakePhotoFile() {
        return mPhotoFile;
    }

    public void setTakePhotoFile(File photoFile) {
        mPhotoFile = photoFile;
    }


    @Override
    public int getCount() {
        int size = mPhotoController.getCurrentSize();
        return size < BasePhotoActivity.COMMENT_MAX_IMAGE_NUM ? size + 1 : size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void takePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = PhotoUpHelper.getCameraPhotoFile(mCheckId);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
        mActivity.startActivityForResult(takePictureIntent, requestCode);
    }

    public void setCheckId(String checkId) {
        this.mCheckId = checkId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.image_grid_item, null);

        if (position == mPhotoController.getCurrentSize()
                && mPhotoController.getCurrentSize() < BasePhotoActivity.MAX_IMAGE_NUM
                && mActivity != null) {
            GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, mGridItemWidth);
            view.setLayoutParams(mImageViewLayoutParams);
            ImageView image_item = (ImageView) view.findViewById(R.id.image_item);
            TextView tv = (TextView) view.findViewById(R.id.upload_state);
            tv.setVisibility(View.GONE);
            image_item.setScaleType(ImageView.ScaleType.FIT_XY);
            image_item.setImageResource(R.drawable.selector_comment_camera);
            image_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto(BasePhotoActivity.CAMERA_PHOTO);
                }
            });
        } else {
            if (position < mPhotoController.getselectedPhotoList().size()) {
                String photoFile = mPhotoController.getselectedPhotoList().get(position);
                GridView.LayoutParams mImageViewLayoutParams = new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mGridItemWidth);
                view.setLayoutParams(mImageViewLayoutParams);
                ImageView image_item = (ImageView) view.findViewById(R.id.image_item);
                TextView tv = (TextView) view.findViewById(R.id.upload_state);
                tv.setVisibility(View.GONE);
                tv.setText(mPhotoController.getPhotoUpStateTxt(photoFile));
                mImageLoader.displayImageFileFitView(new File(photoFile), image_item);

//                image_item.invalidate();
//                image_item.setImageBitmap(null);
//                image_item.destroyDrawingCache();
//                image_item.setImageResource(android.R.color.transparent);
//                image_item.setImageResource(0);

            }
            else {
                System.out.println("position:" + position);
                System.out.println("pic number:"+mPhotoController.getselectedPhotoList().size());
            }
        }
        return view;
    }
}
