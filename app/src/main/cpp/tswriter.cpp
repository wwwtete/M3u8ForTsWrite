//
//  tswriter.cpp
//  Main
//
//  Created by dy on 2/10/17.
//  Copyright © 2017 feitian. All rights reserved.
//

#include <string>
#include <assert.h>
#include <string>
#include "tswriter.h"

static uint8_t flv_mpegts_header[] = {
    
#if 0
    /*  SDT */
    0x47, 0x40, 0x11, 0x10, 0x00, 0x42, 0xF0, 0x25, 0x00, 0x01, 0xC1,
    0x00, 0x00, 0xFF, 0x01, 0xFF, 0x00, 0x01, 0xFC, 0x80, 0x14, 0x48,
    0x12, 0x01, 0x06, 0x46, 0x46, 0x6D, 0x70, 0x65, 0x67, 0x09, 0x53,
    0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x30, 0x31, 0x77, 0x7C, 0x43,
    0xCA, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
    0xFF,
#endif
    
    /* PAT */
    /* TS */
    0x47, 0x40, 0x00, 0x10, 0x00,
    /* PSI */
    0x00, 0xb0, 0x0d, 0x00, 0x01, 0xc1, 0x00, 0x00,
    /* PAT */
    0x00, 0x01, 0xf0, 0x01,
    /* CRC */
    0x2e, 0x70, 0x19, 0x05,
    /* stuffing 167 bytes */
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    
    /* PMT PID == 0x1001 */
    /* TS */
    0x47, 0x50, 0x01, 0x10, 0x00,
    /* PSI */
    0x02, 0xb0, 0x1d, 0x00, 0x01, 0xc1, 0x00, 0x00,
    /* PMT */
    0xe1, 0x00,
    0xf0, 0x00,
    0x1b, 0xe1, 0x00, 0xf0, 0x00, /* h264 */
    0x0F, 0xE1, 0x01, 0xF0, 0x06, 0x0A, 0x04, 0x75, 0x6E, 0x64, 0x00, /* aac with lang descriptor from ffmpeg */
    /* CRC */
    0x08, 0x7D, 0xE8, 0x77, // from ffmpeg for new aac with land descriptor
    /* stuffing 151 bytes */
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff
};

struct flv_mpegts_frame_t {
    int64_t   pts;
    int64_t   dts;
    uint32_t   pid;
    uint32_t   sid;
    uint32_t   cc;
    unsigned    key;
};

struct str_buf_t {
    uint8_t* pos;
    uint8_t* last;
};

/* 700 ms PCR delay */
#define FLV_HLS_DELAY  45000

static void flv_mpegts_write_header(TSFileBuffer &fileBuffer)
{
    memcpy(fileBuffer.data + fileBuffer.ptr, flv_mpegts_header, sizeof(flv_mpegts_header));
    fileBuffer.ptr += sizeof(flv_mpegts_header);
}

static uint8_t* flv_mpegts_write_pcr(uint8_t *p, int64_t pcr)
{
    int64_t pcr_low = pcr % 300, pcr_high = pcr / 300;
    
    *p++ = pcr_high >> 25;
    *p++ = pcr_high >> 17;
    *p++ = pcr_high >>  9;
    *p++ = pcr_high >>  1;
    *p++ = pcr_high <<  7 | pcr_low >> 8 | 0x7e;
    *p++ = pcr_low;
    return p;
}

static uint8_t* flv_mpegts_write_pts(uint8_t *p, uint32_t fb, int64_t pts)
{
    uint32_t val;
    val = fb << 4 | (((pts >> 30) & 0x07) << 1) | 1;
    *p++ = (uint8_t) val;
    
    val = (((pts >> 15) & 0x7fff) << 1) | 1;
    *p++ = (uint8_t) (val >> 8);
    *p++ = (uint8_t) val;
    
    val = (((pts) & 0x7fff) << 1) | 1;
    *p++ = (uint8_t) (val >> 8);
    *p++ = (uint8_t) val;
    return p;
}

