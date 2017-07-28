#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG ("facet_id_check")
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

int LOG_ENABLE = 0;
const char *LOG = "LOG";

int check_facet_id(JNIEnv *, jstring);

jstring get_facet_id(JNIEnv *, jobject, jint);

void printLog(const char *);

extern "C"
jboolean
Java_com_yalin_style_data_utils_FacetIdUtil_checkCurrentFacetId__Landroid_content_Context_2I(
        JNIEnv *env, jclass, jobject context, jint uId) {
    jstring facet_id = get_facet_id(env, context, uId);;
    int check_result = check_facet_id(env, facet_id);
    bool result = check_result == 0;
    return (jboolean) result;
}

extern "C"
jstring
Java_com_yalin_style_data_utils_FacetIdUtil_getFacetId__Landroid_content_Context_2I(
        JNIEnv *env, jclass, jobject context, jint uId) {
    return get_facet_id(env, context, uId);
}

jstring get_facet_id(JNIEnv *env, jobject context, jint uId) {

    jclass contextClazz = env->FindClass("android/content/Context");
    jmethodID getPackageManagerMethodId = env->GetMethodID(contextClazz, "getPackageManager",
                                                           "()Landroid/content/pm/PackageManager;");
    jobject packageManager = env->CallObjectMethod(context, getPackageManagerMethodId);

    printLog("PackageManager obtained.");

    jclass packageManagerClazz = env->FindClass("android/content/pm/PackageManager");
    jmethodID getPackageForUidMethodId = env->GetMethodID(packageManagerClazz, "getPackagesForUid",
                                                          "(I)[Ljava/lang/String;");

    jobjectArray packageNames = (jobjectArray) (jarray) env->CallObjectMethod(packageManager,
                                                                              getPackageForUidMethodId,
                                                                              uId);
    jstring packageName = (jstring) env->GetObjectArrayElement(packageNames, 0);

    printLog("packageName obtained.");

    jmethodID getPackageInfoMethodId = env->GetMethodID(packageManagerClazz, "getPackageInfo",
                                                        "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jfieldID flag = env->GetStaticFieldID(packageManagerClazz, "GET_SIGNATURES", "I");

    jint signaturesFlag = env->GetStaticIntField(packageManagerClazz, flag);

    jobject packageInfo = env->CallObjectMethod(packageManager, getPackageInfoMethodId,
                                                packageName, signaturesFlag);

    printLog("packageInfo obtained.");

    jclass packageInfoClazz = env->FindClass("android/content/pm/PackageInfo");

    jfieldID signatureFieldId = env->GetFieldID(packageInfoClazz, "signatures",
                                                "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray) env->GetObjectField(packageInfo, signatureFieldId);

    printLog("signatures obtained.");

    jclass signatureClazz = env->FindClass("android/content/pm/Signature");
    jmethodID toByteArrayMethodId = env->GetMethodID(signatureClazz, "toByteArray", "()[B");

    jbyteArray cert = (jbyteArray) env->CallObjectMethod(env->GetObjectArrayElement(signatures, 0),
                                                         toByteArrayMethodId);

    printLog("cert obtained.");

    jclass inputStreamClazz = env->FindClass("java/io/ByteArrayInputStream");
    jmethodID inputStreamConstructorMethodId = env->GetMethodID(inputStreamClazz, "<init>",
                                                                "([B)V");
    jobject inputStream = env->NewObject(inputStreamClazz, inputStreamConstructorMethodId, cert);

    printLog("inputStream obtained.");

    jclass certificateFactoryClazz = env->FindClass("java/security/cert/CertificateFactory");
    jmethodID certificateFactoryGetInstanceMethodId = env->GetStaticMethodID(
            certificateFactoryClazz,
            "getInstance",
            "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");
    jstring x509String = env->NewStringUTF("X509");
    jobject certificateFactory = env->CallStaticObjectMethod(certificateFactoryClazz,
                                                             certificateFactoryGetInstanceMethodId,
                                                             x509String);

    printLog("certificateFactory obtained.");

    jmethodID generateCertificateMethodId = env->GetMethodID(certificateFactoryClazz,
                                                             "generateCertificate",
                                                             "(Ljava/io/InputStream;)Ljava/security/cert/Certificate;");
    jclass x509CertificateClazz = env->FindClass("java/security/cert/X509Certificate");

    jobject x509Certificate = env->CallObjectMethod(certificateFactory, generateCertificateMethodId,
                                                    inputStream);

    printLog("x509Certificate obtained.");

    jclass messageDigestClazz = env->FindClass("java/security/MessageDigest");
    jmethodID messageDigestInstanceMethodId = env->GetStaticMethodID(messageDigestClazz,
                                                                     "getInstance",
                                                                     "(Ljava/lang/String;)Ljava/security/MessageDigest;");

    jstring sha1 = env->NewStringUTF("SHA1");
    jobject messageDigest = env->CallStaticObjectMethod(messageDigestClazz,
                                                        messageDigestInstanceMethodId, sha1);

    printLog("messageDigest obtained.");

    jmethodID certificateEncodeMethodId = env->GetMethodID(x509CertificateClazz, "getEncoded",
                                                           "()[B");
    jbyteArray certEncode = (jbyteArray) env->CallObjectMethod(x509Certificate,
                                                               certificateEncodeMethodId);

    printLog("certEncode obtained.");

    jmethodID messageDigestMethodId = env->GetMethodID(messageDigestClazz, "digest", "([B)[B");
    jbyteArray digestArray = (jbyteArray) env->CallObjectMethod(messageDigest,
                                                                messageDigestMethodId,
                                                                certEncode);

    printLog("digestArray obtained.");

    jclass base64Clazz = env->FindClass("android/util/Base64");
    jmethodID encodeToStringMethodId = env->GetStaticMethodID(base64Clazz, "encodeToString",
                                                              "([BI)Ljava/lang/String;");

    jfieldID base64FlagFiledId = env->GetStaticFieldID(base64Clazz, "DEFAULT",
                                                       "I");
    jint base64DefaultFlag = env->GetStaticIntField(base64Clazz, base64FlagFiledId);

    jstring result = (jstring) env->CallStaticObjectMethod(base64Clazz, encodeToStringMethodId,
                                                           digestArray, base64DefaultFlag);

    printLog("result obtained.");

    env->DeleteLocalRef(packageManager);
    env->DeleteLocalRef(packageNames);
    env->DeleteLocalRef(packageInfo);
    env->DeleteLocalRef(signatures);
    env->DeleteLocalRef(cert);
    env->DeleteLocalRef(inputStream);
    env->DeleteLocalRef(certificateFactory);
    env->DeleteLocalRef(x509Certificate);
    env->DeleteLocalRef(messageDigest);
    env->DeleteLocalRef(certEncode);
    env->DeleteLocalRef(digestArray);

    return result;
}

int check_facet_id(JNIEnv *env, jstring facet_id) {
    // modify to your app's facet wallpaperId
    jstring valid_facet_id_string = env->NewStringUTF("unkown");

    const char *valid_facet_id = env->GetStringUTFChars(valid_facet_id_string, 0);
    char *target_facet_id = (char *) env->GetStringUTFChars(facet_id, 0);

//    target_facet_id[strcspn(target_facet_id, "\r\n")] = '\0';

    printLog(target_facet_id);
    printLog(valid_facet_id);

    int result = strcmp(target_facet_id, valid_facet_id);

    char tmp[128];
    sprintf(tmp, "check complete: result=%d", result);
    printLog(tmp);

    env->DeleteLocalRef(valid_facet_id_string);
    return result;
}

void printLog(const char *str) {
    if (LOG_ENABLE == 0) {
        return;
    }

    LOGI(str, LOG);
}