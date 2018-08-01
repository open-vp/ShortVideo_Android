
/**
 */
#ifndef _VIDEO_ENCODE_H264_H
#define _VIDEO_ENCODE_H264_H



#include "base.h"

//#include "user_arguments.h"

using namespace std;
/**
 * 编码h264
 */
class VideoEncodeH264 {
public:
    VideoEncodeH264(UserArguments* arg);
public:
    int initVideoEncoder();

    static void* startEncode(void * obj);

    int startSendOneFrame(uint8_t *buf);

    void user_end();

    void release();

    int encodeEnd();

    void custom_filter(const YUVEncodeH264 *h264_encoder, const uint8_t *picture_buf,
                       int in_y_size,
                       int format);
    ~YUVEncodeH264() {
    }
private:
    int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index);

private:
    //UserArguments *arguments;
    int is_end = 0;
    int is_release = 0;
    threadsafe_queue<uint8_t *> frame_queue;
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
