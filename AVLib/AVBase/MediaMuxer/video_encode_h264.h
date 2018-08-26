#ifndef _VIDEO_ENCODE_H264_H
#define _VIDEO_ENCODE_H264_H

#include "base.h"
#include "user_arguments.h"

/**
 * 编码h264
 */
class VideoEncodeH264 {
public:
    VideoEncodeH264(UserArguments* arg);
public:
    int initVideoEncoder();

    int startEncode();

    void sendVideoFrame(uint8_t* buf,int dataLen);
	
    bool StartThread();

    void StopThread();
 
    pthread_t   m_thrd = 0;

    int m_dwExitCode = 0;

    void user_end();

    void release();

    int encodeEnd();

    void custom_filter(const VideoEncodeH264 *h264_encoder, const uint8_t *picture_buf,
                       int in_y_size,
                       int format);
    ~VideoEncodeH264() {
    }
private:
    int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index);

private:
    UserArguments *arguments;
    int is_end = 0;
    int is_release = 0;
    WLock	cs_list_enc_;
    std::list<EncData*>		lst_enc_data_;
    AVFormatContext *pFormatCtx;
    AVOutputFormat *fmt;
    AVStream *video_st;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;
    AVPacket pkt;
    AVFrame *pFrame;
    int picture_size;
    int out_y_size;
    int framecnt = 0;
    int frame_count = 0;


};

#endif //_VIDEO_ENCODE_H264_H
