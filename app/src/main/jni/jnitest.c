#include "com_test_testshell_JniUtils.h"
/*
 * Class:     io_github_yanbober_ndkapplication_NdkJniUtils
 * Method:    getCLanguageString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_test_testshell_JniUtils_getCLanguageString
        (JNIEnv *env, jobject obj){
    int a = 0;
    const char * retStr = "";
    if (++a == 1)
    {
        retStr = "This just a test for Android Studio NDK JNI developer!";
    }
    return (*env)->NewStringUTF(env, retStr);
}