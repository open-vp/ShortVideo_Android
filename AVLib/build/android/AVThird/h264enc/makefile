# Makefile

include ../../config.mak

# DEBUG can be set to YES to include debugging info, or NO otherwise
# DEBUG          := YES

# PROFILE can be set to YES to include profiling info, or NO otherwise
PROFILE        := NO

PRJECTNAME 		:=libh264enc
GROUPNAME		:=avcore
SRCROOT         :=../../../../third/H264Enc
#****************************************************************************

OUTPUT_OBJDIR	:=${OUTPUT_OBJDIR}${GROUPNAME}/${PRJECTNAME}/

ifeq (YES, ${DEBUG})
   CFLAGS       := ${DEBUG_CFLAGS} -I../../../../include/${GROUPNAME}
   CXXFLAGS     := ${DEBUG_CXXFLAGS} -I../../../../include/${GROUPNAME}
   LDFLAGS      := ${DEBUG_LDFLAGS} -L../../../../lib/android/${GROUPNAME}
   OUTPUT_BINDIR := ${OUTPUT_BINDIR}${GROUPNAME}/debug/
   OUTPUT	:= ${OUTPUT_BINDIR}${PRJECTNAME}.so
   OUTPUT_OBJDIR := ${OUTPUT_OBJDIR}debug
else
   CFLAGS       := ${RELEASE_CFLAGS} -I../../../../include/${GROUPNAME}
   CXXFLAGS     := ${RELEASE_CXXFLAGS} -I../../../../include/${GROUPNAME}
   LDFLAGS      := ${RELEASE_LDFLAGS} -L../../../../lib/android/${GROUPNAME}
   OUTPUT_BINDIR := ${OUTPUT_BINDIR}${GROUPNAME}/release/
   OUTPUT	:= ${OUTPUT_BINDIR}${PRJECTNAME}.so
   OUTPUT_OBJDIR := ${OUTPUT_OBJDIR}release
endif

ifeq (YES, ${PROFILE})
   CFLAGS   := ${CFLAGS} -pg -O3
   CXXFLAGS := ${CXXFLAGS} -pg -O3
   LDFLAGS  := ${LDFLAGS} -pg
endif

LIBS := -limagefilter -lyuv -lh264enc -lwbaselib32 -llog -landroid -lstlport_static -lstdc++

#****************************************************************************
# Include paths
#****************************************************************************

INCS :=

#****************************************************************************
# Static library paths
#****************************************************************************

LIBDIR := 

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

SRCS := ${SRCROOT}/StdAfxH264E.cpp ${SRCROOT}/VideoEncoderX264.cpp ${SRCROOT}/H264Enc.cpp ${SRCROOT}/H264EncAndroid.cpp

# Add on the sources for libraries
SRCS := ${SRCS}

OBJS := $(addsuffix .o,$(basename ${SRCS}))
OBJSD := $(addsuffix .d,$(basename ${SRCS}))

#****************************************************************************
# Output
#****************************************************************************

${OUTPUT}: ${OBJS}
	if [ ! -d "${OUTPUT_BINDIR}" ] ; then mkdir "${OUTPUT_BINDIR}" -p -v; fi
	${LD} ${LDFLAGS} ${OBJS} ${LIBDIR} ${LIBS} ${EXTRA_LIBS} -o $@ 

#****************************************************************************
# common rules
#****************************************************************************

# Rules for compiling source files to object files
%.o : %.cpp
	${CXX} -c ${CXXFLAGS} ${INCS} $< -o $@

%.o : %.c
	${CC} -c ${CFLAGS} ${INCS} $< -o $@

dist:
	bash makedistlinux

clean:
	rm -f ${OBJS} ${OBJSD} ${OUTPUT}

depend:
	#makedepend ${INCS} ${SRCS}