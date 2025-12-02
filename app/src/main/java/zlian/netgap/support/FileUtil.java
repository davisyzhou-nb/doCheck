package zlian.netgap.support;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
	private static final String TAG = "FileUtil";

	public static final String SDPATH = Environment
			.getExternalStorageDirectory().toString();
	public static final String PICTRUEPATH = SDPATH + "/jscs/img/";
	public static final String FILEPATH = SDPATH + "/jscs/file";

	public static final String filePath = SDPATH+"/tecnect/";
	public static final String zipPath = SDPATH+"/tecnectzip/";

	//因为补传添加 add by zlian
	public static final String FILETEMP = SDPATH + "/jscs/temp";
	public static final String FILEDUTY = "duty";
	public static final String FILEDUTYPICTRUE = "picduty";//放勤务的

	public static void checkpath(String path) {
		File file = new File(path);
		Log.e("XXX",file.exists()+"");
		if (!file.exists()) {
			boolean b = file.mkdirs();
			Log.e("YYY",b+"");
		}
	}

	public static void copy(InputStream minput, FileOutputStream moutput)
			throws IOException {
		byte[] buffer = new byte[1024];
		int length;

		while ((length = minput.read(buffer)) > 0) {
			moutput.write(buffer, 0, length);
		}

		moutput.flush();
		moutput.close();
		minput.close();
	}

	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (file != null && file.exists())
			return file.delete();
		return false;

	}

	public static boolean deleteFile(File file) {
		if (file != null && file.exists())
			return file.delete();
		return false;

	}

	public static void deleteFiles(File file) {

		if (file.exists()) {

			File[] files = file.listFiles();
			for (File f : files) {
				f.delete();
			}
		}
	}

	public static String getfilename() {
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
				"yyyyMMddHHmmss");
		Date localDate = new Date();
		String filename = String.valueOf(localSimpleDateFormat
				.format(localDate));
		return filename;
	}

	

	public static void saveObject(Object paramObject, String paramString)
			throws IOException {
		FileOutputStream localFileOutputStream = new FileOutputStream(
				paramString);
		saveObject(paramObject, localFileOutputStream);
	}

	// 保存对象
	public static void saveObject(Object paramObject,
			OutputStream paramOutputStream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(
				makeOutputBuffered(paramOutputStream));
		out.writeObject(paramObject);
		out.close();
	}

	// 保存String
	public static void saveString(String str, String filename) {
		try {
			saveObject(str, filename);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public static boolean doesExisted(File file) {
		if (file != null && file.exists()) {
			return true;
		} else {
			return false;
		}

	}

	public static InputStream makeInputBuffered(InputStream inputStream) {
		if (inputStream instanceof BufferedInputStream) {
			return inputStream;
		} else {
			return new BufferedInputStream(inputStream);
		}

	}

	public static OutputStream makeOutputBuffered(OutputStream outputStream) {
		if (outputStream instanceof BufferedOutputStream) {
			return outputStream;
		} else {
			return new BufferedOutputStream(outputStream);
		}
	}
	
	public static boolean CheckDatacCache() {
		File file = new File(FILETEMP+"/"+FILEDUTY);
		if (!file.exists()) {
			file.mkdirs();
		}
		File[] files = file.listFiles();
		if (files!=null&&files.length > 0) {
			return true;
		}
		file = new File(FILETEMP+"/"+FILEDUTYPICTRUE);
		if (!file.exists()) {
			file.mkdirs();
		}
		files = file.listFiles();
		if (files!=null&&files.length > 0) {
			return true;
		}
		return false;
	}
	
	public static String getString(File file) {
		
		try {
			
			return (String)loadObject(new FileInputStream(file));
			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			}
		return null;
		
	}
	
	public static Object loadObject(InputStream inputStream)
			throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(
				makeInputBuffered(inputStream));
		Object object = null;

		object = in.readObject();
		in.close();

		return object;
	}

}
