/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class atst_giss_abplc_ABPlcioMaster */

#ifndef _Included_atst_giss_abplc_ABPlcioMaster
#define _Included_atst_giss_abplc_ABPlcioMaster
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     atst_giss_abplc_ABPlcioMaster
 * Method:    plc_open
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_atst_giss_abplc_ABPlcioMaster_plc_1open
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     atst_giss_abplc_ABPlcioMaster
 * Method:    plc_close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_atst_giss_abplc_ABPlcioMaster_plc_1close
  (JNIEnv *, jclass, jint);

/*
 * Class:     atst_giss_abplc_ABPlcioMaster
 * Method:    plc_read
 * Signature: (ILjava/lang/String;IILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_atst_giss_abplc_ABPlcioMaster_plc_1read
  (JNIEnv *, jclass, jint, jstring, jint, jint, jstring, jint);

/*
 * Class:     atst_giss_abplc_ABPlcioMaster
 * Method:    plc_write
 * Signature: (ILjava/lang/String;[BIILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_atst_giss_abplc_ABPlcioMaster_plc_1write
  (JNIEnv *, jclass, jint, jstring, jbyteArray, jint, jint, jstring);

/*
 * Class:     atst_giss_abplc_ABPlcioMaster
 * Method:    plc_validaddr
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_atst_giss_abplc_ABPlcioMaster_plc_1validaddr
  (JNIEnv *, jobject, jint, jstring);

#ifdef __cplusplus
}
#endif
#endif