/*
 * Copyright 2021 bytedance
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
 * limitations under the License.
 *
 * Create Date : 2021/2/26
 */
package com.bytedance.volc.voddemo.data.remote.api2;

import static com.bytedance.volc.voddemo.data.remote.api2.Params.Format.MP4Format;

public class Params {

    public static class Value {

        public static Integer format() {
            return MP4Format;
        }

        public static Integer codec() {
            return null;
        }

        public static Integer definition() {
            return null;
        }

        public static String fileType() {
            return null;
        }

        public static Boolean needThumbs() {
            return null;
        }

        public static Boolean enableBarrageMask() {
            return null;
        }

        public static Integer cdnType() {
            return null;
        }

        public static String unionInfo() {
            return null;
        }
    }

    public static class Quality {
        public static final int NormalQuality = 0;
        public static final int LowQuality = 1;
        public static final int HighQuality = 2;
        public static final int MediumQuality = 3;
        public static final int LowerQuality = 4;
        public static final int LowestQuality = 5;
        public static final int HigherQuality = 6;
        public static final int HighestQuality = 7;
        public static final int VeryHighQuality = 9;
        public static final int SuperHighQuality = 10;
        public static final int AllQuality = 8;
        public static final int AdaptQuality = 11;
        public static final int AdaptLowQuality = 12;
        public static final int AdaptLowerQuality = 13;
        public static final int AdaptLowestQuality = 14;
        public static final int VLadderQuality = 15;
        public static final int AdaptHighQuality = 16;
        public static final int AdaptHigherQuality = 17;
    }

    public static class FileType {
        public static final String VIDEO = "video";
        public static final String AUDIO = "audio";
        public static final String EVIDEO = "evideo";
        public static final String EAUDIO = "eaudio";
    }

    public static class Definition {
        public static final int AllDefinition = 0;
        public static final int V360PDefinition = 1;
        public static final int V480PDefinition = 2;
        public static final int V720PDefinition = 3;
        public static final int V1080PDefinition = 4;
        public static final int V240PDefinition = 5;
        public static final int V540PDefinition = 6;
        public static final int HDRDefinition = 7;
        public static final int V420PDefinition = 8;
        public static final int V2KDefinition = 9;
        public static final int V4KDefinition = 10;
    }

    public static class Format {
        public static final int UndefinedFormat = 0;
        public static final int MP4Format = 1;
        public static final int M4AFormat = 2;
        public static final int M3U8Format = 4;
        public static final int GIFFormat = 5;
        public static final int DASHFormat = 6;
        public static final int OGGFormat = 7;
        public static final int FMP4Format = 8;
        public static final int HLSFormat = 9;
    }

    public static class Codec {
        public static final int H264Codec = 0;
        public static final int MH265Codec = 1;// (h264+1)
        public static final int OH265Codec = 2; // (h264+1)_hvc1
        public static final int ALLCodec = 3;
        public static final int OPUSCodec = 4;
        public static final int AACCodec = 5;
        public static final int MP3Codec = 6;
        public static final int H265Codec = 7;
        public static final int AllWithH265Codec = 8; // h264、H265
    }
}
