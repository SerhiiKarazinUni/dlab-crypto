using namespace std;

#include <iostream>

struct biguint {

    uint64_t *data;
    size_t size;

    biguint() {
        data = new uint64_t[0];
        size = 0;
    }

    biguint(biguint *ref) {
        this->data = new uint64_t[ref->size];
        memcpy(this->data, ref->data, sizeof(uint64_t) * ref->size);
        this->size = ref->size;
    }

    biguint(const string hex) {
        setHex(hex);
    }

    biguint &setHex(const string hex) {
        if (hex.length() == 0) {
            data = new uint64_t[0];
            size = 0;
        } else {

            string _hex = string(hex);

            // in case of uneven length of the string -- add padding '0'
            if (hex.length() % 2 == 1) {
                _hex = '0' + hex;
            }

            //adjust internal buffer length to fit the number
            char sz = sizeof(uint64_t);
            this->size = ceil(_hex.length() / 2.0f / sz);
            this->data = new uint64_t[this->size + 1];

            //read each byte into a temporary buffer
            char *buffer = new char[sz];
            unsigned int bufPointer = 0;
            unsigned int portionPointer = 0;
            for (unsigned int i = 0; i < _hex.length(); i += 2) {
                string byteString = _hex.substr(i, 2);
                buffer[bufPointer] = (char) strtol(byteString.c_str(), nullptr, 16);
                bufPointer++;

                if (bufPointer == sz) {
                    //need to push current buffer to struct data buffer
                    memcpy(&(data[portionPointer]), buffer, sz);
                    memset(buffer, 0, sz);
                    portionPointer++;
                    bufPointer = 0;
                }
            }

            // if the temporary buffer is not empty in the end -- flush it into the data buffer
            if (bufPointer > 0) {
                memcpy(&(data[portionPointer]), buffer, sz);
            }
        }
        return *this;
    }

    void getHex(char *buf, size_t sz) const {
        size_t l = 0;
        for (size_t i = 0; i < this->size; i++) { // per each internal buffer portion...
            for (size_t k = 0; k < sizeof(uint64_t); k++) { // per each byte of the buffer portion...

                // prevent output buffer overflow
                if ((l * 2) >= sz) {
                    return;
                }

                unsigned char *byt = ((unsigned char *) &this->data[i]) + k;

                // prevent trailing 00s from printing
                if (i + 1 == this->size && k > 0 && *byt == 0) {
                    if (memcmp(byt, byt + 1, sizeof(uint64_t) - k - 1) == 0) {
                        return;
                    }
                }

                snprintf(buf + 2 * l, 3, "%.2x", *byt);
                l += 1;
            }
        }
    }
};


// thanks to:
// https://stackoverflow.com/questions/10367616/bitwise-shifting-array-of-chars
static void shift_bits(uint8_t *array, size_t len, int shift) {
    bool left = false;

    if (shift < 0) {
        left = true;
        shift *= -1;
    }

    uint8_t macro_shift = shift / 8;
    shift = shift % 8;

    uint8_t array_out[len];
    memset(array_out, 0, len);

    if (left) {
        for (int i = len - 1; i >= 0; i--) {
            if (i - macro_shift >= 0)
                array_out[i - macro_shift] += array[i] << shift;
            if (i - macro_shift - 1 >= 0)
                array_out[i - macro_shift - 1] += array[i] >> (8 - shift);
        }
    } else {
        for (int i = 0; i < len; i++) {
            if (i + macro_shift < len)
                array_out[i + macro_shift] += array[i] >> shift;
            if (i + macro_shift + 1 < len)
                array_out[i + macro_shift + 1] += array[i] << (8 - shift);
        }
    }

    memcpy(array, array_out, len);
}

biguint operator<<(biguint &src, const int shift) {
    auto result = biguint(&src);
    shift_bits((uint8_t *) result.data, result.size * sizeof(uint64_t), -1 * shift);
    return result;
}

