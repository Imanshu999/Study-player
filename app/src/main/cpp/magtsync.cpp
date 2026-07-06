#include <jni.h>

// यह एक खाली फ़ंक्शन है ताकि लाइब्रेरी बिना किसी एरर के कंपाइल हो सके
extern "C" JNIEXPORT jint JNICALL
Java_com_aistudio_studycontroller_pvkqrx_NativeLib_init(JNIEnv* env, jobject thiz) {
    return 0; // सिर्फ 0 रिटर्न करेगा ताकि ऐप आगे बढ़ सके
}
