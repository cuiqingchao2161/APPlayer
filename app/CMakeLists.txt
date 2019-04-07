cmake_minimum_required(VERSION 3.4.1)


set(my_lib_path ${CMAKE_SOURCE_DIR}/src/main/cpp/libs/${ANDROID_ABI})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path}")

file(GLOB SOURCE src/main/cpp/*.cpp)
add_library(
             native-lib
             SHARED
           ${SOURCE})

find_library(
              log-lib

              log )


include_directories(src/main/cpp/include)


target_link_libraries(
                       native-lib
avfilter avformat    avcodec  avutil  swresample swscale
                      # ffmpeg
                     z
                         OpenSLES
                         android
                       ${log-lib} )