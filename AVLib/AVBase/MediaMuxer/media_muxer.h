
/**
 */
#ifndef _MEDIA_MUXER_H
#define _MEDIA_MUXER_H

#include "base.h"
#define USE_H264BSF 1
#define USE_AACBSF 1

class MediaMuxer{
public:
    int startMuxer(const char * video, const char *audio , const char *out_file);
	void setup_video_state(int video_state);
    void setup_audio_state(int audio_state);
    int try_encode_over(UserArguments* arguments);
    void end_notify(UserArguments* arguments);

private:
    int start_muxer(UserArguments* arguments);
	
private:
    int video_state;
    int audio_state;

};

#endif //_MEDIA_MUXER_H
