/** 
 */

#ifndef _BASE_H
#define _BASE_H

extern "C"
{
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavcodec/avcodec.h"
}

#include <jni.h>
#include <string>
#include <stdlib.h>
#include <unistd.h>
#include <list>
#include <android/log.h>

#include "wlock.h"
//#include "wbufferpool.h"



using namespace std;
using namespace OPENVP;



#define LOGE(debug, format, ...) if(debug){__android_log_print(ANDROID_LOG_ERROR, "openvp_AVBase", format, ##__VA_ARGS__);}
#define LOGI(debug, format, ...) if(debug){__android_log_print(ANDROID_LOG_INFO, "openvp_AVBase", format, ##__VA_ARGS__);}

#define END_STATE 1
#define START_STATE 0

#define ROTATE_0_CROP_LT 0

/**
 * 旋转90度剪裁左上
 */
#define ROTATE_90_CROP_LT 1
/**
 * 暂时没处理
 */
#define ROTATE_180 2
/**
 * 旋转270(-90)裁剪左上，左右镜像
 */
#define ROTATE_270_CROP_LT_MIRROR_LR 3

enum ENC_DATA_TYPE{
    VIDEO_DATA,
    AUDIO_DATA,
    META_DATA
};

typedef struct EncData
{
	EncData(void) :_data(NULL), _dataLen(0),
		_bVideo(false), _dts(0) {}
	uint8_t*_data;
	int _dataLen;
	bool _bVideo;
	uint32_t _dts;
	ENC_DATA_TYPE _type;
}EncData;



//typedef WFlexBuffer							CMediaBuffer;
//typedef WPoolTemplate<CMediaBuffer>			CMediaBufferPool;
//typedef WElementAllocator<CMediaBuffer>		MediaBufferAllocator;
//typedef std::list<CMediaBuffer*>			MediaBufferList;



#endif //_BASE_H
