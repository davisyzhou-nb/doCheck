package zlian.netgap.photo;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 对上传图片的成功和失败状态的保存对象
 */
public class PhotoController implements Serializable {

    private static final long serialVersionUID = -7220514523007676006L;
    /**
     * 图片上传的状态 0：等待上传 1：上传成功 2：上传失败
     */
    public static final int STATE_WAITING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FALLEN = 2;

    // 图片上传状态的中文描述信息
    public static final String[] INFOS = {"添加图片信息", "已经上传", "上传失败"};

    // 上传图片的index的值
    private int mUploadIndex;
    // 图片上传的最大值
    private int mMaxNum = 10;
    // 已经选择的图片的列表
    private ArrayList<String> mSelectedPhotoList = new ArrayList<String>(10);

    // 当前图库中的图片的list
    private ArrayList<String> mBucketPhotoList = new ArrayList<String>();

    // 图片描述信息
    private HashMap<String, String> photoDescriptionMap = new HashMap<String, String>();
    // 保存图片的上传的状态信息
    private HashMap<String, Integer> photoUpStateMap = new HashMap<String, Integer>();

    // 上传失败图片的列表
    private ArrayList<String> photoSuccessPics = new ArrayList<String>();

    // 当前图片的路径地址
    private String mCurrentPhoto;

    // 删除图片的路径地址
    private String mDelPhoto;

    public PhotoController(int max) {
        mMaxNum = max;
        mSelectedPhotoList.clear();
    }

    /**
     * 判断是否选中的状态
     *
     * @param image
     * @return
     */
    public boolean isSelected(String image) {
        return mSelectedPhotoList.contains(image);
    }

    /**
     * 将缓存中的图片信息添加到选中图片列表中
     *
     * @param photoList
     */
    public void addAll(ArrayList<String> photoList) {
        if (photoList != null) {
            mSelectedPhotoList.addAll(photoList);
        }
    }

    public void addImage(String image) {
        if (!mSelectedPhotoList.contains(image)) {
            mSelectedPhotoList.add(image);
//            postEvent(new PhotoAddRemoveEvent(mSelectedPhotoList));
        }
    }

    public void removeImage(String image) {
        mSelectedPhotoList.remove(image);
//        postEvent(new PhotoAddRemoveEvent(mSelectedPhotoList));
    }

    public String removeImage(int position) {
        mDelPhoto = mSelectedPhotoList.get(position);
        mSelectedPhotoList.remove(position);
        return mDelPhoto;
//        postEvent(new PhotoAddRemoveEvent(mSelectedPhotoList));
    }

    /**
     * 为当前的list添加imagesList
     *
     * @param imagesList
     */
    public void addBucketImagesAll(ArrayList<String> imagesList) {
        mBucketPhotoList.clear();
        mBucketPhotoList.addAll(imagesList);
    }

    /**
     * 获取当前图片库图片lists
     *
     * @return
     */
    public ArrayList<String> getBucketImagesList() {
        return mBucketPhotoList;
    }

    /**
     * 获取list的url所在的position
     *
     * @param imageUrl
     * @return
     */
    public int getBucketPosition(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return 0;
        }
        if (mBucketPhotoList.contains(imageUrl)) {
            for (int i = 0, j = mBucketPhotoList.size(); i < j; i++) {
                if (mBucketPhotoList.get(i).equals(imageUrl)) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 对图片选中状态的记录
     *
     * @param photo
     * @param state
     */
    public void putPhotoUpState(String photo, int state) {
        photoUpStateMap.put(photo, state);
        // 统计失败的图片
        if (state == STATE_SUCCESS) {
            photoSuccessPics.add(photo);
        }
    }

    /**
     * 对图片信息描述的添加
     *
     * @param photo
     * @param des
     */
    public void putPhotoDes(String photo, String des) {
        photoDescriptionMap.put(photo, des);
    }

    /**
     * 照片没添加过描述会返回空
     *
     * @param photo
     * @return
     */
    public String getPhotoDes(String photo) {
        return photoDescriptionMap.get(photo);
    }

    /**
     * 图片上传的状态信息
     *
     * @param photo
     * @return
     */
    public int getPhotoUpState(String photo) {
        Integer state = photoUpStateMap.get(photo);
        if (state == null) {
            return STATE_WAITING;
        } else {
            return state;
        }
    }

    /**
     * 获取失败的图片的张数
     *
     * @return
     */
    public int getphotoSuccessPics() {
        return photoSuccessPics.size();
    }

    /**
     * 根据id获得图片状态中文文案
     *
     * @param photo
     * @return
     */
    public String getPhotoUpStateTxt(String photo) {
        int state = getPhotoUpState(photo);
        String txt = getPhotoDes(photo);
        if (!TextUtils.isEmpty(txt) && state == STATE_WAITING) {
            return txt;
        } else {
            return INFOS[state];
        }
    }

    public String getPhotoUpStateTxtClearly(String photo) {
        int state = getPhotoUpState(photo);
        if (state == STATE_WAITING) {
            return "";
        } else {
            return INFOS[state];
        }
    }

    /**
     * 判断是否已经图片的size是否已经达到最大值
     *
     * @return
     */
    public boolean capacityFull() {
        return mSelectedPhotoList.size() >= mMaxNum;
    }

    /**
     * 获取图片上传列表的最大值
     *
     * @return
     */
    public int getMaxNum() {
        return mMaxNum;
    }

    public void setMaxNum(int num){
        mMaxNum = num;
    }

    /**
     * 获取当前已经选择的list的size大小
     *
     * @return
     */
    public int getCurrentSize() {
        return mSelectedPhotoList.size();
    }

    /**
     * 获取已经选择的list
     *
     * @return
     */
    public ArrayList<String> getselectedPhotoList() {
        return mSelectedPhotoList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mSelectedPhotoList.size() + " ");
        for (String s : mSelectedPhotoList) {
            sb.append(s + "   ");
        }
        return sb.toString();
    }

    private void postEvent(Object event) {
//        EventBus.getDefault().post(event);
    }

    /**
     * 是否有等待上传的照片
     *
     * @return
     */
    public boolean canUpload() {
        for (String photo : mSelectedPhotoList) {
            if (getPhotoUpState(photo) != 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新上传的index
     */
    public void resetUploadIndex() {
        mUploadIndex = 0;
    }

    /**
     * 获取下一张要上传的照片
     *
     * @return
     */
    public String getNextUpPhoto() {
        mCurrentPhoto = null;
        for (; mUploadIndex < getCurrentSize(); mUploadIndex++) {
            mCurrentPhoto = mSelectedPhotoList.get(mUploadIndex);
            if (getPhotoUpState(mCurrentPhoto) != 1) {
                mUploadIndex++;
                return mCurrentPhoto;
            }
        }
        return mCurrentPhoto;
    }

    /**
     * 获取当前上传的图片的路径
     *
     * @return
     */
    public String getCurrentPhoto() {
        return mCurrentPhoto;
    }

    /**
     * 根据photo的路径拿到position
     *
     * @param photo
     * @return
     */
    public int getPhotoPosition(String photo) {
        return mSelectedPhotoList.indexOf(photo);
    }

    /**
     * @return 界面上还有几张待上传的照片
     */
    public int getWaitingUploadSize() {
        int size = 0;
        for (String photo : mSelectedPhotoList) {
            if (getPhotoUpState(photo) != 1) {
                size++;
            }
        }
        return size;
    }
}
