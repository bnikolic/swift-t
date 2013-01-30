/*
 * Copyright 2013 University of Chicago and Argonne National Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

// FILES.SWIFT

#ifndef FILES_SWIFT
#define FILES_SWIFT

@pure
(file t[]) glob(string s)
"turbine" "0.0.2" "glob";

@pure
@dispatch=LEAF
(string t) readFile(file f)
    "turbine" "0.0.2" "readFile"
    [ "set <<t>> [ turbine::read_file_local <<f>> ]" ];

@pure
(file t) writeFile(string s)
    "turbine" "0.0.2" "writeFile";

// TODO: calling convention not figured out yet
//    [ "set <<t>> [ turbine::write_file_local <<s>> ]" ];


#endif // FILES_SWIFT

