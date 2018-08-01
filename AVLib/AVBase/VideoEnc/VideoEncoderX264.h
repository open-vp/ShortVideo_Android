// VideoEncoderX264.h: interface for the CVideoEncoderX264 class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(__VIDEOENCODERX264_H__INCLUDED_)
#define __VIDEOENCODERX264_H__INCLUDED_

#include "VideoDefine.h"

#ifdef __cplusplus
extern "C" {
#endif
	#include "stdint.h"
	#include "x264.h"
#ifdef __cplusplus
}
#endif


class CVideoEncoderX264
{
public:
	CVideoEncoderX264();
	virtual ~CVideoEncoderX264();

	virtual BOOL	Config( const Video_Encoder_Param* pParam );
	virtual BOOL	Compress( Video_Code_Frame &frame );
	virtual VOID	StopCompress();
	virtual BOOL	StartCompress( const BITMAPINFOHEADER &biIn,const Video_Encoder_Param &param );	
protected:
	void			ParseParam( x264_param_t *param_t );

	BITMAPINFO			m_biIn;			
	int					m_cspIn;
	Video_Encoder_Param  m_EncodeParam;	
	x264_t*				m_pEncoder;
	HANDLE				m_hConverter;
	PBYTE				m_pbConvertBuffer;
#endif
};

#endif 
