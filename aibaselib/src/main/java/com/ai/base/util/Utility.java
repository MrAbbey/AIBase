package com.ai.base.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class Utility {
	
	/**
	 * encode
	 * @param str
	 */
	public static String encode(String str) {
		byte[] bsrc = str.getBytes();
		int len = bsrc.length;
		byte[] bdest = new byte[len];
		for (int i=len-1; i>=0; i--) {
			bdest[len-i-1] = (byte) (bsrc[i]+1);
		}
		return new String(bdest);
	}

	/**
	 * error
	 * @param message
	 */
	public static void error(String message) {
		throw new RuntimeException(message);
	}
	
	/**
	 * error
	 * @param throwable
	 * @param throwable
	 */
	public static void error(Throwable throwable) {
		throw new RuntimeException(throwable);
	}
	
	/**
	 * error
	 * @param message
	 * @param throwable
	 * @param throwable
	 */
	public static void error(String message, Throwable throwable) {
		throw new RuntimeException(message, throwable);
	}
	
	/**
	 * get bottom exception
	 * @param exception
	 * @return Throwable
	 */
	public static Throwable getBottomException(Throwable exception) {
		if (exception == null) return null;
		if (exception.getCause() != null) {
			exception = exception.getCause();
			return getBottomException(exception);
		}
		return exception;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 获取屏幕的px Size
	 * @param context
	 * @return
	 */
	public static Point getScreenMetrics(Context context){
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		return new Point(w_screen,h_screen);
	}

	/**
	 * 屏幕高与宽的比例
	 * @param context
	 * @return
	 */
	public static float getScreenRate(Context context){
		Point s = getScreenMetrics(context);
		float H = s.y;
		float W = s.x;
		return (H/W);
	}


	/**
	 * Bitmap旋转
	 * @param b
	 * @param rotateDegree
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float)rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}

	/**
	 * 保存图片为JPEG
	 * @param b
	 * @param path 图片的全路径,包含文件名和后缀
     */
	public static void saveBitmap(Bitmap b, String path){
		int start = path.lastIndexOf(".");

		start += 1;
		if (start < path.length() ) {
			try {
				FileOutputStream fout = new FileOutputStream(path);
				BufferedOutputStream bos = new BufferedOutputStream(fout);

				String suffix = path.substring(start);
				if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("jpeg")) {
					b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				} else if (suffix.equalsIgnoreCase("png")) {
					b.compress(Bitmap.CompressFormat.PNG, 100, bos);
				} else {
					// 默认就用jpeg保存
					b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				}

				bos.flush();
				bos.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}

	/**
	 * md5
	 * @param string
	 * @return
     */
	public static String md5(String string) {
		if (TextUtils.isEmpty(string)) {
			return "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(string.getBytes());
			String result = "";
			for (byte b : bytes) {
				String temp = Integer.toHexString(b & 0xff);
				if (temp.length() == 1) {
					temp = "0" + temp;
				}
				result += temp;
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
		}
		return "";
	}

	public static String generateZH_string() {
		String s = "湖南亚信软件有限公司陕西移动CRM支撑团队";
		char[] cs = s.toCharArray();
		int length = s.length();

		Random random = new Random();
		String zh_string = new String();
		for(int i = 0; i < 10; i++) {
			int index = random.nextInt(length);
			zh_string += cs[index];
		}

		return zh_string;
	}

	public static String generateGUID(){
		UUID uuid = UUID.randomUUID();
		String uuidString = uuid.toString();
		return uuidString;
	}

	/**
	 * 判断assets文件夹下的文件是否存在
	 *
	 * @return false 不存在    true 存在
	 */
	public static boolean isFileExists(Context context,String filename) {
		AssetManager assetManager = context.getResources().getAssets();
		try {
			String[] names = assetManager.list("");
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(filename.trim())) {
					return true;
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		return false;
	}
}