int flv_mpegts_write_frame(TSFileBuffer &file, flv_mpegts_frame_t *f, str_buf_t *b, int64_t tsbase)
{
    //printf("flv_mpegts_write_frame pid %d payloadsize %d pts %lld tsbase %lld\n", f->pid, (int)(b->last - b->pos), f->pts, tsbase);
    
    f->pts = f->pts - tsbase + FLV_HLS_DELAY * 2;
    f->dts = f->dts - tsbase + FLV_HLS_DELAY * 2;
    
    uint32_t pes_size, header_size, body_size, in_size, stuff_size, flags;
    uint8_t packet[188], *p, *base;
    
    int first = 1;
    while (b->pos < b->last) {
        // PAT and MPT must be inserted into ts file per 40 ts packets (copy from ffmpeg)
        // otherwise, Quictime doesn't work
        if (file.tspacknum == 40) {
            flv_mpegts_write_header(file);
            file.tspacknum = 0;
        }
        
        p = packet;
        
        f->cc++;
        
        *p++ = 0x47;
        *p++ = (uint8_t) (f->pid >> 8);
        
        if (first) {
            p[-1] |= 0x40;
        }
        
        *p++ = (uint8_t) f->pid;
        *p++ = 0x10 | (f->cc & 0x0f); /* payload */
        
        if (first) {
            if (f->key) {
                packet[3] |= 0x20; /* adaptation */
                
                *p++ = 7;    /* size */
                *p++ = 0x50; /* random access + PCR */
                
                // fix this, the file pcr start from 0 !!
               // printf("writing pcr %lld\n", (f->dts - FLV_HLS_DELAY) * 300);
                p = flv_mpegts_write_pcr(p, (f->dts - FLV_HLS_DELAY) * 300);
                
                // in fact, the pcr can be set to 0 in all time, the only problem will be that VLC can't seek
                //p = flv_mpegts_write_pcr(p, 0);///jyf?
            }
            
            /* PES header */
            *p++ = 0x00;
            *p++ = 0x00;
            *p++ = 0x01;
            *p++ = (uint8_t) f->sid;
            
            header_size = 5;
            flags = 0x80; /* PTS */
            
            if (f->dts != f->pts) {
                header_size += 5;
                flags |= 0x40; /* DTS */
            }
            
            pes_size = (b->last - b->pos) + header_size + 3;
            if (pes_size > 0xffff || f->pid == 0x100) {
                pes_size = 0;   // pes size == 0 for video stream
            }
            
            *p++ = (uint8_t) (pes_size >> 8);
            *p++ = (uint8_t) pes_size;
            *p++ = 0x80; /* H222 */
            *p++ = (uint8_t) flags;
            *p++ = (uint8_t) header_size;
            
            p = flv_mpegts_write_pts(p, flags >> 6, f->pts);
            if (f->dts != f->pts) {
                p = flv_mpegts_write_pts(p, 1, f->dts);
            }
            
            first = 0;
        }
        
        body_size = (u_int32_t) (packet + sizeof(packet) - p);
        in_size = (u_int32_t) (b->last - b->pos);
        
        if (body_size <= in_size) {
            memcpy(p, b->pos, body_size);
            b->pos += body_size;
        }
        else {
            stuff_size = (body_size - in_size);
            
            if (packet[3] & 0x20) {
                /* has adaptation */
                base = &packet[5] + packet[4];
                p = (u_int8_t*)memmove((void*)(base + stuff_size), (void*)base, p - base);
                memset(base, 0xff, stuff_size);
                packet[4] += (uint8_t)stuff_size;
                
            }
            else {
                /* no adaptation */
                packet[3] |= 0x20;
                int movecnt = (int)(p - &packet[4]);
                memmove((void*)(&packet[4] + stuff_size), (void*)&packet[4], movecnt);
                p = packet + 4 + stuff_size + movecnt;
                packet[4] = (uint8_t) (stuff_size - 1);
                if (stuff_size >= 2) {
                    packet[5] = 0;
                    memset(&packet[6], 0xff, stuff_size - 2);
                }
            }
            
            memcpy(p, b->pos, in_size);
            b->pos = b->last;
        }
        
        // append a ts packet to file
        memcpy(file.data + file.ptr, packet, sizeof(packet));
        file.ptr += sizeof(packet);
        file.tspacknum++;
    }
    
    return 0;
}
static int g_count = 0;
///////////////////////////////
TSWriter::TSWriter()
{
    memset(&_fileBuffer, 0, sizeof(_fileBuffer));
    _sps = _pps = NULL;
    _spsLength = _ppsLength = 0;
    _tsAudioNum = _tsVideoNum = 0;
    _audioCC = _videoCC = 0;
    _firstTS = _lastTS = 0;
    _baseTS = -1;
    _aacCacheTS = 0;
    _aacCachePtr = 0;
    
    pthread_mutex_init(&m_mutex, NULL);
    pthread_mutex_init(&m_mutex_audio, NULL);

    g_count ++;
    
//    m_file = NULL;
//    m_264file.clear();
}

