#include <iostream>

// s-box
static const uint8_t s_box[16] = {
        0x0C, //00->0C
        0x06, //01->06
        0x0E, //02->0E
        0x04, //...
        0x05,
        0x0D,
        0x02,
        0x07,
        0x03,
        0x01,
        0x0F,
        0x08,
        0x0B,
        0x09,
        0x00, //0E->00
        0x0A  //0F->0A
};

static const uint8_t s_box_inv[16] = {
        0x0E, //00->0E
        0x09, //01->09
        0x06, //02->06
        0x08, //...
        0x03,
        0x04,
        0x01,
        0x07,
        0x0B,
        0x0D,
        0x0F,
        0x0C,
        0x00,
        0x05,
        0x02, //0E->02
        0x0A  //0F->0A
};

uint8_t apply_sbox(const uint8_t *input, const uint8_t *box) {
    // вхідний блок даних розбивається на дві тетради по 4 біти
    uint8_t tetrad1 = 0x00;
    uint8_t tetrad2 = 0x00;
    memcpy(&tetrad1, input, 1);
    memcpy(&tetrad2, input, 1);
    tetrad1 = tetrad1 & ~0xF;
    tetrad2 = tetrad2 & ~0xF0;
    // done

    // need to shift R first 4 bits of tetrad1 as we'll loop through 4 LSB
    tetrad1 = tetrad1 >> 4;

    for (uint8_t i = 0; i < 16; i++) {
        if ((tetrad1 & 0x0F) == (i & 0x0F)) {
            tetrad1 = box[i];
            break;
        }
    }

    for (uint8_t i = 0; i < 16; i++) {
        if ((tetrad2 & 0x0F) == (i & 0x0F)) {
            tetrad2 = box[i];
            break;
        }
    }

    // now combine two tetrades back, the tetrad1 should be shifted L back
    return (tetrad1 << 4) | tetrad2;
}

// p-box
static const uint8_t p_box[8] = { 3, 5, 2, 4, 0, 1, 7, 6 };

// thanks to http://graphics.stanford.edu/~seander/bithacks.html#SwappingBitsXOR
void swapBits(const uint8_t *source, uint8_t *result, const uint8_t i, const uint8_t j){
    unsigned int n = 1; // number of consecutive bits in each sequence

    unsigned int x = ((*source >> i) ^ (*source >> j)) & ((1U << n) - 1); // XOR temporary
    char r = *source ^ ((x << i) | (x << j));
    memcpy(result, &r, 1);
}

uint8_t apply_pbox(const uint8_t *input, const uint8_t *box) {
    uint8_t result = 0x00;
    memcpy(&result, input, 1);

    for (uint8_t i = 0; i < 8; i++) {
        // i means current bit
        swapBits(&result, &result, i, box[i]);
    }

    return result;
}

// utils

void getHex(char *out, const unsigned char *in, size_t sz) {
    size_t l = 0;
    for (size_t i = 0; i < sz; i++) { // per each byte...
            // prevent output buffer overflow
            if ((l * 4) >= sz) {
                return;
            }
            snprintf(out+l*4, 5, "0x%.2X", *(in+i));
            l += 1;
    }
}

int main() {
    uint8_t input = 0xA8; // you may change input here
    uint8_t output = 0x00;

    auto* buffer = new char[5];

    getHex(buffer, &input, 1);
    std::cout << "Input: " << buffer << "\n";

    // s-box
    output = apply_sbox(&input, (uint8_t *) &s_box);
    getHex(buffer, &output, 1);
    std::cout << "S-Box applied: " << buffer << "\n";
    std::cout << "Expected val.: " << "0xF3" << "\n\n";

    output = apply_sbox(&output, (uint8_t *) &s_box_inv);
    getHex(buffer, &output, 1);
    std::cout << "S-Box inverted: " << buffer << "\n";
    std::cout << "Expected val. : " << "0xA8" << "\n\n";

    // p-box
    output = apply_pbox(&input, (uint8_t *) &p_box);
    getHex(buffer, &output, 1);
    std::cout << "P-Box applied: " << buffer << "\n";
    std::cout << "Expected val.: " << "0xB0" << "\n\n";

    output = apply_pbox(&output, (uint8_t *) &p_box);
    getHex(buffer, &output, 1);
    std::cout << "P-Box inverted: " << buffer << "\n";
    std::cout << "Expected val. : " << "0xA8" << "\n\n";

    delete[] buffer;

    return 0;
}
