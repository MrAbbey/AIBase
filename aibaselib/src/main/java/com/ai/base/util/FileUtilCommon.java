package com.ai.base.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileUtilCommon {
	/**
	 * get file name from the path
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath){
		if(filePath==null || filePath.length()==0)
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}
	
	/**
	 * get file name without suffix from the path
	 * @param filePath
	 * @return
	 */
	public static String getFileNameWithoutSuffix(String filePath){
		String fileName = getFileName(filePath);
		
		if(filePath == null || filePath.length() == 0)
			return "";
		
		int index = fileName.lastIndexOf(".");
		if(index ==-1 || index==0){
			return fileName;
		}else{
			return fileName.substring(0, index);
		}
	}

	public static String getFileSuffix(String filePath){
		String filePath_trim = filePath.trim();
		 int index = filePath_trim.lastIndexOf(".");
		 if(index ==-1 || index==0){
			 return "";
		 }else {
			 return filePath.substring(index+1);
		 }

	}
	
	/**
	 * get the directory full path which contains the file
	 * @param filePath
	 * @return directory full path(not include File.separator at end)
	 */
	public static String getParentDirectoryPath(String filePath){
		if(filePath==null || filePath.length()==0)
			return "";
		
		File file = new File(filePath);
		if(file.isDirectory()){
			filePath = file.getAbsolutePath();
		}
		return filePath.substring(0,filePath.lastIndexOf(File.separator));
	}
	
	/**
	 * delete all files in folder(not include the folder)
	 * @param directory absolute full path
	 */
	public static void deleteAllFilesInFolder(String directory){
		File dir = new File(directory);
		if(dir.isDirectory()){
			File[] files = dir.listFiles();
			for(int i=0,size=files.length;i<size;i++){
				if(files[i].isDirectory()){
					deleteDirectory(files[i].getAbsolutePath());
				}else{
					files[i].delete();
				}
			}
		}
	}
	
	/**
	 * delete the directory including all files and directories in it
	 * @param directory
	 */
	public static void deleteDirectory(String directory){
		File dir = new File(directory);
		if(dir.isDirectory()){
			File[] files = dir.listFiles();
			for(int i=0,size=files.length;i<size;i++){
				if(files[i].isDirectory()){
					deleteDirectory(files[i].getAbsolutePath());
				}else{
					files[i].delete();
				}
			}
			
			dir.delete();
		}
	}
	
	/**
	 * create directory if no exist
	 * @param dir
	 * @return
	 */
	public static boolean createDirectoryIfNotExist(String dir){
		File file = new File(dir);
		return file.mkdirs();
	}

    public static String readTxtFromAsset(Context context, String path) {
        StringBuilder sb = new StringBuilder();
        InputStreamReader read = null;
        try {
            read = new InputStreamReader(context.getAssets().open(path));
            BufferedReader br = new BufferedReader(read);
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            read.close();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

	public static String readTxt(String filePath) {
		StringBuilder sb = new StringBuilder();
		InputStreamReader read = null;
		try {  
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {  
               read = new InputStreamReader(new FileInputStream(file));
               BufferedReader br = new BufferedReader(read);
               String line = null;
               while ((line = br.readLine()) != null) {  
                   sb.append(line);
                   sb.append("\n");
               }
               
               read.close();  
            }  
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
        	if (read != null) {
        		try {
					read.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
        	}
        }
		
		return sb.toString();
	}
	
	public static void writeTxt(String txt, String filePath) {
		writeTxt(txt, filePath, false);
	}
	
	public static void writeTxt(String txt, String filePath, boolean append) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filePath, append);
			fw.write(txt,0,txt.length());    
			fw.flush(); 
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean renameTo(String oldPath, String newPath, boolean relpaceIfExist) {
		File oldFile = new File(oldPath);
		if (!oldFile.exists()) {
			return false;
		}
		
		File newFile = new File(newPath);
		if (newFile.exists() && relpaceIfExist) {
			if (newFile.isFile() && oldFile.isFile()) {
				newFile.delete();
			} else if (newFile.isDirectory() && oldFile.isDirectory()) {
				deleteDirectory(newPath);
			}
		}
		
		return oldFile.renameTo(newFile);
		
	}
	
	public static boolean renameTo(String oldPath, String newPath) {
		return renameTo(oldPath, newPath, false);
	}
	
	public static boolean deleteFileIfExist(String path) {
		File file = new File(path);
		if (file.isFile()) {
			return file.delete();
		}
		
		return false;
	}
	
	public static boolean writeByte2File(String path, String fileName, byte[] data, String encoding){
		try {
			File folder = new File(path);
			if(!folder.exists()){
				folder.mkdirs();
			} 
			File file = new File(path + "/" + fileName);
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
		    fos.write(data);
		    fos.close();
			fos = null;
		    return true;
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (NullPointerException e){
			//e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isFileOrFolderExist(String path){
		File file=new File(path);
		if (file.exists())
			return true;
		else 
			return false;
	}
	
	public static String getExternalSDRoot() {
		//in some devices, sd card path
		String[] pathPossible = new String[]{"/mnt/external_sd/", "/mnt/extSdCard/", "/storage/external_sd/", "/storage/extSdCard/", "/storage/ext_sd/"};
		for (String path : pathPossible) {
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				try {
					return new File(path).getCanonicalPath();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
		
		Map<String, String> evn = System.getenv();
		
		return evn.get("SECONDARY_STORAGE");
	}
	
	public static String getCameraAlbumPath() {
		File phoneAlbum = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (phoneAlbum != null) {
			return phoneAlbum.getAbsolutePath();
		} else {
			return Environment.getExternalStoragePublicDirectory(null).getAbsolutePath() + "/" + "DCIM";
		}
	}
	
	public static List<File> getCameraDirectory() {
		List<File> list = new ArrayList<File>();
		String dcimDir;
		File phoneAlbum = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (phoneAlbum != null) {
			list.add(phoneAlbum);
			dcimDir = phoneAlbum.getName();
		} else {
			dcimDir = "DCIM";
			File file = new File(Environment.getExternalStoragePublicDirectory(null).getAbsolutePath() + "/" + dcimDir);
			if (file.exists()) {
				list.add(file);
			}
		}
		
		String sdRootPath = getExternalSDRoot();
		if (sdRootPath != null) {
			File file = new File(sdRootPath + "/" + dcimDir);
			if (file.exists()) {
				list.add(file);
			}
		}
		
		return list;
	}
	
	public static boolean isInCameraPath(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		List<File> cameraDirs = getCameraDirectory();
				
		for (File dir : cameraDirs) {
			if (path.toLowerCase().startsWith(dir.getAbsolutePath().toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	

	

	
	public static boolean copyFile(File fromFile, File toFile) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			if(!toFile.exists()){
				toFile.createNewFile();
			}else{
				toFile.delete();
				toFile.createNewFile();
			}
			fis = new FileInputStream(fromFile);
			fos = new FileOutputStream(toFile);
			int bytesRead;
			byte[] buf = new byte[4 * 1024]; // 4K buffer
			while ((bytesRead = fis.read(buf)) != -1) {
				fos.write(buf, 0, bytesRead);
			}
			fos.flush();
		} catch (IOException e) {
			return false;
		}finally{
			try {
				fos.close();
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return true;
	} 
	
	
	
	
	
	
	
		/**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
    
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
