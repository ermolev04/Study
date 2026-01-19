cmake_minimum_required(VERSION 3.10)
project(MyProject)

# Установка стандарта языка
set(CMAKE_C_STANDARD 11)

# Поиск библиотеки ffmpeg
find_package(FFmpeg REQUIRED)

# Указание пути к библиотеке fftw
set(FFTW_LIBRARY_PATH "library/fftw")

# Указание пути к заголовочным файлам fftw
set(FFTW_INCLUDE_PATH "library/fftw/fftw3.h")

# Настройка пути к заголовочным файлам ffmpeg
include_directories(${FFMPEG_INCLUDE_DIRS})

# Настройка пути к заголовочным файлам fftw
include_directories(${FFTW_INCLUDE_PATH})

# Настройка пути к библиотекам
link_directories(${FFTW_LIBRARY_PATH})

# Создание исполняемого файла
add_executable(MyProject main.c
        main/decode.c
        main/decode.h
        main/cross_cor.c
        main/cross_cor.h
        main/tst.cpp)

# Линковка с библиотеками
target_link_libraries(MyProject ${FFMPEG_LIBRARIES} fftw3)