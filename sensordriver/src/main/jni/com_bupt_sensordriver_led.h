/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_bupt_sensordriver_led */

#ifndef _Included_com_bupt_sensordriver_led
#define _Included_com_bupt_sensordriver_led
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_bupt_sensordriver_led
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Open
  (JNIEnv *, jobject);

/*
 * Class:     com_bupt_sensordriver_led
 * Method:    Close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Close
  (JNIEnv *, jobject);

/*
 * Class:     com_bupt_sensordriver_led
 * Method:    Ioctl
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_led_Ioctl
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif