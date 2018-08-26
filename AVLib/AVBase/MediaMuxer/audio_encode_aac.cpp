/**
 */
#include "audio_encode_aac.h"
#include <pthread.h>

extern "C"
{
static void* AudioThreadProc( void* arg )
{
	AudioEncodeAAC *pThread = (AudioEncodeAAC *)arg;

	int dwRet = pThread->startEncode();
	pThread->m_dwExitCode = dwRet;

	return 0;
}
}

bool AudioEncodeAAC::StartThread()
{
	if (!m_thrd ){ 
		int nRet = pthread_create( &m_thrd,0,AudioThreadProc,this );
		if( nRet != 0 )
			return false;
	}
	return	m_thrd != 0;
}

void AudioEncodeAAC::StopThread()
{
	if ( m_thrd ){
		
		void *ignore = 0;
		pthread_join( m_thrd,&ignore );
		pthread_detach( m_thrd );
	}

	m_thrd=0;
}

AudioEncodeAAC::AudioEncodeAAC(UserArguments* arg){

}

/**
 * 刷出编码器里剩余帧
 * @param fmt_ctx
 * @param stream_index
 * @return
 */
int AudioEncodeAAC::flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_audio2(fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                    NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGI(1,"Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n", enc_pkt.size);
        /* mux encoded frame */
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }
    return ret;
}

/**
 * 初始化音频编码器
 * @return
 */
int AudioEncodeAAC::initAudioEncoder() {
    LOGI(1,"音频编码器初始化开始")
    size_t path_length = strlen(arguments->audio_path);
    char *out_file=( char *)malloc(path_length+1);

    strcpy(out_file, arguments->audio_path);

    av_register_all();

    //Method 1.
    pFormatCtx = avformat_alloc_context();
    fmt = av_guess_format(NULL, out_file, NULL);
    pFormatCtx->oformat = fmt;


    //Open output URL
    if (avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE(1,"Failed to open output file!\n");
        return -1;
    }
//    pFormatCtx->audio_codec_id=AV_CODEC_ID_AAC;

    audio_st = avformat_new_stream(pFormatCtx, 0);
    if (audio_st == NULL) {
        return -1;
    }
    pCodecCtx = audio_st->codec;
    pCodecCtx->codec_id = AV_CODEC_ID_AAC;
    pCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
    pCodecCtx->sample_rate = arguments->audio_sample_rate;
    pCodecCtx->channel_layout = AV_CH_LAYOUT_MONO;
    pCodecCtx->channels = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    pCodecCtx->bit_rate = arguments->audio_bit_rate;
//    pCodecCtx->thread_count = 1;
//    pCodecCtx->profile=FF_PROFILE_AAC_MAIN;

    int b= av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    LOGI(1,"channels:%d",b);

    //Show some information
    av_dump_format(pFormatCtx, 0, out_file, 1);
    pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if (!pCodec) {

        LOGE(1,"Can not find encoder!\n");
        return -1;
    }

//    AVDictionary *param = 0;
//
//    av_dict_set(&param, "profile", "aac_he", 0);

    int state = avcodec_open2(pCodecCtx, pCodec, NULL);
    if (state < 0) {
        LOGE(1,"Failed to open encoder!---%d",state);
        return -1;
    }
    pFrame = av_frame_alloc();
    pFrame->nb_samples = pCodecCtx->frame_size;
    pFrame->format = pCodecCtx->sample_fmt;

    size = av_samples_get_buffer_size(NULL, pCodecCtx->channels, pCodecCtx->frame_size,
                                      pCodecCtx->sample_fmt, 1);

    uint8_t *frame_buf = (uint8_t *) av_malloc(size);
    avcodec_fill_audio_frame(pFrame, pCodecCtx->channels, pCodecCtx->sample_fmt,
                             (const uint8_t *) frame_buf, size, 1);

    //Write Header
    avformat_write_header(pFormatCtx, NULL);

    av_new_packet(&pkt, size);
    is_end=START_STATE;
	
	StartThread();
	
    LOGI(1,"音频编码器初始化完成")
    return 0;

}

/**
 * 用户结束标记
 */
void AudioEncodeAAC::user_end(){
    is_end=END_STATE;
}

void AudioEncodeAAC::release(){
	StopThread();
    is_release=true;
	
}



/**
 * 发送一帧到编码队列
 * @param buf
 * @return
 */
void AudioEncodeAAC::sendAudioFrame(uint8_t* buf,int dataLen)
{

	EncData* pdata = new EncData();
	pdata->_data = new uint8_t[dataLen];
	memcpy(pdata->_data, buf, dataLen);
	pdata->_dataLen = dataLen;
	pdata->_bVideo = false;
	pdata->_type = AUDIO_DATA;
	//pdata->_dts = ts;
	WAutoLock l(&cs_list_enc_);
	lst_enc_data_.push_back(pdata);
}

/**
 * 编码结束操作
 * @return
 */
int AudioEncodeAAC::encodeEnd(){
    //Flush Encoder
    ret = flush_encoder(pFormatCtx, 0);
    if (ret < 0) {
        LOGE(1,"Flushing encoder failed\n");
        return -1;
    }

    //Write Trailer
    av_write_trailer(pFormatCtx);

    //Clean
    if (audio_st) {
        avcodec_close(audio_st->codec);
        av_free(pFrame);
//        av_free(frame_buf);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);
    LOGI(1,"音频编码完成")
    //arguments->handler->setup_audio_state(END_STATE);
    //arguments->handler->try_encode_over(arguments);

    return 0;
}



/**
 * 开启编码线程
 * @param obj
 * @return
 */
int AudioEncodeAAC::startEncode() {
    while (!is_end) {
        if(is_release){
            if (audio_st) {
                avcodec_close(audio_st->codec);
                av_free(pFrame);
            }
            avio_close(pFormatCtx->pb);
            avformat_free_context(pFormatCtx);
            return 0;
        }
		

		EncData* dataPtr = NULL;
		
		WAutoLock l(&cs_list_enc_);

	    if(lst_enc_data_.size() == 0)
	        continue;
			
		if (lst_enc_data_.size() > 0) {
			dataPtr = lst_enc_data_.front();
			lst_enc_data_.pop_front();
		}
		

		if (dataPtr != NULL ) {
			pFrame->data[0] = (unsigned char*)dataPtr->_data;
	        pFrame->pts = i ;
	        i++;
	        got_frame = 0;
	        //Encode
	        ret = avcodec_encode_audio2(pCodecCtx, &pkt, pFrame, &got_frame);
	        if (ret < 0) {
	            LOGE(1,"Failed to encode!\n");
	        }

	        if (got_frame == 1) {
	            LOGI(1,"Succeed to encode 1 frame! \tsize:%5d\n", pkt.size);
	            pkt.stream_index = audio_st->index;
	            ret = av_write_frame(pFormatCtx, &pkt);
	            av_free_packet(&pkt);
	        }
			
		}

		delete[] dataPtr->_data;
		delete dataPtr;
    }
    if (is_end) {
        encodeEnd();
    }
    return 0;
}
