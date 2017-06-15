package com.ai.base.SourceManager.utils.assets;

import java.io.InputStream;

/**
 * Created by baggio on 2017/6/12.
 */

public interface IAssetsFileOperation {
    void fileDo(InputStream var1, String var2) throws Exception;

    boolean fileFliter(String var1) throws Exception;
}
