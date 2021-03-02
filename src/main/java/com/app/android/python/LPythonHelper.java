package com.app.android.python;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.srplab.www.starcore.StarCoreFactory;
import com.srplab.www.starcore.StarCoreFactoryPath;
import com.srplab.www.starcore.StarObjectClass;
import com.srplab.www.starcore.StarServiceClass;
import com.srplab.www.starcore.StarSrvGroupClass;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LPythonHelper {
    private volatile static LPythonHelper instance;

    private Context context;
    private boolean isDebug;

    public StarSrvGroupClass mSrvGroup;
    private StarObjectClass python;
    private StarCoreFactory mStarcore;
    private StarServiceClass mService;
    private String corePath;


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static void initPython(Context context, boolean isDebug) {
        if (instance == null) {
            synchronized (LPythonHelper.class) {
                if (instance == null) {
                    instance = new LPythonHelper(context, isDebug);
                }
            }
        }
    }

    public static LPythonHelper getInstance() {
        if (instance == null) {
            throw new Error("未初始化");
        }
        return instance;
    }


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public LPythonHelper(Context context, boolean isDebug) {
        this.context = context;
        this.isDebug = isDebug;

        corePath = "/data/data/" + context.getPackageName() + "/files";
        init();
    }


    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void init() {
        File destDir = new File(corePath);
        if (!destDir.exists())
            destDir.mkdirs();
        File python2_7_libFile = new File(corePath + "/python3.7.zip");
        if (!python2_7_libFile.exists()) {
            copyFile(context, "python3.7.zip");
        }

        //拷贝库文件
        copyFile(context, "_struct.cpython-37m.so");
        copyFile(context, "binascii.cpython-37m.so");
        copyFile(context, "zlib.cpython-37m.so");
        copyFile(context, "math.cpython-37m.so");
        copyFile(context, "cmath.cpython-37m.so");
        copyFile(context, "_sha512.cpython-37m.so");
        copyFile(context, "_random.cpython-37m.so");
        copyFile(context, "_md5.cpython-37m.so");
        copyFile(context, "_sha3.cpython-37m.so");
        copyFile(context, "_hashlib.cpython-37m.so");
        copyFile(context, "_datetime.cpython-37m.so");
        copyFile(context, "_blake2.cpython-37m.so");


        //加载assets中的python文件
        copyFile(context, "python_common_interface.py");


        //加载sd卡中的python文件
        copyFileFromSdcard(context, "");


        //so的加载路径
        String path = "/system/lib";
        if (isDebug) {
            path = context.getApplicationInfo().nativeLibraryDir;
        }

        try {
            //--load python34 core library first;
            System.load(path + "/libpython3.7m.so");
        } catch (UnsatisfiedLinkError ex) {
            System.out.println(ex.toString());
        }

        /*----init starcore----*/
        Log.i("LPythonHelper", "python加载插件的so文件路径 = " + path);
        StarCoreFactoryPath.StarCoreCoreLibraryPath = path;
        StarCoreFactoryPath.StarCoreShareLibraryPath = path;
        StarCoreFactoryPath.StarCoreOperationPath = corePath;

        mStarcore = StarCoreFactory.GetFactory();
        mStarcore._SRPLock();
        mSrvGroup = mStarcore._GetSrvGroup(0);
        mService = mSrvGroup._GetService("test", "123");
        if (mService == null) {
            mService = mStarcore._InitSimple("test", "123", 0, 0);
        } else {
            mService._CheckPassword(false);
        }

        /*----run python code----*/
        mSrvGroup._InitRaw("python37", mService);
        python = mService._ImportRawContext("python", "", false, "");
        python._Call("import", "sys");
        python._Call("import", "random");

        StarObjectClass pythonSys = python._GetObject("sys");
        StarObjectClass pythonPath = (StarObjectClass) pythonSys._Get("path");
        pythonPath._Call("insert", 0, corePath + "/python3.7.zip");
        pythonPath._Call("insert", 0, path);
        pythonPath._Call("insert", 0, corePath);

        //加载python文件
        mService._DoFile("python", corePath + "/python_common_interface.py", "");

        mStarcore._SRPUnLock();
    }

    private void copyFile(Context c, String Name) {
        try {
            File outfile = new File(corePath + "/" + Name);
            outfile.createNewFile();
            FileOutputStream out = new FileOutputStream(outfile);
            byte[] buffer = new byte[1024];
            InputStream in;
            int readLen = 0;

            in = c.getAssets().open(Name);
            while ((readLen = in.read(buffer)) != -1) {
                out.write(buffer, 0, readLen);
            }
            out.flush();
            in.close();
            out.close();
        } catch (Exception e) {
            Log.d("", e.getMessage());
        }
    }


    private void copyFileFromSdcard(Context c, String name) {
        if (TextUtils.isEmpty(name)) return;
        File outfile = new File(c.getFilesDir(), name);
        BufferedOutputStream outStream = null;
        BufferedInputStream inStream = null;
        InputStream redis = null;
        try {
            String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String filePath = sdpath + File.separator + name;
            redis = new FileInputStream(new File(filePath));
            outStream = new BufferedOutputStream(new FileOutputStream(outfile));
            inStream = new BufferedInputStream(redis);

            byte[] buffer = new byte[1024 * 10];
            int readLen = 0;
            while ((readLen = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public int offlinePlus(int v1, int v2) {
        if (python != null) {
            int result = python._Callint("testPlus", v1, v2);
            return result;
        }
        return 0;
    }

    public Object offlineGetStudyList(String studyDate, String learnCourses, String learnProgress, String knowledgesInCourse, int maxReviewCount) {
        if (python != null) {
            Object result = python._Call("get_study_list", studyDate, learnCourses, learnProgress, knowledgesInCourse, maxReviewCount);
            return result;
        }
        return null;
    }


    public Object offlineNextReviewLogs(String logs, String learnProgress, String knowledgeInfos) {
        if (python != null) {
            Object result = python._Call("schedule_next_review", logs, learnProgress, knowledgeInfos);
            return result;
        }
        return null;
    }
}
