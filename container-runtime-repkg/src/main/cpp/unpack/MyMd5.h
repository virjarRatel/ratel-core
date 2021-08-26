#pragma once

#include <stdint.h>
#include <string>

/* typedef a 32 bit type */
//typedef unsigned long int UINT4;
//原md5库中使用了typedef unsigned long int UINT4声明了32位类型，实际在64位手机中long int为64位，所以产生了差异。
typedef uint32_t UINT4;
/* Data structure for MD5 (Message Digest) computation */
typedef struct {
    UINT4 i[2];                   /* number of _bits_ handled mod 2^64 */
    UINT4 buf[4];                                    /* scratch buffer */
    unsigned char in[64];                              /* input buffer */
    unsigned char digest[16];     /* actual digest after MD5Final call */
} MD5_CTX;

void MD5Init(MD5_CTX *mdContext);
void MD5Update(MD5_CTX *mdContext, unsigned char *inBuf, unsigned int inLen);
void MD5Final(MD5_CTX *mdContext);
static void Transform(UINT4 *buf, UINT4 *in);
char *MD5_file(char *path, int md5_len);
std::string md5(const std::string& input);