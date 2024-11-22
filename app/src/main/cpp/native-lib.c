#include <jni.h>
#include <string.h>


void createSecurePreferencesNew(char *output, const char *packageName) {
    int iArr[] = {102, 76, 120, 89, 66, 57, 77, 56, 52, 65, 98, 101, 117, 115, 69, 82, 77, 89, 57, 89, 70, 122, 86, 71};
    int length = strlen(packageName);

    while (length-- > 0) {
        char c = ((packageName[length] ^ iArr[length % 24]) & 31) + 48;
        strncat(output, &c, 1);
    }
}

JNIEXPORT jstring JNICALL
Java_com_buta_hdagent_LoadLib_getA(JNIEnv *env, jclass clazz) {
    const char *initVector = "fldsjfodasjifuds";
    return (*env)->NewStringUTF(env, initVector);
}

JNIEXPORT jstring JNICALL
Java_com_buta_hdagent_LoadLib_getB(JNIEnv *env, jclass clazz) {
    const char *packageName = "com.supercell.hayday";
    char secureResult[1024] = {0};
    createSecurePreferencesNew(secureResult, packageName);
    return (*env)->NewStringUTF(env, secureResult);
}
