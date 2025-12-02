package zlian.netgap.support;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("NewApi")
public class ImageUtils {
	static ImageLoader sImageLoader = ImageLoader.getInstance();

	public static final DisplayImageOptions DISYPLAY_OPTION_WEB_IMAGE = new DisplayImageOptions.Builder()
//			.showStubImage(R.drawable.app_stub_image)
//			.showImageForEmptyUri(R.drawable.app_fail_image)
//			.showImageOnFail(R.drawable.app_fail_image)
			.build();

	public static void displayWebImage(String url, ImageView imageView) {

		sImageLoader.displayImage(url, imageView, DISYPLAY_OPTION_WEB_IMAGE);
	}

	public static String getImagePathFromProvider(Context context, Uri uri) {
		String[] projection = new String[] { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(uri, projection,
				null, null, null);
		int rowNums = cursor.getCount();
		if (rowNums == 0) {
			return null;
		}
		cursor.moveToFirst();
		String filePath = cursor.getString(0);
		cursor.close();
		return filePath;
	}

	public static Bitmap scaleImage(String imagePath, int requestWidth,
			int requestHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		options.inSampleSize = calculateInSampleSize(options, requestWidth,
				requestHeight);

		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

		String orientation = getExifOrientation(imagePath, "0");

		Matrix matrix = new Matrix();
		matrix.postRotate(Float.valueOf(orientation));

		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, false);

		return newBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqW, int reqH) {
		final int h = options.outHeight;
		final int w = options.outWidth;
		int inSampleSize = 1;

		if (h > reqH || w > reqW) {
			final int heightRatio = Math.round((float) h / (float) reqH);
			final int widthRatio = Math.round((float) w / (float) reqW);

			inSampleSize = Math.min(heightRatio, widthRatio);
		}

		return inSampleSize;
	}

	public static String getExifOrientation(String path, String orientation) {
		// get image EXIF orientation if Android 2.0 or higher, using reflection
		// http://developer.android.com/resources/articles/backward-compatibility.html
		Method exif_getAttribute;
		Constructor<ExifInterface> exif_construct;
		String exifOrientation = "";

		int sdk_int = 0;
		try {
			sdk_int = Integer.valueOf(VERSION.SDK);
		} catch (Exception e1) {
			sdk_int = 3; // assume they are on cupcake
		}
		if (sdk_int >= 5) {
			try {
				exif_construct = ExifInterface.class
						.getConstructor(new Class[] { String.class });
				Object exif = exif_construct.newInstance(path);
				exif_getAttribute = ExifInterface.class
						.getMethod("getAttribute", new Class[] { String.class });
				try {
					exifOrientation = (String) exif_getAttribute.invoke(exif,
							ExifInterface.TAG_ORIENTATION);
					if (exifOrientation != null) {
						if (exifOrientation.equals("1")) {
							orientation = "0";
						} else if (exifOrientation.equals("3")) {
							orientation = "180";
						} else if (exifOrientation.equals("6")) {
							orientation = "90";
						} else if (exifOrientation.equals("8")) {
							orientation = "270";
						}
					} else {
						orientation = "0";
					}
				} catch (InvocationTargetException ite) {
					/* unpack original exception when possible */
					orientation = "0";
				} catch (IllegalAccessException ie) {
					System.err.println("unexpected " + ie);
					orientation = "0";
				}
				/* success, this is a newer device */
			} catch (NoSuchMethodException nsme) {
				orientation = "0";
			} catch (IllegalArgumentException e) {
				orientation = "0";
			} catch (InstantiationException e) {
				orientation = "0";
			} catch (IllegalAccessException e) {
				orientation = "0";
			} catch (InvocationTargetException e) {
				orientation = "0";
			}

		}
		return orientation;
	}