TSWriter::~TSWriter()
{
    pthread_mutex_destroy(&m_mutex);
    pthread_mutex_destroy(&m_mutex_audio);

//    if(m_file){
//        fclose(m_file);
//        m_file = NULL;
//    }
}

void TSWriter::AddH264Data(const uint8_t *data, int length, H264FrameType ftype,
                           int64_t ts, TSFileBuffer &tsfile)
{
    pthread_mutex_lock(&m_mutex);
    
    memset(&tsfile, 0, sizeof(tsfile));
    
    if (ftype == H264FrameType::SPS || ftype == H264FrameType::PPS) {
        // save them
        if (ftype == H264FrameType::SPS) {
//            assert(_sps == NULL);
            _sps = new uint8_t[length];
            _spsLength = length;
            memcpy(_sps, data, length);
        }
        else {
//            assert(_pps == NULL);
            _pps = new uint8_t[length];
            _ppsLength = length;
            memcpy(_pps, data, length);
        }
        pthread_mutex_unlock(&m_mutex);
        return;
    }
    
    if (ftype == H264FrameType::I) {
        // complete the previous ts file, create the new ts file
        // if there is audio frames left in cache, flush it to ts file
        // combine the sps and pps with the first I frame
        if (_aacCachePtr > 0) {
            flv_mpegts_frame_t framet;
            framet.cc = _audioCC;
            framet.key = 0;
            framet.dts = (int64_t) _aacCacheTS * 90; // in 90KHz
            framet.pts = framet.dts;
            framet.pid = 0x101;
            framet.sid = 0xc0;
            
            str_buf_t buf = { 0 };
            buf.pos = (uint8_t*)_aacCache;
            buf.last = (uint8_t*)_aacCache + _aacCachePtr;
            flv_mpegts_write_frame(_fileBuffer, &framet, &buf, 0);
            
            _audioCC = framet.cc;
            _aacCachePtr = 0;
        }
        
        // returning the completed ts file content !
        tsfile = _fileBuffer;
        if (tsfile.data != NULL) {
            tsfile.duration = _lastTS - _firstTS;
        }
        
        // reset for new ts file
        memset(&_fileBuffer, 0, sizeof(_fileBuffer));
        
        _tsAudioNum = 0;
        _tsVideoNum = 0;
        _audioCC = 0;
        _videoCC = 0;
        _firstTS = ts;
        _lastTS = ts;
        
        if (_baseTS == -1) {
            // setting the base timestamp of the whole h264 sequence
            printf("h264 basets = %lld\n", _firstTS);
            _baseTS = _firstTS;
        }
    }
    else if (_tsVideoNum == 0) {
        // ignore the P frames before IDR
        pthread_mutex_unlock(&m_mutex);
        return;
    }
    
    _tsVideoNum++;
    
    if (ts > _lastTS) {
        _lastTS = ts;
    }
    
    if (_fileBuffer.data == NULL) {
        _fileBuffer.data = new uint8_t[1024 * 1024 * 10];   // big enough for testing !!
        _fileBuffer.size = 1024 * 1024 * 10;
        _fileBuffer.ptr = 0;
        _fileBuffer.duration = 0;
        _fileBuffer.tspacknum = 0;
        
        // set PAT and PMT at the beginning of new ts file
        flv_mpegts_write_header(_fileBuffer);
    }
    
    // must added at the beginning of all h264 frames (09 = Access Unit Delimiter, otherwise QuickTime Player can't play a ts file)
    const uint8_t audslice[] = { 0x00, 0x00, 0x00, 0x01, 0x09, 0xF0 };
    
    if (ftype == H264FrameType::I) {
        uint8_t *tmp = new uint8_t[_spsLength + _ppsLength + length + sizeof(audslice)];
        memcpy(tmp, audslice, sizeof(audslice));
        memcpy(tmp + sizeof(audslice), _sps, _spsLength);
        memcpy(tmp + sizeof(audslice) + _spsLength, _pps, _ppsLength);
        memcpy(tmp + sizeof(audslice) + _spsLength + _ppsLength, data, length);
        length = _spsLength + _ppsLength + length + sizeof(audslice);
        
        flv_mpegts_frame_t framet;
        framet.cc = _videoCC;
        framet.key = 1;
        framet.dts = (int64_t) ts * 90; // in 90KHz
        framet.pts = framet.dts;
        framet.pid = 0x100;
        framet.sid = 0xe0;
        
        str_buf_t buf = { 0 };
        buf.pos = tmp;
        buf.last = tmp + length;
        flv_mpegts_write_frame(_fileBuffer, &framet, &buf, 0);
        
        _videoCC = framet.cc;
        delete[] tmp;
    }
    else {
        uint8_t *tmp = new uint8_t[sizeof(audslice) + length];
        memcpy(tmp, audslice, sizeof(audslice));
        memcpy(tmp + sizeof(audslice), data, length);
        length = length + sizeof(audslice);
        
        flv_mpegts_frame_t framet;
        framet.cc = _videoCC;
        framet.key = 0;
        framet.dts = (int64_t) ts * 90; // in 90KHz
        framet.pts = framet.dts;
        framet.pid = 0x100;
        framet.sid = 0xe0;
        
        str_buf_t buf = { 0 };
        buf.pos = (uint8_t*)tmp;
        buf.last = (uint8_t*)tmp + length;
        flv_mpegts_write_frame(_fileBuffer, &framet, &buf, 0);
        
        _videoCC = framet.cc;
        delete[] tmp;
    }
    pthread_mutex_unlock(&m_mutex);
}

