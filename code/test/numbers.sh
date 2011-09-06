#!/bin/bash

THIS=$0
SCRIPT=${THIS%.sh}.tcl
OUTPUT=${THIS%.sh}.out

source $( dirname $0 )/setup.sh > ${OUTPUT} 2>&1

set -x

${LAUNCH} -l -n ${PROCS} bin/turbine ${SCRIPT} >> ${OUTPUT} 2>&1
[[ ${?} == 0 ]] || exit 1

grep -q "result: 4" ${OUTPUT} || exit 1
grep -q "type: integer$" ${OUTPUT} || exit 1

exit 0
