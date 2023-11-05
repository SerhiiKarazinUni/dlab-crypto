#include <iostream>
#include <stdio.h>
#include <string.h>
#include <openssl/sha.h>
#include <chrono>

// implementation mostly inspired by https://github.com/B-Con/crypto-algorithms

#define SHA1_BLOCK_SIZE 20
#define ROTLEFT(a, b) ((a << b) | (a >> (32 - b)))
using BYTE = unsigned char;
using WORD = unsigned int;
using SHA1_CTX = struct {
    BYTE data[64];
    WORD datalen;
    unsigned long long bitlen;
    WORD state[5];
    WORD k[4];
};

void sha1_transform(SHA1_CTX *ctx, const BYTE data[])
{
    WORD a, b, c, d, e, i, j, t, m[80];

    for (i = 0, j = 0; i < 16; ++i, j += 4)
        m[i] = (data[j] << 24) + (data[j + 1] << 16) + (data[j + 2] << 8) + (data[j + 3]);
    for ( ; i < 80; ++i) {
        m[i] = (m[i - 3] ^ m[i - 8] ^ m[i - 14] ^ m[i - 16]);
        m[i] = (m[i] << 1) | (m[i] >> 31);
    }

    a = ctx->state[0];
    b = ctx->state[1];
    c = ctx->state[2];
    d = ctx->state[3];
    e = ctx->state[4];

    for (i = 0; i < 20; ++i) {
        t = ROTLEFT(a, 5) + ((b & c) ^ (~b & d)) + e + ctx->k[0] + m[i];
        e = d;
        d = c;
        c = ROTLEFT(b, 30);
        b = a;
        a = t;
    }
    for ( ; i < 40; ++i) {
        t = ROTLEFT(a, 5) + (b ^ c ^ d) + e + ctx->k[1] + m[i];
        e = d;
        d = c;
        c = ROTLEFT(b, 30);
        b = a;
        a = t;
    }
    for ( ; i < 60; ++i) {
        t = ROTLEFT(a, 5) + ((b & c) ^ (b & d) ^ (c & d))  + e + ctx->k[2] + m[i];
        e = d;
        d = c;
        c = ROTLEFT(b, 30);
        b = a;
        a = t;
    }
    for ( ; i < 80; ++i) {
        t = ROTLEFT(a, 5) + (b ^ c ^ d) + e + ctx->k[3] + m[i];
        e = d;
        d = c;
        c = ROTLEFT(b, 30);
        b = a;
        a = t;
    }

    ctx->state[0] += a;
    ctx->state[1] += b;
    ctx->state[2] += c;
    ctx->state[3] += d;
    ctx->state[4] += e;
}

void sha1_init(SHA1_CTX *ctx)
{
    ctx->datalen = 0;
    ctx->bitlen = 0;
    ctx->state[0] = 0x67452301;
    ctx->state[1] = 0xEFCDAB89;
    ctx->state[2] = 0x98BADCFE;
    ctx->state[3] = 0x10325476;
    ctx->state[4] = 0xc3d2e1f0;
    ctx->k[0] = 0x5a827999;
    ctx->k[1] = 0x6ed9eba1;
    ctx->k[2] = 0x8f1bbcdc;
    ctx->k[3] = 0xca62c1d6;
}

void sha1_update(SHA1_CTX *ctx, const BYTE data[], size_t len)
{
    for (size_t i = 0; i < len; ++i) {
        ctx->data[ctx->datalen] = data[i];
        ctx->datalen++;
        if (ctx->datalen == 64) {
            sha1_transform(ctx, ctx->data);
            ctx->bitlen += 512;
            ctx->datalen = 0;
        }
    }
}

void mine_SHA1(const unsigned char *input, size_t sz, BYTE *hash)
{
    SHA1_CTX context = {};
    SHA1_CTX* ctx = &context;
    WORD i = 0;

    sha1_init(ctx);
    sha1_update(ctx, input, sz);

    i = ctx->datalen;

    // Pad whatever data is left in the buffer.
    if (ctx->datalen < 56) {
        ctx->data[i++] = 0x80;
        while (i < 56)
            ctx->data[i++] = 0x00;
    }
    else {
        ctx->data[i++] = 0x80;
        while (i < 64)
            ctx->data[i++] = 0x00;
        sha1_transform(ctx, ctx->data);
        memset(ctx->data, 0, 56);
    }

    // Append to the padding the total message's length in bits and transform.
    ctx->bitlen += ctx->datalen * 8;
    ctx->data[63] = ctx->bitlen;
    ctx->data[62] = ctx->bitlen >> 8;
    ctx->data[61] = ctx->bitlen >> 16;
    ctx->data[60] = ctx->bitlen >> 24;
    ctx->data[59] = ctx->bitlen >> 32;
    ctx->data[58] = ctx->bitlen >> 40;
    ctx->data[57] = ctx->bitlen >> 48;
    ctx->data[56] = ctx->bitlen >> 56;
    sha1_transform(ctx, ctx->data);

    // Since this implementation uses little endian byte ordering and MD uses big endian,
    // reverse all the bytes when copying the final state to the output hash.
    for (i = 0; i < 4; ++i) {
        hash[i]      = (ctx->state[0] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 4]  = (ctx->state[1] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 8]  = (ctx->state[2] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 12] = (ctx->state[3] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 16] = (ctx->state[4] >> (24 - i * 8)) & 0x000000ff;
    }
}

