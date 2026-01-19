cmake_minimum_required(VERSION 3.27)
project(HW2)

set(CMAKE_CXX_STANDARD 20)

add_subdirectory(lib)

add_subdirectory(src)

enable_testing()

add_subdirectory(tests)

