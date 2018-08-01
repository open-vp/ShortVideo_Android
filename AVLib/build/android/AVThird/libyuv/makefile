# Makefile

include ../../config.mak

# DEBUG can be set to YES to include debugging info, or NO otherwise
DEBUG         := NO

# PROFILE can be set to YES to include profiling info, or NO otherwise
PROFILE        	:= YES

PRJECTNAME 		:=yuv
GROUPNAME		:=avcore
SRCROOT         :=../../../../third/libyuv
#****************************************************************************

OUTPUT_OBJDIR	:=${OUTPUT_OBJDIR}${GROUPNAME}/${PRJECTNAME}/
OUTPUT_LIBDIR   :=${OUTPUT_LIBDIR}${GROUPNAME}/

ifeq (YES, ${DEBUG})
   CFLAGS       := ${DEBUG_CFLAGS} -I../../../../include/${GROUPNAME}
   CXXFLAGS     := ${DEBUG_CXXFLAGS} -I../../../../include/${GROUPNAME}
   LDFLAGS      := ${DEBUG_LDFLAGS} -L../../../../lib/android/${GROUPNAME}
   OUTPUT		:= ${OUTPUT_LIBDIR}lib${PRJECTNAME}d.a
   OUTPUT_OBJDIR:= ${OUTPUT_OBJDIR}debug
else
   CFLAGS       := ${RELEASE_CFLAGS} -I../../../../include/${GROUPNAME}
   CXXFLAGS     := ${RELEASE_CXXFLAGS} -I../../../../include/${GROUPNAME}
   LDFLAGS      := ${RELEASE_LDFLAGS} -L../../../../lib/android/${GROUPNAME}
   OUTPUT		:= ${OUTPUT_LIBDIR}lib${PRJECTNAME}.a
   OUTPUT_OBJDIR := ${OUTPUT_OBJDIR}release
endif

ifeq (YES, ${PROFILE})
   CFLAGS   := ${CFLAGS} -O3
   CXXFLAGS := ${CXXFLAGS} -O3
   LDFLAGS  := ${LDFLAGS}
endif

#****************************************************************************
# Preprocessor directives
#****************************************************************************

DEFS := -DLIBYUV_NEON -mfpu=neon

#****************************************************************************
# Include paths
#****************************************************************************

INCS := -I${SRCROOT}/include

#****************************************************************************
# Makefile code common to all platforms
#****************************************************************************

CFLAGS   := ${CFLAGS}   ${DEFS}
CXXFLAGS := ${CXXFLAGS} ${DEFS}

#****************************************************************************
# Targets of the build
#****************************************************************************

all: ${OUTPUT}
	
#****************************************************************************
# Source files
#****************************************************************************

SRCS :=  \
    ${SRCROOT}/source/compare.cc           \
    ${SRCROOT}/source/compare_common.cc    \
	${SRCROOT}/source/compare_neon.cc      \
	${SRCROOT}/source/compare_neon64.cc    \
	${SRCROOT}/source/convert.cc 		  \
    ${SRCROOT}/source/convert_argb.cc      \
    ${SRCROOT}/source/convert_from.cc      \
    ${SRCROOT}/source/convert_jpeg.cc 	  \
    ${SRCROOT}/source/convert_from_argb.cc \
    ${SRCROOT}/source/convert_to_argb.cc   \
    ${SRCROOT}/source/convert_to_i420.cc   \
    ${SRCROOT}/source/cpu_id.cc            \
    ${SRCROOT}/source/mjpeg_decoder.cc     \
    ${SRCROOT}/source/mjpeg_validate.cc    \
    ${SRCROOT}/source/planar_functions.cc  \
    ${SRCROOT}/source/rotate.cc            \
    ${SRCROOT}/source/rotate_argb.cc       \
	${SRCROOT}/source/rotate_neon.cc       \
	${SRCROOT}/source/rotate_neon64.cc     \
    ${SRCROOT}/source/row_any.cc           \
    ${SRCROOT}/source/row_common.cc        \
    ${SRCROOT}/source/row_neon.cc          \
    ${SRCROOT}/source/row_neon64.cc        \
    ${SRCROOT}/source/scale.cc             \
    ${SRCROOT}/source/scale_argb.cc        \
    ${SRCROOT}/source/scale_common.cc      \
    ${SRCROOT}/source/scale_neon.cc        \
    ${SRCROOT}/source/scale_neon64.cc      \
    ${SRCROOT}/source/video_common.cc	  \
    ${SRCROOT}/source/scale_any.cc	      \
	
ASM_SRCS = 	
				
# Add on the sources for libraries
SRCS := ${SRCS} ${ASM_SRCS}

OBJS := $(addsuffix .o,$(basename ${SRCS}))
OBJSD := $(addsuffix .d,$(basename ${SRCS}))

#****************************************************************************
# Output
#****************************************************************************

${OUTPUT}: ${OBJS}
	if [ ! -d "${OUTPUT_LIBDIR}" ] ; then mkdir "${OUTPUT_LIBDIR}" -p -v; fi
	${AR} $@ ${OBJS} ${EXTRA_LIBS}

#****************************************************************************
# common rules
#****************************************************************************

# Rules for compiling source files to object files
%.o : %.cc
	${CXX} -c ${CXXFLAGS} ${INCS} $< -o $@

%.o : %.S
	${CC} -c ${ASFLAGS} ${INCS} $< -o $@

%.o : %.c
	${CC} -c ${CFLAGS} ${INCS} $< -o $@
	
%.o : %.cxx
	${CC} -c ${CFLAGS} ${INCS} $< -o $@	

dist:
	bash makedistlinux

clean:
	rm -f ${OBJS} ${OBJSD} ${OUTPUT}

depend:
	#makedepend ${INCS} ${SRCS}