// 
//
//////////////////////////////////////////////////////////////////////

#include "stdio.h"
#include "VideoEncoderX264.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////
static void x264_log_vfw( void *p_private, int i_level, const char *psz_fmt, va_list arg )
{

}

static int get_csp( BITMAPINFOHEADER *hdr )
{
    switch( hdr->biCompression )
    {
	case FOURCC_I420:
	case FOURCC_IYUV:
		return X264_CSP_I420;		
	case FOURCC_YV12:
		return X264_CSP_YV12;		
	case BI_RGB:
        {
            int i_vflip = hdr->biHeight < 0 ? 0 : X264_CSP_VFLIP;
			
            if( hdr->biBitCount == 24 )
                return X264_CSP_BGR | i_vflip;
            if( hdr->biBitCount == 32 )
                return X264_CSP_BGRA | i_vflip;
            else
                return X264_CSP_NONE;
        }		
	default:
		return X264_CSP_NONE;
    }
}
void CVideoEncoderX264::ParseParam(x264_param_t *param_t)
{	
     x264_param_default( param_t );

	char* preset = "superfast";
	char * tune = "zerolatency";
	int b_turbo = 1;
    if( preset && !stricmp( preset, "placebo" ) )
        b_turbo = 0;	
    x264_param_default_preset( param_t, preset, tune );
     if( b_turbo )
        x264_param_apply_fastfirstpass( param_t );

	char *profile = "baseline";// "main";
    x264_param_apply_profile( param_t, profile );
	
	//disable OpenCL
	param_t->b_opencl = 0;
	
	// VFR input.  If 1, use timebase and timestamps for ratecontrol purposes, If 0, use fps only.
	param_t->b_vfr_input = 0;
	
    param_t->i_fps_num = param_t->i_timebase_num = m_EncodeParam.nFrameRate*1000;
    param_t->i_fps_den = param_t->i_timebase_den = 1000;
    param_t->i_frame_total = 0;
	
    param_t->i_keyint_min = m_EncodeParam.nFrameRate;
    param_t->i_keyint_max = m_EncodeParam.nKeyFrameInterval>m_EncodeParam.nFrameRate ? m_EncodeParam.nKeyFrameInterval:m_EncodeParam.nFrameRate;

	param_t->i_log_level = X264_LOG_INFO;
    param_t->pf_log = x264_log_vfw;
    param_t->p_log_private = NULL;
	
    param_t->i_width = m_biIn.bmiHeader.biWidth;
    param_t->i_height= m_biIn.bmiHeader.biHeight;
	
    switch( m_EncodeParam.nEncoderMode )
    {
	case VIDEO_ENCODER_MODE_CBR:
		param_t->rc.i_rc_method = X264_RC_ABR;
		param_t->rc.f_rate_tolerance = 0.10f;
		param_t->rc.i_bitrate = m_EncodeParam.nBitRate/1000;
		break;
	case VIDEO_ENCODER_MODE_ABR:
	case VIDEO_ENCODER_MODE_VBR:
		param_t->rc.i_rc_method = X264_RC_CQP;
		param_t->rc.i_qp_constant = (50*(100-m_EncodeParam.nVBRQuality))/100+1;
		break;
	
	default:
		break;
    }
		
}

CVideoEncoderX264::VideoEncoderX264():
m_pEncoder( NULL ),
m_pbConvertBuffer( NULL ),
m_hConverter( NULL ),
m_cspIn( 0 )
{
	ZeroMemory( &m_EncodeParam,sizeof(m_EncodeParam));
	ZeroMemory( &m_biIn,sizeof(m_biIn));
}

CVideoEncoderX264::~CVideoEncoderX264()
{
	StopCompress();
}

