package com.test.shellapp;

/**
 * Created by i on 2016/4/9.
 */
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class DynamicDexClassLoder extends DexClassLoader {

    private static final String TAG = "demo";
    private int cookie;
    private Context mContext;

    /**
     * @param dexBytes
     * @param libraryPath
     * @param parent
     * @throws Exception
     */

    public DynamicDexClassLoder(Context context, byte[] dexBytes,
                                String libraryPath, ClassLoader parent, String oriPath,
                                String fakePath) {
        super(oriPath, fakePath, libraryPath, parent);
        setContext(context);

        //打印DexFile类的所有方法
        try{
            Class clazz = DexFile.class;
            Method[] methods = clazz.getMethods();

            Log.i(TAG, "*******************************");
            for(Method method : methods){
                Log.i(TAG, "method:"+method.getName());
            }
            Log.i(TAG, "*******************************");

            Method[] methods1 = clazz.getDeclaredMethods();
            for(Method method : methods1){
                Log.i(TAG, "++++++++++++++++++++++++++");
                Log.i(TAG, "method:"+method.getName());
                Class returns = method.getReturnType();
                Log.i(TAG, "return:"+returns);
                Class[] parms = method.getParameterTypes();
                for(Class cla : parms){
                    Log.i(TAG, "parm:"+cla.getName());
                }
            }
            Log.i(TAG, "*******************************");
        }catch (Exception e){

        }

        int cookie = 0;//ShellTools.loadDex(dexBytes, dexBytes.length);

        //反射调用openDexFile方法获取dex对应的cookie值
        /*int cookie = (Integer) RefInvoke.invokeDeclaredStaticMethod(
                DexFile.class.getName(),
                "openDexFile",
                new Class[]{Object.class},
                new Object[]{dexBytes});*/

        Log.i(TAG, "cookie:"+cookie);

        setCookie(cookie);

    }

    private void setCookie(int kie) {
        cookie = kie;
    }

    private void setContext(Context context) {
        mContext = context;
    }

    private String[] getClassNameList(int cookie) {
        return (String[]) RefInvoke.invokeDeclaredStaticMethod(DexFile.class.getName(),
                "getClassNameList", new Class[]{int.class},
                new Object[]{cookie});
    }

    private Class defineClass(String name, ClassLoader loader, int cookie) {
        Log.i(TAG, "define class:"+name);
        return (Class) RefInvoke.invokeDeclaredStaticMethod(DexFile.class.getName(),
                "defineClassNative", new Class[]{String.class, ClassLoader.class,
                        int.class}, new Object[]{name, loader, cookie});
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.d(TAG, "findClass-" + name);
        Class<?> cls = null;
        String as[] = getClassNameList(cookie);
        for (int z = 0; z < as.length; z++) {
            Log.i(TAG, "classname:"+as[z]);
            if (as[z].equals(name)) {
                cls = defineClass(as[z].replace('.', '/'),
                        mContext.getClassLoader(), cookie);
            } else {
                //加载其他类
                defineClass(as[z].replace('.', '/'), mContext.getClassLoader(),
                        cookie);
            }
        }

        if (null == cls) {
            cls = super.findClass(name);
        }

        return cls;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        Log.d(TAG, "loadClass-" + className + " resolve " + resolve);
        Class<?> clazz = super.loadClass(className, resolve);
        if (null == clazz) {
            Log.e(TAG, "loadClass fail,maybe get a null-point exception.");
        }
        return clazz;
    }

}
