package zlian.netgap.bean;

import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

import zlian.netgap.data.TempData;

public class CheckPoint {

    /**
     * id
     */
    private String id;
    /**
     * 检查类型
     */
    private String title;
    /**
     * 检查项目
     */
    private String item;
    /**
     * 检查内容
     */
    private String content;
    /**
     * 重要点解项
     */
    private String key;
    /**
     * 期待结果
     */
    private String expected;
    /**
     * 解锁叉车
     */
    private String unlock;

    /**
     * 是否需要运动检测
     * 0 不需要运动检测， 1 需要做运动检测
     */
    private String md;
    /**
     * 最小点检时间(秒)
     */
    private String min;
    /**
     * 最大点检时间(秒)
     */
    private String max;
    /**
     * 图片或视频
     * 2024/1todo
     */
    private String img;
    /**
     * 点检结果 N 否  Y 是
     */
    private String result;

    /**
     * 运动检测结果 0 未作运动检测  1 已做运动检测
     */
    private int mdResult;

    /**
     * 备注
     */
    private String memo;

    /**
     * 图片数量
     */
    private int commentPics;

    /**
     * 图片
     * 2023todo
     */
    private File imgFile;

    /**
     * 图片
     * 2023todo
     */
    private Bitmap imgBitmap;

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(File imgFile) {
        this.imgFile = imgFile;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    private static int ONCE_SEND_MAX_SIZE = 412;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getUnlock() {
        return unlock;
    }

    public void setUnlock(String unlock) {
        this.unlock = unlock;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public int getCommentPics() {
        return commentPics;
    }

    public void setCommentPics(int commentPics) {
        this.commentPics = commentPics;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
        this.memo.trim();
    }

    public String getMd() {
        return md;
    }

    public void setMd(String md) {
        this.md = md;
    }

    public int getMdResult() {
        return mdResult;
    }

    public void setMdResult(int mdResult) {
        this.mdResult = mdResult;
    }

    public static String getCheckResultString(List<CheckPoint> list, String transactionId) {
        StringBuilder sb = new StringBuilder();
        int result = 0;
        //String memo = "我23456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567的";
        if (list != null) {
            /*2023todo start*/
            sb.append("{\"transactionId\": \"" + transactionId + "\",");
            /*2023todo end*/
            sb.append("\"result\":[");
            for (int i = 0; i < list.size(); i++) {
                CheckPoint temp = list.get(i);
                sb.append("{");
                sb.append("\"id\": \"" + temp.getId() + "\",");
                result = temp.getExpected().equalsIgnoreCase(temp.getResult()) ? 1 : 0;
                sb.append("\"md\": " + temp.getMdResult() + ",");
                sb.append("\"result\": " + result + ",");
                //sb.append("\"result\": "+temp.getResult()+",");
                //sb.append("\"memo\": \""+memo+"\"");
                sb.append("\"memo\": \"" + temp.getMemo() + "\"");
                if (i == list.size() - 1) {
                    sb.append("}");
                } else {
                    sb.append("},");
                }
            }

            /*2023todo*/
            sb.append("]}");
        }
        return sb.toString();
    }

    /**
     * 判断重要性是否通过
     *
     * @return
     */

    public static int isKeyPass() {
        int isFlag = 1;
        List<CheckPoint> checkPoints = TempData.getIns().getCheckPointList();
        for (CheckPoint checkPoint : checkPoints) {
            if ("1".equals(checkPoint.getKey()) &&
                    !checkPoint.getExpected().equalsIgnoreCase(checkPoint.getResult())) {
                isFlag = 0;
                break;
            }
        }
        return isFlag;
    }

    public CheckPoint() {
        //this.md = "0";
    }
}
