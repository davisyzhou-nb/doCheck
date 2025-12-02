package zlian.netgap.photo;

import java.io.File;

import zlian.netgap.data.TempData;
import zlian.netgap.support.FileUtil;

public class PhotoUpHelper {

    /**
     * 拍照的图片的保存命名
     *
     * @return
     */
    public static File getCameraPhotoFile(String name) {
        File dir = new File(FileUtil.filePath+ TempData.uuid);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, name + ".jpg");
    }
}
