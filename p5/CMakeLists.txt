cmake_minimum_required(VERSION 3.26)
project(p5)

set(CMAKE_CXX_STANDARD 17)

include_directories(BEFORE /usr/local/Cellar/openssl@3/3.1.4/include)
find_library(OPENSSL_LIB ssl PATHS /usr/local/Cellar/openssl@3/3.1.4/lib NO_DEFAULT_PATH)
find_library(CRYPTO_LIB crypto PATHS /usr/local/Cellar/openssl@3/3.1.4/lib NO_DEFAULT_PATH)

find_package(OpenSSL REQUIRED)

add_executable(p5 main.cpp)
target_link_libraries(p5 OpenSSL::Crypto)