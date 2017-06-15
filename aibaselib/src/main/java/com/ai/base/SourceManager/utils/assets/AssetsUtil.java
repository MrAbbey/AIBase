package com.ai.base.SourceManager.utils.assets;

import android.app.Activity;
import android.content.Context;

import com.ai.base.SourceManager.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by baggio on 2017/6/12.
 */

public class AssetsUtil {
    private static final String TAG = AssetsUtil.class.getSimpleName();

    public AssetsUtil() {
    }

    public static void traversal(Activity context, String assetDir, IAssetsFileOperation fileOper) throws Exception {
        (new AssetsRecursion(context, fileOper)).recursion(assetDir);
    }

    public static void copyAssetsDir(Context context, String assetDir, final String targetDir) throws Exception {
        File targetFile = new File(targetDir);
        if(!targetFile.exists() && !targetFile.mkdirs()) {
        }

        (new AssetsRecursion(context, new IAssetsFileOperation() {
            public boolean fileFliter(String name) throws Exception {
                return true;
            }

            public void fileDo(InputStream is, String filePath) throws Exception {
                File outFile = new File(targetDir, filePath);
                filePath = outFile.getCanonicalPath();
                String dirPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
                FileUtil.createDir(dirPath);
                FileOutputStream fos = new FileOutputStream(outFile);

                try {
                    byte[] buf = new byte[1024];

                    int len;
                    while((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }

                } finally {
                    fos.close();
                }
            }
        })).recursion(assetDir);
    }

    public static void copyAssetsFile(Context context, String assetsFile, String file) throws Exception {
        InputStream in = context.getAssets().open(assetsFile, 3);
        FileUtil.writeFile(in, file);
    }
}