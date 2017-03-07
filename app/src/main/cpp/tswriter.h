//
//  tswriter.hpp
//  Main
//
//  Created by dy on 2/10/17.
//  Copyright © 2017 feitian. All rights reserved.
//

#ifndef tswriter_h
#define tswriter_h

#include <stdio.h>
#include <stdint.h>

// a ts file content
struct TSFileBuffer {
    uint8_t *data;
    int ptr;    // ts file content length
    int size;
    int64_t duration;   // in ms
    int tspacknum;  // used internal
};

class TSWriter {
public:
    enum class H264FrameType {
        SPS,
        PPS,
        I,
        P
    };
    
    
    TSWriter();
    virtual ~TSWriter();
    
    // sps, pps, IDR, P, P .... with start code 0001
    // return:
    // if tsfile.data != nullptr, a ts file content returned
    void AddH264Data(const uint8_t *data, int length, H264FrameType ftype, int64_t ts, TSFileBuffer &tsfile);
    
    // aac frame with adts header
    void AddAACData(const uint8_t *data, int length, int samplerate, int channum, int64_t ts);
    
    void Close(TSFileBuffer &tsfile);
    
private:
    
    TSFileBuffer _fileBuffer;
    
    uint8_t *_sps;
    uint8_t *_pps;
    int _spsLength;
    int _ppsLength;
    
    int _tsVideoNum;
    int _tsAudioNum;
    uint32_t _audioCC;
    uint32_t _videoCC;
    int64_t _firstTS;   // the first video ts
    int64_t _lastTS;
    int64_t _baseTS;
    
    int64_t _aacCacheTS;
    uint8_t _aacCache[1024 * 1024];
    int _aacCachePtr;
};

#endif /* tswriter_hpp */
