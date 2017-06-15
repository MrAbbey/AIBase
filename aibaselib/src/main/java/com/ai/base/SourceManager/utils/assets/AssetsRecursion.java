package com.ai.base.SourceManager.utils.assets;

import android.content.Context;

import java.io.File;
import java.io.InputStream;

/** * Created by song on 2017/6/12.
 */

public class AssetsRecursion {
    private Context context;
    private IAssetsFileOperation fileOper;
    private String separator;
    private String baseDir;

    public AssetsRecursion(Context context, IAssetsFileOperation fileOper) {
        this.context = context;
        this.fileOper = fileOper;
        this.separator = File.separator;
    }

    public void recursion(String baseDir) throws Exception {
        this.baseDir = baseDir;
        this._recursion(baseDir);
    }

    private void _recursion(String assetDir) throws Exception {
        String[] fileLists = this.context.getAssets().list(assetDir);
        String[] arr$ = fileLists;
        int len$ = fileLists.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String fileName = arr$[i$];
            String filePath = assetDir + this.separator + fileName;
            if(!fileName.contains(".")) {
                this._recursion(filePath);
            } else if(this.fileOper.fileFliter(fileName)) {
                InputStream inputStream = this.context.getAssets().open(filePath);

                try {
                    this.fileOper.fileDo(inputStream, filePath.replace(this.baseDir + this.separator, ""));
                } finally {
                    if(inputStream != null) {
                        inputStream.close();
                    }

                }
            }
        }

    }
}