biguint operator>>(biguint &src, const int shift) {
    auto result = biguint(&src);
    shift_bits((uint8_t *) result.data, result.size * sizeof(uint64_t), shift);
    return result;
}

ostream &operator<<(ostream &out, const biguint &num) {
    size_t sz = sizeof(uint64_t) * 2 * num.size;
    char *buffer = new char[sz + 1];
    memset(buffer, 0, sz + 1);

    num.getHex(buffer, sz);
    out << buffer;

    return out;
}

biguint operator~(biguint &num) {
    auto result = biguint(&num);

    for (size_t i = 0; i < result.size; i++) {
        result.data[i] = ~result.data[i];
    }

    return result;
}

biguint operator^(biguint &a, biguint &b) {
    biguint result;

    if (a.size > b.size) {
        result = biguint(&a);
    } else {
        result = biguint(&b);
    }

    uint64_t zeros = 0;

    for (size_t i = 0; i < result.size; i++) {
        if (i <= a.size && i <= b.size) {
            result.data[i] = a.data[i] ^ b.data[i];
        }
        if (i <= a.size && i > b.size) {
            result.data[i] = a.data[i] ^ zeros;
        }
        if (i > a.size && i <= b.size) {
            result.data[i] = zeros ^ b.data[i];
        }
    }

    return result;
}

biguint operator|(biguint &a, biguint &b) {
    biguint result;

    // copy the largest operand as result
    if (a.size > b.size) {
        result = biguint(&a);
    } else {
        result = biguint(&b);
    }

    uint64_t zeros = 0;

    for (size_t i = 0; i < result.size; i++) {
        if (i <= a.size && i <= b.size) {
            result.data[i] = a.data[i] | b.data[i];
        }
        if (i <= a.size && i > b.size) {
            result.data[i] = a.data[i] | zeros;
        }
        if (i > a.size && i <= b.size) {
            result.data[i] = zeros | b.data[i];
        }
    }

    return result;
}

biguint operator&(biguint &a, biguint &b) {
    //FIXME: refactor copy-paste from | operator

    biguint result;

    if (a.size > b.size) {
        result = biguint(&a);
    } else {
        result = biguint(&b);
    }

    uint64_t zeros = 0;

    for (size_t i = 0; i < result.size; i++) {
        if (i <= a.size && i <= b.size) {
            result.data[i] = a.data[i] & b.data[i];
        }
        if (i <= a.size && i > b.size) {
            result.data[i] = a.data[i] & zeros;
        }
        if (i > a.size && i <= b.size) {
            result.data[i] = zeros & b.data[i];
        }
    }

    return result;
}


int main() {
    biguint numberA = biguint();
    biguint numberB = biguint();

    // note: getHex() is under the hood of << operator
    cout << "setHex(), getHex():\n";
    cout << "numberA = FFFFFFFFFFFFFFFF0000000000000000: " << numberA.setHex("FFFFFFFFFFFFFFFF0000000000000000")
         << endl;
    cout << "numberB = FF0000FF00000000000000FF00000000: " << numberB.setHex("FF0000FF00000000000000FF00000000") << endl
         << endl;

    cout << "INV:\n";
    cout << "~numberA: " << (~numberA) << "\n";
    cout << "~numberB: " << (~numberB) << "\n\n";

    cout << "XOR:\n";
    cout << "numberA^numberB: " << (numberA ^ numberB) << "\n";
    cout << "should be      : 00ffff00ffffffff000000ff\n\n";

    cout << "OR:\n";
    cout << "numberA|numberB: " << (numberA | numberB) << "\n";
    cout << "should be      : ffffffffffffffff000000ff\n\n";

    cout << "AND:\n";
    cout << "numberA&numberB: " << (numberA & numberB) << "\n";
    cout << "should be      : ff0000ff\n\n";

    cout << "Bitwise shifting left and right 4 bits of numberA:\n";
    cout << "SRC: " << numberA << "\n";
    cout << "LFT: " << (numberA << 4) << "\n";
    cout << "RGT: " << (numberA >> 4) << "\n";
    return 0;
}
