#
# Makefile created by createPackageMake on Fri Oct 12 16:49:40 BST 2012
#   (Really just a shell that points back to Makefile.master in ../../../../..)
# 
# ------------------------------------------------------------------- #

# Package processed by this Makefile
PACKAGE=java.atst.giss.abplc

# Relative path from here to root of Package tree
ROOTDIR=../../../../..

help:
	@echo "You may make: build_all ice classes gcc_all docs install_scripts" >&2
	@echo "              clean class_clean ice_clean gcc_clean gcc_all" >&2
	@echo "              install_gcc package_clean" >&2

build_all:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master build_all \
				PACKAGES=src.${PACKAGE}

ice:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master ice \
				PACKAGES=src.${PACKAGE}

classes:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master classes \
				PACKAGES=src.${PACKAGE}

docs:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master docs

install_scripts:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master install_scripts \
				PACKAGES=src.${PACKAGE}

install_properties: ${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master install_properties \
				PACKAGES=src.${PACKAGE}

extract_properties: ${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master extract_properties \
				PACKAGES=src.${PACKAGE}

clean:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master clean \
				PACKAGES=src.${PACKAGE}

class_clean:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master class_clean \
				PACKAGES=src.${PACKAGE}

ice_clean:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master ice_clean \
				PACKAGES=src.${PACKAGE}

package_clean:	${ROOTDIR}/Makefile.master
	@$(MAKE) -C ${ROOTDIR} -f Makefile.master distclean \
				PACKAGES=src.${PACKAGE}

# These next targets are to build C/C++ based libraries

gcc_clean:	Makefile.gcc
	@$(MAKE) -f Makefile.gcc gcc_clean

gcc_all:	Makefile.gcc
	@$(MAKE) -f Makefile.gcc all

install_gcc:    Makefile.gcc
	@$(MAKE) -f Makefile.gcc install

# These next targsets are to build jni librarires
.PHONY: jni jni_clean

jni_clean:	Makefile.jni
	@$(MAKE) -f Makefile.jni jni_clean

jni_header:	Makefile.jni
	@$(MAKE) -f Makefile.jni jni_header

jni_lib:	Makefile.jni
	@$(MAKE) -f Makefile.jni jni_lib

jni:	classes jni_header jni_lib Makefile.jni


