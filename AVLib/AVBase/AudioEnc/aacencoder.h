#ifndef	__AAC_ENCODER_H
#define	__AAC_ENCODER_H

#include "wbasetype.h"
#include "Export_AF.h"
#include "audiocodec.h"
#include "iaudiocodec.h"



#ifdef __cplusplus
extern "C" {
#endif
#include "faac.h"
#ifdef __cplusplus
}
#endif

namespace WAUDIOFILTER{

class CAACEncoder:public IAudioEncoder
{
public:
	CAACEncoder();
	virtual ~CAACEncoder();

	virtual BOOL  Open( AudioCodecInfo& info );
	virtual BOOL  Encode( AudioStreamPacket& header );
	virtual VOID  Close();
protected:
	faacEncHandle m_pEncoder;
	int 		  m_nSampleRate;
	int			  m_nBitsPerSample;
	int			  m_nChannels;
	unsigned long m_uSampleInput;
	unsigned long m_uMaxOutputSize;
	int           m_nMaxInputSize;
	char*         m_pEncBuf;
	int           m_nWritePos;
};

}

#endif
