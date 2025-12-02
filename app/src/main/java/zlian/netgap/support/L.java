package zlian.netgap.support;

import android.util.Log;

public class L {

	private static boolean sDebug = true;

	public static void setDebug(boolean debug) {
		sDebug = debug;
	}

	public static boolean getDebug() {
		return sDebug;
	}

	private static String getTagName(Object tag) {
		if (tag instanceof Class) {
			return ((Class) tag).getSimpleName();
		}
		return getTagName(tag.getClass());
	}

	public static void d(Object tag, String msg) {
		if (sDebug) {
			Log.d(getTagName(tag), msg);
		}
	}

	public static void e(Object tag, String msg) {
		if (sDebug) {
			Log.e(getTagName(tag), msg);
		}
	}

	public static void e(Object tag, String msg, Throwable throwable) {
		if (sDebug) {
			Log.e(getTagName(tag), msg, throwable);
		}
	}

	public static void e(Object tag, Throwable throwable) {
		if (sDebug) {
			Log.e(getTagName(tag), "", throwable);
		}
	}

	public static void i(Object tag, String msg) {
		if (sDebug) {
			Log.i(getTagName(tag), msg);
		}
	}

	public static void w(Object tag, String msg) {
		if (sDebug) {
			Log.w(getTagName(tag), msg);
		}
	}
}