//
static int SamplingFrequencyIndex[] = {
    96000,
    88200,
    64000,
    48000,
    44100,
    32000,
    24000,
    22050,
    16000,
    12000,
    11025,
    8000 ,
    7350 ,
    0    ,
    0
};

int indexSampleRate(int index)
{
    int size = sizeof(SamplingFrequencyIndex)/sizeof(int);
    if(index < 0 || index >= size){
        return -1;
    }
    
    return SamplingFrequencyIndex[index];
}

void TSWriter::AddAACData(unsigned int sampleRate, unsigned int channels, const uint8_t *data, int length, int64_t ts)
{
    pthread_mutex_lock(&m_mutex);
//    printf("AddAACData length %d, ts %lld\n", length, ts);
    
    if (_tsVideoNum == 0) {
//        printf("aacdata ignored by videonum == 0\n");
        pthread_mutex_unlock(&m_mutex);
        return; // ignore
    }
    
    if (ts <_firstTS) {
//        printf("aacdata ignored by small firstts\n");
        pthread_mutex_unlock(&m_mutex);
        return;
    }
    
    if (ts > _lastTS) {
        _lastTS = ts;
    }
    
    
    //    {
    //        int adtsLength = 7;
    //        unsigned int fullLength = adtsLength + length;
    //        // fill in ADTS data
    //        char adtsHead[7] = {0};
    //        adtsHead[0] = (char)0xFF;	// 11111111  	= syncword
    //        adtsHead[1] = (char)0xF9;	// 1111 1 00 1  = syncword MPEG-2 Layer CRC
    //        adtsHead[2] = (char)(((profile-1)<<6) + (indexSampleRate(sampleRate)<<2) +(channels>>2));
    //        adtsHead[3] = (char)(((channels&3)<<6) + (fullLength>>11));
    //        adtsHead[4] = (char)((fullLength&0x7FF) >> 3);
    //        adtsHead[5] = (char)(((fullLength&7)<<5) + 0x1F);
    //        adtsHead[6] = (char)0xFC;
    //    }
    
    if (_aacCachePtr == 0) {
        memcpy(_aacCache, data, length);
        _aacCachePtr = length;
        _aacCacheTS = ts;
    }
    else {
        memcpy(_aacCache + _aacCachePtr, data, length);
        _aacCachePtr += length;
        
        if ((ts - _aacCacheTS) * 90 > FLV_HLS_DELAY) {
            // flush cached audio frames as a PES
            flv_mpegts_frame_t framet;
            framet.cc = _audioCC;
            framet.key = 0;
            framet.dts = (int64_t) _aacCacheTS * 90; // in 90KHz
            framet.pts = framet.dts;
            framet.pid = 0x101;
            framet.sid = 0xc0;
            
            str_buf_t buf = { 0 };
            buf.pos = (uint8_t*)_aacCache;
            buf.last = (uint8_t*)_aacCache + _aacCachePtr;
            flv_mpegts_write_frame(_fileBuffer, &framet, &buf, 0);
            
            _audioCC = framet.cc;
            _aacCachePtr = 0;
            _tsAudioNum++;
        }
    }
    pthread_mutex_unlock(&m_mutex);
}

