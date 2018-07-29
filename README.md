# ShortVideo_Android                                 

OpenVP开源技术社区

android短视频录制功能

短视频录制：采集摄像头画面和麦克风声音，经过图像和声音处理后，进行（H264、AAC）编码压缩生成指定分辨率的MP4文件。 


1、基础录制

//设置正方形、或者全屏录制

mVPRecord.setOrientation(VIDOE_ORIENTATION_FULL);  

//设置视频录制比例：3 ：4      9 ：1 6      1 ：1

mVPRecord.setAspectRatio(VIDEO_RATIO_9_16); 

//设置视频录制速率

 mVPRecord.setRecordSpeed(VIDEO_RECORD_SPEED_NOMAL); 

//开始录制

 mVPRecord.startRecord(); 
 
 //开始录制, 可以指定输出视频文件地址和时长 30s  2m
 
 mVPRecord.startRecord(string videoPath, int time); 
  
  
 //暂停录制
 
  mVPRecord.pauseRecord(); 
 

//继续录制

mVPRecord.resumeRecord(); 
 

//结束录制

mVPRecord.stopRecord(); 



2、滤镜功能

//设置风格滤镜,选择滤镜背景

mVPRecord.setFilter(string filterImage); 


//设置风格滤镜效果程度

mVPRecord.setSpecialRatio(int level);


3、美颜功能

//设置美颜和美白

mVPRecord.setBeautyDepth(int beautyDepth, int whiteningDepth); 
 
//设置大眼级别

mVPRecord.setEyeScaleLevel(int scaleLevel);

//设置V 字脸级别

mVPRecord.setFaceVLevel(int level); 
  
//设置短脸

mVPRecord.setFaceShortLevel(int level);  

//设置瘦脸级别

mVPRecord.setFaceScaleLevel(int Level); 

//设置瘦鼻

mVPRecord.setNoseSlimLevel(int level);  
 
5、设置无绿幕特效，替换自然背景

//替换自然背景

mVPRecord.setNatureScreen(string background);   




