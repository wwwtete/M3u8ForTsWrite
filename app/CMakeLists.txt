cmake_minimum_required(VERSION 3.4.1)

add_library( tswirte
             SHARED
             src/main/cpp/TsWriteJNI.cpp
             src/main/cpp/tswriter.cpp)

add_library( tsmuxer
             SHARED
             src/main/cpp/TsmuxerJNI.cpp
             src/main/cpp/tswriter.cpp)

find_library( log-lib
              log)



target_link_libraries(tswirte
                        ${log-lib})
target_link_libraries(tsmuxer
                        ${log-lib})