void TSWriter::Close(TSFileBuffer &filebuffer)
{
    pthread_mutex_lock(&m_mutex);
    // flush the uncompleted ts file
    if (_aacCachePtr > 0) {
        flv_mpegts_frame_t framet;
        framet.cc = _audioCC;
        framet.key = 0;
        framet.dts = (int64_t) _aacCacheTS * 90; // in 90KHz
        framet.pts = framet.dts;
        framet.pid = 0x101;
        framet.sid = 0xc0;
        
        str_buf_t buf = { 0 };
        buf.pos = (uint8_t*)_aacCache;
        buf.last = (uint8_t*)_aacCache + _aacCachePtr;
        flv_mpegts_write_frame(_fileBuffer, &framet, &buf, 0);
        
        _audioCC = framet.cc;
        _aacCachePtr = 0;
    }
    
    if(_sps){
        delete [] _sps;
        _sps = NULL;
    }
    _spsLength = 0;
    if(_pps){
        delete [] _pps;
        _pps = NULL;
    }
    _ppsLength = 0;
    
    if (_fileBuffer.data != NULL && _fileBuffer.ptr > 0) {
        _fileBuffer.duration = _lastTS - _firstTS;
    }
    
    filebuffer = _fileBuffer;
    memset(&_fileBuffer, 0, sizeof(_fileBuffer));
    pthread_mutex_unlock(&m_mutex);
    
}


//void TSWriter::writeH264File(char * path, const uint8_t *data, int length)
//{
//        if(m_264file.empty()){
//            if(path){
//                char temp[200]{0};
//                sprintf(temp, "%s/264_%d.264", path, g_count);
//                m_264file = temp;
//            }
//        }
//    
//        if(! m_264file.empty()){
//            if(m_file == NULL){
//                m_file = fopen(m_264file.c_str(), "wb");
//            }
//        }
//    
//        if(m_file){
//            fwrite(data, 1, length, m_file);
//            fflush(m_file);
//        }
//    
//        printf("AddH264Data 1111111111");
    
//}


//void TSWriter::closeH264File()
//{
//        if(m_file){
//            fflush(m_file);
//            fclose(m_file);
//            m_file = NULL;
//        }
//        m_264file.clear();

//}


