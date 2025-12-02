package zlian.netgap.photo;

/**
 * 图片加载管理类，上层业务和下层图片管理插件的中间层（没有使用Build模式，以防止不在使用毕加索） hl09287@ly.com
 */

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import zlian.netgap.R;
import zlian.netgap.app.MyApplication;

public class ImageLoader {

    public static final int STUB_NULL = -1;// 不设置默认图
    public static final int RES_LOAD_SMALL = R.drawable.icon;

    private static final int STUB_ID = R.drawable.icon; // 默认图片（再不设置默认图的情况下用这个）
    private static final Config DEFAULT_CONFIG = Config.RGB_565;


    private static final long WARING_SIZE = 300 * 1024;

    private static ImageLoader imageLoader = null;
    private Context context;


    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();

        // Picasso.with(context).setIndicatorsEnabled(true);//显示图片的来源
    }

    public static synchronized ImageLoader getInstance() {
        if (imageLoader == null) {
            imageLoader = new ImageLoader(MyApplication.getInstance());
        }
        return imageLoader;
    }

    /**
     * 默认图片加载
     *
     * @param imageUrl  图片url地址
     * @param imageView 显示控件
     */
    public void displayImage(String imageUrl, ImageView imageView) {
        displayImage(imageUrl, imageView, STUB_ID, STUB_ID, DEFAULT_CONFIG);
    }

    /**
     * 带默认图片的图片加载（加载成功前和加载失败后的图片一样）
     *
     * @param imageUrl 图片url地址
     * @param view     图片显示view控件
     * @param stub_id  默认图resid（成功 and 失败）
     */
    public void displayImage(String imageUrl, ImageView view, int stub_id) {
        /*if (TextUtils.isEmpty(imageUrl)) {
            return;
        }*/
        if (stub_id == STUB_NULL) {
            if (TextUtils.isEmpty(imageUrl)) {
                return;
            }
            Picasso.with(context).load(imageUrl).config(DEFAULT_CONFIG)
                    .into(view);
        } else {
            displayImage(imageUrl, view, stub_id, stub_id, DEFAULT_CONFIG);
        }
    }

    /**
     * 带有设定图片品质的接口
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param config    图片显示配置信息（图片品质）
     */
    public void displayImage(String imageUrl, ImageView imageView, Config config) {
        displayImage(imageUrl, imageView, STUB_ID, STUB_ID, config);
    }

    /**
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param stub_id   默认加载图resid（成功 and 失败）
     * @param config    图片显示配置信息
     */
    public void displayImage(String imageUrl, ImageView imageView, int stub_id, Config config) {
        displayImage(imageUrl, imageView, stub_id, stub_id, config);
    }

    /**
     * 为大首页定制的，没有动画的图片加载。（默认情况下在图片设置时会有动画）
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param stub_id   默认加载图resid（成功 and 失败）
     * @param isNoFade  图片显示效果关闭（true）
     */
    public void displayImage(String imageUrl, ImageView imageView, int stub_id, boolean isNoFade) {
        if (imageUrl == null || "".equals(imageUrl)) {
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setImageResource(stub_id);
            return;
        }
        if (stub_id == STUB_NULL) {
            Picasso.with(context).load(imageUrl).config(DEFAULT_CONFIG).noFade().into(imageView);
            return;
        }
        if (isNoFade) {
            Picasso.with(context).load(imageUrl).placeholder(stub_id).error(stub_id).config(DEFAULT_CONFIG).noFade()
                    .into(imageView);
            return;
        }
        Picasso.with(context).load(imageUrl).placeholder(stub_id).error(stub_id).config(DEFAULT_CONFIG).into(imageView);
    }

    /**
     * 带默认图片的图片加载（加载成功前和加载失败后的图片显示）
     *
     * @param imageUrl       图片url地址
     * @param view           图片显示view控件
     * @param stub_id        默认加载图resid（成功）
     * @param stub_id_no_img 默认加载图resid（失败）
     * @param config         图片显示配置信息
     */
    public void displayImage(String imageUrl, ImageView view, int stub_id, int stub_id_no_img, Config config) {
        if (imageUrl == null || "".equals(imageUrl)) {
            if (view instanceof ImageView) {
                ((ImageView) view).setScaleType(ScaleType.FIT_XY);
                ((ImageView) view).setImageResource(stub_id);
            } else {
                view.setBackgroundResource(stub_id);
            }
            return;
        }
        Picasso.with(context).load(imageUrl).placeholder(stub_id).error(stub_id_no_img).config(config).into(view);
    }

    /**
     * 加载图片 不压缩 截取中间部分展示
     *
     * @param imageUrl     图片url地址
     * @param view         图片显示view控件
     * @param stub_id      默认加载图resid（成功 and 失败）
     * @param targetWidth  截取图片的宽度
     * @param targetHeight 截取图片的高度
     */
    public void displayImage(String imageUrl, ImageView view, int stub_id, int targetWidth, int targetHeight) {
        if (stub_id == STUB_NULL) {
            Picasso.with(context).load(imageUrl).config(DEFAULT_CONFIG).into(view);
        } else {
            displayImage(imageUrl, view, stub_id, stub_id, DEFAULT_CONFIG, targetWidth, targetHeight);
        }
    }

    /**
     * 加载图片 不压缩 截取中间部分展示 图片配置信息
     *
     * @param imageUrl       图片url地址
     * @param view           图片显示view控件
     * @param stub_id        默认加载图resid（成功）
     * @param stub_id_no_img 默认加载图resid（失败）
     * @param config         图片显示配置信息
     * @param targetWidth    截取图片的宽度
     * @param targetHeight   截取图片的高度
     */
    public void displayImage(String imageUrl, ImageView view, int stub_id, int stub_id_no_img, Config config, int targetWidth, int targetHeight) {
        if (imageUrl == null || "".equals(imageUrl)) {
            if (view instanceof ImageView) {
                ((ImageView) view).setScaleType(ScaleType.CENTER_CROP);
                ((ImageView) view).setImageResource(stub_id);
            } else {
                view.setBackgroundResource(stub_id);
            }
            return;
        }
        Picasso.with(context).load(imageUrl).centerCrop().resize(targetWidth, targetHeight).placeholder(stub_id).error(stub_id_no_img).config(config).into(view);
    }

    /**
     * 带回调的图片加载（加载成功、失败、进度的回调）
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param callback  图片加载的回调(进度、成功、失败)
     */
    public void displayImage(String imageUrl, ImageView imageView, Callback callback) {
        displayImage(imageUrl, imageView, STUB_ID, STUB_ID, callback, DEFAULT_CONFIG);
    }

    /**
     * 带回调的图片加载（加载成功、失败、进度的回调）and 设置默认图
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param callback  图片加载的回调(进度、成功、失败)
     * @param stub_id   默认加载图resid（成功 and 失败）
     */
    public void displayImage(String imageUrl, ImageView imageView, Callback callback, int stub_id) {
        if (stub_id == STUB_NULL) {
            Picasso.with(context).load(imageUrl).config(DEFAULT_CONFIG).into(imageView, callback);
            return;
        }
        displayImage(imageUrl, imageView, stub_id, stub_id, callback, DEFAULT_CONFIG);
    }

    /**
     * 带回调的图片加载（加载成功、失败、进度的回调） and 图片显示配置信息
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param callback  图片加载的回调(进度、成功、失败)
     * @param config    图片显示配置信息
     */
    public void displayImage(String imageUrl, ImageView imageView, Callback callback, Config config) {
        displayImage(imageUrl, imageView, STUB_ID, STUB_ID, callback, config);
    }

    /**
     * 带回调的图片加载（加载成功、失败、进度的回调） and 图片显示配置信息 and 默认加载图resid
     *
     * @param imageUrl  图片url地址
     * @param imageView 图片显示view控件
     * @param callback  图片加载的回调(进度、成功、失败)
     * @param stub_id   默认加载图resid（成功 and 失败）
     * @param config    图片显示配置信息
     */
    public void displayImage(String imageUrl, ImageView imageView, Callback callback, int stub_id, Config config) {
        displayImage(imageUrl, imageView, stub_id, stub_id, callback, config);
    }

    /**
     * 带回调的图片加载（加载成功、失败、进度的回调） and 图片显示配置信息 and 默认加载图resid
     *
     * @param imageUrl       图片url地址
     * @param imageView      图片显示view控件
     * @param stub_id        默认加载图resid（成功）
     * @param stub_id_no_img 默认加载图resid（失败）
     * @param callback       图片加载的回调(进度、成功、失败)
     * @param config         图片显示配置信息
     */
    public void displayImage(String imageUrl, ImageView imageView, int stub_id, int stub_id_no_img, Callback callback,
                             Config config) {
        if (imageUrl == null || "".equals(imageUrl)) {
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setImageResource(stub_id);
            return;
        }
        Picasso.with(context).load(imageUrl).placeholder(stub_id).error(stub_id_no_img).config(config)
                .into(imageView, callback);

    }

    /**
     * 显示图片 本地file文件
     *
     * @param file      图片file
     * @param imageView 图片显示view控件
     */
    public void displayImageFileFitView(File file, ImageView imageView) {
        if (file == null) {
            return;
        }
        Picasso.with(context).load(file).fit().centerInside().into(imageView);
    }

    /**
     * 仅仅fetch图片
     *
     * @param imageUrl
     */
    public void fetch(String imageUrl) {
        if (imageUrl == null || "".equals(imageUrl)) {
            return;
        }
        Picasso.with(context).load(imageUrl).fit();
    }

    /**
     * 获取图片，并且拿到bitmap进行特殊需求（bitmap、成功、失败）
     *
     * @param imageUrl 图片url地址
     * @param target   图片加载（成功、失败、预备）
     */
    public void fetchToTarget(String imageUrl, Target target) {
        if (imageUrl == null || "".equals(imageUrl)) {
            return;
        }
        Picasso.with(context).load(imageUrl).into(target);
    }

    /**
     * 获取图片，并且拿到bitmap进行特殊需求（bitmap、成功、失败） and 设置默认加载中的显示图
     *
     * @param imageUrl       图片url地址
     * @param target         图片加载（成功、失败、预备）
     * @param placeHolderRes 图片加载中默认图
     */
    public void fetchToTarget(String imageUrl, Target target, int placeHolderRes) {
        if (imageUrl == null || "".equals(imageUrl)) {
            return;
        }
        Picasso.with(context).load(imageUrl).placeholder(placeHolderRes).into(target);
    }

    /**
     * 取消请求（用于释放资源）
     *
     * @param view 图片显示view控件
     */
    public void cancelRequest(ImageView view) {
        Picasso.with(context).cancelRequest(view);
    }

}
