package zlian.netgap.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import zlian.netgap.R;
import zlian.netgap.bean.CheckPoint;
import zlian.netgap.data.TempData;
import zlian.netgap.photo.ImageLoader;
import zlian.netgap.photo.PhotoController;
import zlian.netgap.photo.PictureAdapter;
import zlian.netgap.support.FileUtil;
import zlian.netgap.ui.CheckActivity;
import zlian.netgap.ui.SummaryActivity;
import zlian.netgap.view.NoScrollGridView;

public class CheckPointAdapter extends BaseOptionAdapter<CheckPoint> {

    /**
     * 点击basephotoupload中选中图片--->图片预览photoviewactivity.java
     */
    protected AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SummaryActivity.instance.chkposition = position;
            CheckActivity.startCheckActivityForResult(mContext, position);
        }
    };
    ArrayList<PhotoController> mPicAdpList = new ArrayList<PhotoController>(40);
    private Context context;
//    private View photoView;
    private int mGridItemWidth;
    private PictureAdapter mPictureAdapter;
    // photopick的控制数据
    private PhotoController mPhotoController;
    private int mMaxNum;

    public CheckPointAdapter(Context context, int maxNum, ArrayList<CheckPoint> l) {
        super(context, l);
        this.context = context;
        this.mMaxNum = maxNum;

        //mPhotoController = new PhotoController(maxNum);
        // get checkpoint list
        for (int i = 0; i < l.size(); i++) {
            mPhotoController = new PhotoController(maxNum);

            CheckPoint checkPoint = (CheckPoint) getItem(i);
            int commentPics = checkPoint.getCommentPics();
            for (int j = 1; j < commentPics; j++) {
                String filename = "";
                filename = checkPoint.getId() + j + ".jpg";
                String fullname = FileUtil.filePath + TempData.uuid + "/" + filename;
                File photofile = new File(fullname);
                if (photofile.exists()) {
                    mPhotoController.addImage(photofile.getPath());
                }
            }

            mPicAdpList.add(mPhotoController);
        }
    }

    public static Bitmap decodeSampledBitmapFromFd(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    private View createDispPictureView(ListView listView, String checkId, int position) {
        DisplayMetrics dm = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);
        listView.getDisplay().getMetrics(dm);
        mGridItemWidth = (int) ((dm.widthPixels - dm.density * (16 + 16 + 8 * 3)) / 4);
        View view = LayoutInflater.from(this.context).inflate(R.layout.comment_picture_grid, null);
        NoScrollGridView gridView = (NoScrollGridView) view.findViewById(R.id.gv);

        mPhotoController = mPicAdpList.get(position);
        mPictureAdapter = new PictureAdapter(LayoutInflater.from(this.context), mPhotoController, ImageLoader.getInstance(), mGridItemWidth, checkId, null);

        gridView.setAdapter(mPictureAdapter);
        gridView.setOnItemClickListener(itemClickListener);

        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_mlist,
                    parent, false);
            mHolder = new ViewHolder();
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        CheckPoint checkPoint = (CheckPoint) getItem(position);
        TextView tv_index = (TextView) convertView.findViewById(R.id.tv_index);
        TextView tv_category = (TextView) convertView.findViewById(R.id.tv_category_value);
        TextView tv_item = (TextView) convertView.findViewById(R.id.tv_item_value);
        TextView tv_desc = (TextView) convertView.findViewById(R.id.tv_desc_value);
        TextView tv_result = (TextView) convertView.findViewById(R.id.tv_result_value);
        TextView tv_star = (TextView) convertView.findViewById(R.id.tv_star);

        // 2023todo 结果页显示图片
        ImageView imageView = convertView.findViewById(R.id.imageView);
        if (checkPoint.getImgBitmap() != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(checkPoint.getImgBitmap());
        } else {
            imageView.setVisibility(View.GONE);
        }

//        mHolder.tv_photo = (TextView) convertView.findViewById(R.id.tv_photo_title);
//        mHolder.mrl_photo = (RelativeLayout) convertView.findViewById(R.id.mrl_photo);
//        photoView = createDispPictureView((ListView) parent, checkPoint.getId(), position);
//        mHolder.mrl_photo.addView(photoView);
//
//        int commentPics = checkPoint.getCommentPics();
//        if (commentPics <= 1) {
//            //mHolder.tv_photo.setVisibility(View.INVISIBLE);
//            //mHolder.mrl_photo.setVisibility(View.INVISIBLE);
//            RelativeLayout rlp = (RelativeLayout) convertView.findViewById(R.id.relativeLayout_photo);
//            rlp.setVisibility(View.GONE);
//        } else {
//            RelativeLayout rlp = (RelativeLayout) convertView.findViewById(R.id.relativeLayout_photo);
//            rlp.setVisibility(View.VISIBLE);
//            mHolder.tv_photo.setVisibility(View.VISIBLE);
//            mHolder.mrl_photo.setVisibility(View.VISIBLE);
//        }



//        for (int i = 1; i < commentPics; i++) {
//            String filename = "";
//            filename = checkPoint.getId()+i+".jpg";
//            String fullname = FileUtil.filePath+ TempData.uuid+"/"+filename;
//            File photofile = new File(fullname);
//            if (photofile.exists()) {
//                mPhotoController.addImage(photofile.getPath());
//            }
////            Bitmap createScaleBitmap = decodeSampledBitmapFromFd(FileUtil.filePath + TempData.uuid + "/" + checkPoint.getId() + i + ".jpg", 100, 100);
////            if (createScaleBitmap != null) {
////                ImageView picture = new ImageView(context);
////                picture.setImageBitmap(createScaleBitmap);
////                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
////                        RelativeLayout.LayoutParams.WRAP_CONTENT);
////                lp.topMargin = getScrollY((ListView)parent);
////                lp.leftMargin = 110 * (i - 1);
////                lp.alignWithParent = true;
////                picture.setLayoutParams(lp);
////                mHolder.mrl_photo.addView(picture);
////                picture.setTag(createScaleBitmap);
////            }
////            else {
////                mHolder.mrl_photo.setVisibility(View.INVISIBLE);
////            }
//        }

        tv_index.setText("NO." + (position + 1));
        tv_category.setText(checkPoint.getTitle());
        tv_item.setText(checkPoint.getItem());
        tv_desc.setText(checkPoint.getContent());
        if (checkPoint.getExpected().equalsIgnoreCase(checkPoint.getResult())) {
            tv_result.setText(R.string.ok);
            //tv_result.setTextColor(0xff00ff00);
            //tv_result.setTextColor(0xFF1509FF);
            tv_result.setTextColor(0xFF7975FF);
        } else {
            tv_result.setText(R.string.ng);
            tv_result.setTextColor(0xffff0000);
        }
        if ("1".equals(checkPoint.getKey())) {
            tv_star.setVisibility(View.VISIBLE);
        } else {
            tv_star.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public int getScrollY(ListView listview) {
        View c = listview.getChildAt(0);
        if (c == null) {
            return 0;
        } else {
            int firstVisiblePostion = listview.getFirstVisiblePosition();
            int top = c.getTop();
            int scrollheight = top + firstVisiblePostion * c.getHeight();
            return scrollheight;
        }
    }

    private static class ViewHolder {
//        TextView tv_photo;
//        RelativeLayout mrl_photo;
    }
}