int main(int argc, char *argv[]) {
    unsigned char* ibuf;
    unsigned char obuf[20];
    memset(obuf, 0x00 , 20);

    // check if we're called for test mode
    bool testMode = false;
    bool mineSha = false;
    if(argc > 1){
        if(memcmp("1", argv[1], 1) == 0){
            testMode = true;
            mineSha = false;
        }
        if(memcmp("2", argv[1], 1) == 0){
            testMode = true;
            mineSha = true;
        }
    }

    // there may be file name given
    if (argc == 3) {
        if(FILE *f = fopen(argv[2], "rb")) {
            fseek(f, 0, SEEK_END);
            long fsize = ftell(f);
            fseek(f, 0, SEEK_SET);

            ibuf = (unsigned char *) malloc(fsize + 1);
            fread(ibuf, fsize, 1, f);
            fclose(f);
        }else{
            printf("file not found\n");
            return 0;
        }
        printf("INFO: using file contents as input. Read %zu bytes\n\n", strlen((char *) ibuf));
    }else{
        ibuf = static_cast<unsigned char *>(malloc(11));
        memcpy(ibuf, "Hello world", 11);
        printf("INFO: using 'Hello world' as input\n\n");
    }

    if(!testMode) {
        //1. basic implementation
        printf("=== [ 1. Smoke test ] ===\n");
        printf("Input data length: %zu\n", strlen((char *) ibuf));
        //mine implementation
        printf("SHA1 hash using mine   : ");
        mine_SHA1(ibuf, strlen((char *) ibuf), obuf);
        for (uint16_t i = 0; i < 20; i++) {
            printf("%02x", obuf[i]);
        }
        printf("\n");

        //openssl's implementation
        printf("SHA1 hash using OpenSSL: ");
        SHA1(ibuf, strlen((char *) ibuf), obuf);
        for (uint16_t i = 0; i < 20; i++) {
            printf("%02x", obuf[i]);
        }
        printf("\n");

        printf("\n=== [ 2. Different input lengths test ] ===\n");
        const char* inputs[5] = {
                "a", // 1 byte, less than 1 block
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",  // 64 bytes, = 1 block
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 65 bytes, more than 1 block
                // 128 bytes, = 2 blocks
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                // 129 bytes, more than 2 blocks
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        };

        const unsigned char expected[] = {
                0x86, 0xf7, 0xe4, 0x37, 0xfa, 0xa5, 0xa7, 0xfc, 0xe1, 0x5d, 0x1d, 0xdc, 0xb9, 0xea, 0xea, 0xea, 0x37, 0x76, 0x67, 0xb8,
                0x00, 0x98, 0xba, 0x82, 0x4b, 0x5c, 0x16, 0x42, 0x7b, 0xd7, 0xa1, 0x12, 0x2a, 0x5a, 0x44, 0x2a, 0x25, 0xec, 0x64, 0x4d,
                0x11, 0x65, 0x53, 0x26, 0xc7, 0x08, 0xd7, 0x03, 0x19, 0xbe, 0x26, 0x10, 0xe8, 0xa5, 0x7d, 0x9a, 0x5b, 0x95, 0x9d, 0x3b,
                0xad, 0x5b, 0x3f, 0xdb, 0xcb, 0x52, 0x67, 0x78, 0xc2, 0x83, 0x9d, 0x2f, 0x15, 0x1e, 0xa7, 0x53, 0x99, 0x5e, 0x26, 0xa0,
                0xd9, 0x6d, 0xeb, 0xf1, 0xbd, 0xcb, 0xc8, 0x96, 0xe6, 0xc1, 0x34, 0xea, 0x76, 0xe8, 0x14, 0x1f, 0x40, 0xd7, 0x85, 0x36
        };

        unsigned char obuf2[20];
        for(uint16_t i =0;i<5;i++){
            printf("%zu byte input ", strlen((char *) inputs[i]));

            mine_SHA1((unsigned char *)inputs[i], strlen((char *) inputs[i]), obuf);
            SHA1((unsigned char *)inputs[i], strlen((char *) inputs[i]), obuf2);
            if(memcmp(obuf, obuf2, 20) == 0 && memcmp(obuf, &(expected[20*i]), 20) == 0){
                printf("PASSED\n");
            }else{
                printf("NOT PASSED\n");
            }
        }
    }

    //2. speed test
    if(!testMode){
        printf("\n=== [ 3. Speed tests ] ===\n");
    }

    if(!mineSha || !testMode) {
        //openssl
        auto t1 = std::chrono::high_resolution_clock::now();
        for (uint32_t i = 0; i < 100000; i++) {
            SHA1(ibuf, strlen((char *) ibuf), obuf);
        }
        auto t2 = std::chrono::high_resolution_clock::now();
        std::chrono::duration<double, std::milli> ms_double = t2 - t1;
        std::cout << "100k executions of OpenSSL's SHA-1 took: " << ms_double.count() << "ms\n";
    }

    if(mineSha || !testMode) {
        //mine
        auto t1 = std::chrono::high_resolution_clock::now();
        for (uint32_t i = 0; i < 100000; i++) {
            mine_SHA1(ibuf, strlen((char *) ibuf), obuf);
        }
        auto t2 = std::chrono::high_resolution_clock::now();
        std::chrono::duration<double, std::milli> ms_double = t2 - t1;
        std::cout << "100k executions of mine SHA-1 took: " << ms_double.count() << "ms\n";
    }

    free(ibuf);

    return 0;
}
