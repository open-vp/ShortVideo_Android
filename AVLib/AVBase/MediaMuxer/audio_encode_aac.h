#ifndef OPENVP_AUDIO_ENCODE_AAC_H
#define OPENVP_AUDIO_ENCODE_AAC_H

#include "wlock.h"
#include "base.h"
#include "user_arguments.h"

using namespace OPENVP;

/**
 * pcm编码为aac
 */
class AudioEncodeAAC {
public:
    AudioEncodeAAC(UserArguments* arg);
public:
    int initAudioEncoder();

    int startEncode();
	
    bool StartThread();

    void StopThread();
    
    
    pthread_t   m_thrd = 0;
    int m_dwExitCode = 0;

    void user_end();

    void release();

    void sendAudioFrame(uint8_t* buf,int dataLen);

    int encodeEnd();
    ~AudioEncodeAAC() {
    }

private:
    int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index);

private:
    WLock	cs_list_enc_;
    std::list<EncData*>		lst_enc_data_;
    //CMediaBufferPool		m_AudioBufferPool;
	
    AVFormatContext *pFormatCtx;
    AVOutputFormat *fmt;
    AVStream *audio_st;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;

    AVFrame *pFrame;
    AVPacket pkt;

    int got_frame = 0;
    int ret = 0;
    int size = 0;

    int i;
    int is_end=false;
    int is_release=false;
    UserArguments *arguments;

};

#endif //OPENVP_AUDIO_ENCODE_AAC_H
