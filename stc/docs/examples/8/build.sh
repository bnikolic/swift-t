#!/bin/bash -e

source $(turbine -S)

set -x

MPICC=$( which mpicc )
MPI=$( dirname $( dirname $MPICC ) )

swig -I${MPI}/include f.i

mpicc -c -fPIC -I . f.c
mpicc -c -fPIC $TCL_INCLUDE_SPEC f_wrap.c
mpicc -shared -o libf.so f_wrap.o f.o
tclsh make-package.tcl > pkgIndex.tcl

stc -r $PWD test-f.swift
