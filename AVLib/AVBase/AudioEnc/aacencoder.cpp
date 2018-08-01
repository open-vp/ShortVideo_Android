#include "stdafxwaf.h"
#include "aacencoder.h"

using namespace WAUDIOFILTER;

CAACEncoder::CAACEncoder():
m_pEncoder(NULL),
m_nSampleRate(0),
m_nBitsPerSample(0),
m_nChannels(0),
m_uSampleInput(0),
m_uMaxOutputSize(0),
m_nMaxInputSize(0),
m_pEncBuf(NULL),
m_nWritePos(0)
{
}

CAACEncoder::~CAACEncoder()
{
}

BOOL	CAACEncoder::Open(AudioCodecInfo& info)
{
	m_nSampleRate = info.nSampleRate;
	m_nBitsPerSample = info.nBitsPerSample;
	m_nChannels = info.nChannels;

	if (NULL != m_pEncoder)
	{
		return FALSE;
	}

	m_pEncoder = faacEncOpen(m_nSampleRate, m_nChannels, &m_uSampleInput, &m_uMaxOutputSize);
	if (NULL == m_pEncoder)
	{
		return FALSE;
	}

	faacEncConfigurationPtr cfg;
	cfg = faacEncGetCurrentConfiguration(m_pEncoder);
	switch (m_nBitsPerSample)
	{
	case 16:
		cfg->inputFormat = FAAC_INPUT_16BIT;
		break;
	case 24:
		cfg->inputFormat = FAAC_INPUT_24BIT;
		break;
	case 32:
		cfg->inputFormat = FAAC_INPUT_32BIT;
		break;
	}

	if (!faacEncSetConfiguration(m_pEncoder, cfg)) 
	{
		return -1;
	}

	m_nMaxInputSize = m_uSampleInput * m_nBitsPerSample / 8;
	if (m_nMaxInputSize > 0) 
	{
		m_pEncBuf = (char*)malloc(m_nMaxInputSize);		
		if (NULL == m_pEncBuf)
		{
			return FALSE;
		}

		m_nWritePos = 0;
	}

	return TRUE;
}

BOOL	CAACEncoder::Encode(AudioStreamPacket& header)
{
	if (NULL == m_pEncoder)
	{
		return FALSE;
	}

	void* src = header.pbSrc;
	int lsrc = header.unSrcLen;
	void* dst = header.pbDes;
	int* ldst = (int*)&header.unDesUsed;

	*ldst = 0;
	header.unSrcUsed = header.unSrcLen;
	if ((m_nWritePos + lsrc) >= m_nMaxInputSize)
	{
		int retlen = 0;
		int difflen = m_nMaxInputSize - m_nWritePos;
		memcpy(m_pEncBuf + m_nWritePos, src, difflen);
		memset(dst, 0, m_uMaxOutputSize);
		retlen = faacEncEncode(m_pEncoder, (int*)m_pEncBuf, m_uSampleInput, (unsigned char*)dst, m_uMaxOutputSize);
		*ldst = retlen;
		if (lsrc - difflen > 0)
		{
			memcpy((char*)m_pEncBuf, (char*)src + difflen, lsrc - difflen);
			m_nWritePos = lsrc - difflen;
		}
		else
		{
			m_nWritePos = 0;
		}
	}
	else
	{
		if (lsrc == 0)
		{
			int retlen = 0;
			if (m_nWritePos > 0)
			{
				retlen = faacEncEncode(m_pEncoder, (int*)m_pEncBuf, m_uSampleInput, (unsigned char*)dst, m_uMaxOutputSize);
				m_nWritePos = 0;
			}
			else
			{
				retlen = faacEncEncode(m_pEncoder, NULL, 0, (unsigned char*)dst, m_uMaxOutputSize);
			}

			*ldst = retlen;
		}
		else
		{
			memcpy(m_pEncBuf + m_nWritePos, src, lsrc);
			m_nWritePos += lsrc;
			*ldst = 0;
		}
	}
	
	return TRUE;
}

VOID	CAACEncoder::Close()
{
	if (m_pEncoder)
	{
		faacEncClose(m_pEncoder);
		m_pEncoder = NULL;
	}

	if (m_pEncBuf)
	{
		free(m_pEncBuf);
		m_pEncBuf = NULL;
	}
}