BOOL	CVideoEncoderX264::Compress( Video_Code_Frame &frame )
{
	if( NULL == m_pEncoder )
		return FALSE;
	PBYTE pbData = frame.pbIn;
    int i_csp;
	
	x264_picture_t pic;
    x264_picture_t pic_out;

    int        i_nal;
    x264_nal_t *nal;
    int        i_out;

    /* Init the picture */
	x264_picture_init( &pic );
    
    pic.img.i_csp = X264_CSP_I420;
    /* For now biWidth can be divided by 16 so no problem */
    switch( pic.img.i_csp & X264_CSP_MASK )
    {
        case X264_CSP_I420:
            pic.img.i_plane = 3;
            pic.img.i_stride[0] = m_biIn.bmiHeader.biWidth;
            pic.img.i_stride[1] = 
            pic.img.i_stride[2] = m_biIn.bmiHeader.biWidth / 2;

            pic.img.plane[0]    = (uint8_t*)pbData;
            pic.img.plane[1]    = pic.img.plane[0] + m_biIn.bmiHeader.biWidth * m_biIn.bmiHeader.biHeight;
            pic.img.plane[2]    = pic.img.plane[1] + m_biIn.bmiHeader.biWidth * m_biIn.bmiHeader.biHeight / 4;
            break;

        case X264_CSP_YV12:	
            pic.img.i_plane = 3;
            pic.img.i_stride[0] = m_biIn.bmiHeader.biWidth;
            pic.img.i_stride[1] = 
			pic.img.i_stride[2] = m_biIn.bmiHeader.biWidth / 2;
			
            pic.img.plane[0]    = (uint8_t*)pbData;
            pic.img.plane[1]    = pic.img.plane[0] + m_biIn.bmiHeader.biWidth * m_biIn.bmiHeader.biHeight;
            pic.img.plane[2]    = pic.img.plane[1] + m_biIn.bmiHeader.biWidth * m_biIn.bmiHeader.biHeight / 4;
            break;
        case X264_CSP_BGR:
            pic.img.i_plane = 1;
            pic.img.i_stride[0] = 3 * m_biIn.bmiHeader.biWidth;
            pic.img.plane[0]    = (uint8_t*)pbData;
            break;

        case X264_CSP_BGRA:
            pic.img.i_plane = 1;
            pic.img.i_stride[0] = 4 * m_biIn.bmiHeader.biWidth;
            pic.img.plane[0]    = (uint8_t*)pbData;
            break;
        default:
            return FALSE;
    }
	if( frame.bKeyFrame ){
		pic.i_type |= X264_TYPE_IDR;
	}

	/* encode it */
    i_out = x264_encoder_encode( m_pEncoder, &nal, &i_nal, &pic, &pic_out );

    /* create bitstream, unless we're dropping it in 1st pass */
	if( i_out > 0 ){
		memcpy( frame.pbOut,nal[0].p_payload,i_out );
	}

    frame.unOutLen = i_out;

    /* Set key frame only for IDR, as they are real synch point, I frame
       aren't always synch point (ex: with multi refs, ref marking) */
	if( pic_out.i_type == X264_TYPE_IDR ){
        frame.bKeyFrame = TRUE;
	}else{
        frame.bKeyFrame = FALSE;
	}
	if( pic_out.i_type == X264_TYPE_AUTO )
		return FALSE;
	return TRUE;
}

VOID	CVideoEncoderX264::StopCompress()
{
	if( m_pEncoder ){

		x264_encoder_close( m_pEncoder );
		m_pEncoder = NULL;
	}
}

BOOL	CVideoEncoderX264::StartCompress( const BITMAPINFOHEADER &biIn,const Video_Encoder_Param &param )
{

	if( m_pEncoder )
		StopCompress();

    x264_param_t param_t;
	ParseParam( &param_t );

    m_pEncoder = x264_encoder_open( &param_t );
	if( NULL == m_pEncoder )
		return FALSE;
    x264_encoder_parameters( m_pEncoder, &param_t );


	return m_pEncoder != NULL;
}

BOOL CVideoEncoderX264::Config(const Video_Encoder_Param *pParam)
{
	if( NULL == pParam )
		return FALSE;
	CopyMemory( &m_EncodeParam,pParam,sizeof(Video_Encoder_Param));
	
    x264_param_t param_t;
	ParseParam( &param_t );
	if( m_pEncoder ){
		x264_encoder_close( m_pEncoder );
		m_pEncoder = x264_encoder_open( &param_t );
		if( NULL == m_pEncoder )
			return FALSE;
		x264_encoder_parameters( m_pEncoder, &param_t );
		return TRUE;
	}

	return FALSE;
}