	public static String getPicPathFromUri(Uri uri, Activity activity) {
		String value = uri.getPath();

		if (value.startsWith("/external")) {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else {
			return value;
		}
	}

	/**
	 * 鍥剧墖涓婄敾瀛�
	 * */
	public static Bitmap drawTextAtBitmap(Bitmap bitmap, String text) {

		return bitmap;
	}

	public static Bitmap getScaleImageWithText(String imagePath,
			int requestWidth, int requestHeight, String text) {
		Bitmap bitmap = scaleImage(imagePath, requestWidth, requestHeight);
		return drawTextAtBitmap(bitmap, text);
	}
	
	private static final int MAX_TEXTURE_SIZE = getOpengl2MaxTextureSize();

	public static int getOpengl2MaxTextureSize() {
		int[] maxTextureSize = new int[1];
		maxTextureSize[0] = 2048;
		android.opengl.GLES20.glGetIntegerv(
				android.opengl.GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
		return maxTextureSize[0];
	}

	/**
	 * Get the size in bytes of a bitmap.
	 * 
	 * @param bitmap
	 * @return size in bytes
	 */
	@SuppressLint("NewApi")
	public static int getBitmapSize(Bitmap bitmap) {
		if (VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Decode and sample down a bitmap from resources to the requested width and
	 * height.
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param resId
	 *            The resource id of the image data
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height(inMutable)
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		return decodeSampledBitmapFromResource(res, resId, reqWidth, reqHeight,
				false);
	}

	/**
	 * Decode and sample down a bitmap from resources to the requested width and
	 * height.
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param resId
	 *            The resource id of the image data
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param isMutable
	 *            锟缴编辑
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	@SuppressLint("NewApi")
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight, boolean isMutable) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		if (isMutable && VERSION.SDK_INT >= 11) {
			options.inMutable = true;
		}
		Bitmap result = BitmapFactory.decodeResource(res, resId, options);
		if (isMutable) {
			result = createMutableBitmap(result);
		}
		return result;
	}

	public static Bitmap decodeSampledBitmapFromFile(String filePath,
			int sampledSize) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = sampledSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and
	 * height.
	 * 
	 * @param filePath
	 *            The full path of the file to decode
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height(inmutable)
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filePath,
			int reqWidth, int reqHeight) {
		return decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight,
				false, false);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and
	 * height.
	 * 
	 * @param filePath
	 *            The full path of the file to decode
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param isMutable
	 *            锟缴编辑
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	@SuppressLint("NewApi")
	public static Bitmap decodeSampledBitmapFromFile(String filePath,
			int reqWidth, int reqHeight, boolean isMutable, boolean region) {
		if (filePath == null) {
			return null;
		}
		if (reqHeight == 0) {
			reqHeight = MAX_TEXTURE_SIZE;
		}
		if (reqWidth == 0) {
			reqWidth = MAX_TEXTURE_SIZE;
		}

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(filePath, options);

		if (options.outWidth == -1 || options.outHeight == -1) {
			return null;
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		if (isMutable && VERSION.SDK_INT >= 11) {
			options.inMutable = true;
		}

		Bitmap result = null;

		if ((options.outWidth > MAX_TEXTURE_SIZE
				|| options.outHeight > MAX_TEXTURE_SIZE || (options.outHeight > options.outWidth * 3))
				&& region) {
			// 锟斤拷图
			try {
				result = regionDecode(filePath, reqWidth, reqHeight,
						options.outWidth, options.outHeight);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			result = BitmapFactory.decodeFile(filePath, options);
		}

		if (isMutable) {
			result = createMutableBitmap(result);
		}

		return result;
	}

	private static Bitmap regionDecode(String path, int reqWidth,
			int reqHeight, int outWidth, int outHeight) throws IOException {
		BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(
				path, true);
		if (reqWidth > outWidth) {
			reqWidth = outWidth;
		}
		if (reqHeight > outHeight) {
			reqHeight = outHeight;
		}

		return regionDecoder.decodeRegion(new Rect(0, 0, reqWidth, reqHeight),
				null);
	}

	/**
	 * createMutableBitmap
	 * 
	 * @param src
	 * @return
	 */
	public static Bitmap createMutableBitmap(Bitmap src) {
		Bitmap result = null;
		if (src == null) {
			return null;
		}
		result = src.copy(Config.ARGB_8888, true);

		return result;
	}

	/**
	 * mergeBitmap
	 * 
	 * @param oriBmp
	 * @param subBmp
	 * @param oriRect
	 *            subBmp锟斤拷取锟斤拷锟斤拷bitmap锟斤拷要锟斤拷涞給riRect锟叫碉拷锟斤拷锟斤拷
	 * @param subRect
	 *            锟斤拷subBmp锟斤拷取锟斤拷锟斤拷锟斤拷锟斤拷
	 * @param paint
	 * @return
	 */
	public static Bitmap mergeBitmap(Bitmap oriBmp, Bitmap subBmp,
			final Rect oriRect, final Rect subRect) {
		if (subBmp == null) {
			return oriBmp;
		}

		if (oriBmp == null) {
			return null;
		}

		if (!oriBmp.isMutable()) {
			oriBmp = createMutableBitmap(oriBmp);
		}

		Canvas canvas = new Canvas(oriBmp);
		canvas.drawBitmap(subBmp, subRect, oriRect, null);
		return oriBmp;
	}

	/**
	 * 锟斤拷subBmp图锟斤拷喜锟斤拷锟給riBmp锟斤拷
	 * 
	 * @param oriBmp
	 * @param subBmp
	 * @param paint
	 * @return oriBmp
	 */
	public static Bitmap mergeBitmap(Bitmap oriBmp, Bitmap subBmp) {
		if (subBmp == null) {
			return oriBmp;
		}

		if (oriBmp == null) {
			return null;
		}

		return mergeBitmap(oriBmp, subBmp, new Rect(0, 0, oriBmp.getWidth(),
				oriBmp.getHeight()),
				new Rect(0, 0, subBmp.getWidth(), subBmp.getHeight()));
	}

	private static final PorterDuffXfermode SRC_IN_MODE = new PorterDuffXfermode(
			PorterDuff.Mode.SRC_IN);

	private final static Paint SRC_IN_PAINT = new Paint();

	static {
		SRC_IN_PAINT.setXfermode(SRC_IN_MODE);
	}

	/**
	 * 锟斤拷锟斤拷图片
	 * 
	 * @param dstBmp
	 * @param mask
	 * @param paint
	 * @return 锟斤拷锟街猴拷锟酵计�
	 */
	public static Bitmap maskBitmap(final Bitmap dstBmp, final Bitmap mask) {
		if (dstBmp == null || mask == null) {
			return dstBmp;
		}
		Bitmap result = Bitmap.createBitmap(dstBmp.getWidth(),
				dstBmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(),
				null, Canvas.ALL_SAVE_FLAG);
		canvas.drawBitmap(mask,
				new Rect(0, 0, mask.getWidth(), mask.getHeight()), new Rect(0,
						0, dstBmp.getWidth(), dstBmp.getHeight()), null);
		canvas.drawBitmap(dstBmp, 0, 0, SRC_IN_PAINT);

		canvas.restoreToCount(sc);
		return result;
	}

	public static Bitmap convertToAlphaMask(Bitmap b) {
		Bitmap a = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
				Config.ALPHA_8);
		Canvas c = new Canvas(a);
		c.drawBitmap(b, 0.0f, 0.0f, null);
		return a;
	}


	public static byte[] getbyteFromBitmap(Bitmap bitmap, boolean recycle)
			throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, out);
		if (recycle) {
			bitmap.recycle();
		}

		return out.toByteArray();

	}

}
