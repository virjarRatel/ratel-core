//
// Created by 邓维佳 on 2019-08-08.
//

#ifndef RATEL2_RATELLICENCE_H
#define RATEL2_RATELLICENCE_H


#include <jni.h>

#define AuthorizeTypeDebug 0x0001u
#define AuthorizeTypePerson 0x0002u
#define AuthorizeTypeQunar 0x0004u
#define AuthorizeTypeTest 0x0008u
#define AuthorizeTypeMiniGroup 0x0010u
#define AuthorizeTypeMediumGroup 0x0020u
#define AuthorizeTypeBigGroup 0x0040u
#define AuthorizeTypeSlave 0x0080u
#define AuthorizeTypeSelfExplosion 0x0100u
#define AuthorizeTypeB 0x0200u
#define AuthorizeTypeC 0x0400u
#define AuthorizeTypeD 0x0800u
#define AuthorizeTypeE 0x1000u
#define AuthorizeTypeF 0x2000u
#define AuthorizeTypeG 0x4000u
#define AuthorizeTypeH 0x8000u

extern int hasParseCertificate;
extern int hasCheckLicence;
extern int hasCallRatelDecryptFunction;


#define hasParseCertificateFlag 4547657
#define hasCheckLicenceFlag 9828908
#define hasCallRatelDecryptFunctionFlag 7525340


#define LicenceCheckPassed 0
#define LicenceCheckExpired 1
#define LicenceCheckPackageNotAllow 2
#define LicenceCheckDeviceNotAllow 3
#define LicenceNotCheck 4

extern int licenceStatus;

class RatelLicence {
public:
    //证书id，每个证书有一个唯一id
    char *licenceId;
    //证书版本，用于用户升级使用
    int32_t licenceVersion = 0;
    //证书协议版本，如果未来我们的协议被破解了，那么通过这个字段标记是否为废弃的证书
    int32_t licenceProtocolVersion = 0;
    //证书有效期
    int64_t expire = 0;
    //证书类型，
    unsigned int licenceType;
    //账户，一个用户可以拥有多个证书
    char *account = nullptr;
    //授权应用列表
    char **packageList = nullptr;
    int32_t packageListLength = 0;
    //授权设备列表
    int32_t deviceListLength = 0;
    char **deviceList;

    char *extra = nullptr;
    char *the_data = nullptr;

    bool isLicenceValid = false;

    RatelLicence(char *data, size_t data_length);

    ~RatelLicence();

    bool isDebug();
};


void loadRatelLicence(JNIEnv *env, jobject context, jstring originPkgName);

extern RatelLicence *ratelLicenceGlobal;


/**
 * ratel扩展base64算法
 * @param in_data 密文
 * @param in_data_length 密文长度
 * @param output 解密输出空间
 * @param outputLength 解密明文长度（由于解密明文是二进制，而非字符串。无法使用'\0'作为字符串自然终止符）
 * @return 是否解密成功
 */
char *
RatelBase64Decode(const char *in_data, size_t in_data_length, size_t &outputLength);

/**
 * ratel扩展des算法,请注意考虑密码会写入到客户端，使用会被逆向者发现，所以密码直接写入算法内部
 * @param cipher_text 密文
 * @param cipher_text_length 密文长度
 * @param output 解密输出空间
 * @param output_length  解密明文长度，一般比密文短一点点，考虑存在padding问题（8字节对齐）
 *
 */
signed char *
RatelDESDecrypt(const signed char *cipher_text, size_t cipher_text_length,
                size_t &output_length);

signed char *RatelRSADecrypt(const char *cipher_text, size_t cipher_text_length,
                             size_t &output_length);

char *RatelDecrypt(char *cipher_text, size_t cipher_text_length, size_t &outputLength);


bool registerAuthorizeMethod(JNIEnv *env, const char *authorizeClassChars);


#endif //RATEL2_RATELLICENCE_H
