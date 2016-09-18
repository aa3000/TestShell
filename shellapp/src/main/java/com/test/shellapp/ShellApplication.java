package com.test.shellapp;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import com.test.dshell.DShell;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dalvik.system.DexClassLoader;

/**
 * Created by Liam on 2016/6/2.
 */
public class ShellApplication extends Application {

    static {
        DShell.init();
    }

    private static final String appkey = "APPLICATION_CLASS_NAME";

    private byte[] inputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;

    }

    public static void byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void inputStreamToFile(InputStream ins, String filePath){
        try {
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            Log.i("demo", "error:"+Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    @TargetApi(19)
    private void injectDexClassLoader(Context context, InputStream in){

        Object currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[] {}, new Object[] {});
        String packageName = getPackageName();
        Object mPackages = RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mPackages");
        WeakReference refToLoadedApk = null;
        if (mPackages instanceof HashMap) {
            Log.i("demo", "mPackages is HashMap");
            refToLoadedApk = (WeakReference) ((HashMap)mPackages).get(packageName);
        }else if (mPackages instanceof ArrayMap) {
            Log.i("demo", "mPackages is ArrayMap");
            refToLoadedApk = (WeakReference) ((ArrayMap) mPackages).get(packageName);
        }

        File odex = this.getDir("payload_odex", MODE_PRIVATE);
        File libs = this.getDir("payload_lib", MODE_PRIVATE);
        String odexPath = odex.getAbsolutePath();
        String libPath = libs.getAbsolutePath();

        //byte2File(dexContent, libPath, "classes.dex");
        String dexFilePath = libPath + File.separator + "classes.dex";
        inputStreamToFile(in, dexFilePath);

        class MyDexClassLoader extends DexClassLoader {
            public MyDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
                super(dexPath, optimizedDirectory, libraryPath, parent);
            }

            public Class<?> findClass(String name) throws ClassNotFoundException {
                return super.findClass(name);
            }

            public String findLibrary(String libName) {
                String ret = (String)RefInvoke.invokeMethod(
                        "dalvik.system.PathClassLoader", "findLibrary", getParent(),
                        new Class[]{String.class},
                        new Object[]{libName});
                return ret;
            }
        }

        if (refToLoadedApk == null)
        {
            Log.i("demo", "error: android.app.LoadedApk not found!");
            return;
        }

        ClassLoader clzLoader = (ClassLoader) RefInvoke.getFieldOjbect("android.app.LoadedApk", refToLoadedApk.get(), "mClassLoader");
        MyDexClassLoader dLoader = new MyDexClassLoader(dexFilePath, odexPath, null, clzLoader);

        RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader", refToLoadedApk.get(), dLoader);

        Log.i("demo", "inject finished!");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        //TurboDex.enableTurboDex();

        Log.i("demo", this.getClass().getName() + " attachBaseContext");

        try {

            //创建两个文件夹payload_odex，payload_lib 私有的，可写的文件目录

            InputStream in = getResources().getAssets().open("classes.dex");

            injectDexClassLoader(base, in);

            // 配置动态加载环境
/*            Object currentActivityThread = RefInvoke.invokeStaticMethod(
                    "android.app.ActivityThread", "currentActivityThread",
                    new Class[] {}, new Object[] {});//获取主线程对象 http://blog.csdn.net/myarrow/article/details/14223493
            String packageName = this.getPackageName();//当前apk的包名
            //下面两句不是太理解
            Object mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mPackages");
            WeakReference wr = null;
            if (mPackages instanceof ArrayMap) {
                Log.i("demo", "mPackages is ArrayMap");
                wr = (WeakReference) ((ArrayMap) mPackages).get(packageName);
            } else if (mPackages instanceof HashMap) {
                Log.i("demo", "mPackages is HashMap");
                wr = (WeakReference) ((HashMap)mPackages).get(packageName);
            }

            Log.i("demo", "mPackages class : " + mPackages.getClass());

            Log.i("demo", "wr:" + wr);

            if (wr != null) {
                //创建被加壳apk的DexClassLoader对象  加载apk内的类和本地代码（c/c++代码）
                DexClassLoader dLoader = new DexClassLoader(apkFileName, odexPath,
                        libPath, (ClassLoader) RefInvoke.getFieldOjbect(
                        "android.app.LoadedApk", wr.get(), "mClassLoader"));
                //base.getClassLoader(); 是不是就等同于 (ClassLoader) RefInvoke.getFieldOjbect()? 有空验证下//?
                //把当前进程的DexClassLoader 设置成了被加壳apk的DexClassLoader  ----有点c++中进程环境的意思~~
                RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader",
                        wr.get(), dLoader);

                Log.i("demo", "classloader:" + dLoader);
            }

            Log.i("demo", "classloader: null");*/

        } catch (Exception e) {
            Log.i("demo", "error:"+Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    @Override
    @TargetApi(19)
    public void onCreate() {
        {
            //loadResources(apkFileName);
            super.onCreate();

            Log.i("demo", "ShellApplication onCreate");
            // 如果源应用配置有Appliction对象，则替换为源应用Applicaiton，以便不影响源程序逻辑。
            String appClassName = null;
            try {
                ApplicationInfo ai = this.getPackageManager()
                        .getApplicationInfo(this.getPackageName(),
                                PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                if (bundle != null && bundle.containsKey("APPLICATION_CLASS_NAME")) {
                    appClassName = bundle.getString("APPLICATION_CLASS_NAME");//className 是配置在xml文件中的。
                } else {
                    Log.i("demo", "have no application class name");
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.i("demo", "error:"+Log.getStackTraceString(e));
                e.printStackTrace();
            }
            //有值的话调用该Applicaiton
            Object currentActivityThread = RefInvoke.invokeStaticMethod(
                    "android.app.ActivityThread", "currentActivityThread",
                    new Class[] {}, new Object[] {});
            Object mBoundApplication = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mBoundApplication");
            Object loadedApkInfo = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread$AppBindData",
                    mBoundApplication, "info");
            //把当前进程的mApplication 设置成了null
            RefInvoke.setFieldOjbect("android.app.LoadedApk", "mApplication",
                    loadedApkInfo, null);
            Application oldApplication = (Application)RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mInitialApplication");
            //http://www.codeceo.com/article/android-context.html
            ArrayList<Application> mAllApplications = cast(RefInvoke
                    .getFieldOjbect("android.app.ActivityThread",
                            currentActivityThread, "mAllApplications"));
            mAllApplications.remove(oldApplication);//删除oldApplication

            ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvoke
                    .getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
                            "mApplicationInfo");
            ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvoke
                    .getFieldOjbect("android.app.ActivityThread$AppBindData",
                            mBoundApplication, "appInfo");
            if (appinfo_In_LoadedApk != null) {
                appinfo_In_LoadedApk.className = appClassName;
            }
            if (appinfo_In_AppBindData != null) {
                appinfo_In_AppBindData.className = appClassName;
            }
            Application app = (Application) RefInvoke.invokeMethod(
                    "android.app.LoadedApk", "makeApplication", loadedApkInfo,
                    new Class[]{boolean.class, Instrumentation.class},
                    new Object[]{false, null});//执行 makeApplication（false,null）
            if (app != null) {
                RefInvoke.setFieldOjbect("android.app.ActivityThread",
                        "mInitialApplication", currentActivityThread, app);

                Object mProviderMap = RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread", currentActivityThread,
                        "mProviderMap");
                if (mProviderMap != null) {
                    Iterator it = null;
                    if (mProviderMap instanceof HashMap) {
                        it = ((HashMap) mProviderMap).values().iterator();
                    } else if (mProviderMap instanceof ArrayMap) {
                        it = ((ArrayMap) mProviderMap).values().iterator();
                    }  else {
                        Log.i(this.getClass().getName(), "mProviderMap class is : " + mProviderMap.getClass().getName());
                    }
                    while (it != null && it.hasNext()) {
                        Object providerClientRecord = it.next();
                        Object localProvider = RefInvoke.getFieldOjbect(
                                "android.app.ActivityThread$ProviderClientRecord",
                                providerClientRecord, "mLocalProvider");
                        RefInvoke.setFieldOjbect("android.content.ContentProvider",
                                "mContext", localProvider, app);
                    }
                }
                Log.i("demo", "app:" + app);

                app.onCreate();
            }
        }
    }

    @Override
    public AssetManager getAssets() {
        Log.i("demo", "getAssets");
        return super.getAssets();
    }

    @Override
    public Resources getResources() {
        Log.i("demo", "getResources");
        return super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        Log.i("demo", "getTheme");
        return super.getTheme();
    }
}
