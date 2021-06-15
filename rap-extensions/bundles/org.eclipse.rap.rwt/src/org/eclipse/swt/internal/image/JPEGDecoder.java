/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This source file is based in part on the work of the Independent JPEG Group (IJG)
 * and is made available under the terms contained in the about_files/IJG_README
 * file accompanying this program.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.image;

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class JPEGDecoder {
	
	static final int DCTSIZE = 8;
	static final int DCTSIZE2 = 64;
	static final int NUM_QUANT_TBLS = 4;
	static final int NUM_HUFF_TBLS = 4;
	static final int NUM_ARITH_TBLS = 16;
	static final int MAX_COMPS_IN_SCAN = 4;
	static final int MAX_COMPONENTS = 10;
	static final int MAX_SAMP_FACTOR = 4;
	static final int D_MAX_BLOCKS_IN_MCU = 10;
	static final int HUFF_LOOKAHEAD = 8;
	static final int MAX_Q_COMPS = 4;
	static final int IFAST_SCALE_BITS = 2;
	static final int MAXJSAMPLE = 255;
	static final int CENTERJSAMPLE = 128;
	static final int MIN_GET_BITS = 32-7;
	static final int INPUT_BUFFER_SIZE = 4096;

	static final int SCALEBITS = 16;	/* speediest right-shift on some machines */
	static final int ONE_HALF = 1 << (SCALEBITS-1);
	
	static final int RGB_RED = 2;	/* Offset of Red in an RGB scanline element */
	static final int RGB_GREEN = 1;	/* Offset of Green */
	static final int RGB_BLUE = 0;	/* Offset of Blue */
	static final int RGB_PIXELSIZE = 3;
	
	static final int JBUF_PASS_THRU = 0;
	static final int JBUF_SAVE_SOURCE = 1;	/* Run source subobject only, save output */
	static final int JBUF_CRANK_DEST = 2;	/* Run dest subobject only, using saved data */
	static final int JBUF_SAVE_AND_PASS = 3;	
	
	static final int JPEG_MAX_DIMENSION = 65500;
	static final int BITS_IN_JSAMPLE = 8;
	
	static final int JDITHER_NONE = 0;		/* no dithering */
	static final int JDITHER_ORDERED = 1;	/* simple ordered dither */
	static final int JDITHER_FS = 2;	
	
	static final int JDCT_ISLOW = 0;	/* slow but accurate integer algorithm */
	static final int JDCT_IFAST = 1;	/* faster, less accurate integer method */
	static final int JDCT_FLOAT = 2;	/* floating-point: accurate, fast on fast HW */
	static final int JDCT_DEFAULT = JDCT_ISLOW;

	static final int JCS_UNKNOWN = 0;		/* error/unspecified */
	static final int JCS_GRAYSCALE = 1;		/* monochrome */
	static final int JCS_RGB = 2;		/* red/green/blue */
	static final int JCS_YCbCr = 3;		/* Y/Cb/Cr (also known as YUV) */
	static final int JCS_CMYK = 4;		/* C/M/Y/K */
	static final int JCS_YCCK = 5;		/* Y/Cb/Cr/K */

	static final int SAVED_COEFS = 6;
	static final int Q01_POS = 1;
	static final int Q10_POS = 8;
	static final int Q20_POS = 16;
	static final int Q11_POS = 9;
	static final int Q02_POS = 2;
	
	static final int CTX_PREPARE_FOR_IMCU = 0;	/* need to prepare for MCU row */
	static final int CTX_PROCESS_IMCU = 1;	/* feeding iMCU to postprocessor */
	static final int CTX_POSTPONED_ROW = 2;	/* feeding postponed row group */
	
	static final int APP0_DATA_LEN = 14;	/* Length of interesting data in APP0 */
	static final int APP14_DATA_LEN = 12;	/* Length of interesting data in APP14 */
	static final int APPN_DATA_LEN = 14;	/* Must be the largest of the above!! */

	/* markers */
	static final int M_SOF0 = 0xc0;
	static final int M_SOF1 = 0xc1;
	static final int M_SOF2 = 0xc2;
	static final int M_SOF3 = 0xc3;
	static final int M_SOF5 = 0xc5;
	static final int M_SOF6 = 0xc6;
	static final int M_SOF7 = 0xc7;
	static final int M_JPG = 0xc8;
	static final int M_SOF9 = 0xc9;
	static final int M_SOF10 = 0xca;
	static final int M_SOF11 = 0xcb;
	static final int M_SOF13 = 0xcd;
	static final int M_SOF14 = 0xce;
	static final int M_SOF15 = 0xcf;
	static final int M_DHT = 0xc4;
	static final int M_DAC = 0xcc;
	static final int M_RST0 = 0xd0;
	static final int M_RST1 = 0xd1;
	static final int M_RST2	= 0xd2;
	static final int M_RST3 = 0xd3;
	static final int M_RST4 = 0xd4;
	static final int M_RST5 = 0xd5;
	static final int M_RST6 = 0xd6;
	static final int M_RST7 = 0xd7;
	static final int M_SOI = 0xd8;
	static final int M_EOI = 0xd9;
	static final int M_SOS = 0xda;
	static final int M_DQT = 0xdb;
	static final int M_DNL = 0xdc;
	static final int M_DRI = 0xdd;
	static final int M_DHP = 0xde;
	static final int M_EXP = 0xdf;
	static final int M_APP0 = 0xe0;
	static final int M_APP1 = 0xe1;
	static final int M_APP2 = 0xe2;
	static final int M_APP3 = 0xe3;
	static final int M_APP4 = 0xe4;
	static final int M_APP5 = 0xe5;
	static final int M_APP6 = 0xe6;
	static final int M_APP7 = 0xe7;
	static final int M_APP8 = 0xe8;
	static final int M_APP9 = 0xe9;
	static final int M_APP10 = 0xea;
	static final int M_APP11 = 0xeb;
	static final int M_APP12 = 0xec;
	static final int M_APP13 = 0xed;
	static final int M_APP14 = 0xee;
	static final int M_APP15 = 0xef;
 	static final int M_JPG0 = 0xf0;
	static final int M_JPG13 = 0xfd;
	static final int M_COM = 0xfe;
	static final int M_TEM = 0x01;
	static final int M_ERROR = 0x100;
	
	/* Values of global_state field (jdapi.c has some dependencies on ordering!) */
	static final int CSTATE_START = 100;	/* after create_compress */
	static final int CSTATE_SCANNING = 101;	/* start_compress done, write_scanlines OK */
	static final int CSTATE_RAW_OK = 102;	/* start_compress done, write_raw_data OK */
	static final int CSTATE_WRCOEFS = 103;	/* jpeg_write_coefficients done */
	static final int DSTATE_START = 200;	/* after create_decompress */
	static final int DSTATE_INHEADER = 201;	/* reading header markers, no SOS yet */
	static final int DSTATE_READY = 202;	/* found SOS, ready for start_decompress */
	static final int DSTATE_PRELOAD = 203;	/* reading multiscan file in start_decompress*/
	static final int DSTATE_PRESCAN = 204;	/* performing dummy pass for 2-pass quant */
	static final int DSTATE_SCANNING = 205;	/* start_decompress done, read_scanlines OK */
	static final int DSTATE_RAW_OK = 206;	/* start_decompress done, read_raw_data OK */
	static final int DSTATE_BUFIMAGE = 207;	/* expecting jpeg_start_output */
	static final int DSTATE_BUFPOST = 208;	/* looking for SOS/EOI in jpeg_finish_output */
	static final int DSTATE_RDCOEFS = 209;	/* reading file in jpeg_read_coefficients */
	static final int DSTATE_STOPPING = 210;	/* looking for EOI in jpeg_finish_decompress */

	static final int JPEG_REACHED_SOS = 1; /* Reached start of new scan */
	static final int JPEG_REACHED_EOI = 2; /* Reached end of image */
	static final int JPEG_ROW_COMPLETED = 3; /* Completed one iMCU row */
	static final int JPEG_SCAN_COMPLETED = 4; /* Completed last iMCU row of a scan */
	
	static final int JPEG_SUSPENDED = 0; /* Suspended due to lack of input data */
	static final int JPEG_HEADER_OK = 1; /* Found valid image datastream */
	static final int JPEG_HEADER_TABLES_ONLY = 2; /* Found valid table-specs-only datastream */

	/* Function pointers */
	static final int DECOMPRESS_DATA = 0;
	static final int DECOMPRESS_SMOOTH_DATA = 1;
	static final int DECOMPRESS_ONEPASS = 2;
	
	static final int CONSUME_DATA = 0;
	static final int DUMMY_CONSUME_DATA = 1;
	
	static final int PROCESS_DATA_SIMPLE_MAIN = 0;
	static final int PROCESS_DATA_CONTEXT_MAIN = 1;
	static final int PROCESS_DATA_CRANK_POST = 2;
	
	static final int POST_PROCESS_1PASS = 0;
	static final int POST_PROCESS_DATA_UPSAMPLE = 1;
	
	static final int NULL_CONVERT = 0;
	static final int GRAYSCALE_CONVERT = 1;
	static final int YCC_RGB_CONVERT = 2;
	static final int GRAY_RGB_CONVERT = 3;
	static final int YCCK_CMYK_CONVERT = 4;
	
	static final int NOOP_UPSAMPLE = 0;
	static final int FULLSIZE_UPSAMPLE = 1;
	static final int H2V1_FANCY_UPSAMPLE = 2;
	static final int H2V1_UPSAMPLE = 3;
	static final int H2V2_FANCY_UPSAMPLE = 4;
	static final int H2V2_UPSAMPLE = 5;
	static final int INT_UPSAMPLE = 6;
	
	static final int INPUT_CONSUME_INPUT = 0;
	static final int COEF_CONSUME_INPUT = 1;
	
	static int extend_test[] =	 /* entry n is 2**(n-1) */
	{
		0, 0x0001, 0x0002, 0x0004, 0x0008, 0x0010, 0x0020, 0x0040, 0x0080,
		0x0100, 0x0200, 0x0400, 0x0800, 0x1000, 0x2000, 0x4000
	};

	static int extend_offset[] = /* entry n is (-1 << n) + 1 */
	{
		0, ((-1)<<1) + 1, ((-1)<<2) + 1, ((-1)<<3) + 1, ((-1)<<4) + 1,
		((-1)<<5) + 1, ((-1)<<6) + 1, ((-1)<<7) + 1, ((-1)<<8) + 1,
		((-1)<<9) + 1, ((-1)<<10) + 1, ((-1)<<11) + 1, ((-1)<<12) + 1,
		((-1)<<13) + 1, ((-1)<<14) + 1, ((-1)<<15) + 1
	};
	
	static int jpeg_natural_order[] = {
		0,	1,	8, 16,	9,	2,	3, 10,
		17, 24, 32, 25, 18, 11,	4,	5,
		12, 19, 26, 33, 40, 48, 41, 34,
		27, 20, 13,	6,	7, 14, 21, 28,
		35, 42, 49, 56, 57, 50, 43, 36,
		29, 22, 15, 23, 30, 37, 44, 51,
		58, 59, 52, 45, 38, 31, 39, 46,
		53, 60, 61, 54, 47, 55, 62, 63,
		63, 63, 63, 63, 63, 63, 63, 63, /* extra entries for safety in decoder */
		63, 63, 63, 63, 63, 63, 63, 63
	};
	
	static final class JQUANT_TBL {
		/* This array gives the coefficient quantizers in natural array order
		 * (not the zigzag order in which they are stored in a JPEG DQT marker).
		 * CAUTION: IJG versions prior to v6a kept this array in zigzag order.
		 */
		short[] quantval = new short[DCTSIZE2];	/* quantization step for each coefficient */
		/* This field is used only during compression.	It's initialized false when
		 * the table is created, and set true when it's been output to the file.
		 * You could suppress output of a table by setting this to true.
		 * (See jpeg_suppress_tables for an example.)
		 */
		boolean sent_table;		/* true when table has been output */
	}
	
	static final class JHUFF_TBL {
		/* These two fields directly represent the contents of a JPEG DHT marker */
		byte[] bits = new byte[17]; /* bits[k] = # of symbols with codes of */
									/* length k bits; bits[0] is unused */
		byte[] huffval = new byte[256];		/* The symbols, in order of incr code length */
		/* This field is used only during compression.	It's initialized false when
		 * the table is created, and set true when it's been output to the file.
		 * You could suppress output of a table by setting this to true.
		 * (See jpeg_suppress_tables for an example.)
		 */
		boolean sent_table;		/* true when table has been output */
	}
	
	static final class bitread_perm_state {		/* Bitreading state saved across MCUs */
		int get_buffer;	/* current bit-extraction buffer */
		int bits_left;		/* # of unused bits in it */
	}
	
	static final class bitread_working_state {		/* Bitreading working state within an MCU */
		/* Current data source location */
		/* We need a copy, rather than munging the original, in case of suspension */
		byte[] buffer; /* => next byte to read from source */
		int bytes_offset;
		int bytes_in_buffer;	/* # of bytes remaining in source buffer */
		/* Bit input buffer --- note these values are kept in register variables,
		 * not in this struct, inside the inner loops.
		 */
		int get_buffer;	/* current bit-extraction buffer */
		int bits_left;		/* # of unused bits in it */
		/* Pointer needed by jpeg_fill_bit_buffer. */
		jpeg_decompress_struct cinfo;	/* back link to decompress master record */
	} 
	
	static final class savable_state {
		int EOBRUN; //Note that this is only used in the progressive case
		int[] last_dc_val = new int[MAX_COMPS_IN_SCAN]; /* last DC coef for each component */
	}
	
	static final class d_derived_tbl {
		/* Basic tables: (element [0] of each array is unused) */
		int[] maxcode = new int[18];		/* largest code of length k (-1 if none) */
		/* (maxcode[17] is a sentinel to ensure jpeg_huff_decode terminates) */
		int[] valoffset = new int[17];		/* huffval[] offset for codes of length k */
		/* valoffset[k] = huffval[] index of 1st symbol of code length k, less
		 * the smallest code of length k; so given a code of length k, the
		 * corresponding symbol is huffval[code + valoffset[k]]
		 */

		/* Link to public Huffman table (needed only in jpeg_huff_decode) */
		JHUFF_TBL pub;

		/* Lookahead tables: indexed by the next HUFF_LOOKAHEAD bits of
		 * the input data stream.	If the next Huffman code is no more
		 * than HUFF_LOOKAHEAD bits long, we can obtain its length and
		 * the corresponding symbol directly from these tables.
		 */
		int[] look_nbits = new int[1<<HUFF_LOOKAHEAD]; /* # bits, or 0 if too long */
		byte[] look_sym = new byte[1<<HUFF_LOOKAHEAD]; /* symbol, or unused */
	} 
	
	static final class jpeg_d_coef_controller {
		int consume_data;
		int decompress_data;

		/* Pointer to array of coefficient virtual arrays, or null if none */
		short[][][] coef_arrays;
	
		/* These variables keep track of the current location of the input side. */
		/* cinfo.input_iMCU_row is also used for this. */
		int MCU_ctr;		/* counts MCUs processed in current row */
		int MCU_vert_offset;		/* counts MCU rows within iMCU row */
		int MCU_rows_per_iMCU_row;	/* number of such rows needed */

		/* The output side's location is represented by cinfo.output_iMCU_row. */

		/* In single-pass modes, it's sufficient to buffer just one MCU.
		 * We allocate a workspace of D_MAX_BLOCKS_IN_MCU coefficient blocks,
		 * and let the entropy decoder write into that workspace each time.
		 * (On 80x86, the workspace is FAR even though it's not really very big;
		 * this is to keep the module interfaces unchanged when a large coefficient
		 * buffer is necessary.)
		 * In multi-pass modes, this array points to the current MCU's blocks
		 * within the virtual arrays; it is used only by the input side.
		 */
		short[][] MCU_buffer = new short[D_MAX_BLOCKS_IN_MCU][];

		/* In multi-pass modes, we need a virtual block array for each component. */
		short[][][][] whole_image = new short[MAX_COMPONENTS][][][];

		/* When doing block smoothing, we latch coefficient Al values here */
		int[] coef_bits_latch;
		
		short[] workspace;

		void start_input_pass (jpeg_decompress_struct cinfo) {
			cinfo.input_iMCU_row = 0;
			start_iMCU_row(cinfo);
		}
		
		/* Reset within-iMCU-row counters for a new row (input side) */
		void start_iMCU_row (jpeg_decompress_struct cinfo) {
			jpeg_d_coef_controller coef = cinfo.coef;

			/* In an interleaved scan, an MCU row is the same as an iMCU row.
			 * In a noninterleaved scan, an iMCU row has v_samp_factor MCU rows.
			 * But at the bottom of the image, process only what's left.
			 */
			if (cinfo.comps_in_scan > 1) {
				coef.MCU_rows_per_iMCU_row = 1;
			} else {
				if (cinfo.input_iMCU_row < (cinfo.total_iMCU_rows-1))
					coef.MCU_rows_per_iMCU_row = cinfo.cur_comp_info[0].v_samp_factor;
				else
					coef.MCU_rows_per_iMCU_row = cinfo.cur_comp_info[0].last_row_height;
			}

			coef.MCU_ctr = 0;
			coef.MCU_vert_offset = 0;
		}
		
	}
	
	static abstract class jpeg_entropy_decoder {
		abstract void start_pass (jpeg_decompress_struct cinfo);
		abstract boolean decode_mcu (jpeg_decompress_struct cinfo, short[][] MCU_data);

		/* This is here to share code between baseline and progressive decoders; */
		/* other modules probably should not use it */
		boolean insufficient_data;	/* set true after emitting warning */
		
		bitread_working_state br_state_local = new bitread_working_state();
		savable_state state_local = new savable_state();
	}	

	static final class huff_entropy_decoder extends jpeg_entropy_decoder {
		bitread_perm_state bitstate = new bitread_perm_state();	/* Bit buffer at start of MCU */
		savable_state saved = new savable_state();		/* Other state at start of MCU */

		/* These fields are NOT loaded into local working state. */
		int restarts_to_go;	/* MCUs left in this restart interval */

		/* Pointers to derived tables (these workspaces have image lifespan) */
		d_derived_tbl[] dc_derived_tbls = new d_derived_tbl[NUM_HUFF_TBLS];
		d_derived_tbl[] ac_derived_tbls = new d_derived_tbl[NUM_HUFF_TBLS];

		/* Precalculated info set up by start_pass for use in decode_mcu: */

		/* Pointers to derived tables to be used for each block within an MCU */
		d_derived_tbl[] dc_cur_tbls = new d_derived_tbl[D_MAX_BLOCKS_IN_MCU];
		d_derived_tbl[] ac_cur_tbls = new d_derived_tbl[D_MAX_BLOCKS_IN_MCU];
		/* Whether we care about the DC and AC coefficient values for each block */
		boolean[] dc_needed = new boolean[D_MAX_BLOCKS_IN_MCU];
		boolean[] ac_needed = new boolean[D_MAX_BLOCKS_IN_MCU];
		
		@Override
		void start_pass (jpeg_decompress_struct cinfo) {
			start_pass_huff_decoder(cinfo);
		}

		@Override
		boolean decode_mcu (jpeg_decompress_struct cinfo, short[][] MCU_data) {
			huff_entropy_decoder entropy = this;
			int blkn;
//			BITREAD_STATE_VARS;
			int get_buffer;
			int bits_left;
//			bitread_working_state br_state = new bitread_working_state();
//			savable_state state = new savable_state();
			bitread_working_state br_state = br_state_local;
			savable_state state = state_local;

				/* Process restart marker if needed; may have to suspend */
			if (cinfo.restart_interval != 0) {
				if (entropy.restarts_to_go == 0)
					if (! process_restart(cinfo))
						return false;
			}

			/* If we've run out of data, just leave the MCU set to zeroes.
			 * This way, we return uniform gray for the remainder of the segment.
			 */
			if (! entropy.insufficient_data) {

				/* Load up working state */
//				BITREAD_LOAD_STATE(cinfo,entropy.bitstate);
				br_state.cinfo = cinfo;
				br_state.buffer = cinfo.buffer; 
				br_state.bytes_in_buffer = cinfo.bytes_in_buffer;
				br_state.bytes_offset = cinfo.bytes_offset;
				get_buffer = entropy.bitstate.get_buffer;
				bits_left = entropy.bitstate.bits_left;
					
//				ASSIGN_STATE(state, entropy.saved);
				state.last_dc_val[0] = entropy.saved.last_dc_val[0];
				state.last_dc_val[1] = entropy.saved.last_dc_val[1];
				state.last_dc_val[2] = entropy.saved.last_dc_val[2];
				state.last_dc_val[3] = entropy.saved.last_dc_val[3];

				/* Outer loop handles each block in the MCU */

				for (blkn = 0; blkn < cinfo.blocks_in_MCU; blkn++) {
					short[] block = MCU_data[blkn];
					d_derived_tbl dctbl = entropy.dc_cur_tbls[blkn];
					d_derived_tbl actbl = entropy.ac_cur_tbls[blkn];
					int s = 0, k, r;

					/* Decode a single block's worth of coefficients */

					/* Section F.2.2.1: decode the DC coefficient difference */
//					HUFF_DECODE(s, br_state, dctbl, return FALSE, label1);
					{
					int nb = 0, look;
					if (bits_left < HUFF_LOOKAHEAD) {
						if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
							return false;
						}
						get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						if (bits_left < HUFF_LOOKAHEAD) {
							nb = 1;
//							goto slowlabel;
							if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,dctbl,nb)) < 0) {
								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						}
					}
//					look = PEEK_BITS(HUFF_LOOKAHEAD);
					if (nb != 1) {
						look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));
						if ((nb = dctbl.look_nbits[look]) != 0) {
//							DROP_BITS(nb);
							bits_left -= nb;
							s = dctbl.look_sym[look] & 0xFF;
						} else {
							nb = HUFF_LOOKAHEAD+1;
//							slowlabel:
							if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,dctbl,nb)) < 0) {
								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						}
					}
					}

					if (s != 0) {
//						CHECK_BIT_BUFFER(br_state, s, return FALSE);
						{
						if (bits_left < (s)) {
							if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,s)) {
								return false;
							}
							get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
						}
						}
//						r = GET_BITS(s);
						r = (( (get_buffer >> (bits_left -= (s)))) & ((1<<(s))-1));
//						s = HUFF_EXTEND(r, s);
						s = ((r) < extend_test[s] ? (r) + extend_offset[s] : (r));
					}

					if (entropy.dc_needed[blkn]) {
						/* Convert DC difference to actual value, update last_dc_val */
						int ci = cinfo.MCU_membership[blkn];
						s += state.last_dc_val[ci];
						state.last_dc_val[ci] = s;
						/* Output the DC coefficient (assumes jpeg_natural_order[0] = 0) */
						block[0] = (short) s;
					}

					if (entropy.ac_needed[blkn]) {

						/* Section F.2.2.2: decode the AC coefficients */
						/* Since zeroes are skipped, output area must be cleared beforehand */
						for (k = 1; k < DCTSIZE2; k++) {
//							HUFF_DECODE(s, br_state, actbl, return FALSE, label2);	
							{
							int nb = 0, look;
							if (bits_left < HUFF_LOOKAHEAD) {
								if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								if (bits_left < HUFF_LOOKAHEAD) {
									nb = 1; 
//									goto slowlabel;
									if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,actbl,nb)) < 0) {
										return false;
									}
									get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								}
							}
							if (nb != 1) {
//								look = PEEK_BITS(HUFF_LOOKAHEAD);
								look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));
								if ((nb = actbl.look_nbits[look]) != 0) {
//									DROP_BITS(nb);
									bits_left -= (nb);
									s = actbl.look_sym[look] & 0xFF;
								} else {
									nb = HUFF_LOOKAHEAD+1;
//									slowlabel:
									if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,actbl,nb)) < 0) {
										return false;
									}
									get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								}
							}
							}			
							r = s >> 4;
							s &= 15;
						
							if (s != 0) {
								k += r;
//								CHECK_BIT_BUFFER(br_state, s, return FALSE);
								{
								if (bits_left < (s)) {
									if (!jpeg_fill_bit_buffer(br_state, get_buffer, bits_left, s)) {
										return false;
									}
									get_buffer = (br_state).get_buffer;
									bits_left = (br_state).bits_left;
								}
								}
//								r = GET_BITS(s);
								r = (((get_buffer >> (bits_left -= (s)))) & ((1 << (s)) - 1));
//								s = HUFF_EXTEND(r, s);
								s = ((r) < extend_test[s] ? (r) + extend_offset[s] : (r));
								/*
								 * Output coefficient in natural (dezigzagged)
								 * order. Note: the extra entries in
								 * jpeg_natural_order[] will save us if k >=
								 * DCTSIZE2, which could happen if the data is
								 * corrupted.
								 */
								block[jpeg_natural_order[k]] = (short) s;
							} else {
								if (r != 15)
									break;
								k += 15;
							}
						}

					} else {

						/* Section F.2.2.2: decode the AC coefficients */
						/* In this path we just discard the values */
						for (k = 1; k < DCTSIZE2; k++) {
//							HUFF_DECODE(s, br_state, actbl, return FALSE, label3);
							{
							int nb = 0, look;
							if (bits_left < HUFF_LOOKAHEAD) {
								if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								if (bits_left < HUFF_LOOKAHEAD) {
									nb = 1;
//									goto slowlabel;
									if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,actbl,nb)) < 0) {
										return false;
									}
									get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								}
							}
							if (nb != 1) {
//								look = PEEK_BITS(HUFF_LOOKAHEAD);
								look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));
								if ((nb = actbl.look_nbits[look]) != 0) {
//									DROP_BITS(nb);
									bits_left -= (nb);
									s = actbl.look_sym[look] & 0xFF;
								} else {
									nb = HUFF_LOOKAHEAD+1;
//									slowlabel:
									if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,actbl,nb)) < 0) {
										return false;
									}
									get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
								}
							}
							}			
							r = s >> 4;
							s &= 15;
						
							if (s != 0) {
								k += r;
//								CHECK_BIT_BUFFER(br_state, s, return FALSE);
								{
								if (bits_left < (s)) {
									if (!jpeg_fill_bit_buffer((br_state),get_buffer,bits_left,s)) {
										return false;
									}
									get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
									}
								}
//								DROP_BITS(s);
								bits_left -= s;
							} else {
								if (r != 15)
									break;
								k += 15;
							}
						}

					}
				}

				/* Completed MCU, so update state */
//				BITREAD_SAVE_STATE(cinfo,entropy.bitstate);
				cinfo.buffer = br_state.buffer;
				cinfo.bytes_in_buffer = br_state.bytes_in_buffer;
				cinfo.bytes_offset = br_state.bytes_offset;
				entropy.bitstate.get_buffer = get_buffer;
				entropy.bitstate.bits_left = bits_left;
//				ASSIGN_STATE(entropy.saved, state);
				entropy.saved.last_dc_val[0] = state.last_dc_val[0];
				entropy.saved.last_dc_val[1] = state.last_dc_val[1];
				entropy.saved.last_dc_val[2] = state.last_dc_val[2];
				entropy.saved.last_dc_val[3] = state.last_dc_val[3];
			}

			/* Account for restart interval (no-op if not using restarts) */
			entropy.restarts_to_go--;

			return true;
		}

		void start_pass_huff_decoder (jpeg_decompress_struct cinfo) {
			huff_entropy_decoder entropy = this;
			int ci, blkn, dctbl, actbl;
			jpeg_component_info compptr;

			/* Check that the scan parameters Ss, Se, Ah/Al are OK for sequential JPEG.
			 * This ought to be an error condition, but we make it a warning because
			 * there are some baseline files out there with all zeroes in these bytes.
			 */
			if (cinfo.Ss != 0 || cinfo.Se != DCTSIZE2-1 || cinfo.Ah != 0 || cinfo.Al != 0) {
//				WARNMS(cinfo, JWRN_NOT_SEQUENTIAL);
			}

			for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
				compptr = cinfo.cur_comp_info[ci];
				dctbl = compptr.dc_tbl_no;
				actbl = compptr.ac_tbl_no;
				/* Compute derived values for Huffman tables */
				/* We may do this more than once for a table, but it's not expensive */
				jpeg_make_d_derived_tbl(cinfo, true, dctbl, entropy.dc_derived_tbls[dctbl] = new d_derived_tbl());
				jpeg_make_d_derived_tbl(cinfo, false, actbl, entropy.ac_derived_tbls[actbl] = new d_derived_tbl());
				/* Initialize DC predictions to 0 */
				entropy.saved.last_dc_val[ci] = 0;
			}

			/* Precalculate decoding info for each block in an MCU of this scan */
			for (blkn = 0; blkn < cinfo.blocks_in_MCU; blkn++) {
				ci = cinfo.MCU_membership[blkn];
				compptr = cinfo.cur_comp_info[ci];
				/* Precalculate which table to use for each block */
				entropy.dc_cur_tbls[blkn] = entropy.dc_derived_tbls[compptr.dc_tbl_no];
				entropy.ac_cur_tbls[blkn] = entropy.ac_derived_tbls[compptr.ac_tbl_no];
				/* Decide whether we really care about the coefficient values */
				if (compptr.component_needed) {
					entropy.dc_needed[blkn] = true;
					/* we don't need the ACs if producing a 1/8th-size image */
					entropy.ac_needed[blkn] = (compptr.DCT_scaled_size > 1);
				} else {
					entropy.dc_needed[blkn] = entropy.ac_needed[blkn] = false;
				}
			}

			/* Initialize bitread state variables */
			entropy.bitstate.bits_left = 0;
			entropy.bitstate.get_buffer = 0; /* unnecessary, but keeps Purify quiet */
			entropy.insufficient_data = false;

			/* Initialize restart counter */
			entropy.restarts_to_go = cinfo.restart_interval;
		}
	
		boolean process_restart (jpeg_decompress_struct cinfo) {
			huff_entropy_decoder entropy = this;
			int ci;

			/* Throw away any unused bits remaining in bit buffer; */
			/* include any full bytes in next_marker's count of discarded bytes */
			cinfo.marker.discarded_bytes += entropy.bitstate.bits_left / 8;
			entropy.bitstate.bits_left = 0;

			/* Advance past the RSTn marker */
			if (! read_restart_marker (cinfo))
				return false;

			/* Re-initialize DC predictions to 0 */
			for (ci = 0; ci < cinfo.comps_in_scan; ci++)
				entropy.saved.last_dc_val[ci] = 0;

			/* Reset restart counter */
			entropy.restarts_to_go = cinfo.restart_interval;

			/* Reset out-of-data flag, unless read_restart_marker left us smack up
			 * against a marker.	In that case we will end up treating the next data
			 * segment as empty, and we can avoid producing bogus output pixels by
			 * leaving the flag set.
			 */
			if (cinfo.unread_marker == 0)
				entropy.insufficient_data = false;

			return true;
		}
	}
	
	static final class phuff_entropy_decoder extends jpeg_entropy_decoder {

		/* These fields are loaded into local variables at start of each MCU.
		 * In case of suspension, we exit WITHOUT updating them.
		 */
		bitread_perm_state bitstate = new bitread_perm_state();	/* Bit buffer at start of MCU */
		savable_state saved = new savable_state();		/* Other state at start of MCU */

		/* These fields are NOT loaded into local working state. */
		int restarts_to_go;	/* MCUs left in this restart interval */

		/* Pointers to derived tables (these workspaces have image lifespan) */
		d_derived_tbl[] derived_tbls = new d_derived_tbl[NUM_HUFF_TBLS];

		d_derived_tbl ac_derived_tbl; /* active table during an AC scan */
		
		int[] newnz_pos = new int[DCTSIZE2];
			
		@Override
		void start_pass (jpeg_decompress_struct cinfo) {
			start_pass_phuff_decoder(cinfo);
		}
			
		@Override
		boolean decode_mcu (jpeg_decompress_struct cinfo, short[][] MCU_data) {
			boolean is_DC_band = (cinfo.Ss == 0);
			if (cinfo.Ah == 0) {
				if (is_DC_band)
					return decode_mcu_DC_first(cinfo, MCU_data);
				else
					return decode_mcu_AC_first(cinfo, MCU_data);
			} else {
				if (is_DC_band)
					return decode_mcu_DC_refine(cinfo, MCU_data);
				else
					return decode_mcu_AC_refine(cinfo, MCU_data);
			}
		}
			
		boolean decode_mcu_DC_refine (jpeg_decompress_struct cinfo, short[][] MCU_data) {
			phuff_entropy_decoder entropy = this;
			int p1 = 1 << cinfo.Al;	/* 1 in the bit position being coded */
			int blkn;
			short[] block;
//			BITREAD_STATE_VARS;
			int get_buffer;
			int bits_left;
//			bitread_working_state br_state = new bitread_working_state();
			bitread_working_state br_state = br_state_local;
					
			/* Process restart marker if needed; may have to suspend */
			if (cinfo.restart_interval != 0) {
				if (entropy.restarts_to_go == 0)
					if (! process_restart(cinfo))
						return false;
			}

			/* Not worth the cycles to check insufficient_data here,
			 * since we will not change the data anyway if we read zeroes.
			 */

			/* Load up working state */
//			BITREAD_LOAD_STATE(cinfo,entropy.bitstate);
			br_state.cinfo = cinfo;
			br_state.buffer = cinfo.buffer; 
			br_state.bytes_in_buffer = cinfo.bytes_in_buffer;
			br_state.bytes_offset = cinfo.bytes_offset;
			get_buffer = entropy.bitstate.get_buffer;
			bits_left = entropy.bitstate.bits_left;
					
			/* Outer loop handles each block in the MCU */

			for (blkn = 0; blkn < cinfo.blocks_in_MCU; blkn++) {
				block = MCU_data[blkn];

				/* Encoded data is simply the next bit of the two's-complement DC value */
//				CHECK_BIT_BUFFER(br_state, 1, return FALSE);
				{
				if (bits_left < (1)) {
					if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,1)) {
						 return false;
					}
					get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
				}
				}
//				if (GET_BITS(1))
				if ((( (get_buffer >> (bits_left -= (1)))) & ((1<<(1))-1)) != 0)
					block[0] |= p1;
					/* Note: since we use |=, repeating the assignment later is safe */
			}

			/* Completed MCU, so update state */
//			BITREAD_SAVE_STATE(cinfo,entropy.bitstate);
			cinfo.buffer = br_state.buffer;
			cinfo.bytes_in_buffer = br_state.bytes_in_buffer;
			cinfo.bytes_offset = br_state.bytes_offset;
			entropy.bitstate.get_buffer = get_buffer;
			entropy.bitstate.bits_left = bits_left;
					
			/* Account for restart interval (no-op if not using restarts) */
			entropy.restarts_to_go--;

			return true;

		}
			
		boolean decode_mcu_AC_refine (jpeg_decompress_struct cinfo, short[][] MCU_data) {
			phuff_entropy_decoder entropy = this;
			int Se = cinfo.Se;
			int p1 = 1 << cinfo.Al;	/* 1 in the bit position being coded */
			int m1 = (-1) << cinfo.Al;	/* -1 in the bit position being coded */
			int s = 0, k, r;
			int EOBRUN;
			short[] block;
			short[] thiscoef;
//			BITREAD_STATE_VARS;
			int get_buffer;
			int bits_left;
//			bitread_working_state br_state = new bitread_working_state();
			bitread_working_state br_state = br_state_local;
				
			d_derived_tbl tbl;
			int num_newnz;
			int[] newnz_pos = entropy.newnz_pos;

				/* Process restart marker if needed; may have to suspend */
			if (cinfo.restart_interval != 0) {
				if (entropy.restarts_to_go == 0)
					if (! process_restart(cinfo))
						return false;
			}

			/* If we've run out of data, don't modify the MCU.
			 */
			if (! entropy.insufficient_data) {

				/* Load up working state */
//				BITREAD_LOAD_STATE(cinfo,entropy.bitstate);
				br_state.cinfo = cinfo;
				br_state.buffer = cinfo.buffer; 
				br_state.bytes_in_buffer = cinfo.bytes_in_buffer;
				br_state.bytes_offset = cinfo.bytes_offset;
				get_buffer = entropy.bitstate.get_buffer;
				bits_left = entropy.bitstate.bits_left;
					
				EOBRUN = entropy.saved.EOBRUN; /* only part of saved state we need */

				/* There is always only one block per MCU */
				block = MCU_data[0];
				tbl = entropy.ac_derived_tbl;

				/* If we are forced to suspend, we must undo the assignments to any newly
				 * nonzero coefficients in the block, because otherwise we'd get confused
				 * next time about which coefficients were already nonzero.
				 * But we need not undo addition of bits to already-nonzero coefficients;
				 * instead, we can test the current bit to see if we already did it.
				 */
				num_newnz = 0;

				/* initialize coefficient loop counter to start of band */
				k = cinfo.Ss;

				if (EOBRUN == 0) {
					for (; k <= Se; k++) {
//						HUFF_DECODE(s, br_state, tbl, goto undoit, label3);
						{
						int nb = 0, look;
						if (bits_left < HUFF_LOOKAHEAD) {
							if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
//								failaction;
								while (num_newnz > 0)
									block[newnz_pos[--num_newnz]] = 0;

								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							if (bits_left < HUFF_LOOKAHEAD) {
								nb = 1; 
//								goto slowlabel;
								if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) {
//									failaction;
									while (num_newnz > 0)
										block[newnz_pos[--num_newnz]] = 0;

									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							}
						}
						if (nb != 1) {
//							look = PEEK_BITS(HUFF_LOOKAHEAD);
							look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));
							if ((nb = tbl.look_nbits[look]) != 0) {
//								DROP_BITS(nb);
								bits_left -= nb;
								s = tbl.look_sym[look] & 0xFF;
							} else {
								nb = HUFF_LOOKAHEAD+1;
//								slowlabel:
								if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) { 
//									failaction;
									while (num_newnz > 0)
										block[newnz_pos[--num_newnz]] = 0;

									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							}
						}
						}
						r = s >> 4;
						s &= 15;
						if (s != 0) {
							if (s != 1) {		/* size of new coef should always be 1 */
//								WARNMS(cinfo, JWRN_HUFF_BAD_CODE);
							}
//							CHECK_BIT_BUFFER(br_state, 1, goto undoit);
							{
							if (bits_left < (1)) {
								if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,1)) {
//									failaction;
									while (num_newnz > 0)
										block[newnz_pos[--num_newnz]] = 0;

									return false;
								}
								get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
								}
							}
//							if (GET_BITS(1))
							if ((( (get_buffer >> (bits_left -= (1)))) & ((1<<(1))-1)) != 0)
								s = p1;		/* newly nonzero coef is positive */
							else
								s = m1;		/* newly nonzero coef is negative */
						} else {
							if (r != 15) {
								EOBRUN = 1 << r;	/* EOBr, run length is 2^r + appended bits */
								if (r != 0) {
//									CHECK_BIT_BUFFER(br_state, r, goto undoit);
									{
									if (bits_left < (r)) {
										if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,r)) {
//											failaction;
											while (num_newnz > 0)
												block[newnz_pos[--num_newnz]] = 0;

											return false;
										}
										get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
									}
									}
//									r = GET_BITS(r);
									r = (( (get_buffer >> (bits_left -= (r)))) & ((1<<(r))-1));
									EOBRUN += r;
								}
								break;		/* rest of block is handled by EOB logic */
							}
							/* note s = 0 for processing ZRL */
						}
						/* Advance over already-nonzero coefs and r still-zero coefs,
						 * appending correction bits to the nonzeroes.	A correction bit is 1
						 * if the absolute value of the coefficient must be increased.
						 */
						do {
							thiscoef = block;
							int thiscoef_offset = jpeg_natural_order[k];
							if (thiscoef[thiscoef_offset] != 0) {
//								CHECK_BIT_BUFFER(br_state, 1, goto undoit);
								{
								if (bits_left < (1)) {
									if (!jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,1)) {
//										failaction;
										while (num_newnz > 0)
											block[newnz_pos[--num_newnz]] = 0;

										return false;
									}
									get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
								}
								}
//								if (GET_BITS(1)) {
								if ((( (get_buffer >> (bits_left -= (1)))) & ((1<<(1))-1)) != 0) {
									if ((thiscoef[thiscoef_offset] & p1) == 0) { /* do nothing if already set it */
										if (thiscoef[thiscoef_offset] >= 0)
											thiscoef[thiscoef_offset] += p1;
										else
											thiscoef[thiscoef_offset] += m1;
									}
								}
							} else {
								if (--r < 0)
									break;		/* reached target zero coefficient */
							}
							k++;
						} while (k <= Se);
						if (s != 0) {
							int pos = jpeg_natural_order[k];
							/* Output newly nonzero coefficient */
							block[pos] = (short) s;
							/* Remember its position in case we have to suspend */
							newnz_pos[num_newnz++] = pos;
						}
					}
				}

				if (EOBRUN > 0) {
					/* Scan any remaining coefficient positions after the end-of-band
					 * (the last newly nonzero coefficient, if any).	Append a correction
					 * bit to each already-nonzero coefficient.	A correction bit is 1
					 * if the absolute value of the coefficient must be increased.
					 */
					for (; k <= Se; k++) {
						thiscoef = block;
						int thiscoef_offset = jpeg_natural_order[k];
						if (thiscoef[thiscoef_offset] != 0) {
//							CHECK_BIT_BUFFER(br_state, 1, goto undoit);
							{
							if (bits_left < (1)) {
								if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,1)) {
//									failaction;
									while (num_newnz > 0)
										block[newnz_pos[--num_newnz]] = 0;
	
									return false;
								}
								get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
							}
							}
//							if (GET_BITS(1)) {
							if ((( (get_buffer >> (bits_left -= (1)))) & ((1<<(1))-1)) != 0) {
								if ((thiscoef[thiscoef_offset] & p1) == 0) { /* do nothing if already changed it */
									if (thiscoef[thiscoef_offset] >= 0)
										thiscoef[thiscoef_offset] += p1;
									else
										thiscoef[thiscoef_offset] += m1;
								}
							}
						}
					}
						/* Count one block completed in EOB run */
					EOBRUN--;
				}

				/* Completed MCU, so update state */
//				BITREAD_SAVE_STATE(cinfo,entropy.bitstate);
				cinfo.buffer = br_state.buffer;
				cinfo.bytes_in_buffer = br_state.bytes_in_buffer;
				cinfo.bytes_offset = br_state.bytes_offset;
				entropy.bitstate.get_buffer = get_buffer;
				entropy.bitstate.bits_left = bits_left;
						
				entropy.saved.EOBRUN = EOBRUN; /* only part of saved state we need */
			}

			/* Account for restart interval (no-op if not using restarts) */
			entropy.restarts_to_go--;

			return true;

//			undoit:
//				/* Re-zero any output coefficients that we made newly nonzero */
//				while (num_newnz > 0)
//					(*block)[newnz_pos[--num_newnz]] = 0;
//
//				return false;

		}			
			
		boolean decode_mcu_AC_first (jpeg_decompress_struct cinfo, short[][] MCU_data) {
			phuff_entropy_decoder entropy = this;
			int Se = cinfo.Se;
			int Al = cinfo.Al;
			int s = 0, k, r;
			int EOBRUN;
			short[] block;
//			BITREAD_STATE_VARS;
			int get_buffer;
			int bits_left;
//			bitread_working_state br_state = new bitread_working_state();
			bitread_working_state br_state = br_state_local;
					
			d_derived_tbl tbl;

			/* Process restart marker if needed; may have to suspend */
			if (cinfo.restart_interval != 0) {
				if (entropy.restarts_to_go == 0)
					if (! process_restart(cinfo))
						return false;
			}

			/* If we've run out of data, just leave the MCU set to zeroes.
			 * This way, we return uniform gray for the remainder of the segment.
			 */
			if (! entropy.insufficient_data) {

				/* Load up working state.
				 * We can avoid loading/saving bitread state if in an EOB run.
				 */
				EOBRUN = entropy.saved.EOBRUN;	/* only part of saved state we need */

				/* There is always only one block per MCU */

				if (EOBRUN > 0)		/* if it's a band of zeroes... */
					EOBRUN--;			/* ...process it now (we do nothing) */
				else {
//					BITREAD_LOAD_STATE(cinfo,entropy.bitstate);
					br_state.cinfo = cinfo;
					br_state.buffer = cinfo.buffer; 
					br_state.bytes_in_buffer = cinfo.bytes_in_buffer;
					br_state.bytes_offset = cinfo.bytes_offset;
					get_buffer = entropy.bitstate.get_buffer;
					bits_left = entropy.bitstate.bits_left;
						
					block = MCU_data[0];
					tbl = entropy.ac_derived_tbl;

					for (k = cinfo.Ss; k <= Se; k++) {
//						HUFF_DECODE(s, br_state, tbl, return FALSE, label2);
						{
						int nb = 0, look;
						if (bits_left < HUFF_LOOKAHEAD) {
							if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							if (bits_left < HUFF_LOOKAHEAD) {
								nb = 1;
//								goto slowlabel;
								if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) {
									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							}
						}
						if (nb != 1) {
//							look = PEEK_BITS(HUFF_LOOKAHEAD);
							look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));

							if ((nb = tbl.look_nbits[look]) != 0) {
//								DROP_BITS(nb);
								bits_left -= nb;
								s = tbl.look_sym[look] & 0xFF;
							} else {
								nb = HUFF_LOOKAHEAD+1;
//								slowlabel:
								if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) {
									return false;
								}
								get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
							}
						}
						}
						r = s >> 4;
						s &= 15;
						if (s != 0) {
							k += r;
//							CHECK_BIT_BUFFER(br_state, s, return FALSE);
							{
							if (bits_left < (s)) {
								if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,s)) {
									return false;
								}
								get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
							}
							}
//							r = GET_BITS(s);
							r = (( (get_buffer >> (bits_left -= (s)))) & ((1<<(s))-1));
//							s = HUFF_EXTEND(r, s);
							s = ((r) < extend_test[s] ? (r) + extend_offset[s] : (r));
							/* Scale and output coefficient in natural (dezigzagged) order */
							block[jpeg_natural_order[k]] = (short) (s << Al);
						} else {
							if (r == 15) {	/* ZRL */
								k += 15;		/* skip 15 zeroes in band */
							} else {		/* EOBr, run length is 2^r + appended bits */
								EOBRUN = 1 << r;
								if (r != 0) {		/* EOBr, r > 0 */
//									CHECK_BIT_BUFFER(br_state, r, return FALSE);
									{
									if (bits_left < (r)) {
										if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,r)) {
											return false;
										}
										get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
									}
									}
//									r = GET_BITS(r);
									r = (( (get_buffer >> (bits_left -= (r)))) & ((1<<(r))-1));
									EOBRUN += r;
								}
								EOBRUN--;		/* this band is processed at this moment */
								break;		/* force end-of-band */
							}
						}
					}

//					BITREAD_SAVE_STATE(cinfo,entropy.bitstate);
					cinfo.buffer = br_state.buffer;
					cinfo.bytes_in_buffer = br_state.bytes_in_buffer;
					cinfo.bytes_offset = br_state.bytes_offset;
					entropy.bitstate.get_buffer = get_buffer;
					entropy.bitstate.bits_left = bits_left;
				}

				/* Completed MCU, so update state */
				entropy.saved.EOBRUN = EOBRUN;	/* only part of saved state we need */
			}

			/* Account for restart interval (no-op if not using restarts) */
			entropy.restarts_to_go--;

			return true;
		}
			
		boolean decode_mcu_DC_first (jpeg_decompress_struct cinfo, short[][] MCU_data) {	 
			phuff_entropy_decoder entropy = this;
			int Al = cinfo.Al;
			int s = 0, r;
			int blkn, ci;
			short[] block;
//			BITREAD_STATE_VARS;
			int get_buffer;
			int bits_left;
//			bitread_working_state br_state = new bitread_working_state();
			bitread_working_state br_state = br_state_local;
				
//			savable_state state = new savable_state();
			savable_state state = state_local;
			d_derived_tbl tbl;
			jpeg_component_info compptr;

			/* Process restart marker if needed; may have to suspend */
			if (cinfo.restart_interval != 0) {
				if (entropy.restarts_to_go == 0)
					if (! process_restart(cinfo))
						return false;
			}

			/* If we've run out of data, just leave the MCU set to zeroes.
			 * This way, we return uniform gray for the remainder of the segment.
			 */
			if (! entropy.insufficient_data) {

				/* Load up working state */
//				BITREAD_LOAD_STATE(cinfo,entropy.bitstate);
				br_state.cinfo = cinfo;
				br_state.buffer = cinfo.buffer; 
				br_state.bytes_in_buffer = cinfo.bytes_in_buffer;
				br_state.bytes_offset = cinfo.bytes_offset;
				get_buffer = entropy.bitstate.get_buffer;
				bits_left = entropy.bitstate.bits_left;
					
//				ASSIGN_STATE(state, entropy.saved);
				state.EOBRUN = entropy.saved.EOBRUN;
				state.last_dc_val[0] = entropy.saved.last_dc_val[0];
				state.last_dc_val[1] = entropy.saved.last_dc_val[1];
				state.last_dc_val[2] = entropy.saved.last_dc_val[2];
				state.last_dc_val[3] = entropy.saved.last_dc_val[3];
					
				/* Outer loop handles each block in the MCU */

				for (blkn = 0; blkn < cinfo.blocks_in_MCU; blkn++) {
					block = MCU_data[blkn];
					ci = cinfo.MCU_membership[blkn];
					compptr = cinfo.cur_comp_info[ci];
					tbl = entropy.derived_tbls[compptr.dc_tbl_no];

					/* Decode a single block's worth of coefficients */

					/* Section F.2.2.1: decode the DC coefficient difference */
//					HUFF_DECODE(s, br_state, tbl, return FALSE, label1);
					{
					int nb = 0, look;
					if (bits_left < HUFF_LOOKAHEAD) {
						if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left, 0)) {
							return false;
						}
						get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						if (bits_left < HUFF_LOOKAHEAD) {
							nb = 1;
//							goto slowlabel;
							if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) {
								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						}
					}
					if (nb != 1) {
//						look = PEEK_BITS(HUFF_LOOKAHEAD);
						look = (( (get_buffer >> (bits_left -	(HUFF_LOOKAHEAD)))) & ((1<<(HUFF_LOOKAHEAD))-1));

						if ((nb = tbl.look_nbits[look]) != 0) {
//							DROP_BITS(nb);
							bits_left -= nb;
							s = tbl.look_sym[look] & 0xFF;
						} else {
							nb = HUFF_LOOKAHEAD+1;
//							slowlabel:
							if ((s=jpeg_huff_decode(br_state,get_buffer,bits_left,tbl,nb)) < 0) {
								return false;
							}
							get_buffer = br_state.get_buffer; bits_left = br_state.bits_left;
						}
					}
					}
					if (s != 0) {
//						CHECK_BIT_BUFFER(br_state, s, return FALSE);
						{
						if (bits_left < (s)) {
							if (! jpeg_fill_bit_buffer(br_state,get_buffer,bits_left,s)) {
								return false;
							}
							get_buffer = (br_state).get_buffer; bits_left = (br_state).bits_left;
						}
						}
//						r = GET_BITS(s);
						r = (( (get_buffer >> (bits_left -= (s)))) & ((1<<(s))-1));
//						s = HUFF_EXTEND(r, s);
						s = ((r) < extend_test[s] ? (r) + extend_offset[s] : (r));
					}

						/* Convert DC difference to actual value, update last_dc_val */
					s += state.last_dc_val[ci];
					state.last_dc_val[ci] = s;
					/* Scale and output the coefficient (assumes jpeg_natural_order[0]=0) */
					block[0] = (short) (s << Al);
				}

				/* Completed MCU, so update state */
//				BITREAD_SAVE_STATE(cinfo,entropy.bitstate);
				cinfo.buffer = br_state.buffer;
				cinfo.bytes_in_buffer = br_state.bytes_in_buffer;
				cinfo.bytes_offset = br_state.bytes_offset;
				entropy.bitstate.get_buffer = get_buffer;
				entropy.bitstate.bits_left = bits_left;
//				ASSIGN_STATE(entropy.saved, state);
				entropy.saved.EOBRUN = state.EOBRUN;
				entropy.saved.last_dc_val[0] = state.last_dc_val[0];
				entropy.saved.last_dc_val[1] = state.last_dc_val[1];
				entropy.saved.last_dc_val[2] = state.last_dc_val[2];
				entropy.saved.last_dc_val[3] = state.last_dc_val[3];
			}

			/* Account for restart interval (no-op if not using restarts) */
			entropy.restarts_to_go--;

			return true;
		}
			
		boolean process_restart (jpeg_decompress_struct cinfo) {
			phuff_entropy_decoder entropy = this;
			int ci;

			/* Throw away any unused bits remaining in bit buffer; */
			/* include any full bytes in next_marker's count of discarded bytes */
			cinfo.marker.discarded_bytes += entropy.bitstate.bits_left / 8;
			entropy.bitstate.bits_left = 0;

			/* Advance past the RSTn marker */
			if (! read_restart_marker (cinfo))
				return false;

			/* Re-initialize DC predictions to 0 */
			for (ci = 0; ci < cinfo.comps_in_scan; ci++)
				entropy.saved.last_dc_val[ci] = 0;
				/* Re-init EOB run count, too */
			entropy.saved.EOBRUN = 0;

			/* Reset restart counter */
			entropy.restarts_to_go = cinfo.restart_interval;

			/* Reset out-of-data flag, unless read_restart_marker left us smack up
			 * against a marker.	In that case we will end up treating the next data
			 * segment as empty, and we can avoid producing bogus output pixels by
			 * leaving the flag set.
			 */
			if (cinfo.unread_marker == 0)
				entropy.insufficient_data = false;

			return true;
		}

		void start_pass_phuff_decoder (jpeg_decompress_struct cinfo) {
			phuff_entropy_decoder entropy = this;
			boolean is_DC_band, bad;
			int ci, coefi, tbl;
			int[] coef_bit_ptr;
			jpeg_component_info compptr;

			is_DC_band = (cinfo.Ss == 0);

			/* Validate scan parameters */
			bad = false;
			if (is_DC_band) {
				if (cinfo.Se != 0)
					bad = true;
			} else {
				/* need not check Ss/Se < 0 since they came from unsigned bytes */
				if (cinfo.Ss > cinfo.Se || cinfo.Se >= DCTSIZE2)
					bad = true;
				/* AC scans may have only one component */
				if (cinfo.comps_in_scan != 1)
					bad = true;
			}
			if (cinfo.Ah != 0) {
				/* Successive approximation refinement scan: must have Al = Ah-1. */
				if (cinfo.Al != cinfo.Ah-1)
					bad = true;
			}
			if (cinfo.Al > 13)		/* need not check for < 0 */
				bad = true;
			/* Arguably the maximum Al value should be less than 13 for 8-bit precision,
			 * but the spec doesn't say so, and we try to be liberal about what we
			 * accept.	Note: large Al values could result in out-of-range DC
			 * coefficients during early scans, leading to bizarre displays due to
			 * overflows in the IDCT math.	But we won't crash.
			 */
			if (bad)
				error();
//				ERREXIT4(cinfo, JERR_BAD_PROGRESSION, cinfo.Ss, cinfo.Se, cinfo.Ah, cinfo.Al);
			/* Update progression status, and verify that scan order is legal.
			 * Note that inter-scan inconsistencies are treated as warnings
			 * not fatal errors ... not clear if this is right way to behave.
			 */
			for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
				int cindex = cinfo.cur_comp_info[ci].component_index;
				coef_bit_ptr = cinfo.coef_bits[cindex];
				if (!is_DC_band && coef_bit_ptr[0] < 0) {/* AC without prior DC scan */
//					WARNMS2(cinfo, JWRN_BOGUS_PROGRESSION, cindex, 0);
				}
				for (coefi = cinfo.Ss; coefi <= cinfo.Se; coefi++) {
					int expected = (coef_bit_ptr[coefi] < 0) ? 0 : coef_bit_ptr[coefi];
					if (cinfo.Ah != expected) {
//						WARNMS2(cinfo, JWRN_BOGUS_PROGRESSION, cindex, coefi);
					}
					coef_bit_ptr[coefi] = cinfo.Al;
				}
			}

			/* Select MCU decoding routine */
//			if (cinfo.Ah == 0) {
//				if (is_DC_band)
//					entropy.pub.decode_mcu = decode_mcu_DC_first;
//				else
//					entropy.pub.decode_mcu = decode_mcu_AC_first;
//			} else {
//				if (is_DC_band)
//					entropy.pub.decode_mcu = decode_mcu_DC_refine;
//				else
//					entropy.pub.decode_mcu = decode_mcu_AC_refine;
//			}

			for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
				compptr = cinfo.cur_comp_info[ci];
				/* Make sure requested tables are present, and compute derived tables.
				 * We may build same derived table more than once, but it's not expensive.
				 */
				if (is_DC_band) {
					if (cinfo.Ah == 0) {	/* DC refinement needs no table */
						tbl = compptr.dc_tbl_no;
						jpeg_make_d_derived_tbl(cinfo, true, tbl, entropy.derived_tbls[tbl] = new d_derived_tbl());
					}
				} else {
					tbl = compptr.ac_tbl_no;
					jpeg_make_d_derived_tbl(cinfo, false, tbl, entropy.derived_tbls[tbl] = new d_derived_tbl());
					/* remember the single active table */
					entropy.ac_derived_tbl = entropy.derived_tbls[tbl];
				}
				/* Initialize DC predictions to 0 */
				entropy.saved.last_dc_val[ci] = 0;
			}

			/* Initialize bitread state variables */
			entropy.bitstate.bits_left = 0;
			entropy.bitstate.get_buffer = 0; /* unnecessary, but keeps Purify quiet */
			entropy.insufficient_data = false;

			/* Initialize private state variables */
			entropy.saved.EOBRUN = 0;

			/* Initialize restart counter */
			entropy.restarts_to_go = cinfo.restart_interval;
		}

	}
	
	static final class jpeg_component_info {
		/* These values are fixed over the whole image. */
		/* For compression, they must be supplied by parameter setup; */
		/* for decompression, they are read from the SOF marker. */
		int component_id;		/* identifier for this component (0..255) */
		int component_index;		/* its index in SOF or cinfo.comp_info[] */
		int h_samp_factor;		/* horizontal sampling factor (1..4) */
		int v_samp_factor;		/* vertical sampling factor (1..4) */
		int quant_tbl_no;		/* quantization table selector (0..3) */
		/* These values may vary between scans. */
		/* For compression, they must be supplied by parameter setup; */
		/* for decompression, they are read from the SOS marker. */
		/* The decompressor output side may not use these variables. */
		int dc_tbl_no;		/* DC entropy table selector (0..3) */
		int ac_tbl_no;		/* AC entropy table selector (0..3) */
		
		/* Remaining fields should be treated as private by applications. */
		
		/* These values are computed during compression or decompression startup: */
		/* Component's size in DCT blocks.
		 * Any dummy blocks added to complete an MCU are not counted; therefore
		 * these values do not depend on whether a scan is interleaved or not.
		 */
		int width_in_blocks;
		int height_in_blocks;
		/* Size of a DCT block in samples.	Always DCTSIZE for compression.
		 * For decompression this is the size of the output from one DCT block,
		 * reflecting any scaling we choose to apply during the IDCT step.
		 * Values of 1,2,4,8 are likely to be supported.	Note that different
		 * components may receive different IDCT scalings.
		 */
		int DCT_scaled_size;
		/* The downsampled dimensions are the component's actual, unpadded number
		 * of samples at the main buffer (preprocessing/compression interface), thus
		 * downsampled_width = ceil(image_width * Hi/Hmax)
		 * and similarly for height.	For decompression, IDCT scaling is included, so
		 * downsampled_width = ceil(image_width * Hi/Hmax * DCT_scaled_size/DCTSIZE)
		 */
		int downsampled_width;	 /* actual width in samples */
		int downsampled_height; /* actual height in samples */
		/* This flag is used only for decompression.	In cases where some of the
		 * components will be ignored (eg grayscale output from YCbCr image),
		 * we can skip most computations for the unused components.
		 */
		boolean component_needed;	/* do we need the value of this component? */

		/* These values are computed before starting a scan of the component. */
		/* The decompressor output side may not use these variables. */
		int MCU_width;		/* number of blocks per MCU, horizontally */
		int MCU_height;		/* number of blocks per MCU, vertically */
		int MCU_blocks;		/* MCU_width * MCU_height */
		int MCU_sample_width;		/* MCU width in samples, MCU_width*DCT_scaled_size */
		int last_col_width;		/* # of non-dummy blocks across in last MCU */
		int last_row_height;		/* # of non-dummy blocks down in last MCU */

		/* Saved quantization table for component; null if none yet saved.
		 * See jdinput.c comments about the need for this information.
		 * This field is currently used only for decompression.
		 */
		JQUANT_TBL quant_table;

		/* Private per-component storage for DCT or IDCT subsystem. */
		int[] dct_table;
	}
	
	static final class jpeg_color_quantizer {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo, boolean is_pre_scan));
//		JMETHOD(void, color_quantize, (j_decompress_ptr cinfo,
//					 JSAMPARRAY input_buf, JSAMPARRAY output_buf,
//					 int num_rows));
//		JMETHOD(void, finish_pass, (j_decompress_ptr cinfo));
//		JMETHOD(void, new_color_map, (j_decompress_ptr cinfo));
		
		/* Initially allocated colormap is saved here */
		int[][] sv_colormap;	/* The color map as a 2-D pixel array */
		int sv_actual;		/* number of entries in use */

		int[][] colorindex;	/* Precomputed mapping for speed */
		/* colorindex[i][j] = index of color closest to pixel value j in component i,
		 * premultiplied as described above.	Since colormap indexes must fit into
		 * JSAMPLEs, the entries of this array will too.
		 */
		boolean is_padded;		/* is the colorindex padded for odither? */

		int[] Ncolors = new int [MAX_Q_COMPS];	/* # of values alloced to each component */

		/* Variables for ordered dithering */
		int row_index;		/* cur row's vertical index in dither matrix */
//			ODITHER_MATRIX_PTR odither[MAX_Q_COMPS]; /* one dither array per component */

		/* Variables for Floyd-Steinberg dithering */
//			FSERRPTR fserrors[MAX_Q_COMPS]; /* accumulated errors */
		boolean on_odd_row;	
			
		void start_pass (jpeg_decompress_struct cinfo, boolean is_pre_scan) {
			error();
		}
	}
	
	static final class jpeg_upsampler {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo));
//		JMETHOD(void, upsample, (j_decompress_ptr cinfo,
//					 JSAMPIMAGE input_buf,
//					 JDIMENSION *in_row_group_ctr,
//					 JDIMENSION in_row_groups_avail,
//					 JSAMPARRAY output_buf,
//					 JDIMENSION *out_row_ctr,
//					 JDIMENSION out_rows_avail));

		boolean need_context_rows;	/* TRUE if need rows above & below */
			
		/* Color conversion buffer.	When using separate upsampling and color
		 * conversion steps, this buffer holds one upsampled row group until it
		 * has been color converted and output.
		 * Note: we do not allocate any storage for component(s) which are full-size,
		 * ie do not need rescaling.	The corresponding entry of color_buf[] is
		 * simply set to point to the input data array, thereby avoiding copying.
		 */
		byte[][][] color_buf = new byte[MAX_COMPONENTS][][];
		int[] color_buf_offset = new int[MAX_COMPONENTS];

		/* Per-component upsampling method pointers */
		int[] methods = new int[MAX_COMPONENTS];

		int next_row_out;		/* counts rows emitted from color_buf */
		int rows_to_go;	/* counts rows remaining in image */

		/* Height of an input row group for each component. */
		int[] rowgroup_height = new int[MAX_COMPONENTS];

		/* These arrays save pixel expansion factors so that int_expand need not
		 * recompute them each time.	They are unused for other upsampling methods.
		 */
		byte[] h_expand = new byte[MAX_COMPONENTS];
		byte[] v_expand = new byte[MAX_COMPONENTS];
			
		void start_pass (jpeg_decompress_struct cinfo) {
			jpeg_upsampler upsample = cinfo.upsample;

			/* Mark the conversion buffer empty */
			upsample.next_row_out = cinfo.max_v_samp_factor;
			/* Initialize total-height counter for detecting bottom of image */
			upsample.rows_to_go = cinfo.output_height;
		}
			
	}
	
	static final class jpeg_marker_reader {
		/* Read a restart marker --- exported for use by entropy decoder only */
//		jpeg_marker_parser_method read_restart_marker;

		/* State of marker reader --- nominally internal, but applications
		 * supplying COM or APPn handlers might like to know the state.
		 */
		boolean saw_SOI;		/* found SOI? */
		boolean saw_SOF;		/* found SOF? */
		int next_restart_num;		/* next restart number expected (0-7) */
		int discarded_bytes;	/* # of bytes skipped looking for a marker */
		
		/* Application-overridable marker processing methods */
//		jpeg_marker_parser_method process_COM;
//		jpeg_marker_parser_method process_APPn[16];

		/* Limit on marker data length to save for each marker type */
		int length_limit_COM;
		int[] length_limit_APPn = new int[16];

		/* Status of COM/APPn marker saving */
//		jpeg_marker_reader cur_marker;	/* null if not processing a marker */
//		int bytes_read;		/* data bytes read so far in marker */
		/* Note: cur_marker is not linked into marker_list until it's all read. */
	}
	
	
	static final class jpeg_d_main_controller {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo, J_BUF_MODE pass_mode));
		int process_data;
				
		 /* Pointer to allocated workspace (M or M+2 row groups). */
		byte[][][] buffer = new byte[MAX_COMPONENTS][][];
		int[] buffer_offset = new int[MAX_COMPONENTS];

		boolean buffer_full;		/* Have we gotten an iMCU row from decoder? */
		int[] rowgroup_ctr = new int[1];	/* counts row groups output to postprocessor */

		/* Remaining fields are only used in the context case. */

		/* These are the master pointers to the funny-order pointer lists. */
		byte[][][][] xbuffer = new byte[2][][][];	/* pointers to weird pointer lists */
		int[][] xbuffer_offset = new int[2][];

		int whichptr;			/* indicates which pointer set is now in use */
		int context_state;		/* process_data state machine status */
		int rowgroups_avail;	/* row groups available to postprocessor */
		int iMCU_row_ctr;	/* counts iMCU rows to detect image top/bot */
					
		void start_pass (jpeg_decompress_struct cinfo, int pass_mode) {
			jpeg_d_main_controller main = cinfo.main;

			switch (pass_mode) {
				case JBUF_PASS_THRU:
					if (cinfo.upsample.need_context_rows) {
						main.process_data = PROCESS_DATA_CONTEXT_MAIN;
						make_funny_pointers(cinfo); /* Create the xbuffer[] lists */
						main.whichptr = 0;	/* Read first iMCU row into xbuffer[0] */
						main.context_state = CTX_PREPARE_FOR_IMCU;
						main.iMCU_row_ctr = 0;
					} else {
						/* Simple case with no context needed */
						main.process_data = PROCESS_DATA_SIMPLE_MAIN;
					}
					main.buffer_full = false;	/* Mark buffer empty */
					main.rowgroup_ctr[0] = 0;
					break;
//				#ifdef QUANT_2PASS_SUPPORTED
//				case JBUF_CRANK_DEST:
//					/* For last pass of 2-pass quantization, just crank the postprocessor */
//					main.process_data = PROCESS_DATA_CRANK_POST;
//					break;
//				#endif
				default:
					error();
//					ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);
					break;
			}
		}
					
	}

	static final class jpeg_decomp_master {
//		JMETHOD(void, prepare_for_output_pass, (j_decompress_ptr cinfo));
//		JMETHOD(void, finish_output_pass, (j_decompress_ptr cinfo));

		/* State variables made visible to other modules */
		boolean is_dummy_pass;

		int pass_number;		/* # of passes completed */

		boolean using_merged_upsample; /* true if using merged upsample/cconvert */

		/* Saved references to initialized quantizer modules,
		 * in case we need to switch modes.
		 */
		jpeg_color_quantizer quantizer_1pass;
		jpeg_color_quantizer quantizer_2pass;
	}
	
	static final class jpeg_inverse_dct {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo));
//		/* It is useful to allow each component to have a separate IDCT method. */
//		inverse_DCT_method_ptr inverse_DCT[MAX_COMPONENTS];
		int[] cur_method = new int[MAX_COMPONENTS];
			
		void start_pass (jpeg_decompress_struct cinfo) {
			jpeg_inverse_dct idct = cinfo.idct;
			int ci, i;
			jpeg_component_info compptr;
			int method = 0;
//			inverse_DCT_method_ptr method_ptr = NULL;
			JQUANT_TBL qtbl;

			for (ci = 0; ci < cinfo.num_components; ci++) {
				compptr = cinfo.comp_info[ci];
				/* Select the proper IDCT routine for this component's scaling */
				switch (compptr.DCT_scaled_size) {
//					#ifdef IDCT_SCALING_SUPPORTED
//					case 1:
//						method_ptr = jpeg_idct_1x1;
//						method = JDCT_ISLOW;	/* jidctred uses islow-style table */
//						break;
//					case 2:
//						method_ptr = jpeg_idct_2x2;
//						method = JDCT_ISLOW;	/* jidctred uses islow-style table */
//						break;
//					case 4:
//						method_ptr = jpeg_idct_4x4;
//						method = JDCT_ISLOW;	/* jidctred uses islow-style table */
//						break;
//					#endif
					case DCTSIZE:
						switch (cinfo.dct_method) {
//							#ifdef DCT_ISLOW_SUPPORTED
							case JDCT_ISLOW:
//								method_ptr = jpeg_idct_islow;
								method = JDCT_ISLOW;
								break;
//							#endif
//							#ifdef DCT_IFAST_SUPPORTED
//							case JDCT_IFAST:
//								method_ptr = jpeg_idct_ifast;
//								method = JDCT_IFAST;
//								break;
//							#endif
//							#ifdef DCT_FLOAT_SUPPORTED
//							case JDCT_FLOAT:
//								method_ptr = jpeg_idct_float;
//								method = JDCT_FLOAT;
//								break;
//							#endif
							default:
								error();
//								ERREXIT(cinfo, JERR_NOT_COMPILED);
								break;
						}
						break;
					default:
						error();
//						ERREXIT1(cinfo, JERR_BAD_DCTSIZE, compptr.DCT_scaled_size);
						break;
					}
//					idct.inverse_DCT[ci] = method_ptr;
					/* Create multiplier table from quant table.
					 * However, we can skip this if the component is uninteresting
					 * or if we already built the table.	Also, if no quant table
					 * has yet been saved for the component, we leave the
					 * multiplier table all-zero; we'll be reading zeroes from the
					 * coefficient controller's buffer anyway.
					 */
					if (! compptr.component_needed || idct.cur_method[ci] == method)
						continue;
					qtbl = compptr.quant_table;
					if (qtbl == null)		/* happens if no data yet for component */
						continue;
					idct.cur_method[ci] = method;
					switch (method) {
//						#ifdef PROVIDE_ISLOW_TABLES
						case JDCT_ISLOW:
						{
							/* For LL&M IDCT method, multipliers are equal to raw quantization
							 * coefficients, but are stored as ints to ensure access efficiency.
							 */
							int[] ismtbl = compptr.dct_table;
							for (i = 0; i < DCTSIZE2; i++) {
								ismtbl[i] = qtbl.quantval[i];
							}
						}
						break;
//						#endif
//						#ifdef DCT_IFAST_SUPPORTED
//						case JDCT_IFAST:
//						{
//							/* For AA&N IDCT method, multipliers are equal to quantization
//							 * coefficients scaled by scalefactor[row]*scalefactor[col], where
//							 *	 scalefactor[0] = 1
//							 *	 scalefactor[k] = cos(k*PI/16) * sqrt(2)		for k=1..7
//							 * For integer operation, the multiplier table is to be scaled by
//							 * IFAST_SCALE_BITS.
//							 */
//							int[] ifmtbl = compptr.dct_table;
//							short aanscales[] = {
//								/* precomputed values scaled up by 14 bits */
//								16384, 22725, 21407, 19266, 16384, 12873,	8867,	4520,
//								22725, 31521, 29692, 26722, 22725, 17855, 12299,	6270,
//								21407, 29692, 27969, 25172, 21407, 16819, 11585,	5906,
//								19266, 26722, 25172, 22654, 19266, 15137, 10426,	5315,
//								16384, 22725, 21407, 19266, 16384, 12873,	8867,	4520,
//								12873, 17855, 16819, 15137, 12873, 10114,	6967,	3552,
//								8867, 12299, 11585, 10426,	8867,	6967,	4799,	2446,
//								4520,	6270,	5906,	5315,	4520,	3552,	2446,	1247
//							};
//							SHIFT_TEMPS
//							
//							for (i = 0; i < DCTSIZE2; i++) {
//								ifmtbl[i] = DESCALE(MULTIPLY16V16( qtbl.quantval[i], aanscales[i]), CONST_BITS-IFAST_SCALE_BITS);
//							}
//						}
//						break;
//						#endif
//						#ifdef DCT_FLOAT_SUPPORTED
//						case JDCT_FLOAT:
//						{
//							/* For float AA&N IDCT method, multipliers are equal to quantization
//							 * coefficients scaled by scalefactor[row]*scalefactor[col], where
//							 *	 scalefactor[0] = 1
//							 *	 scalefactor[k] = cos(k*PI/16) * sqrt(2)		for k=1..7
//							 */
//							FLOAT_MULT_TYPE * fmtbl = (FLOAT_MULT_TYPE *) compptr.dct_table;
//							int row, col;
//							static const double aanscalefactor[DCTSIZE] = {
//								1.0, 1.387039845, 1.306562965, 1.175875602,
//								1.0, 0.785694958, 0.541196100, 0.275899379
//							};
//
//							i = 0;
//							for (row = 0; row < DCTSIZE; row++) {
//								for (col = 0; col < DCTSIZE; col++) {
//									fmtbl[i] = (FLOAT_MULT_TYPE)
//										((double) qtbl.quantval[i] *
//									 aanscalefactor[row] * aanscalefactor[col]);
//									i++;
//								}
//							}
//						}
//						break;
//						#endif
					default:
						error();
//						ERREXIT(cinfo, JERR_NOT_COMPILED);
						break;
				}
			}
		}
	}
			
	static final class jpeg_input_controller {
		int consume_input;
		boolean has_multiple_scans;	/* True if file has multiple scans */
		boolean eoi_reached;

		boolean inheaders;		/* true until first SOS is reached */
	}
	
	static final class	jpeg_color_deconverter {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo));
		int color_convert;
		
		/* Private state for YCC.RGB conversion */
		int[] Cr_r_tab;		/* => table for Cr to R conversion */
		int[] Cb_b_tab;		/* => table for Cb to B conversion */
		int[] Cr_g_tab;		/* => table for Cr to G conversion */
		int[] Cb_g_tab;		/* => table for Cb to G conversion */
			
		void start_pass (jpeg_decompress_struct cinfo) {
			/* no work needed */
		}

	}
		
	static final class jpeg_d_post_controller {
//		JMETHOD(void, start_pass, (j_decompress_ptr cinfo, J_BUF_MODE pass_mode));
		int post_process_data;
			
		/* Color quantization source buffer: this holds output data from
		 * the upsample/color conversion step to be passed to the quantizer.
		 * For two-pass color quantization, we need a full-image buffer;
		 * for one-pass operation, a strip buffer is sufficient.
		 */
		int[] whole_image;	/* virtual array, or NULL if one-pass */
		int[][] buffer;		/* strip buffer, or current strip of virtual */
		int strip_height;	/* buffer size in rows */
		/* for two-pass mode only: */
		int starting_row;	/* row # of first row in current strip */
		int next_row;		/* index of next row to fill/empty in strip */
				
		void start_pass (jpeg_decompress_struct cinfo, int pass_mode) {
			jpeg_d_post_controller post = cinfo.post;

			switch (pass_mode) {
				case JBUF_PASS_THRU:
					if (cinfo.quantize_colors) {
						error(SWT.ERROR_NOT_IMPLEMENTED);
//						/* Single-pass processing with color quantization. */
//						post.post_process_data = POST_PROCESS_1PASS;
//						/* We could be doing buffered-image output before starting a 2-pass
//						 * color quantization; in that case, jinit_d_post_controller did not
//						 * allocate a strip buffer.	Use the virtual-array buffer as workspace.
//						 */
//						if (post.buffer == null) {
//							post.buffer = (*cinfo.mem.access_virt_sarray)
//								((j_common_ptr) cinfo, post.whole_image,
//						 		(JDIMENSION) 0, post.strip_height, TRUE);
//						}
					} else {
						/* For single-pass processing without color quantization,
						 * I have no work to do; just call the upsampler directly.
						 */
						post.post_process_data = POST_PROCESS_DATA_UPSAMPLE;
					}
					break;
//				#ifdef QUANT_2PASS_SUPPORTED
//				case JBUF_SAVE_AND_PASS:
//					/* First pass of 2-pass quantization */
//					if (post.whole_image == NULL)
//						ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);
//					post.pub.post_process_data = post_process_prepass;
//					break;
//				case JBUF_CRANK_DEST:
//					/* Second pass of 2-pass quantization */
//					if (post.whole_image == NULL)
//						ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);
//					post.pub.post_process_data = post_process_2pass;
//					break;
//				#endif /* QUANT_2PASS_SUPPORTED */
					default:
						error();
//						ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);
						break;
			}
			post.starting_row = post.next_row = 0;
		}

	}
	
	static final class jpeg_decompress_struct {
//		jpeg_error_mgr * err;	/* Error handler module */\
//		struct jpeg_memory_mgr * mem;	/* Memory manager module */\
//		struct jpeg_progress_mgr * progress; /* Progress monitor, or null if none */\
//		void * client_data;		/* Available for use by application */\
		boolean is_decompressor;	/* So common code can tell which is which */
		int global_state;		/* For checking call sequence validity */

//		/* Source of compressed data */
//		struct jpeg_source_mgr * src;
		InputStream inputStream;
		byte[] buffer;
		int bytes_in_buffer;
		int bytes_offset;
		boolean start_of_file;

		/* Basic description of image --- filled in by jpeg_read_header(). */
		/* Application may inspect these values to decide how to process image. */

		int image_width;	/* nominal image width (from SOF marker) */
		int image_height;	/* nominal image height */
		int num_components;		/* # of color components in JPEG image */
		int jpeg_color_space; /* colorspace of JPEG image */

		/* Decompression processing parameters --- these fields must be set before
		 * calling jpeg_start_decompress().	Note that jpeg_read_header() initializes
		 * them to default values.
		 */

		int out_color_space; /* colorspace for output */

		int scale_num, scale_denom; /* fraction by which to scale image */

		double output_gamma;		/* image gamma wanted in output */

		boolean buffered_image;	/* true=multiple output passes */
		boolean raw_data_out;		/* true=downsampled data wanted */

		int dct_method;	/* IDCT algorithm selector */
		boolean do_fancy_upsampling;	/* true=apply fancy upsampling */
		boolean do_block_smoothing;	/* true=apply interblock smoothing */

		boolean quantize_colors;	/* true=colormapped output wanted */
		/* the following are ignored if not quantize_colors: */
		int dither_mode;	/* type of color dithering to use */
		boolean two_pass_quantize;	/* true=use two-pass color quantization */
		int desired_number_of_colors;	/* max # colors to use in created colormap */
		/* these are significant only in buffered-image mode: */
		boolean enable_1pass_quant;	/* enable future use of 1-pass quantizer */
		boolean enable_external_quant;/* enable future use of external colormap */
		boolean enable_2pass_quant;	/* enable future use of 2-pass quantizer */

		/* Description of actual output image that will be returned to application.
		 * These fields are computed by jpeg_start_decompress().
		 * You can also use jpeg_calc_output_dimensions() to determine these values
		 * in advance of calling jpeg_start_decompress().
		 */

		int output_width;	/* scaled image width */
		int output_height;	/* scaled image height */
		int out_color_components;	/* # of color components in out_color_space */
		int output_components;	/* # of color components returned */
		/* output_components is 1 (a colormap index) when quantizing colors;
		 * otherwise it equals out_color_components.
		 */
		int rec_outbuf_height;	/* min recommended height of scanline buffer */
		/* If the buffer passed to jpeg_read_scanlines() is less than this many rows
		 * high, space and time will be wasted due to unnecessary data copying.
		 * Usually rec_outbuf_height will be 1 or 2, at most 4.
		 */

		/* When quantizing colors, the output colormap is described by these fields.
		 * The application can supply a colormap by setting colormap non-null before
		 * calling jpeg_start_decompress; otherwise a colormap is created during
		 * jpeg_start_decompress or jpeg_start_output.
		 * The map has out_color_components rows and actual_number_of_colors columns.
		 */
		int actual_number_of_colors;	/* number of entries in use */
		int[] colormap;		/* The color map as a 2-D pixel array */

		/* State variables: these variables indicate the progress of decompression.
		 * The application may examine these but must not modify them.
		 */

		/* Row index of next scanline to be read from jpeg_read_scanlines().
		 * Application may use this to control its processing loop, e.g.,
		 * "while (output_scanline < output_height)".
		 */
		int output_scanline;	/* 0 .. output_height-1	*/

		/* Current input scan number and number of iMCU rows completed in scan.
		 * These indicate the progress of the decompressor input side.
		 */
		int input_scan_number;	/* Number of SOS markers seen so far */
		int input_iMCU_row;	/* Number of iMCU rows completed */

		/* The "output scan number" is the notional scan being displayed by the
		 * output side.	The decompressor will not allow output scan/row number
		 * to get ahead of input scan/row, but it can fall arbitrarily far behind.
		 */
		int output_scan_number;	/* Nominal scan number being displayed */
		int output_iMCU_row;	/* Number of iMCU rows read */

		/* Current progression status.	coef_bits[c][i] indicates the precision
		 * with which component c's DCT coefficient i (in zigzag order) is known.
		 * It is -1 when no data has yet been received, otherwise it is the point
		 * transform (shift) value for the most recent scan of the coefficient
		 * (thus, 0 at completion of the progression).
		 * This pointer is null when reading a non-progressive file.
		 */
		int[][] coef_bits;	/* -1 or current Al value for each coef */

		/* Internal JPEG parameters --- the application usually need not look at
		 * these fields.	Note that the decompressor output side may not use
		 * any parameters that can change between scans.
		 */

		/* Quantization and Huffman tables are carried forward across input
		 * datastreams when processing abbreviated JPEG datastreams.
		 */

		JQUANT_TBL[] quant_tbl_ptrs = new JQUANT_TBL[NUM_QUANT_TBLS];
		/* ptrs to coefficient quantization tables, or null if not defined */

		JHUFF_TBL[] dc_huff_tbl_ptrs = new JHUFF_TBL[NUM_HUFF_TBLS];
		JHUFF_TBL[] ac_huff_tbl_ptrs = new JHUFF_TBL[NUM_HUFF_TBLS];
		/* ptrs to Huffman coding tables, or null if not defined */

		/* These parameters are never carried across datastreams, since they
		 * are given in SOF/SOS markers or defined to be reset by SOI.
		 */

		int data_precision;		/* bits of precision in image data */

		jpeg_component_info[] comp_info;
		/* comp_info[i] describes component that appears i'th in SOF */

		boolean progressive_mode;	/* true if SOFn specifies progressive mode */
		boolean arith_code;		/* true=arithmetic coding, false=Huffman */

		byte[] arith_dc_L = new byte[NUM_ARITH_TBLS]; /* L values for DC arith-coding tables */
		byte[] arith_dc_U = new byte[NUM_ARITH_TBLS]; /* U values for DC arith-coding tables */
		byte[] arith_ac_K = new byte[NUM_ARITH_TBLS]; /* Kx values for AC arith-coding tables */

		int restart_interval; /* MCUs per restart interval, or 0 for no restart */

		/* These fields record data obtained from optional markers recognized by
		 * the JPEG library.
		 */
		boolean saw_JFIF_marker;	/* true iff a JFIF APP0 marker was found */
		/* Data copied from JFIF marker; only valid if saw_JFIF_marker is true: */
		byte JFIF_major_version;	/* JFIF version number */
		byte JFIF_minor_version;
		byte density_unit;		/* JFIF code for pixel size units */
		short X_density;		/* Horizontal pixel density */
		short Y_density;		/* Vertical pixel density */
		boolean saw_Adobe_marker;	/* true iff an Adobe APP14 marker was found */
		byte Adobe_transform;	/* Color transform code from Adobe marker */

		boolean CCIR601_sampling;	/* true=first samples are cosited */

		/* Aside from the specific data retained from APPn markers known to the
		 * library, the uninterpreted contents of any or all APPn and COM markers
		 * can be saved in a list for examination by the application.
		 */
		jpeg_marker_reader marker_list; /* Head of list of saved markers */

		/* Remaining fields are known throughout decompressor, but generally
		 * should not be touched by a surrounding application.
		 */

		/*
		 * These fields are computed during decompression startup
		 */
		int max_h_samp_factor;	/* largest h_samp_factor */
		int max_v_samp_factor;	/* largest v_samp_factor */

		int min_DCT_scaled_size;	/* smallest DCT_scaled_size of any component */

		int total_iMCU_rows;	/* # of iMCU rows in image */
		/* The coefficient controller's input and output progress is measured in
		 * units of "iMCU" (interleaved MCU) rows.	These are the same as MCU rows
		 * in fully interleaved JPEG scans, but are used whether the scan is
		 * interleaved or not.	We define an iMCU row as v_samp_factor DCT block
		 * rows of each component.	Therefore, the IDCT output contains
		 * v_samp_factor*DCT_scaled_size sample rows of a component per iMCU row.
		 */

		byte[] sample_range_limit; /* table for fast range-limiting */
		int sample_range_limit_offset;

		/*
		 * These fields are valid during any one scan.
		 * They describe the components and MCUs actually appearing in the scan.
		 * Note that the decompressor output side must not use these fields.
		 */
		int comps_in_scan;		/* # of JPEG components in this scan */
		jpeg_component_info[] cur_comp_info = new jpeg_component_info[MAX_COMPS_IN_SCAN];
		/* *cur_comp_info[i] describes component that appears i'th in SOS */

		int MCUs_per_row;	/* # of MCUs across the image */
		int MCU_rows_in_scan;	/* # of MCU rows in the image */

		int blocks_in_MCU;		/* # of DCT blocks per MCU */
		int[] MCU_membership = new int[D_MAX_BLOCKS_IN_MCU];
		/* MCU_membership[i] is index in cur_comp_info of component owning */
		/* i'th block in an MCU */

		int Ss, Se, Ah, Al;		/* progressive JPEG parameters for scan */

		/* This field is shared between entropy decoder and marker parser.
		 * It is either zero or the code of a JPEG marker that has been
		 * read from the data source, but has not yet been processed.
		 */
		int unread_marker;
		
		int[] workspace = new int[DCTSIZE2];
		int[] row_ctr = new int[1];

		/*
		 * Links to decompression subobjects (methods, private variables of modules)
		 */
		jpeg_decomp_master master;
		jpeg_d_main_controller main;
		jpeg_d_coef_controller coef;
		jpeg_d_post_controller post;
		jpeg_input_controller inputctl;
		jpeg_marker_reader marker;
		jpeg_entropy_decoder entropy;
		jpeg_inverse_dct idct;
		jpeg_upsampler upsample;
		jpeg_color_deconverter cconvert;
		jpeg_color_quantizer cquantize;
	}

static void error() {
	SWT.error(SWT.ERROR_INVALID_IMAGE);
}

static void error(int code) {
	SWT.error(code);
}

static void error(String msg) {
	SWT.error(SWT.ERROR_INVALID_IMAGE, null, msg);
}

static void jinit_marker_reader (jpeg_decompress_struct cinfo) {
	jpeg_marker_reader marker = cinfo.marker = new jpeg_marker_reader();
//	int i;

	/* Initialize COM/APPn processing.
	 * By default, we examine and then discard APP0 and APP14,
	 * but simply discard COM and all other APPn.
	 */
//	marker.process_COM = skip_variable;
	marker.length_limit_COM = 0;
//	for (i = 0; i < 16; i++) {
//		marker.process_APPn[i] = skip_variable;
//		marker.length_limit_APPn[i] = 0;
//	}
//	marker.process_APPn[0] = get_interesting_appn;
//	marker.process_APPn[14] = get_interesting_appn;
	/* Reset marker processing state */
	reset_marker_reader(cinfo);
}

static void jinit_d_coef_controller (jpeg_decompress_struct cinfo, boolean need_full_buffer) {
	jpeg_d_coef_controller coef = new jpeg_d_coef_controller();
	cinfo.coef = coef;
//	coef.pub.start_input_pass = start_input_pass;
//	coef.pub.start_output_pass = start_output_pass;
	coef.coef_bits_latch = null;

	/* Create the coefficient buffer. */
	if (need_full_buffer) {
//#ifdef D_MULTISCAN_FILES_SUPPORTED
		/* Allocate a full-image virtual array for each component, */
		/* padded to a multiple of samp_factor DCT blocks in each direction. */
		/* Note we ask for a pre-zeroed array. */
		int ci; //, access_rows;
		jpeg_component_info compptr;

		for (ci = 0; ci < cinfo.num_components; ci++) {
			compptr = cinfo.comp_info[ci];
			//access_rows = compptr.v_samp_factor;
//#ifdef BLOCK_SMOOTHING_SUPPORTED
			/* If block smoothing could be used, need a bigger window */
			//if (cinfo.progressive_mode)
				//access_rows *= 3;
//#endif
			coef.whole_image[ci] = 
				new short
					[(int)jround_up( compptr.height_in_blocks, compptr.v_samp_factor)]
				    [(int)jround_up( compptr.width_in_blocks, compptr.h_samp_factor)]
				    [DCTSIZE2];
		}
//		coef.consume_data = consume_data;
		coef.decompress_data = DECOMPRESS_DATA;
		coef.coef_arrays = coef.whole_image[0]; /* link to virtual arrays */
//		#else
//				ERREXIT(cinfo, JERR_NOT_COMPILED);
//		#endif
	} else {
		/* We only need a single-MCU buffer. */
		coef.MCU_buffer = new short[D_MAX_BLOCKS_IN_MCU][DCTSIZE2];
//		coef.consume_data = dummy_consume_data;
		coef.decompress_data = DECOMPRESS_ONEPASS;
		coef.coef_arrays = null; /* flag for no virtual arrays */
	}
}

static void start_output_pass (jpeg_decompress_struct cinfo) {
//#ifdef BLOCK_SMOOTHING_SUPPORTED
	jpeg_d_coef_controller coef = cinfo.coef;

	/* If multipass, check to see whether to use block smoothing on this pass */
	if (coef.coef_arrays != null) {
		if (cinfo.do_block_smoothing && smoothing_ok(cinfo))
			coef.decompress_data = DECOMPRESS_SMOOTH_DATA;
		else
			coef.decompress_data = DECOMPRESS_DATA;
	}
//#endif
	cinfo.output_iMCU_row = 0;
}

static void jpeg_create_decompress(jpeg_decompress_struct cinfo) {
	cinfo.is_decompressor = true;


	/* Initialize marker processor so application can override methods
	 * for COM, APPn markers before calling jpeg_read_header.
	 */
	cinfo.marker_list = null;
	jinit_marker_reader(cinfo);

	/* And initialize the overall input controller. */
	jinit_input_controller(cinfo);

	/* OK, I'm ready */
	cinfo.global_state = DSTATE_START;
}

static void jpeg_calc_output_dimensions (jpeg_decompress_struct cinfo)
/* Do computations that are needed before master selection phase */
{
//#ifdef IDCT_SCALING_SUPPORTED
//	int ci;
//	jpeg_component_info compptr;
//#endif

	/* Prevent application from calling me at wrong times */
	if (cinfo.global_state != DSTATE_READY)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);

//#ifdef IDCT_SCALING_SUPPORTED
//
//	/* Compute actual output image dimensions and DCT scaling choices. */
//	if (cinfo.scale_num * 8 <= cinfo.scale_denom) {
//		/* Provide 1/8 scaling */
//		cinfo.output_width = (int)
//			jdiv_round_up(cinfo.image_width, 8L);
//		cinfo.output_height = (int)
//			jdiv_round_up(cinfo.image_height, 8L);
//		cinfo.min_DCT_scaled_size = 1;
//	} else if (cinfo.scale_num * 4 <= cinfo.scale_denom) {
//		/* Provide 1/4 scaling */
//		cinfo.output_width = (int)
//			jdiv_round_up(cinfo.image_width, 4L);
//		cinfo.output_height = (int)
//			jdiv_round_up(cinfo.image_height, 4L);
//		cinfo.min_DCT_scaled_size = 2;
//	} else if (cinfo.scale_num * 2 <= cinfo.scale_denom) {
//		/* Provide 1/2 scaling */
//		cinfo.output_width = (int)
//			jdiv_round_up(cinfo.image_width, 2L);
//		cinfo.output_height = (int)
//			jdiv_round_up(cinfo.image_height, 2L);
//		cinfo.min_DCT_scaled_size = 4;
//	} else {
//		/* Provide 1/1 scaling */
//		cinfo.output_width = cinfo.image_width;
//		cinfo.output_height = cinfo.image_height;
//		cinfo.min_DCT_scaled_size = DCTSIZE;
//	}
//	/* In selecting the actual DCT scaling for each component, we try to
//	 * scale up the chroma components via IDCT scaling rather than upsampling.
//	 * This saves time if the upsampler gets to use 1:1 scaling.
//	 * Note this code assumes that the supported DCT scalings are powers of 2.
//	 */
//	for (ci = 0; ci < cinfo.num_components; ci++) {
//		compptr = cinfo.comp_info[ci];
//		int ssize = cinfo.min_DCT_scaled_size;
//		while (ssize < DCTSIZE &&
//			(compptr.h_samp_factor * ssize * 2 <= cinfo.max_h_samp_factor * cinfo.min_DCT_scaled_size) &&
//			(compptr.v_samp_factor * ssize * 2 <= cinfo.max_v_samp_factor * cinfo.min_DCT_scaled_size))
//		{
//			ssize = ssize * 2;
//		}
//		compptr.DCT_scaled_size = ssize;
//	}
//
//	/* Recompute downsampled dimensions of components;
//	 * application needs to know these if using raw downsampled data.
//	 */
//	for (ci = 0; ci < cinfo.num_components; ci++) {
//		compptr = cinfo.comp_info[ci];
//		/* Size in samples, after IDCT scaling */
//		compptr.downsampled_width = (int)
//			jdiv_round_up((long) cinfo.image_width * (long) (compptr.h_samp_factor * compptr.DCT_scaled_size),
//				(cinfo.max_h_samp_factor * DCTSIZE));
//		compptr.downsampled_height = (int)
//			jdiv_round_up((long) cinfo.image_height * (long) (compptr.v_samp_factor * compptr.DCT_scaled_size),
//				(cinfo.max_v_samp_factor * DCTSIZE));
//	}
//
//#else /* !IDCT_SCALING_SUPPORTED */

	/* Hardwire it to "no scaling" */
	cinfo.output_width = cinfo.image_width;
	cinfo.output_height = cinfo.image_height;
	/* jdinput.c has already initialized DCT_scaled_size to DCTSIZE,
	 * and has computed unscaled downsampled_width and downsampled_height.
	 */

//#endif /* IDCT_SCALING_SUPPORTED */

	/* Report number of components in selected colorspace. */
	/* Probably this should be in the color conversion module... */
	switch (cinfo.out_color_space) {
		case JCS_GRAYSCALE:
			cinfo.out_color_components = 1;
			break;
		case JCS_RGB:
		case JCS_YCbCr:
			cinfo.out_color_components = 3;
			break;
		case JCS_CMYK:
		case JCS_YCCK:
			cinfo.out_color_components = 4;
			break;
		default:			/* else must be same colorspace as in file */
			cinfo.out_color_components = cinfo.num_components;
			break;
	}
	cinfo.output_components = (cinfo.quantize_colors ? 1 : cinfo.out_color_components);

	/* See if upsampler will want to emit more than one row at a time */
	if (use_merged_upsample(cinfo))
		cinfo.rec_outbuf_height = cinfo.max_v_samp_factor;
	else
		cinfo.rec_outbuf_height = 1;
}

static boolean use_merged_upsample (jpeg_decompress_struct cinfo) {
//#ifdef UPSAMPLE_MERGING_SUPPORTED
	/* Merging is the equivalent of plain box-filter upsampling */
	if (cinfo.do_fancy_upsampling || cinfo.CCIR601_sampling)
		return false;
	/* jdmerge.c only supports YCC=>RGB color conversion */
	if (cinfo.jpeg_color_space != JCS_YCbCr || cinfo.num_components != 3 ||
			cinfo.out_color_space != JCS_RGB ||
			cinfo.out_color_components != RGB_PIXELSIZE)
		return false;
	/* and it only handles 2h1v or 2h2v sampling ratios */
	if (cinfo.comp_info[0].h_samp_factor != 2 ||
			cinfo.comp_info[1].h_samp_factor != 1 ||
			cinfo.comp_info[2].h_samp_factor != 1 ||
			cinfo.comp_info[0].v_samp_factor >	2 ||
			cinfo.comp_info[1].v_samp_factor != 1 ||
			cinfo.comp_info[2].v_samp_factor != 1)
		return false;
	/* furthermore, it doesn't work if we've scaled the IDCTs differently */
	if (cinfo.comp_info[0].DCT_scaled_size != cinfo.min_DCT_scaled_size ||
			cinfo.comp_info[1].DCT_scaled_size != cinfo.min_DCT_scaled_size ||
			cinfo.comp_info[2].DCT_scaled_size != cinfo.min_DCT_scaled_size)
		return false;
	/* ??? also need to test for upsample-time rescaling, when & if supported */
	return true;			/* by golly, it'll work... */
//#else
//	return false;
//#endif
}

static void prepare_range_limit_table (jpeg_decompress_struct cinfo)
/* Allocate and fill in the sample_range_limit table */
{
	byte[] table;
	int i;

	table = new byte[5 * (MAXJSAMPLE+1) + CENTERJSAMPLE];
	int offset = (MAXJSAMPLE+1);	/* allow negative subscripts of simple table */
	cinfo.sample_range_limit_offset = offset;
	cinfo.sample_range_limit = table;
	/* First segment of "simple" table: limit[x] = 0 for x < 0 */
	/* Main part of "simple" table: limit[x] = x */
	for (i = 0; i <= MAXJSAMPLE; i++)
		table[i + offset] = (byte)i;
	offset += CENTERJSAMPLE;	/* Point to where post-IDCT table starts */
	/* End of simple table, rest of first half of post-IDCT table */
	for (i = CENTERJSAMPLE; i < 2*(MAXJSAMPLE+1); i++)
		table[i+offset] = (byte)MAXJSAMPLE;
	/* Second half of post-IDCT table */
	System.arraycopy(cinfo.sample_range_limit, cinfo.sample_range_limit_offset, table, offset + (4 * (MAXJSAMPLE+1) - CENTERJSAMPLE), CENTERJSAMPLE);
}

static void build_ycc_rgb_table (jpeg_decompress_struct cinfo) {
	jpeg_color_deconverter cconvert = cinfo.cconvert;
	int i;
	int x;
//	SHIFT_TEMPS

	cconvert.Cr_r_tab = new int[MAXJSAMPLE+1];
	cconvert.Cb_b_tab = new int[MAXJSAMPLE+1];
	cconvert.Cr_g_tab = new int[MAXJSAMPLE+1];
	cconvert.Cb_g_tab = new int[MAXJSAMPLE+1];

	for (i = 0, x = -CENTERJSAMPLE; i <= MAXJSAMPLE; i++, x++) {
		/* i is the actual input pixel value, in the range 0..MAXJSAMPLE */
		/* The Cb or Cr value we are thinking of is x = i - CENTERJSAMPLE */
		/* Cr=>R value is nearest int to 1.40200 * x */
		cconvert.Cr_r_tab[i] = ((int)(1.40200f * (1<<SCALEBITS) + 0.5f) * x + ONE_HALF) >> SCALEBITS;
		/* Cb=>B value is nearest int to 1.77200 * x */
		cconvert.Cb_b_tab[i] = ((int)(1.77200f * (1<<SCALEBITS) + 0.5f) * x + ONE_HALF) >> SCALEBITS;
		/* Cr=>G value is scaled-up -0.71414 * x */
		cconvert.Cr_g_tab[i] = ((int)(- (0.71414f * (1<<SCALEBITS) + 0.5f)) * x);
		/* Cb=>G value is scaled-up -0.34414 * x */
		/* We also add in ONE_HALF so that need not do it in inner loop */
		cconvert.Cb_g_tab[i] = ((int)(- (0.34414f* (1<<SCALEBITS) + 0.5f)) * x + ONE_HALF);
	}
}

static void jinit_color_deconverter (jpeg_decompress_struct cinfo) {
	jpeg_color_deconverter cconvert = cinfo.cconvert = new jpeg_color_deconverter();
//	cconvert.start_pass = start_pass_dcolor;

	/* Make sure num_components agrees with jpeg_color_space */
	switch (cinfo.jpeg_color_space) {
		case JCS_GRAYSCALE:
			if (cinfo.num_components != 1)
				error();
//				ERREXIT(cinfo, JERR_BAD_J_COLORSPACE);
			break;

		case JCS_RGB:
		case JCS_YCbCr:
			if (cinfo.num_components != 3)
				error();
//				ERREXIT(cinfo, JERR_BAD_J_COLORSPACE);
			break;

		case JCS_CMYK:
		case JCS_YCCK:
			if (cinfo.num_components != 4)
				error();
//				ERREXIT(cinfo, JERR_BAD_J_COLORSPACE);
			break;

		default:			/* JCS_UNKNOWN can be anything */
			if (cinfo.num_components < 1)
				error();
//				ERREXIT(cinfo, JERR_BAD_J_COLORSPACE);
			break;
	}

	/* Set out_color_components and conversion method based on requested space.
	 * Also clear the component_needed flags for any unused components,
	 * so that earlier pipeline stages can avoid useless computation.
	 */

	int ci;
	switch (cinfo.out_color_space) {
		case JCS_GRAYSCALE:
			cinfo.out_color_components = 1;
			if (cinfo.jpeg_color_space == JCS_GRAYSCALE || cinfo.jpeg_color_space == JCS_YCbCr) {
				cconvert.color_convert = GRAYSCALE_CONVERT;
				/* For color.grayscale conversion, only the Y (0) component is needed */
				for (ci = 1; ci < cinfo.num_components; ci++)
					cinfo.comp_info[ci].component_needed = false;
			} else
				error();
//				ERREXIT(cinfo, JERR_CONVERSION_NOTIMPL);
			break;

		case JCS_RGB:
			cinfo.out_color_components = RGB_PIXELSIZE;
			if (cinfo.jpeg_color_space == JCS_YCbCr) {
				cconvert.color_convert = YCC_RGB_CONVERT;
				build_ycc_rgb_table(cinfo);
			} else if (cinfo.jpeg_color_space == JCS_GRAYSCALE) {
				cconvert.color_convert = GRAY_RGB_CONVERT;
			} else if (cinfo.jpeg_color_space == JCS_RGB) {
				cconvert.color_convert = NULL_CONVERT;
			} else
				error();
//				ERREXIT(cinfo, JERR_CONVERSION_NOTIMPL);
				break;

		case JCS_CMYK:
			cinfo.out_color_components = 4;
			if (cinfo.jpeg_color_space == JCS_YCCK) {
				cconvert.color_convert = YCCK_CMYK_CONVERT;
				build_ycc_rgb_table(cinfo);
			} else if (cinfo.jpeg_color_space == JCS_CMYK) {
				cconvert.color_convert = NULL_CONVERT;
			} else
				error();
//				ERREXIT(cinfo, JERR_CONVERSION_NOTIMPL);
			break;

		default:
			/* Permit null conversion to same output space */
			if (cinfo.out_color_space == cinfo.jpeg_color_space) {
				cinfo.out_color_components = cinfo.num_components;
				cconvert.color_convert = NULL_CONVERT;
			} else	/* unsupported non-null conversion */
				error();
//				ERREXIT(cinfo, JERR_CONVERSION_NOTIMPL);
			break;
	}

	if (cinfo.quantize_colors)
		cinfo.output_components = 1; /* single colormapped output component */
	else
		cinfo.output_components = cinfo.out_color_components;
}

static void jinit_d_post_controller (jpeg_decompress_struct cinfo, boolean need_full_buffer) {
	jpeg_d_post_controller post = cinfo.post = new jpeg_d_post_controller();
//	post.pub.start_pass = start_pass_dpost;
	post.whole_image = null;	/* flag for no virtual arrays */
	post.buffer = null;		/* flag for no strip buffer */

	/* Create the quantization buffer, if needed */
	if (cinfo.quantize_colors) {
		error(SWT.ERROR_NOT_IMPLEMENTED);
//		/* The buffer strip height is max_v_samp_factor, which is typically
//		 * an efficient number of rows for upsampling to return.
//		 * (In the presence of output rescaling, we might want to be smarter?)
//		 */
//		post.strip_height = cinfo.max_v_samp_factor;
//		if (need_full_buffer) {
//			/* Two-pass color quantization: need full-image storage. */
//			/* We round up the number of rows to a multiple of the strip height. */
//#ifdef QUANT_2PASS_SUPPORTED
//			post.whole_image = (*cinfo.mem.request_virt_sarray)
//				((j_common_ptr) cinfo, JPOOL_IMAGE, FALSE,
//	 			cinfo.output_width * cinfo.out_color_components,
//	 			(JDIMENSION) jround_up((long) cinfo.output_height,
//				(long) post.strip_height),
//	 post.strip_height);
//#else
//			ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);
//#endif /* QUANT_2PASS_SUPPORTED */
//		} else {
//			/* One-pass color quantization: just make a strip buffer. */
//			post.buffer = (*cinfo.mem.alloc_sarray)
//				((j_common_ptr) cinfo, JPOOL_IMAGE,
//	 			cinfo.output_width * cinfo.out_color_components,
//	 			post.strip_height);
//		}
	}
}

static void make_funny_pointers (jpeg_decompress_struct cinfo)
/* Create the funny pointer lists discussed in the comments above.
 * The actual workspace is already allocated (in main.buffer),
 * and the space for the pointer lists is allocated too.
 * This routine just fills in the curiously ordered lists.
 * This will be repeated at the beginning of each pass.
 */
{
	jpeg_d_main_controller main = cinfo.main;
	int ci, i, rgroup;
	int M = cinfo.min_DCT_scaled_size;
	jpeg_component_info compptr;
	byte[][] buf, xbuf0, xbuf1;
	
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		rgroup = (compptr.v_samp_factor * compptr.DCT_scaled_size) /
			cinfo.min_DCT_scaled_size; /* height of a row group of component */
		xbuf0 = main.xbuffer[0][ci];
		int xbuf0_offset = main.xbuffer_offset[0][ci];
		xbuf1 = main.xbuffer[1][ci];
		int xbuf1_offset = main.xbuffer_offset[1][ci];
		/* First copy the workspace pointers as-is */
		buf = main.buffer[ci];
		for (i = 0; i < rgroup * (M + 2); i++) {
			xbuf0[i + xbuf0_offset] = xbuf1[i + xbuf1_offset] = buf[i];
		}
		/* In the second list, put the last four row groups in swapped order */
		for (i = 0; i < rgroup * 2; i++) {
			xbuf1[rgroup*(M-2) + i + xbuf1_offset] = buf[rgroup*M + i];
			xbuf1[rgroup*M + i + xbuf1_offset] = buf[rgroup*(M-2) + i];
		}
		/* The wraparound pointers at top and bottom will be filled later
		 * (see set_wraparound_pointers, below).	Initially we want the "above"
		 * pointers to duplicate the first actual data line.	This only needs
		 * to happen in xbuffer[0].
		 */
		for (i = 0; i < rgroup; i++) {
			xbuf0[i - rgroup + xbuf0_offset] = xbuf0[0 + xbuf0_offset];
		}
	}
}

static void alloc_funny_pointers (jpeg_decompress_struct cinfo)
/* Allocate space for the funny pointer lists.
 * This is done only once, not once per pass.
 */
{
	jpeg_d_main_controller main = cinfo.main;
	int ci, rgroup;
	int M = cinfo.min_DCT_scaled_size;
	jpeg_component_info compptr;
	byte[][] xbuf;

	/* Get top-level space for component array pointers.
	 * We alloc both arrays with one call to save a few cycles.
	 */
	main.xbuffer[0] = new byte[cinfo.num_components][][];
	main.xbuffer[1] = new byte[cinfo.num_components][][];
	main.xbuffer_offset[0] = new int[cinfo.num_components];
	main.xbuffer_offset[1] = new int[cinfo.num_components];

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		rgroup = (compptr.v_samp_factor * compptr.DCT_scaled_size) / cinfo.min_DCT_scaled_size; /* height of a row group of component */
		/* Get space for pointer lists --- M+4 row groups in each list.
		 * We alloc both pointer lists with one call to save a few cycles.
		 */
		xbuf = new byte[2 * (rgroup * (M + 4))][];
		int offset = rgroup;
		main.xbuffer_offset[0][ci] = offset;
		main.xbuffer[0][ci] = xbuf;
		offset += rgroup * (M + 4);
		main.xbuffer_offset[1][ci] = offset;
		main.xbuffer[1][ci] = xbuf;
	}
}


static void jinit_d_main_controller (jpeg_decompress_struct cinfo, boolean need_full_buffer) {
	int ci, rgroup, ngroups;
	jpeg_component_info compptr;

	jpeg_d_main_controller main = cinfo.main = new jpeg_d_main_controller();
//	main.pub.start_pass = start_pass_main;

	if (need_full_buffer)		/* shouldn't happen */
		error();
//		ERREXIT(cinfo, JERR_BAD_BUFFER_MODE);

	/* Allocate the workspace.
	 * ngroups is the number of row groups we need.
	 */
	if (cinfo.upsample.need_context_rows) {
		if (cinfo.min_DCT_scaled_size < 2) /* unsupported, see comments above */
			error();
//			ERREXIT(cinfo, JERR_NOTIMPL);
		alloc_funny_pointers(cinfo); /* Alloc space for xbuffer[] lists */
		ngroups = cinfo.min_DCT_scaled_size + 2;
	} else {
		ngroups = cinfo.min_DCT_scaled_size;
	}

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		rgroup = (compptr.v_samp_factor * compptr.DCT_scaled_size) / cinfo.min_DCT_scaled_size; /* height of a row group of component */
		main.buffer[ci] = new byte[rgroup * ngroups][compptr.width_in_blocks * compptr.DCT_scaled_size];
	}
}

static long jround_up (long a, long b)
/* Compute a rounded up to next multiple of b, ie, ceil(a/b)*b */
/* Assumes a >= 0, b > 0 */
{
	a += b - 1L;
	return a - (a % b);
}

static void jinit_upsampler (jpeg_decompress_struct cinfo) {
	int ci;
	jpeg_component_info compptr;
	boolean need_buffer, do_fancy;
	int h_in_group, v_in_group, h_out_group, v_out_group;

	jpeg_upsampler upsample = new jpeg_upsampler();
	cinfo.upsample = upsample;
//	upsample.start_pass = start_pass_upsample;
//	upsample.upsample = sep_upsample;
	upsample.need_context_rows = false; /* until we find out differently */

	if (cinfo.CCIR601_sampling)	/* this isn't supported */
		error();
//		ERREXIT(cinfo, JERR_CCIR601_NOTIMPL);

	/* jdmainct.c doesn't support context rows when min_DCT_scaled_size = 1,
	 * so don't ask for it.
	 */
	do_fancy = cinfo.do_fancy_upsampling && cinfo.min_DCT_scaled_size > 1;

	/* Verify we can handle the sampling factors, select per-component methods,
	 * and create storage as needed.
	 */
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* Compute size of an "input group" after IDCT scaling.	This many samples
		 * are to be converted to max_h_samp_factor * max_v_samp_factor pixels.
		 */
		h_in_group = (compptr.h_samp_factor * compptr.DCT_scaled_size) /
		 cinfo.min_DCT_scaled_size;
		v_in_group = (compptr.v_samp_factor * compptr.DCT_scaled_size) /
		 cinfo.min_DCT_scaled_size;
		h_out_group = cinfo.max_h_samp_factor;
		v_out_group = cinfo.max_v_samp_factor;
		upsample.rowgroup_height[ci] = v_in_group; /* save for use later */
		need_buffer = true;
		if (! compptr.component_needed) {
			/* Don't bother to upsample an uninteresting component. */
			upsample.methods[ci] = NOOP_UPSAMPLE;
			need_buffer = false;
		} else if (h_in_group == h_out_group && v_in_group == v_out_group) {
			/* Fullsize components can be processed without any work. */
			upsample.methods[ci] = FULLSIZE_UPSAMPLE;
			need_buffer = false;
		} else if (h_in_group * 2 == h_out_group && v_in_group == v_out_group) {
			/* Special cases for 2h1v upsampling */
			if (do_fancy && compptr.downsampled_width > 2)
				upsample.methods[ci] = H2V1_FANCY_UPSAMPLE;
			else
				upsample.methods[ci] = H2V1_UPSAMPLE;
		} else if (h_in_group * 2 == h_out_group && v_in_group * 2 == v_out_group) {
			/* Special cases for 2h2v upsampling */
			if (do_fancy && compptr.downsampled_width > 2) {
				upsample.methods[ci] = H2V2_FANCY_UPSAMPLE;
				upsample.need_context_rows = true;
			} else
				upsample.methods[ci] = H2V2_UPSAMPLE;
		} else if ((h_out_group % h_in_group) == 0 && (v_out_group % v_in_group) == 0) {
			/* Generic integral-factors upsampling method */
			upsample.methods[ci] = INT_UPSAMPLE;
			upsample.h_expand[ci] = (byte) (h_out_group / h_in_group);
			upsample.v_expand[ci] = (byte) (v_out_group / v_in_group);
		} else
			error();
//			ERREXIT(cinfo, JERR_FRACT_SAMPLE_NOTIMPL);
		if (need_buffer) {
			upsample.color_buf[ci] = new byte[cinfo.max_v_samp_factor]
						 [(int) jround_up(cinfo.output_width, cinfo.max_h_samp_factor)];
		}
	}
}

static void jinit_phuff_decoder (jpeg_decompress_struct cinfo) {
	int[][] coef_bit_ptr;
	int ci, i;

	cinfo.entropy = new phuff_entropy_decoder();
//	entropy.pub.start_pass = start_pass_phuff_decoder;

	/* Create progression status table */
	cinfo.coef_bits = new int[cinfo.num_components][DCTSIZE2];
	coef_bit_ptr = cinfo.coef_bits;
	for (ci = 0; ci < cinfo.num_components; ci++) 
		for (i = 0; i < DCTSIZE2; i++)
			coef_bit_ptr[ci][i] = -1;
}


static void jinit_huff_decoder (jpeg_decompress_struct cinfo) {

	cinfo.entropy = new huff_entropy_decoder();
//	entropy.pub.start_pass = start_pass_huff_decoder;
//	entropy.pub.decode_mcu = decode_mcu;

}

static void jinit_inverse_dct (jpeg_decompress_struct cinfo) {
	int ci;
	jpeg_component_info compptr;

	jpeg_inverse_dct idct = cinfo.idct = new jpeg_inverse_dct();
//	idct.pub.start_pass = start_pass;

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* Allocate and pre-zero a multiplier table for each component */
		compptr.dct_table = new int[DCTSIZE2];
		/* Mark multiplier table not yet set up for any method */
		idct.cur_method[ci] = -1;
	}
}

static final int CONST_BITS = 13;
static final int PASS1_BITS = 2;
static final int RANGE_MASK =(MAXJSAMPLE * 4 + 3);
static void jpeg_idct_islow (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	short[] coef_block,
	byte[][] output_buf, int output_buf_offset, int output_col)
{
	int tmp0, tmp1, tmp2, tmp3;
	int tmp10, tmp11, tmp12, tmp13;
	int z1, z2, z3, z4, z5;
	short[] inptr;
	int[] quantptr;
	int[] wsptr;
	byte[] outptr;
	byte[] range_limit = cinfo.sample_range_limit;
	int range_limit_offset = cinfo.sample_range_limit_offset + CENTERJSAMPLE;
	int ctr;
	int[] workspace = cinfo.workspace;	/* buffers data between passes */
//	SHIFT_TEMPS
	
	/* Pass 1: process columns from input, store into work array. */
	/* Note results are scaled up by sqrt(8) compared to a true IDCT; */
	/* furthermore, we scale the results by 2**PASS1_BITS. */
	
	inptr = coef_block;
	quantptr = compptr.dct_table;
	wsptr = workspace;
	int inptr_offset = 0, quantptr_offset = 0, wsptr_offset = 0;
	for (ctr = DCTSIZE; ctr > 0; ctr--) {
		/* Due to quantization, we will usually find that many of the input
		 * coefficients are zero, especially the AC terms.	We can exploit this
		 * by short-circuiting the IDCT calculation for any column in which all
		 * the AC terms are zero.	In that case each output is equal to the
		 * DC coefficient (with scale factor as needed).
		 * With typical images and quantization tables, half or more of the
		 * column DCT calculations can be simplified this way.
		 */
		
		if (inptr[DCTSIZE*1+inptr_offset] == 0 && inptr[DCTSIZE*2+inptr_offset] == 0 &&
			inptr[DCTSIZE*3+inptr_offset] == 0 && inptr[DCTSIZE*4+inptr_offset] == 0 &&
			inptr[DCTSIZE*5+inptr_offset] == 0 && inptr[DCTSIZE*6+inptr_offset] == 0 &&
			inptr[DCTSIZE*7+inptr_offset] == 0)
		{
			/* AC terms all zero */
			int dcval = ((inptr[DCTSIZE*0+inptr_offset]) * quantptr[DCTSIZE*0+quantptr_offset]) << PASS1_BITS;
			
			wsptr[DCTSIZE*0+wsptr_offset] = dcval;
			wsptr[DCTSIZE*1+wsptr_offset] = dcval;
			wsptr[DCTSIZE*2+wsptr_offset] = dcval;
			wsptr[DCTSIZE*3+wsptr_offset] = dcval;
			wsptr[DCTSIZE*4+wsptr_offset] = dcval;
			wsptr[DCTSIZE*5+wsptr_offset] = dcval;
			wsptr[DCTSIZE*6+wsptr_offset] = dcval;
			wsptr[DCTSIZE*7+wsptr_offset] = dcval;
			
			inptr_offset++;			/* advance pointers to next column */
			quantptr_offset++;
			wsptr_offset++;
			continue;
		}
		
		/* Even part: reverse the even part of the forward DCT. */
		/* The rotator is sqrt(2)*c(-6). */
		
		z2 = ((inptr[DCTSIZE*2+inptr_offset]) * quantptr[DCTSIZE*2+quantptr_offset]);
		z3 = ((inptr[DCTSIZE*6+inptr_offset]) * quantptr[DCTSIZE*6+quantptr_offset]);
		
		z1 = ((z2 + z3) * 4433/*FIX_0_541196100*/);
		tmp2 = z1 + (z3 * - 15137/*FIX_1_847759065*/);
		tmp3 = z1 + (z2 * 6270/*FIX_0_765366865*/);
		
		z2 = ((inptr[DCTSIZE*0+inptr_offset]) * quantptr[DCTSIZE*0+quantptr_offset]);
		z3 = ((inptr[DCTSIZE*4+inptr_offset]) * quantptr[DCTSIZE*4+quantptr_offset]);

		tmp0 = (z2 + z3) << CONST_BITS;
		tmp1 = (z2 - z3) << CONST_BITS;
		
		tmp10 = tmp0 + tmp3;
		tmp13 = tmp0 - tmp3;
		tmp11 = tmp1 + tmp2;
		tmp12 = tmp1 - tmp2;
		
		/* Odd part per figure 8; the matrix is unitary and hence its
		 * transpose is its inverse.	i0..i3 are y7,y5,y3,y1 respectively.
		 */
		
		tmp0 = ((inptr[DCTSIZE*7+inptr_offset]) * quantptr[DCTSIZE*7+quantptr_offset]);
		tmp1 = ((inptr[DCTSIZE*5+inptr_offset]) * quantptr[DCTSIZE*5+quantptr_offset]);
		tmp2 = ((inptr[DCTSIZE*3+inptr_offset]) * quantptr[DCTSIZE*3+quantptr_offset]);
		tmp3 = ((inptr[DCTSIZE*1+inptr_offset]) * quantptr[DCTSIZE*1+quantptr_offset]);
		
		z1 = tmp0 + tmp3;
		z2 = tmp1 + tmp2;
		z3 = tmp0 + tmp2;
		z4 = tmp1 + tmp3;
		z5 = ((z3 + z4) * 9633/*FIX_1_175875602*/); /* sqrt(2) * c3 */
		
		tmp0 = (tmp0 * 2446/*FIX_0_298631336*/); /* sqrt(2) * (-c1+c3+c5-c7) */
		tmp1 = (tmp1 * 16819/*FIX_2_053119869*/); /* sqrt(2) * ( c1+c3-c5+c7) */
		tmp2 = (tmp2 * 25172/*FIX_3_072711026*/); /* sqrt(2) * ( c1+c3+c5-c7) */
		tmp3 = (tmp3 * 12299/*FIX_1_501321110*/); /* sqrt(2) * ( c1+c3-c5-c7) */
		z1 = (z1 * - 7373/*FIX_0_899976223*/); /* sqrt(2) * (c7-c3) */
		z2 = (z2 * - 20995/*FIX_2_562915447*/); /* sqrt(2) * (-c1-c3) */
		z3 = (z3 * - 16069/*FIX_1_961570560*/); /* sqrt(2) * (-c3-c5) */
		z4 = (z4 * - 3196/*FIX_0_390180644*/); /* sqrt(2) * (c5-c3) */
		
		z3 += z5;
		z4 += z5;
		
		tmp0 += z1 + z3;
		tmp1 += z2 + z4;
		tmp2 += z2 + z3;
		tmp3 += z1 + z4;
		
		/* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */
		
//		#define DESCALE(x,n)	RIGHT_SHIFT((x) + (ONE << ((n)-1)), n)
		wsptr[DCTSIZE*0+wsptr_offset] = (((tmp10 + tmp3) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*7+wsptr_offset] = (((tmp10 - tmp3) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*1+wsptr_offset] = (((tmp11 + tmp2) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*6+wsptr_offset] = (((tmp11 - tmp2) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*2+wsptr_offset] = (((tmp12 + tmp1) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*5+wsptr_offset] = (((tmp12 - tmp1) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*3+wsptr_offset] = (((tmp13 + tmp0) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		wsptr[DCTSIZE*4+wsptr_offset] = (((tmp13 - tmp0) + (1 << ((CONST_BITS-PASS1_BITS)-1))) >> (CONST_BITS-PASS1_BITS));
		
		inptr_offset++;			/* advance pointers to next column */
		quantptr_offset++;
		wsptr_offset++;
	}

	
	/* Pass 2: process rows from work array, store into output array. */
	/* Note that we must descale the results by a factor of 8 == 2**3, */
	/* and also undo the PASS1_BITS scaling. */

	int outptr_offset = 0;
	wsptr = workspace;
	wsptr_offset =0;
	for (ctr = 0; ctr < DCTSIZE; ctr++) {
		outptr = output_buf[ctr+output_buf_offset];
		outptr_offset = output_col;
		/* Rows of zeroes can be exploited in the same way as we did with columns.
		 * However, the column calculation has created many nonzero AC terms, so
		 * the simplification applies less often (typically 5% to 10% of the time).
		 * On machines with very fast multiplication, it's possible that the
		 * test takes more time than it's worth.	In that case this section
		 * may be commented out.
		 */
		
//#ifndef NO_ZERO_ROW_TEST
		if (wsptr[1+wsptr_offset] == 0 && wsptr[2+wsptr_offset] == 0 && wsptr[3+wsptr_offset] == 0 && wsptr[4+wsptr_offset] == 0 &&
			wsptr[5+wsptr_offset] == 0 && wsptr[6+wsptr_offset] == 0 && wsptr[7+wsptr_offset] == 0)
		{
			/* AC terms all zero */
//			#define DESCALE(x,n)	RIGHT_SHIFT((x) + (ONE << ((n)-1)), n)
			byte dcval = range_limit[range_limit_offset + ((((wsptr[0+wsptr_offset]) + (1 << ((PASS1_BITS+3)-1))) >> PASS1_BITS+3)
					& RANGE_MASK)];
			
			outptr[0+outptr_offset] = dcval;
			outptr[1+outptr_offset] = dcval;
			outptr[2+outptr_offset] = dcval;
			outptr[3+outptr_offset] = dcval;
			outptr[4+outptr_offset] = dcval;
			outptr[5+outptr_offset] = dcval;
			outptr[6+outptr_offset] = dcval;
			outptr[7+outptr_offset] = dcval;

			wsptr_offset += DCTSIZE;		/* advance pointer to next row */
			continue;
		}
//#endif
		
		/* Even part: reverse the even part of the forward DCT. */
		/* The rotator is sqrt(2)*c(-6). */
		
		z2 = wsptr[2+wsptr_offset];
		z3 = wsptr[6+wsptr_offset];
		
		z1 = ((z2 + z3) * 4433/*FIX_0_541196100*/);
		tmp2 = z1 + (z3 * - 15137/*FIX_1_847759065*/);
		tmp3 = z1 + (z2 * 6270/*FIX_0_765366865*/);
		
		tmp0 = (wsptr[0+wsptr_offset] + wsptr[4+wsptr_offset]) << CONST_BITS;
		tmp1 = (wsptr[0+wsptr_offset] - wsptr[4+wsptr_offset]) << CONST_BITS;
		
		tmp10 = tmp0 + tmp3;
		tmp13 = tmp0 - tmp3;
		tmp11 = tmp1 + tmp2;
		tmp12 = tmp1 - tmp2;
		
		/* Odd part per figure 8; the matrix is unitary and hence its
		 * transpose is its inverse.	i0..i3 are y7,y5,y3,y1 respectively.
		 */
		
		tmp0 = wsptr[7+wsptr_offset];
		tmp1 = wsptr[5+wsptr_offset];
		tmp2 = wsptr[3+wsptr_offset];
		tmp3 = wsptr[1+wsptr_offset];
		
		z1 = tmp0 + tmp3;
		z2 = tmp1 + tmp2;
		z3 = tmp0 + tmp2;
		z4 = tmp1 + tmp3;
		z5 = ((z3 + z4) * 9633/*FIX_1_175875602*/); /* sqrt(2) * c3 */
		
		tmp0 = (tmp0 * 2446/*FIX_0_298631336*/); /* sqrt(2) * (-c1+c3+c5-c7) */
		tmp1 = (tmp1 * 16819/*FIX_2_053119869*/); /* sqrt(2) * ( c1+c3-c5+c7) */
		tmp2 = (tmp2 * 25172/*FIX_3_072711026*/); /* sqrt(2) * ( c1+c3+c5-c7) */
		tmp3 = (tmp3 * 12299/*FIX_1_501321110*/); /* sqrt(2) * ( c1+c3-c5-c7) */
		z1 = (z1 * - 7373/*FIX_0_899976223*/); /* sqrt(2) * (c7-c3) */
		z2 = (z2 * - 20995/*FIX_2_562915447*/); /* sqrt(2) * (-c1-c3) */
		z3 = (z3 * - 16069/*FIX_1_961570560*/); /* sqrt(2) * (-c3-c5) */
		z4 = (z4 * - 3196/*FIX_0_390180644*/); /* sqrt(2) * (c5-c3) */
		
		z3 += z5;
		z4 += z5;
		
		tmp0 += z1 + z3;
		tmp1 += z2 + z4;
		tmp2 += z2 + z3;
		tmp3 += z1 + z4;
		
		/* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */
		

//		#define DESCALE(x,n)	RIGHT_SHIFT((x) + (ONE << ((n)-1)), n)
		outptr[0+outptr_offset] = range_limit[range_limit_offset + ((((tmp10 + tmp3) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
						CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[7+outptr_offset] = range_limit[range_limit_offset + ((((tmp10 - tmp3) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[1+outptr_offset] = range_limit[range_limit_offset + ((((tmp11 + tmp2) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[6+outptr_offset] = range_limit[range_limit_offset + ((((tmp11 - tmp2) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[2+outptr_offset] = range_limit[range_limit_offset + ((((tmp12 + tmp1) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[5+outptr_offset] = range_limit[range_limit_offset + ((((tmp12 - tmp1) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[3+outptr_offset] = range_limit[range_limit_offset + ((((tmp13 + tmp0) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];
		outptr[4+outptr_offset] = range_limit[range_limit_offset + ((((tmp13 - tmp0) + (1 << ((CONST_BITS+PASS1_BITS+3)-1))) >>
									CONST_BITS+PASS1_BITS+3)
					& RANGE_MASK)];

		wsptr_offset += DCTSIZE;		/* advance pointer to next row */
	}
}

static void upsample (jpeg_decompress_struct cinfo,
	byte[][][] input_buf, int[] input_buf_offset, int[] in_row_group_ctr,
	int in_row_groups_avail,
	byte[][] output_buf, int[] out_row_ctr,
	int out_rows_avail)
{
	sep_upsample(cinfo, input_buf, input_buf_offset, in_row_group_ctr, in_row_groups_avail, output_buf, out_row_ctr, out_rows_avail);
}

static boolean smoothing_ok (jpeg_decompress_struct cinfo) {
	jpeg_d_coef_controller coef = cinfo.coef;
	boolean smoothing_useful = false;
	int ci, coefi;
	jpeg_component_info compptr;
	JQUANT_TBL qtable;
	int[] coef_bits;
	int[] coef_bits_latch;

	if (! cinfo.progressive_mode || cinfo.coef_bits == null)
		return false;

	/* Allocate latch area if not already done */
	if (coef.coef_bits_latch == null)
		coef.coef_bits_latch = new int[cinfo.num_components * SAVED_COEFS];
	coef_bits_latch = coef.coef_bits_latch;
	int coef_bits_latch_offset = 0;

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* All components' quantization values must already be latched. */
		if ((qtable = compptr.quant_table) == null)
			return false;
		/* Verify DC & first 5 AC quantizers are nonzero to avoid zero-divide. */
		if (qtable.quantval[0] == 0 ||
			qtable.quantval[Q01_POS] == 0 ||
			qtable.quantval[Q10_POS] == 0 ||
			qtable.quantval[Q20_POS] == 0 ||
			qtable.quantval[Q11_POS] == 0 ||
			qtable.quantval[Q02_POS] == 0)
				return false;
		/* DC values must be at least partly known for all components. */
		coef_bits = cinfo.coef_bits[ci];
		if (coef_bits[0] < 0)
			return false;
		/* Block smoothing is helpful if some AC coefficients remain inaccurate. */
		for (coefi = 1; coefi <= 5; coefi++) {
			coef_bits_latch[coefi+coef_bits_latch_offset] = coef_bits[coefi];
			if (coef_bits[coefi] != 0)
				smoothing_useful = true;
		}
		coef_bits_latch_offset += SAVED_COEFS;
	}

	return smoothing_useful;
}

static void master_selection (jpeg_decompress_struct cinfo) {
	jpeg_decomp_master master = cinfo.master;
	boolean use_c_buffer;
	long samplesperrow;
	int jd_samplesperrow;

	/* Initialize dimensions and other stuff */
	jpeg_calc_output_dimensions(cinfo);
	prepare_range_limit_table(cinfo);

	/* Width of an output scanline must be representable as JDIMENSION. */
	samplesperrow = (long) cinfo.output_width * (long) cinfo.out_color_components;
	jd_samplesperrow = (int) samplesperrow;
	if ( jd_samplesperrow != samplesperrow)
		error();
//		ERREXIT(cinfo, JERR_WIDTH_OVERFLOW);

	/* Initialize my private state */
	master.pass_number = 0;
	master.using_merged_upsample = use_merged_upsample(cinfo);

	/* Color quantizer selection */
	master.quantizer_1pass = null;
	master.quantizer_2pass = null;
	/* No mode changes if not using buffered-image mode. */
	if (! cinfo.quantize_colors || ! cinfo.buffered_image) {
		cinfo.enable_1pass_quant = false;
		cinfo.enable_external_quant = false;
		cinfo.enable_2pass_quant = false;
	}
	if (cinfo.quantize_colors) {
		error(SWT.ERROR_NOT_IMPLEMENTED);
//		if (cinfo.raw_data_out)
//			ERREXIT(cinfo, JERR_NOTIMPL);
//		/* 2-pass quantizer only works in 3-component color space. */
//		if (cinfo.out_color_components != 3) {
//			cinfo.enable_1pass_quant = true;
//			cinfo.enable_external_quant = false;
//			cinfo.enable_2pass_quant = false;
//			cinfo.colormap = null;
//		} else if (cinfo.colormap != null) {
//			cinfo.enable_external_quant = true;
//		} else if (cinfo.two_pass_quantize) {
//			cinfo.enable_2pass_quant = true;
//		} else {
//			cinfo.enable_1pass_quant = true;
//		}
//
//		if (cinfo.enable_1pass_quant) {
//#ifdef QUANT_1PASS_SUPPORTED
//			jinit_1pass_quantizer(cinfo);
//			master.quantizer_1pass = cinfo.cquantize;
//#else
//			ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif
//		}
//
//		/* We use the 2-pass code to map to external colormaps. */
//		if (cinfo.enable_2pass_quant || cinfo.enable_external_quant) {
//#ifdef QUANT_2PASS_SUPPORTED
//			jinit_2pass_quantizer(cinfo);
//			master.quantizer_2pass = cinfo.cquantize;
//#else
//			ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif
//		}
//		/* If both quantizers are initialized, the 2-pass one is left active;
//		 * this is necessary for starting with quantization to an external map.
//		 */
	}

	/* Post-processing: in particular, color conversion first */
	if (! cinfo.raw_data_out) {
		if (master.using_merged_upsample) {
//#ifdef UPSAMPLE_MERGING_SUPPORTED
//			jinit_merged_upsampler(cinfo); /* does color conversion too */
//#else
			error();
//			ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif
		} else {
			jinit_color_deconverter(cinfo);
			jinit_upsampler(cinfo);
		}
		jinit_d_post_controller(cinfo, cinfo.enable_2pass_quant);
	}
	/* Inverse DCT */
	jinit_inverse_dct(cinfo);
	/* Entropy decoding: either Huffman or arithmetic coding. */
	if (cinfo.arith_code) {
		error();
//		ERREXIT(cinfo, JERR_ARITH_NOTIMPL);
	} else {
		if (cinfo.progressive_mode) {
//#ifdef D_PROGRESSIVE_SUPPORTED
			jinit_phuff_decoder(cinfo);
//#else
//			ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif
		} else
			jinit_huff_decoder(cinfo);
	}

	/* Initialize principal buffer controllers. */
	use_c_buffer = cinfo.inputctl.has_multiple_scans || cinfo.buffered_image;
	jinit_d_coef_controller(cinfo, use_c_buffer);

	if (! cinfo.raw_data_out)
		jinit_d_main_controller(cinfo, false /* never need full buffer here */);

	/* Initialize input side of decompressor to consume first scan. */
	start_input_pass (cinfo);

//#ifdef D_MULTISCAN_FILES_SUPPORTED
	/* If jpeg_start_decompress will read the whole file, initialize
	 * progress monitoring appropriately.	The input step is counted
	 * as one pass.
	 */
//	if (cinfo.progress != null && ! cinfo.buffered_image &&
//			cinfo.inputctl.has_multiple_scans) {
//		int nscans;
//		/* Estimate number of scans to set pass_limit. */
//		if (cinfo.progressive_mode) {
//			/* Arbitrarily estimate 2 interleaved DC scans + 3 AC scans/component. */
//			nscans = 2 + 3 * cinfo.num_components;
//		} else {
//			/* For a nonprogressive multiscan file, estimate 1 scan per component. */
//			nscans = cinfo.num_components;
//		}
//		cinfo.progress.pass_counter = 0L;
//		cinfo.progress.pass_limit = (long) cinfo.total_iMCU_rows * nscans;
//		cinfo.progress.completed_passes = 0;
//		cinfo.progress.total_passes = (cinfo.enable_2pass_quant ? 3 : 2);
//		/* Count the input pass as done */
//		master.pass_number++;
//	}
//#endif /* D_MULTISCAN_FILES_SUPPORTED */
}

static void jinit_master_decompress (jpeg_decompress_struct cinfo) {
	jpeg_decomp_master master = new jpeg_decomp_master();
	cinfo.master = master;
//	master.prepare_for_output_pass = prepare_for_output_pass;
//	master.finish_output_pass = finish_output_pass;

	master.is_dummy_pass = false;

	master_selection(cinfo);
}

static void
jcopy_sample_rows (byte[][] input_array, int source_row,
		   byte[][] output_array, int dest_row,
		   int num_rows, int num_cols)
/* Copy some rows of samples from one place to another.
 * num_rows rows are copied from input_array[source_row++]
 * to output_array[dest_row++]; these areas may overlap for duplication.
 * The source and destination arrays must be at least as wide as num_cols.
 */
{
  byte[] inptr, outptr;
  int count = num_cols;
  int row;

  int input_array_offset = source_row;
  int output_array_offset = dest_row;

  for (row = num_rows; row > 0; row--) {
    inptr = input_array[input_array_offset++];
    outptr = output_array[output_array_offset++];
    System.arraycopy(inptr, 0, outptr, 0, count);
  }
}

static boolean jpeg_start_decompress (jpeg_decompress_struct cinfo) {
	if (cinfo.global_state == DSTATE_READY) {
		/* First call: initialize master control, select active modules */
		jinit_master_decompress(cinfo);
		if (cinfo.buffered_image) {
			/* No more work here; expecting jpeg_start_output next */
			cinfo.global_state = DSTATE_BUFIMAGE;
			return true;
		}
		cinfo.global_state = DSTATE_PRELOAD;
	}
	if (cinfo.global_state == DSTATE_PRELOAD) {
		/* If file has multiple scans, absorb them all into the coef buffer */
		if (cinfo.inputctl.has_multiple_scans) {
//#ifdef D_MULTISCAN_FILES_SUPPORTED
			for (;;) {
				int retcode;
				/* Call progress monitor hook if present */
//				if (cinfo.progress != null)
//					(*cinfo.progress.progress_monitor) ((j_common_ptr) cinfo);
				/* Absorb some more input */
				retcode = consume_input (cinfo);
				if (retcode == JPEG_SUSPENDED)
					return false;
				if (retcode == JPEG_REACHED_EOI)
					break;
				/* Advance progress counter if appropriate */
//				if (cinfo.progress != null && (retcode == JPEG_ROW_COMPLETED || retcode == JPEG_REACHED_SOS)) {
//					if (++cinfo.progress.pass_counter >= cinfo.progress.pass_limit) {
//						/* jdmaster underestimated number of scans; ratchet up one scan */
//						cinfo.progress.pass_limit += (long) cinfo.total_iMCU_rows;
//					}
//				}
			}
//#else
//			ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif /* D_MULTISCAN_FILES_SUPPORTED */
		}
		cinfo.output_scan_number = cinfo.input_scan_number;
	} else if (cinfo.global_state != DSTATE_PRESCAN)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	/* Perform any dummy output passes, and set up for the final pass */
	return output_pass_setup(cinfo);
}

static void prepare_for_output_pass (jpeg_decompress_struct cinfo) {
	jpeg_decomp_master master = cinfo.master;

	if (master.is_dummy_pass) {
//#ifdef QUANT_2PASS_SUPPORTED
//		/* Final pass of 2-pass quantization */
//		master.pub.is_dummy_pass = FALSE;
//		(*cinfo.cquantize.start_pass) (cinfo, FALSE);
//		(*cinfo.post.start_pass) (cinfo, JBUF_CRANK_DEST);
//		(*cinfo.main.start_pass) (cinfo, JBUF_CRANK_DEST);
//#else
		error(SWT.ERROR_NOT_IMPLEMENTED);
//		ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif /* QUANT_2PASS_SUPPORTED */
	} else {
		if (cinfo.quantize_colors && cinfo.colormap == null) {
			/* Select new quantization method */
			if (cinfo.two_pass_quantize && cinfo.enable_2pass_quant) {
				cinfo.cquantize = master.quantizer_2pass;
				master.is_dummy_pass = true;
			} else if (cinfo.enable_1pass_quant) {
				cinfo.cquantize = master.quantizer_1pass;
			} else {
				error();
//	ERREXIT(cinfo, JERR_MODE_CHANGE);
			}
		}
		cinfo.idct.start_pass (cinfo);
		start_output_pass (cinfo);
		if (! cinfo.raw_data_out) {
			if (! master.using_merged_upsample)
				cinfo.cconvert.start_pass (cinfo);
			cinfo.upsample.start_pass (cinfo);
			if (cinfo.quantize_colors) 
				cinfo.cquantize.start_pass (cinfo, master.is_dummy_pass);
			cinfo.post.start_pass (cinfo, (master.is_dummy_pass ? JBUF_SAVE_AND_PASS : JBUF_PASS_THRU));
			cinfo.main.start_pass (cinfo, JBUF_PASS_THRU);
		}
	}

//	/* Set up progress monitor's pass info if present */
//	if (cinfo.progress != NULL) {
//		cinfo.progress.completed_passes = master.pass_number;
//		cinfo.progress.total_passes = master.pass_number +
//						(master.pub.is_dummy_pass ? 2 : 1);
//		/* In buffered-image mode, we assume one more output pass if EOI not
//		 * yet reached, but no more passes if EOI has been reached.
//		 */
//		if (cinfo.buffered_image && ! cinfo.inputctl.eoi_reached) {
//			cinfo.progress.total_passes += (cinfo.enable_2pass_quant ? 2 : 1);
//		}
//	}
}


static boolean jpeg_resync_to_restart (jpeg_decompress_struct cinfo, int desired) {
	int marker = cinfo.unread_marker;
	int action = 1;
	
	/* Always put up a warning. */
//	WARNMS2(cinfo, JWRN_MUST_RESYNC, marker, desired);
	
	/* Outer loop handles repeated decision after scanning forward. */
	for (;;) {
		if (marker < M_SOF0)
			action = 2;		/* invalid marker */
		else if (marker < M_RST0 || marker > M_RST7)
			action = 3;		/* valid non-restart marker */
		else {
			if (marker == (M_RST0 + ((desired+1) & 7)) || marker == ( M_RST0 + ((desired+2) & 7)))
				action = 3;		/* one of the next two expected restarts */
			else if (marker == (M_RST0 + ((desired-1) & 7)) || marker == ( M_RST0 + ((desired-2) & 7)))
				action = 2;		/* a prior restart, so advance */
			else
				action = 1;		/* desired restart or too far away */
		}
//		TRACEMS2(cinfo, 4, JTRC_RECOVERY_ACTION, marker, action);
		switch (action) {
			case 1:
				/* Discard marker and let entropy decoder resume processing. */
				cinfo.unread_marker = 0;
				return true;
			case 2:
				/* Scan to the next marker, and repeat the decision loop. */
				if (! next_marker(cinfo))
					return false;
				marker = cinfo.unread_marker;
				break;
			case 3:
				/* Return without advancing past this marker. */
				/* Entropy decoder will be forced to process an empty segment. */
				return true;
		}
	} /* end loop */
}

static boolean read_restart_marker (jpeg_decompress_struct cinfo) {
	/* Obtain a marker unless we already did. */
	/* Note that next_marker will complain if it skips any data. */
	if (cinfo.unread_marker == 0) {
		if (! next_marker(cinfo))
			return false;
	}

	if (cinfo.unread_marker == (M_RST0 + cinfo.marker.next_restart_num)) {
		/* Normal case --- swallow the marker and let entropy decoder continue */
//		TRACEMS1(cinfo, 3, JTRC_RST, cinfo.marker.next_restart_num);
		cinfo.unread_marker = 0;
	} else {
		/* Uh-oh, the restart markers have been messed up. */
		/* Let the data source manager determine how to resync. */
		if (! jpeg_resync_to_restart (cinfo, cinfo.marker.next_restart_num))
			return false;
	}

	/* Update next-restart state */
	cinfo.marker.next_restart_num = (cinfo.marker.next_restart_num + 1) & 7;

	return true;
}

static boolean jpeg_fill_bit_buffer (bitread_working_state state, int get_buffer, int bits_left, int nbits)
/* Load up the bit buffer to a depth of at least nbits */
{
	/* Copy heavily used state fields into locals (hopefully registers) */
	byte[] buffer = state.buffer;
	int bytes_in_buffer = state.bytes_in_buffer;
	int bytes_offset = state.bytes_offset;
	jpeg_decompress_struct cinfo = state.cinfo;

	/* Attempt to load at least MIN_GET_BITS bits into get_buffer. */
	/* (It is assumed that no request will be for more than that many bits.) */
	/* We fail to do so only if we hit a marker or are forced to suspend. */

	if (cinfo.unread_marker == 0) {	/* cannot advance past a marker */
		while (bits_left < MIN_GET_BITS) {
			int c;

			/* Attempt to read a byte */
			if (bytes_offset == bytes_in_buffer) {
				if (! fill_input_buffer (cinfo))
					return false;
				buffer = cinfo.buffer;
				bytes_in_buffer = cinfo.bytes_in_buffer;
				bytes_offset = cinfo.bytes_offset;
			}
			c = buffer[bytes_offset++] & 0xFF;

			/* If it's 0xFF, check and discard stuffed zero byte */
			if (c == 0xFF) {
				/* Loop here to discard any padding FF's on terminating marker,
				 * so that we can save a valid unread_marker value.	NOTE: we will
				 * accept multiple FF's followed by a 0 as meaning a single FF data
				 * byte.	This data pattern is not valid according to the standard.
				 */
				do {
					if (bytes_offset == bytes_in_buffer) {
						if (! fill_input_buffer (cinfo))
							return false;
						buffer = cinfo.buffer;
						bytes_in_buffer = cinfo.bytes_in_buffer;
						bytes_offset = cinfo.bytes_offset;
					}
					c = buffer[bytes_offset++] & 0xFF;
				} while (c == 0xFF);

				if (c == 0) {
					/* Found FF/00, which represents an FF data byte */
					c = 0xFF;
				} else {
					/* Oops, it's actually a marker indicating end of compressed data.
					 * Save the marker code for later use.
					 * Fine point: it might appear that we should save the marker into
					 * bitread working state, not straight into permanent state.	But
					 * once we have hit a marker, we cannot need to suspend within the
					 * current MCU, because we will read no more bytes from the data
					 * source.	So it is OK to update permanent state right away.
					 */
					cinfo.unread_marker = c;
					/* See if we need to insert some fake zero bits. */
//					goto no_more_bytes;
					if (nbits > bits_left) {
						/* Uh-oh.	Report corrupted data to user and stuff zeroes into
						 * the data stream, so that we can produce some kind of image.
						 * We use a nonvolatile flag to ensure that only one warning message
						 * appears per data segment.
						 */
						if (! cinfo.entropy.insufficient_data) {
//							WARNMS(cinfo, JWRN_HIT_MARKER);
							cinfo.entropy.insufficient_data = true;
						}
					/* Fill the buffer with zero bits */
						get_buffer <<= MIN_GET_BITS - bits_left;
						bits_left = MIN_GET_BITS;
					}

					/* Unload the local registers */
					state.buffer = buffer;
					state.bytes_in_buffer = bytes_in_buffer;
					state.bytes_offset = bytes_offset;
					state.get_buffer = get_buffer;
					state.bits_left = bits_left;

					return true;
		
				}
			}

			/* OK, load c into get_buffer */
			get_buffer = (get_buffer << 8) | c;
			bits_left += 8;
		} /* end while */
	} else {
//		no_more_bytes:
		/* We get here if we've read the marker that terminates the compressed
		 * data segment.	There should be enough bits in the buffer register
		 * to satisfy the request; if so, no problem.
		 */
		if (nbits > bits_left) {
			/* Uh-oh.	Report corrupted data to user and stuff zeroes into
			 * the data stream, so that we can produce some kind of image.
			 * We use a nonvolatile flag to ensure that only one warning message
			 * appears per data segment.
			 */
			if (! cinfo.entropy.insufficient_data) {
//				WARNMS(cinfo, JWRN_HIT_MARKER);
				cinfo.entropy.insufficient_data = true;
			}
			/* Fill the buffer with zero bits */
			get_buffer <<= MIN_GET_BITS - bits_left;
			bits_left = MIN_GET_BITS;
		}
	}

	/* Unload the local registers */
	state.buffer = buffer;
	state.bytes_in_buffer = bytes_in_buffer;
	state.bytes_offset = bytes_offset;
	state.get_buffer = get_buffer;
	state.bits_left = bits_left;

	return true;
}

static int jpeg_huff_decode (bitread_working_state state, int get_buffer, int bits_left, d_derived_tbl htbl, int min_bits) {
	int l = min_bits;
	int code;

	/* HUFF_DECODE has determined that the code is at least min_bits */
	/* bits long, so fetch that many bits in one swoop. */

//	CHECK_BIT_BUFFER(*state, l, return -1);
	{
	if (bits_left < (l)) {
		if (! jpeg_fill_bit_buffer(state,get_buffer,bits_left,l)) { 
			return -1;
		}
		get_buffer = (state).get_buffer; bits_left = (state).bits_left;
	}
	}
//	code = GET_BITS(l);
	code = (( (get_buffer >> (bits_left -= (l)))) & ((1<<(l))-1));

	/* Collect the rest of the Huffman code one bit at a time. */
	/* This is per Figure F.16 in the JPEG spec. */

	while (code > htbl.maxcode[l]) {
		code <<= 1;
//		CHECK_BIT_BUFFER(*state, 1, return -1);
		{
		if (bits_left < (1)) {
			if (! jpeg_fill_bit_buffer(state,get_buffer,bits_left,1)) { 
				return -1;
			}
			get_buffer = (state).get_buffer; bits_left = (state).bits_left;
		}
		}
//		code |= GET_BITS(1);
		code |= (( (get_buffer >> (bits_left -= (1)))) & ((1<<(1))-1));
		l++;
	}

	/* Unload the local registers */
	state.get_buffer = get_buffer;
	state.bits_left = bits_left;

	/* With garbage input we may reach the sentinel value l = 17. */

	if (l > 16) {
//		WARNMS(state.cinfo, JWRN_HUFF_BAD_CODE);
		return 0;			/* fake a zero as the safest result */
	}

	return htbl.pub.huffval[ (code + htbl.valoffset[l]) ] & 0xFF;
}

static int decompress_onepass (jpeg_decompress_struct cinfo, byte[][][] output_buf, int[] output_buf_offset) {
	jpeg_d_coef_controller coef = cinfo.coef;
	int MCU_col_num;	/* index of current MCU within row */
	int last_MCU_col = cinfo.MCUs_per_row - 1;
	int last_iMCU_row = cinfo.total_iMCU_rows - 1;
	int blkn, ci, xindex, yindex, yoffset, useful_width;
	byte[][] output_ptr;
	int start_col, output_col;
	jpeg_component_info compptr;
//	inverse_DCT_method_ptr inverse_DCT;

	/* Loop to process as much as one whole iMCU row */
	for (yoffset = coef.MCU_vert_offset; yoffset < coef.MCU_rows_per_iMCU_row; yoffset++) {
		for (MCU_col_num = coef.MCU_ctr; MCU_col_num <= last_MCU_col; MCU_col_num++) {
			/* Try to fetch an MCU.	Entropy decoder expects buffer to be zeroed. */
			for (int i = 0; i < cinfo.blocks_in_MCU; i++) {
				short[] blk = coef.MCU_buffer[i];
				for (int j = 0; j < blk.length; j++) {
					blk[j] = 0;
				}
			}
			if (! cinfo.entropy.decode_mcu (cinfo, coef.MCU_buffer)) {
				/* Suspension forced; update state counters and exit */
				coef.MCU_vert_offset = yoffset;
				coef.MCU_ctr = MCU_col_num;
				return JPEG_SUSPENDED;
			}
			/* Determine where data should go in output_buf and do the IDCT thing.
			 * We skip dummy blocks at the right and bottom edges (but blkn gets
			 * incremented past them!).	Note the inner loop relies on having
			 * allocated the MCU_buffer[] blocks sequentially.
			 */
			blkn = 0;			/* index of current DCT block within MCU */
			for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
				compptr = cinfo.cur_comp_info[ci];
				/* Don't bother to IDCT an uninteresting component. */
				if (! compptr.component_needed) {
					blkn += compptr.MCU_blocks;
					continue;
				}
//				inverse_DCT = cinfo.idct.inverse_DCT[compptr.component_index];
				useful_width = (MCU_col_num < last_MCU_col) ? compptr.MCU_width	: compptr.last_col_width;
				output_ptr = output_buf[compptr.component_index];
				int output_ptr_offset = output_buf_offset[compptr.component_index] + yoffset * compptr.DCT_scaled_size;
				start_col = MCU_col_num * compptr.MCU_sample_width;
				for (yindex = 0; yindex < compptr.MCU_height; yindex++) {
					if (cinfo.input_iMCU_row < last_iMCU_row ||	yoffset+yindex < compptr.last_row_height) {
						output_col = start_col;
						for (xindex = 0; xindex < useful_width; xindex++) {
							jpeg_idct_islow(cinfo, compptr, coef.MCU_buffer[blkn+xindex], output_ptr, output_ptr_offset, output_col);
							output_col += compptr.DCT_scaled_size;
						}
					}
					blkn += compptr.MCU_width;
					output_ptr_offset += compptr.DCT_scaled_size;
				}
			}
		}
		/* Completed an MCU row, but perhaps not an iMCU row */
		coef.MCU_ctr = 0;
	}
	/* Completed the iMCU row, advance counters for next one */
	cinfo.output_iMCU_row++;
	if (++(cinfo.input_iMCU_row) < cinfo.total_iMCU_rows) {
		coef.start_iMCU_row(cinfo);
		return JPEG_ROW_COMPLETED;
	}
	/* Completed the scan */
	finish_input_pass (cinfo);
	return JPEG_SCAN_COMPLETED;
}

static int decompress_smooth_data (jpeg_decompress_struct cinfo, byte[][][] output_buf, int[] output_buf_offset) {
	jpeg_d_coef_controller coef = cinfo.coef;
	int last_iMCU_row = cinfo.total_iMCU_rows - 1;
	int block_num, last_block_column;
	int ci, block_row, block_rows; //, access_rows;
	short[][][] buffer;
	short[][] buffer_ptr, prev_block_row, next_block_row;
	byte[][] output_ptr;
	int output_col;
	jpeg_component_info compptr;
//	inverse_DCT_method_ptr inverse_DCT;
	boolean first_row, last_row;
	short[] workspace = coef.workspace;
	if (workspace == null) workspace = coef.workspace = new short[DCTSIZE2];
	int[] coef_bits;
	JQUANT_TBL quanttbl;
	int Q00,Q01,Q02,Q10,Q11,Q20, num;
	int DC1,DC2,DC3,DC4,DC5,DC6,DC7,DC8,DC9;
	int Al, pred;

	/* Force some input to be done if we are getting ahead of the input. */
	while (cinfo.input_scan_number <= cinfo.output_scan_number && ! cinfo.inputctl.eoi_reached) {
		if (cinfo.input_scan_number == cinfo.output_scan_number) {
			/* If input is working on current scan, we ordinarily want it to
			 * have completed the current row.	But if input scan is DC,
			 * we want it to keep one row ahead so that next block row's DC
			 * values are up to date.
			 */
			int delta = (cinfo.Ss == 0) ? 1 : 0;
			if (cinfo.input_iMCU_row > cinfo.output_iMCU_row+delta)
				break;
		}
		if (consume_input(cinfo) == JPEG_SUSPENDED)
			return JPEG_SUSPENDED;
	}

	/* OK, output from the virtual arrays. */
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* Don't bother to IDCT an uninteresting component. */
		if (! compptr.component_needed)
			continue;
		/* Count non-dummy DCT block rows in this iMCU row. */
		if (cinfo.output_iMCU_row < last_iMCU_row) {
			block_rows = compptr.v_samp_factor;
			//access_rows = block_rows * 2; /* this and next iMCU row */
			last_row = false;
		} else {
			/* NB: can't use last_row_height here; it is input-side-dependent! */
			block_rows = (compptr.height_in_blocks % compptr.v_samp_factor);
			if (block_rows == 0) block_rows = compptr.v_samp_factor;
			//access_rows = block_rows; /* this iMCU row only */
			last_row = true;
		}
		/* Align the virtual buffer for this component. */
		int buffer_offset;
		if (cinfo.output_iMCU_row > 0) {
			//access_rows += compptr.v_samp_factor; /* prior iMCU row too */
			buffer = coef.whole_image[ci];
			buffer_offset = (cinfo.output_iMCU_row - 1) * compptr.v_samp_factor;
			buffer_offset += compptr.v_samp_factor;	/* point to current iMCU row */
			first_row = false;
		} else {
			buffer = coef.whole_image[ci];
			buffer_offset = 0;
			first_row = true;
		}
		/* Fetch component-dependent info */
		coef_bits = coef.coef_bits_latch;
		int coef_offset = (ci * SAVED_COEFS);
		quanttbl = compptr.quant_table;
		Q00 = quanttbl.quantval[0];
		Q01 = quanttbl.quantval[Q01_POS];
		Q10 = quanttbl.quantval[Q10_POS];
		Q20 = quanttbl.quantval[Q20_POS];
		Q11 = quanttbl.quantval[Q11_POS];
		Q02 = quanttbl.quantval[Q02_POS];
//		inverse_DCT = cinfo.idct.inverse_DCT[ci];
		output_ptr = output_buf[ci];
		int output_ptr_offset = output_buf_offset[ci];
		/* Loop over all DCT blocks to be processed. */
		for (block_row = 0; block_row < block_rows; block_row++) {
			buffer_ptr = buffer[block_row+buffer_offset];
			int buffer_ptr_offset = 0, prev_block_row_offset = 0, next_block_row_offset = 0;
			if (first_row && block_row == 0) {
				prev_block_row = buffer_ptr;
				prev_block_row_offset = buffer_ptr_offset;
			} else {
				prev_block_row = buffer[block_row-1+buffer_offset];
				prev_block_row_offset = 0;
			}
			if (last_row && block_row == block_rows-1) {
				next_block_row = buffer_ptr;
				next_block_row_offset = buffer_ptr_offset;
			} else {
				next_block_row = buffer[block_row+1+buffer_offset];
				next_block_row_offset = 0;
			}
			/* We fetch the surrounding DC values using a sliding-register approach.
			 * Initialize all nine here so as to do the right thing on narrow pics.
			 */
			DC1 = DC2 = DC3 = prev_block_row[0+prev_block_row_offset][0];
			DC4 = DC5 = DC6 = buffer_ptr[0+buffer_ptr_offset][0];
			DC7 = DC8 = DC9 = next_block_row[0+next_block_row_offset][0];
			output_col = 0;
			last_block_column = compptr.width_in_blocks - 1;
			for (block_num = 0; block_num <= last_block_column; block_num++) {
				/* Fetch current DCT block into workspace so we can modify it. */
//				jcopy_block_row(buffer_ptr, workspace, 1);
				System.arraycopy(buffer_ptr[buffer_ptr_offset], 0, workspace, 0, workspace.length);
				/* Update DC values */
				if (block_num < last_block_column) {
					DC3 = prev_block_row[1+prev_block_row_offset][0];
					DC6 = buffer_ptr[1+buffer_ptr_offset][0];
					DC9 = next_block_row[1+next_block_row_offset][0];
				}
				/* Compute coefficient estimates per K.8.
				 * An estimate is applied only if coefficient is still zero,
				 * and is not known to be fully accurate.
				 */
				/* AC01 */
				if ((Al=coef_bits[1+coef_offset]) != 0 && workspace[1] == 0) {
					num = 36 * Q00 * (DC4 - DC6);
					if (num >= 0) {
						pred = (((Q01<<7) + num) / (Q01<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
					} else {
						pred = (((Q01<<7) - num) / (Q01<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
						pred = -pred;
					}
					workspace[1] = (short) pred;
				}
				/* AC10 */
				if ((Al=coef_bits[2+coef_offset]) != 0 && workspace[8] == 0) {
					num = 36 * Q00 * (DC2 - DC8);
					if (num >= 0) {
						pred = (((Q10<<7) + num) / (Q10<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
					} else {
						pred = (((Q10<<7) - num) / (Q10<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
						pred = -pred;
					}
					workspace[8] = (short) pred;
				}
				/* AC20 */
				if ((Al=coef_bits[3+coef_offset]) != 0 && workspace[16] == 0) {
					num = 9 * Q00 * (DC2 + DC8 - 2*DC5);
					if (num >= 0) {
						pred = (((Q20<<7) + num) / (Q20<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
					} else {
						pred = (((Q20<<7) - num) / (Q20<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
						pred = -pred;
					}
					workspace[16] = (short) pred;
				}
				/* AC11 */
				if ((Al=coef_bits[4+coef_offset]) != 0 && workspace[9] == 0) {
					num = 5 * Q00 * (DC1 - DC3 - DC7 + DC9);
					if (num >= 0) {
						pred = (((Q11<<7) + num) / (Q11<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
					} else {
						pred = (((Q11<<7) - num) / (Q11<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
						pred = -pred;
					}
					workspace[9] = (short) pred;
				}
				/* AC02 */
				if ((Al=coef_bits[5+coef_offset]) != 0 && workspace[2] == 0) {
					num = 9 * Q00 * (DC4 + DC6 - 2*DC5);
					if (num >= 0) {
						pred = (((Q02<<7) + num) / (Q02<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
					} else {
						pred = (((Q02<<7) - num) / (Q02<<8));
						if (Al > 0 && pred >= (1<<Al))
							pred = (1<<Al)-1;
						pred = -pred;
					}
					workspace[2] = (short) pred;
				}
				/* OK, do the IDCT */
				jpeg_idct_islow(cinfo, compptr, workspace, output_ptr, output_ptr_offset, output_col);
				/* Advance for next column */
				DC1 = DC2; DC2 = DC3;
				DC4 = DC5; DC5 = DC6;
				DC7 = DC8; DC8 = DC9;
				buffer_ptr_offset++; prev_block_row_offset++; next_block_row_offset++;
				output_col += compptr.DCT_scaled_size;
			}
			output_ptr_offset += compptr.DCT_scaled_size;
		}
	}

	if (++(cinfo.output_iMCU_row) < cinfo.total_iMCU_rows)
		return JPEG_ROW_COMPLETED;
	return JPEG_SCAN_COMPLETED;
}

static int decompress_data (jpeg_decompress_struct cinfo, byte[][][] output_buf, int[] output_buf_offset) {
	jpeg_d_coef_controller coef = cinfo.coef;
	int last_iMCU_row = cinfo.total_iMCU_rows - 1;
	int block_num;
	int ci, block_row, block_rows;
	short[][][] buffer;
	short[][] buffer_ptr;
	byte[][] output_ptr;
	int output_col;
	jpeg_component_info compptr;
//	inverse_DCT_method_ptr inverse_DCT;

	/* Force some input to be done if we are getting ahead of the input. */
	while (cinfo.input_scan_number < cinfo.output_scan_number ||
	 (cinfo.input_scan_number == cinfo.output_scan_number &&
		cinfo.input_iMCU_row <= cinfo.output_iMCU_row))
	{
		if (consume_input(cinfo) == JPEG_SUSPENDED)
			return JPEG_SUSPENDED;
	}

	/* OK, output from the virtual arrays. */
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* Don't bother to IDCT an uninteresting component. */
		if (! compptr.component_needed)
			continue;
		/* Align the virtual buffer for this component. */
		buffer = coef.whole_image[ci];
		int buffer_offset = cinfo.output_iMCU_row * compptr.v_samp_factor;
		/* Count non-dummy DCT block rows in this iMCU row. */
		if (cinfo.output_iMCU_row < last_iMCU_row)
			block_rows = compptr.v_samp_factor;
		else {
			/* NB: can't use last_row_height here; it is input-side-dependent! */
			block_rows = (compptr.height_in_blocks % compptr.v_samp_factor);
			if (block_rows == 0) block_rows = compptr.v_samp_factor;
		}
//		inverse_DCT = cinfo.idct.inverse_DCT[ci];
		output_ptr = output_buf[ci];
		int output_ptr_offset = output_buf_offset[ci];
		/* Loop over all DCT blocks to be processed. */
		for (block_row = 0; block_row < block_rows; block_row++) {
			buffer_ptr = buffer[block_row+buffer_offset];
			int buffer_ptr_offset = 0;
			output_col = 0;
			for (block_num = 0; block_num < compptr.width_in_blocks; block_num++) {
				jpeg_idct_islow(cinfo, compptr, buffer_ptr[buffer_ptr_offset], output_ptr, output_ptr_offset, output_col);

				buffer_ptr_offset++;
				output_col += compptr.DCT_scaled_size;
			}
			output_ptr_offset += compptr.DCT_scaled_size;
		}
	}

	if (++(cinfo.output_iMCU_row) < cinfo.total_iMCU_rows)
		return JPEG_ROW_COMPLETED;
	return JPEG_SCAN_COMPLETED;
}

static void post_process_data (jpeg_decompress_struct cinfo,
				byte[][][] input_buf, int[] input_buf_offset, int[] in_row_group_ctr,
				int in_row_groups_avail,
				byte[][] output_buf, int[] out_row_ctr,
				int out_rows_avail)
{
	upsample(cinfo, input_buf, input_buf_offset, in_row_group_ctr, in_row_groups_avail, output_buf, out_row_ctr, out_rows_avail);
}

static void set_bottom_pointers (jpeg_decompress_struct cinfo)
/* Change the pointer lists to duplicate the last sample row at the bottom
 * of the image.	whichptr indicates which xbuffer holds the final iMCU row.
 * Also sets rowgroups_avail to indicate number of nondummy row groups in row.
 */
{
	jpeg_d_main_controller main = cinfo.main;
	int ci, i, rgroup, iMCUheight, rows_left;
	jpeg_component_info compptr;
	byte[][] xbuf;

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		/* Count sample rows in one iMCU row and in one row group */
		iMCUheight = compptr.v_samp_factor * compptr.DCT_scaled_size;
		rgroup = iMCUheight / cinfo.min_DCT_scaled_size;
		/* Count nondummy sample rows remaining for this component */
		rows_left = (compptr.downsampled_height % iMCUheight);
		if (rows_left == 0) rows_left = iMCUheight;
		/* Count nondummy row groups.	Should get same answer for each component,
		 * so we need only do it once.
		 */
		if (ci == 0) {
			main.rowgroups_avail = ((rows_left-1) / rgroup + 1);
		}
		/* Duplicate the last real sample row rgroup*2 times; this pads out the
		 * last partial rowgroup and ensures at least one full rowgroup of context.
		 */
		xbuf = main.xbuffer[main.whichptr][ci];
		int xbuf_offset = main.xbuffer_offset[main.whichptr][ci];
		for (i = 0; i < rgroup * 2; i++) {
			xbuf[rows_left + i + xbuf_offset] = xbuf[rows_left-1 + xbuf_offset];
		}
	}
}

static void set_wraparound_pointers (jpeg_decompress_struct cinfo)
/* Set up the "wraparound" pointers at top and bottom of the pointer lists.
 * This changes the pointer list state from top-of-image to the normal state.
 */
{
	jpeg_d_main_controller main = cinfo.main;
	int ci, i, rgroup;
	int M = cinfo.min_DCT_scaled_size;
	jpeg_component_info compptr;
	byte[][] xbuf0, xbuf1;

	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		rgroup = (compptr.v_samp_factor * compptr.DCT_scaled_size) / cinfo.min_DCT_scaled_size; /* height of a row group of component */
		xbuf0 = main.xbuffer[0][ci];
		int xbuf0_offset = main.xbuffer_offset[0][ci];
		xbuf1 = main.xbuffer[1][ci];
		int xbuf1_offset = main.xbuffer_offset[1][ci];
		for (i = 0; i < rgroup; i++) {
			xbuf0[i - rgroup + xbuf0_offset] = xbuf0[rgroup*(M+1) + i + xbuf0_offset];
			xbuf1[i - rgroup + xbuf1_offset] = xbuf1[rgroup*(M+1) + i + xbuf1_offset];
			xbuf0[rgroup*(M+2) + i + xbuf0_offset] = xbuf0[i + xbuf0_offset];
			xbuf1[rgroup*(M+2) + i + xbuf1_offset] = xbuf1[i + xbuf1_offset];
		}
	}
}

static void process_data_crank_post (jpeg_decompress_struct cinfo,
	byte[][] output_buf, int[] out_row_ctr,
	int out_rows_avail)
{
	error();
}

static void process_data_context_main (jpeg_decompress_struct cinfo,
	byte[][] output_buf, int[] out_row_ctr,
	int out_rows_avail)
{
	jpeg_d_main_controller main = cinfo.main;

	/* Read input data if we haven't filled the main buffer yet */
	if (! main.buffer_full) {
		int result;
		switch (cinfo.coef.decompress_data) {
			case DECOMPRESS_DATA:
				result = decompress_data(cinfo, main.xbuffer[main.whichptr], main.xbuffer_offset[main.whichptr]);
				break;
			case DECOMPRESS_SMOOTH_DATA:
				result = decompress_smooth_data(cinfo, main.xbuffer[main.whichptr], main.xbuffer_offset[main.whichptr]);
				break;
			case DECOMPRESS_ONEPASS:
				result = decompress_onepass(cinfo, main.xbuffer[main.whichptr], main.xbuffer_offset[main.whichptr]);
				break;
			default: result = 0;
		}
		if (result == 0)
			return;			/* suspension forced, can do nothing more */
		main.buffer_full = true;	/* OK, we have an iMCU row to work with */
		main.iMCU_row_ctr++;	/* count rows received */
	}

	/* Postprocessor typically will not swallow all the input data it is handed
	 * in one call (due to filling the output buffer first).	Must be prepared
	 * to exit and restart.	This switch lets us keep track of how far we got.
	 * Note that each case falls through to the next on successful completion.
	 */
	switch (main.context_state) {
		case CTX_POSTPONED_ROW:
			/* Call postprocessor using previously set pointers for postponed row */
			post_process_data (cinfo, main.xbuffer[main.whichptr], main.xbuffer_offset[main.whichptr], main.rowgroup_ctr, main.rowgroups_avail, output_buf, out_row_ctr, out_rows_avail);
			if (main.rowgroup_ctr[0] < main.rowgroups_avail)
				return;			/* Need to suspend */
			main.context_state = CTX_PREPARE_FOR_IMCU;
			if (out_row_ctr[0] >= out_rows_avail)
				return;			/* Postprocessor exactly filled output buf */
			/*FALLTHROUGH*/
		case CTX_PREPARE_FOR_IMCU:
			/* Prepare to process first M-1 row groups of this iMCU row */
			main.rowgroup_ctr[0] = 0;
			main.rowgroups_avail = (cinfo.min_DCT_scaled_size - 1);
			/* Check for bottom of image: if so, tweak pointers to "duplicate"
			 * the last sample row, and adjust rowgroups_avail to ignore padding rows.
			 */
			if (main.iMCU_row_ctr == cinfo.total_iMCU_rows)
				set_bottom_pointers(cinfo);
			main.context_state = CTX_PROCESS_IMCU;
			/*FALLTHROUGH*/
		case CTX_PROCESS_IMCU:
			/* Call postprocessor using previously set pointers */
			post_process_data (cinfo, main.xbuffer[main.whichptr], main.xbuffer_offset[main.whichptr], main.rowgroup_ctr, main.rowgroups_avail, output_buf, out_row_ctr, out_rows_avail);
			if (main.rowgroup_ctr[0] < main.rowgroups_avail)
				return;			/* Need to suspend */
			/* After the first iMCU, change wraparound pointers to normal state */
			if (main.iMCU_row_ctr == 1)
				set_wraparound_pointers(cinfo);
			/* Prepare to load new iMCU row using other xbuffer list */
			main.whichptr ^= 1;	/* 0=>1 or 1=>0 */
			main.buffer_full = false;
			/* Still need to process last row group of this iMCU row, */
			/* which is saved at index M+1 of the other xbuffer */
			main.rowgroup_ctr[0] = (cinfo.min_DCT_scaled_size + 1);
			main.rowgroups_avail =	(cinfo.min_DCT_scaled_size + 2);
			main.context_state = CTX_POSTPONED_ROW;
	}
}

static void process_data_simple_main (jpeg_decompress_struct cinfo, byte[][] output_buf, int[] out_row_ctr, int out_rows_avail) {
	jpeg_d_main_controller main = cinfo.main;
	int rowgroups_avail;

	/* Read input data if we haven't filled the main buffer yet */
	if (! main.buffer_full) {
		int result;
		switch (cinfo.coef.decompress_data) {
			case DECOMPRESS_DATA:
				result = decompress_data(cinfo, main.buffer, main.buffer_offset);
				break;
			case DECOMPRESS_SMOOTH_DATA:
				result = decompress_smooth_data(cinfo, main.buffer, main.buffer_offset);
				break;
			case DECOMPRESS_ONEPASS: 
				result = decompress_onepass(cinfo, main.buffer, main.buffer_offset);
				break;
			default: result = 0;
		}
		if (result == 0)
			return;			/* suspension forced, can do nothing more */
		main.buffer_full = true;	/* OK, we have an iMCU row to work with */
	}

	/* There are always min_DCT_scaled_size row groups in an iMCU row. */
	rowgroups_avail = cinfo.min_DCT_scaled_size;
	/* Note: at the bottom of the image, we may pass extra garbage row groups
	 * to the postprocessor.	The postprocessor has to check for bottom
	 * of image anyway (at row resolution), so no point in us doing it too.
	 */

	/* Feed the postprocessor */
	post_process_data (cinfo, main.buffer, main.buffer_offset, main.rowgroup_ctr, rowgroups_avail, output_buf, out_row_ctr, out_rows_avail);

	/* Has postprocessor consumed all the data yet? If so, mark buffer empty */
	if (main.rowgroup_ctr[0] >= rowgroups_avail) {
		main.buffer_full = false;
		main.rowgroup_ctr[0] = 0;
	}
}

static int jpeg_read_scanlines (jpeg_decompress_struct cinfo, byte[][] scanlines, int max_lines) {

	if (cinfo.global_state != DSTATE_SCANNING)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	if (cinfo.output_scanline >= cinfo.output_height) {
//		WARNMS(cinfo, JWRN_TOO_MUCH_DATA);
		return 0;
	}

	/* Call progress monitor hook if present */
//	if (cinfo.progress != NULL) {
//		cinfo.progress.pass_counter = (long) cinfo.output_scanline;
//		cinfo.progress.pass_limit = (long) cinfo.output_height;
//		(*cinfo.progress.progress_monitor) ((j_common_ptr) cinfo);
//	}

	/* Process some data */
	cinfo.row_ctr[0] = 0;
	switch (cinfo.main.process_data) {
		case PROCESS_DATA_SIMPLE_MAIN:
			process_data_simple_main (cinfo, scanlines, cinfo.row_ctr, max_lines);
			break;
		case PROCESS_DATA_CONTEXT_MAIN:
			process_data_context_main (cinfo, scanlines, cinfo.row_ctr, max_lines);
			break;
		case PROCESS_DATA_CRANK_POST:
			process_data_crank_post (cinfo, scanlines, cinfo.row_ctr, max_lines);
			break;
		default: error();
	}
	cinfo.output_scanline += cinfo.row_ctr[0];
	return cinfo.row_ctr[0];
}


static boolean output_pass_setup (jpeg_decompress_struct cinfo) {
	if (cinfo.global_state != DSTATE_PRESCAN) {
		/* First call: do pass setup */
		prepare_for_output_pass (cinfo);
		cinfo.output_scanline = 0;
		cinfo.global_state = DSTATE_PRESCAN;
	}
	/* Loop over any required dummy passes */
	while (cinfo.master.is_dummy_pass) {
		error();
//#ifdef QUANT_2PASS_SUPPORTED
//		/* Crank through the dummy pass */
//		while (cinfo.output_scanline < cinfo.output_height) {
//			JDIMENSION last_scanline;
//			/* Call progress monitor hook if present */
//			if (cinfo.progress != NULL) {
//	cinfo.progress.pass_counter = (long) cinfo.output_scanline;
//	cinfo.progress.pass_limit = (long) cinfo.output_height;
//	(*cinfo.progress.progress_monitor) ((j_common_ptr) cinfo);
//			}
//			/* Process some data */
//			last_scanline = cinfo.output_scanline;
//			(*cinfo.main.process_data) (cinfo, (JSAMPARRAY) NULL,
//						&cinfo.output_scanline, (JDIMENSION) 0);
//			if (cinfo.output_scanline == last_scanline)
//	return FALSE;		/* No progress made, must suspend */
//		}
//		/* Finish up dummy pass, and set up for another one */
//		(*cinfo.master.finish_output_pass) (cinfo);
//		(*cinfo.master.prepare_for_output_pass) (cinfo);
//		cinfo.output_scanline = 0;
//#else
//		ERREXIT(cinfo, JERR_NOT_COMPILED);
//#endif /* QUANT_2PASS_SUPPORTED */
	}
	/* Ready for application to drive output pass through
	 * jpeg_read_scanlines or jpeg_read_raw_data.
	 */
	cinfo.global_state = cinfo.raw_data_out ? DSTATE_RAW_OK : DSTATE_SCANNING;
	return true;
}

static boolean get_dht (jpeg_decompress_struct cinfo)
/* Process a DHT marker */
{
	int length;
	byte[] bits = new byte[17];
	byte[] huffval = new byte[256];
	int i, index, count;
	JHUFF_TBL htblptr;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	length -= 2;
	
	while (length > 16) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		index = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

//		TRACEMS1(cinfo, 1, JTRC_DHT, index);
			
		bits[0] = 0;
		count = 0;
		for (i = 1; i <= 16; i++) {
			if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		bits[i] = cinfo.buffer[cinfo.bytes_offset++];
			count += bits[i] & 0xFF;
		}

		length -= 1 + 16;

//		TRACEMS8(cinfo, 2, JTRC_HUFFBITS,
//			 bits[1], bits[2], bits[3], bits[4],
//			 bits[5], bits[6], bits[7], bits[8]);
//		TRACEMS8(cinfo, 2, JTRC_HUFFBITS,
//			 bits[9], bits[10], bits[11], bits[12],
//			 bits[13], bits[14], bits[15], bits[16]);

		/* Here we just do minimal validation of the counts to avoid walking
		 * off the end of our table space.	jdhuff.c will check more carefully.
		 */
		if (count > 256 || (count) > length)
			error();
//			ERREXIT(cinfo, JERR_BAD_HUFF_TABLE);

		for (i = 0; i < count; i++) {
	 		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	 		huffval[i] = cinfo.buffer[cinfo.bytes_offset++];
		}

		length -= count;

		if ((index & 0x10) != 0) {		/* AC table definition */
			index -= 0x10;
			htblptr = cinfo.ac_huff_tbl_ptrs[index] = new JHUFF_TBL();
		} else {			/* DC table definition */
			htblptr = cinfo.dc_huff_tbl_ptrs[index] = new JHUFF_TBL();
		}

		if (index < 0 || index >= NUM_HUFF_TBLS)
			error();
//			ERREXIT1(cinfo, JERR_DHT_INDEX, index);

		System.arraycopy(bits, 0, htblptr.bits, 0, bits.length);
		System.arraycopy(huffval, 0, htblptr.huffval, 0, huffval.length);
	}

	if (length != 0)
		error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	return true;
}


static boolean get_dqt (jpeg_decompress_struct cinfo)
/* Process a DQT marker */
{
	int length;
	int n, i, prec;
	int tmp;
	JQUANT_TBL quant_ptr;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	length -= 2;

	while (length > 0) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	n = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		prec = n >> 4;
		n &= 0x0F;

//		TRACEMS2(cinfo, 1, JTRC_DQT, n, prec);

		if (n >= NUM_QUANT_TBLS)
			error();
//			ERREXIT1(cinfo, JERR_DQT_INDEX, n);
			
		if (cinfo.quant_tbl_ptrs[n] == null)
			cinfo.quant_tbl_ptrs[n] = new JQUANT_TBL();
		quant_ptr = cinfo.quant_tbl_ptrs[n];

		for (i = 0; i < DCTSIZE2; i++) {
			if (prec != 0) {
				if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
				tmp = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
				if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
				tmp |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
			} else {
					if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
				tmp = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
			}
			/* We convert the zigzag-order table to natural array order. */
			quant_ptr.quantval[jpeg_natural_order[i]] = (short) tmp;
		}

//		if (cinfo.err.trace_level >= 2) {
//			for (i = 0; i < DCTSIZE2; i += 8) {
//				TRACEMS8(cinfo, 2, JTRC_QUANTVALS,
//		 			quant_ptr.quantval[i],	 quant_ptr.quantval[i+1],
//					 quant_ptr.quantval[i+2], quant_ptr.quantval[i+3],
//					 quant_ptr.quantval[i+4], quant_ptr.quantval[i+5],
//					 quant_ptr.quantval[i+6], quant_ptr.quantval[i+7]);
//			}
//		}

		length -= (DCTSIZE2+1);
		if (prec != 0) length -= DCTSIZE2;
	}

	if (length != 0)
		error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	return true;
}

static boolean get_dri (jpeg_decompress_struct cinfo)
/* Process a DRI marker */
{
	int length;
	int tmp;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	
	if (length != 4)
	error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	tmp = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	tmp |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

//	TRACEMS1(cinfo, 1, JTRC_DRI, tmp);

	cinfo.restart_interval = tmp;

	return true;
}

static boolean get_dac (jpeg_decompress_struct cinfo)
/* Process a DAC marker */
{
	int length;
	int index, val;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	length -= 2;
	
	while (length > 0) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		index = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		val = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

		length -= 2;

//		TRACEMS2(cinfo, 1, JTRC_DAC, index, val);

		if (index < 0 || index >= (2*NUM_ARITH_TBLS))
			error();
//			ERREXIT1(cinfo, JERR_DAC_INDEX, index);

		if (index >= NUM_ARITH_TBLS) { /* define AC table */
			cinfo.arith_ac_K[index-NUM_ARITH_TBLS] = (byte) val;
		} else {			/* define DC table */
			cinfo.arith_dc_L[index] = (byte) (val & 0x0F);
			cinfo.arith_dc_U[index] = (byte) (val >> 4);
			if (cinfo.arith_dc_L[index] > cinfo.arith_dc_U[index])
				error();
//	ERREXIT1(cinfo, JERR_DAC_VALUE, val);
		}
	}

	if (length != 0)
		error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	return true;
}


static boolean get_sos (jpeg_decompress_struct cinfo)
/* Process a SOS marker */
{
	int length;
	int i, ci, n, c, cc;
	jpeg_component_info compptr = null;

	if (! cinfo.marker.saw_SOF)
		error();
//		ERREXIT(cinfo, JERR_SOS_NO_SOF);

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	n = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

//	TRACEMS1(cinfo, 1, JTRC_SOS, n);

	if (length != (n * 2 + 6) || n < 1 || n > MAX_COMPS_IN_SCAN)
		error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	cinfo.comps_in_scan = n;

	/* Collect the component-spec parameters */

	for (i = 0; i < n; i++) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		cc = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		
		for (ci = 0; ci < cinfo.num_components; ci++) {
			compptr = cinfo.comp_info[ci];
			if (cc == compptr.component_id)
				break;
		}

		if (ci == cinfo.num_components)
			error();
//			ERREXIT1(cinfo, JERR_BAD_COMPONENT_ID, cc);

		cinfo.cur_comp_info[i] = compptr;
		compptr.dc_tbl_no = (c >> 4) & 15;
		compptr.ac_tbl_no = (c		 ) & 15;
		
//		TRACEMS3(cinfo, 1, JTRC_SOS_COMPONENT, cc, compptr.dc_tbl_no, compptr.ac_tbl_no);
	}

	/* Collect the additional scan parameters Ss, Se, Ah/Al. */
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	cinfo.Ss = c;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	cinfo.Se = c;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	cinfo.Ah = (c >> 4) & 15;
	cinfo.Al = (c		 ) & 15;

//	TRACEMS4(cinfo, 1, JTRC_SOS_PARAMS, cinfo.Ss, cinfo.Se, cinfo.Ah, cinfo.Al);

	/* Prepare to scan data & restart markers */
	cinfo.marker.next_restart_num = 0;

	/* Count another SOS marker */
	cinfo.input_scan_number++;

	return true;
}

static boolean get_sof (jpeg_decompress_struct cinfo, boolean is_prog, boolean is_arith) {
	int length;
	int c, ci;

	cinfo.progressive_mode = is_prog;
	cinfo.arith_code = is_arith;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.data_precision = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.image_height = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.image_height |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.image_width = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.image_width |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	cinfo.num_components = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	length -= 8;

//	TRACEMS4(cinfo, 1, JTRC_SOF, cinfo.unread_marker,
//		 (int) cinfo.image_width, (int) cinfo.image_height,
//		 cinfo.num_components);

	if (cinfo.marker.saw_SOF)
		error();
//		ERREXIT(cinfo, JERR_SOF_DUPLICATE);

	/* We don't support files in which the image height is initially specified */
	/* as 0 and is later redefined by DNL.	As long as we have to check that,	*/
	/* might as well have a general sanity check. */
	if (cinfo.image_height <= 0 || cinfo.image_width <= 0 || cinfo.num_components <= 0)
		error();
//		ERREXIT(cinfo, JERR_EMPTY_IMAGE);

	if (length != (cinfo.num_components * 3))
		error();
//		ERREXIT(cinfo, JERR_BAD_LENGTH);

	if (cinfo.comp_info == null)	/* do only once, even if suspend */
		cinfo.comp_info = new jpeg_component_info[cinfo.num_components];
	
	for (ci = 0; ci < cinfo.num_components; ci++) {
		jpeg_component_info compptr = cinfo.comp_info[ci] = new jpeg_component_info();
		compptr.component_index = ci;
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		compptr.component_id = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		compptr.h_samp_factor = (c >> 4) & 15;
		compptr.v_samp_factor = (c		 ) & 15;
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		compptr.quant_tbl_no = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

//		TRACEMS4(cinfo, 1, JTRC_SOF_COMPONENT,
//			 compptr.component_id, compptr.h_samp_factor,
//			 compptr.v_samp_factor, compptr.quant_tbl_no);
	}

	cinfo.marker.saw_SOF = true;

	return true;
}

static void sep_upsample (jpeg_decompress_struct cinfo, byte[][][] input_buf, int[] input_buf_offset,
		int[] in_row_group_ctr, int in_row_groups_avail,
		byte[][] output_buf, int[] out_row_ctr,	int out_rows_avail)
{
	jpeg_upsampler upsample = cinfo.upsample;
	int ci;
	jpeg_component_info compptr;
	int num_rows;

	/* Fill the conversion buffer, if it's empty */
	if (upsample.next_row_out >= cinfo.max_v_samp_factor) {
		for (ci = 0; ci < cinfo.num_components; ci++) {
			compptr = cinfo.comp_info[ci];
			/* Invoke per-component upsample method.	Notice we pass a POINTER
			 * to color_buf[ci], so that fullsize_upsample can change it.
			 */
			int offset = input_buf_offset[ci] + (in_row_group_ctr[0] * upsample.rowgroup_height[ci]);
			switch (upsample.methods[ci]) {
				case NOOP_UPSAMPLE: noop_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case FULLSIZE_UPSAMPLE: fullsize_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case H2V1_FANCY_UPSAMPLE: h2v1_fancy_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case H2V1_UPSAMPLE: h2v1_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case H2V2_FANCY_UPSAMPLE: h2v2_fancy_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case H2V2_UPSAMPLE: h2v2_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
				case INT_UPSAMPLE: int_upsample(cinfo, compptr, input_buf[ci], offset, upsample.color_buf, upsample.color_buf_offset, ci); break;
			}
		}
		upsample.next_row_out = 0;
	}

	/* Color-convert and emit rows */

	/* How many we have in the buffer: */
	num_rows =	(cinfo.max_v_samp_factor - upsample.next_row_out);
	/* Not more than the distance to the end of the image.	Need this test
	 * in case the image height is not a multiple of max_v_samp_factor:
	 */
	if (num_rows > upsample.rows_to_go) 
		num_rows = upsample.rows_to_go;
	/* And not more than what the client can accept: */
	out_rows_avail -= out_row_ctr[0];
	if (num_rows > out_rows_avail)
		num_rows = out_rows_avail;

	switch (cinfo.cconvert.color_convert) {
		case NULL_CONVERT: null_convert (cinfo, upsample.color_buf, upsample.color_buf_offset, upsample.next_row_out, output_buf, out_row_ctr[0], num_rows); break;
		case GRAYSCALE_CONVERT: grayscale_convert (cinfo, upsample.color_buf, upsample.color_buf_offset, upsample.next_row_out, output_buf, out_row_ctr[0], num_rows); break;
		case YCC_RGB_CONVERT: ycc_rgb_convert (cinfo, upsample.color_buf, upsample.color_buf_offset, upsample.next_row_out, output_buf, out_row_ctr[0], num_rows); break;
		case GRAY_RGB_CONVERT: gray_rgb_convert (cinfo, upsample.color_buf, upsample.color_buf_offset, upsample.next_row_out, output_buf, out_row_ctr[0], num_rows); break;
		case YCCK_CMYK_CONVERT: error(); break;
	}

	/* Adjust counts */
	out_row_ctr[0] += num_rows;
	upsample.rows_to_go -= num_rows;
	upsample.next_row_out += num_rows;
	/* When the buffer is emptied, declare this input row group consumed */
	if (upsample.next_row_out >= cinfo.max_v_samp_factor) {
		in_row_group_ctr[0]++;
	}
}
	
static void noop_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	 byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	output_data_ptr[output_data_index] = null;	/* safety check */
}
	
static void fullsize_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	 byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	output_data_ptr[output_data_index] = input_data;
	output_data_offset[output_data_index] = input_data_offset;
}
	
static void h2v1_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	 byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	byte[][] output_data = output_data_ptr[output_data_index];
	byte[] inptr, outptr;
	byte invalue;
	int outend;
	int inrow;
	output_data_offset[output_data_index] = 0;

	for (inrow = 0; inrow < cinfo.max_v_samp_factor; inrow++) {
		inptr = input_data[inrow+input_data_offset];
		outptr = output_data[inrow];
		int inptr_offset = 0, outptr_offset = 0;
		outend = outptr_offset + cinfo.output_width;
		while (outptr_offset < outend) {
			invalue = inptr[inptr_offset++];	/* don't need GETJSAMPLE() here */
			outptr[outptr_offset++] = invalue;
			outptr[outptr_offset++] = invalue;
		}
	}
}
	
static void h2v2_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	byte[][] output_data = output_data_ptr[output_data_index];
	byte[] inptr, outptr;
	byte invalue;
	int outend;
	int inrow, outrow;
	output_data_offset[output_data_index] = 0;

	inrow = outrow = 0;
	while (outrow < cinfo.max_v_samp_factor) {
		inptr = input_data[inrow+input_data_offset];
		outptr = output_data[outrow];
		int inptr_offset = 0, outptr_offset = 0;
		outend = outptr_offset + cinfo.output_width;
		while (outptr_offset < outend) {
			invalue = inptr[inptr_offset++];	/* don't need GETJSAMPLE() here */
			outptr[outptr_offset++] = invalue;
			outptr[outptr_offset++] = invalue;
		}
		jcopy_sample_rows(output_data, outrow, output_data, outrow+1, 1, cinfo.output_width);
		inrow++;
		outrow += 2;
	}
}
	
static void h2v1_fancy_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	 byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	byte[][] output_data = output_data_ptr[output_data_index];
	byte[] inptr, outptr;
	int invalue;
	int colctr;
	int inrow;					
	output_data_offset[output_data_index] = 0;

	for (inrow = 0; inrow < cinfo.max_v_samp_factor; inrow++) {
		inptr = input_data[inrow+input_data_offset];
		outptr = output_data[inrow];
		int inptr_offset = 0, outptr_offset = 0;
		/* Special case for first column */
		invalue = inptr[inptr_offset++] & 0xFF;
		outptr[outptr_offset++] = (byte) invalue;
		outptr[outptr_offset++] = (byte) ((invalue * 3 + (inptr[inptr_offset] & 0xFF) + 2) >> 2);

		for (colctr = compptr.downsampled_width - 2; colctr > 0; colctr--) {
			/* General case: 3/4 * nearer pixel + 1/4 * further pixel */
			invalue = (inptr[inptr_offset++] & 0xFF) * 3;
			outptr[outptr_offset++] = (byte) ((invalue + (inptr[inptr_offset-2] & 0xFF) + 1) >> 2);
			outptr[outptr_offset++] = (byte) ((invalue + (inptr[inptr_offset] & 0xFF) + 2) >> 2);
		}

		/* Special case for last column */
		invalue = (inptr[inptr_offset] & 0xFF);
		outptr[outptr_offset++] = (byte) ((invalue * 3 + (inptr[inptr_offset-1] & 0xFF) + 1) >> 2);
		outptr[outptr_offset++] = (byte) invalue;
	}
}
	
static void h2v2_fancy_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	byte[][] output_data = output_data_ptr[output_data_index];
	byte[] inptr0, inptr1, outptr;
	int thiscolsum, lastcolsum, nextcolsum;
	int colctr;
	int inrow, outrow, v;
	output_data_offset[output_data_index] = 0;

	inrow = outrow = 0;
	while (outrow < cinfo.max_v_samp_factor) {
		for (v = 0; v < 2; v++) {
			/* inptr0 points to nearest input row, inptr1 points to next nearest */
			inptr0 = input_data[inrow+input_data_offset];
			if (v == 0)		/* next nearest is row above */
				inptr1 = input_data[inrow-1+input_data_offset];
			else			/* next nearest is row below */
				inptr1 = input_data[inrow+1+input_data_offset];
			outptr = output_data[outrow++];
				
			int inptr0_offset = 0, inptr1_offset = 0, outptr_offset = 0;

			/* Special case for first column */
			thiscolsum = (inptr0[inptr0_offset++] & 0xFF) * 3 + (inptr1[inptr1_offset++] & 0xFF);
			nextcolsum = (inptr0[inptr0_offset++] & 0xFF) * 3 + (inptr1[inptr1_offset++] & 0xFF);
			outptr[outptr_offset++] = (byte) ((thiscolsum * 4 + 8) >> 4);
			outptr[outptr_offset++] = (byte) ((thiscolsum * 3 + nextcolsum + 7) >> 4);
			lastcolsum = thiscolsum; thiscolsum = nextcolsum;

			for (colctr = compptr.downsampled_width - 2; colctr > 0; colctr--) {
				/* General case: 3/4 * nearer pixel + 1/4 * further pixel in each */
				/* dimension, thus 9/16, 3/16, 3/16, 1/16 overall */
				nextcolsum = (inptr0[inptr0_offset++] & 0xFF) * 3 + (inptr1[inptr1_offset++] & 0xFF);
				outptr[outptr_offset++] = (byte) ((thiscolsum * 3 + lastcolsum + 8) >> 4);
				outptr[outptr_offset++] = (byte) ((thiscolsum * 3 + nextcolsum + 7) >> 4);
				lastcolsum = thiscolsum; thiscolsum = nextcolsum;
			}

			/* Special case for last column */
			outptr[outptr_offset++] = (byte) ((thiscolsum * 3 + lastcolsum + 8) >> 4);
			outptr[outptr_offset++] = (byte) ((thiscolsum * 4 + 7) >> 4);
		}
		inrow++;
	}
}
	
static void int_upsample (jpeg_decompress_struct cinfo, jpeg_component_info compptr,
	 byte[][] input_data, int input_data_offset, byte[][][] output_data_ptr, int[] output_data_offset, int output_data_index)
{
	jpeg_upsampler upsample = cinfo.upsample;
	byte[][] output_data = output_data_ptr[output_data_index];
	byte[] inptr, outptr;
	byte invalue;
	int h;
	int outend;
	int h_expand, v_expand;
	int inrow, outrow;
	output_data_offset[output_data_index] = 0;
		
	h_expand = upsample.h_expand[compptr.component_index];
	v_expand = upsample.v_expand[compptr.component_index];

	inrow = outrow = 0;
	while (outrow < cinfo.max_v_samp_factor) {
		/* Generate one output row with proper horizontal expansion */
		inptr = input_data[inrow+input_data_offset];
		int inptr_offset = 0;
		outptr = output_data[outrow];
		int outptr_offset = 0;
		outend = outptr_offset + cinfo.output_width;
		while (outptr_offset < outend) {
			invalue = inptr[inptr_offset++];	/* don't need GETJSAMPLE() here */
			for (h = h_expand; h > 0; h--) {
				outptr[outptr_offset++] = invalue;
			}
		}
		/* Generate any additional output rows by duplicating the first one */
		if (v_expand > 1) {
			jcopy_sample_rows(output_data, outrow, output_data, outrow+1, v_expand-1, cinfo.output_width);
		}
		inrow++;
		outrow += v_expand;
	}
}

static void null_convert (jpeg_decompress_struct cinfo,
	byte[][][] input_buf, int[] input_buf_offset, int input_row,
	byte[][] output_buf, int output_buf_offset, int num_rows)
{
	byte[] inptr, outptr;
	int count;
	int num_components = cinfo.num_components;
	int num_cols = cinfo.output_width;
	int ci;

	while (--num_rows >= 0) {
		for (ci = 0; ci < num_components; ci++) {
			inptr = input_buf[ci][input_row+input_buf_offset[0]];
			outptr = output_buf[output_buf_offset];
			/* BGR instead of RGB */
			int offset = 0;
			switch (ci) {
				case 2: offset = RGB_BLUE; break;
				case 1: offset = RGB_GREEN; break;
				case 0: offset = RGB_RED; break;
			}
			int outptr_offset = offset, inptr_offset = 0;
			for (count = num_cols; count > 0; count--) {
				outptr[outptr_offset] = inptr[inptr_offset++];	/* needn't bother with GETJSAMPLE() here */
				outptr_offset += num_components;
			}
		}
		input_row++;
		output_buf_offset++;
	}
}
	
static void grayscale_convert (jpeg_decompress_struct cinfo,
	byte[][][] input_buf, int[] input_buf_offset, int input_row,
	byte[][] output_buf, int output_buf_offset, int num_rows)
{
  jcopy_sample_rows(input_buf[0], input_row+input_buf_offset[0], output_buf, output_buf_offset,
		    num_rows, cinfo.output_width);
}

static void gray_rgb_convert (jpeg_decompress_struct cinfo,
	byte[][][] input_buf, int[] input_buf_offset, int input_row,
	byte[][] output_buf, int output_buf_offset, int num_rows)
{
	byte[] inptr, outptr;
	int col;
	int num_cols = cinfo.output_width;

	while (--num_rows >= 0) {
		inptr = input_buf[0][input_row+++input_buf_offset[0]];
		outptr = output_buf[output_buf_offset++];
		int outptr_offset = 0;
		for (col = 0; col < num_cols; col++) {
			/* We can dispense with GETJSAMPLE() here */
			outptr[RGB_RED+outptr_offset] = outptr[RGB_GREEN+outptr_offset] = outptr[RGB_BLUE+outptr_offset] = inptr[col];
			outptr_offset += RGB_PIXELSIZE;
		}
	}
}
	
static void ycc_rgb_convert (jpeg_decompress_struct cinfo,
	byte[][][] input_buf, int[] input_buf_offset, int input_row,
	byte[][] output_buf, int output_buf_offset, int num_rows)
{
	jpeg_color_deconverter cconvert = cinfo.cconvert;
	int y, cb, cr;
	byte[] outptr;
	byte[] inptr0, inptr1, inptr2;
	int col;
	int num_cols = cinfo.output_width;
	/* copy these pointers into registers if possible */
	byte[] range_limit = cinfo.sample_range_limit;
	int range_limit_offset = cinfo.sample_range_limit_offset;
	int[] Crrtab = cconvert.Cr_r_tab;
	int[] Cbbtab = cconvert.Cb_b_tab;
	int[] Crgtab = cconvert.Cr_g_tab;
	int[] Cbgtab = cconvert.Cb_g_tab;
//		SHIFT_TEMPS

	while (--num_rows >= 0) {
		inptr0 = input_buf[0][input_row+input_buf_offset[0]];
		inptr1 = input_buf[1][input_row+input_buf_offset[1]];
		inptr2 = input_buf[2][input_row+input_buf_offset[2]];
		input_row++;
		outptr = output_buf[output_buf_offset++];
		int outptr_offset = 0;
		for (col = 0; col < num_cols; col++) {
			y = (inptr0[col] & 0xFF);
			cb = (inptr1[col] & 0xFF);
			cr = (inptr2[col] & 0xFF);
			/* Range-limiting is essential due to noise introduced by DCT losses. */
			outptr[outptr_offset + RGB_RED] =	range_limit[y + Crrtab[cr] + range_limit_offset];
			outptr[outptr_offset + RGB_GREEN] = range_limit[y + ((Cbgtab[cb] + Crgtab[cr]>>SCALEBITS)) + range_limit_offset];
			outptr[outptr_offset + RGB_BLUE] =	range_limit[y + Cbbtab[cb] + range_limit_offset];
			outptr_offset += RGB_PIXELSIZE;
		}
	}
}

static boolean process_APPn(int n, jpeg_decompress_struct cinfo) {
	if (n == 0 || n == 14) {
		return get_interesting_appn(cinfo);
	}
	return skip_variable(cinfo);
}

static boolean process_COM(jpeg_decompress_struct cinfo) {
	return skip_variable(cinfo);
}

static void skip_input_data (jpeg_decompress_struct cinfo, int num_bytes) {
	if (num_bytes > 0) {
		while (num_bytes > cinfo.bytes_in_buffer - cinfo.bytes_offset) {
			num_bytes -= cinfo.bytes_in_buffer - cinfo.bytes_offset;
			if (!fill_input_buffer(cinfo)) error();
			/* note we assume that fill_input_buffer will never return FALSE,
			 * so suspension need not be handled.
			 */
		}
		cinfo.bytes_offset += num_bytes;
	}
}

static boolean skip_variable (jpeg_decompress_struct cinfo)
/* Skip over an unknown or uninteresting variable-length marker */
{
	int length;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;

	length -= 2;
	
//	TRACEMS2(cinfo, 1, JTRC_MISC_MARKER, cinfo.unread_marker, (int) length);

	if (length > 0) {
		skip_input_data (cinfo, length);
	}
	
	return true;
}

static boolean get_interesting_appn (jpeg_decompress_struct cinfo)
/* Process an APP0 or APP14 marker without saving it */
{
	int length;
	byte[] b = new byte[APPN_DATA_LEN];
	int i, numtoread;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length = (cinfo.buffer[cinfo.bytes_offset++] & 0xFF) << 8;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	length |= cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	length -= 2;

	/* get the interesting part of the marker data */
	if (length >= APPN_DATA_LEN)
		numtoread = APPN_DATA_LEN;
	else if (length > 0)
		numtoread = length;
	else
		numtoread = 0;
	for (i = 0; i < numtoread; i++) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		b[i] = cinfo.buffer[cinfo.bytes_offset++];
	}
	length -= numtoread;

	/* process it */
	switch (cinfo.unread_marker) {
		case M_APP0:
			examine_app0(cinfo, b, numtoread, length);
			break;
		case M_APP14:
			examine_app14(cinfo, b, numtoread, length);
			break;
		default:
			/* can't get here unless jpeg_save_markers chooses wrong processor */
			error();
//			ERREXIT1(cinfo, JERR_UNKNOWN_MARKER, cinfo.unread_marker);
			break;
	}

	/* skip any remaining data -- could be lots */
	if (length > 0)
		skip_input_data (cinfo, length);

	return true;
}

static void examine_app0 (jpeg_decompress_struct cinfo, byte[] data, int datalen, int remaining)
/* Examine first few bytes from an APP0.
 * Take appropriate action if it is a JFIF marker.
 * datalen is # of bytes at data[], remaining is length of rest of marker data.
 */
{
	int totallen = datalen + remaining;

	if (datalen >= APP0_DATA_LEN &&
			(data[0] & 0xFF) == 0x4A &&
			(data[1] & 0xFF) == 0x46 &&
			(data[2] & 0xFF) == 0x49 &&
			(data[3] & 0xFF) == 0x46 &&
			(data[4] & 0xFF) == 0)
	{
		/* Found JFIF APP0 marker: save info */
		cinfo.saw_JFIF_marker = true;
		cinfo.JFIF_major_version = (data[5]);
		cinfo.JFIF_minor_version = (byte)(data[6] & 0xFF);
		cinfo.density_unit = (byte)(data[7] & 0xFF);
		cinfo.X_density = (short)(((data[8] & 0xFF) << 8) + (data[9] & 0xFF));
		cinfo.Y_density = (short)(((data[10] & 0xFF) << 8) + (data[11] & 0xFF));
		/* Check version.
		 * Major version must be 1, anything else signals an incompatible change.
		 * (We used to treat this as an error, but now it's a nonfatal warning,
		 * because some bozo at Hijaak couldn't read the spec.)
		 * Minor version should be 0..2, but process anyway if newer.
		 */
		if (cinfo.JFIF_major_version != 1) {
//			WARNMS2(cinfo, JWRN_JFIF_MAJOR,
//				cinfo.JFIF_major_version, cinfo.JFIF_minor_version);
		}
		/* Generate trace messages */
//		TRACEMS5(cinfo, 1, JTRC_JFIF,
//			 cinfo.JFIF_major_version, cinfo.JFIF_minor_version,
//			 cinfo.X_density, cinfo.Y_density, cinfo.density_unit);
		/* Validate thumbnail dimensions and issue appropriate messages */
		if (((data[12] & 0xFF) | (data[13]) & 0xFF) != 0) {
//			TRACEMS2(cinfo, 1, JTRC_JFIF_THUMBNAIL,
//				 GETJOCTET(data[12]), GETJOCTET(data[13]));
		}
		totallen -= APP0_DATA_LEN;
		if (totallen !=	((data[12] & 0xFF) * (data[13] & 0xFF) * 3)) {
//			TRACEMS1(cinfo, 1, JTRC_JFIF_BADTHUMBNAILSIZE, (int) totallen);
		}
	} else if (datalen >= 6 &&
			(data[0] & 0xFF) == 0x4A &&
			(data[1] & 0xFF) == 0x46 &&
			(data[2] & 0xFF) == 0x58 &&
			(data[3] & 0xFF) == 0x58 &&
			(data[4] & 0xFF) == 0)
	{
		/* Found JFIF "JFXX" extension APP0 marker */
		/* The library doesn't actually do anything with these,
		 * but we try to produce a helpful trace message.
		 */
		switch ((data[5]) & 0xFF) {
			case 0x10:
//				TRACEMS1(cinfo, 1, JTRC_THUMB_JPEG, (int) totallen);
				break;
			case 0x11:
//				TRACEMS1(cinfo, 1, JTRC_THUMB_PALETTE, (int) totallen);
				break;
			case 0x13:
//				TRACEMS1(cinfo, 1, JTRC_THUMB_RGB, (int) totallen);
				break;
			default:
//				TRACEMS2(cinfo, 1, JTRC_JFIF_EXTENSION, GETJOCTET(data[5]), (int) totallen);
			break;
		}
	} else {
		/* Start of APP0 does not match "JFIF" or "JFXX", or too short */
//		TRACEMS1(cinfo, 1, JTRC_APP0, (int) totallen);
	}
}

static void examine_app14 (jpeg_decompress_struct cinfo, byte[] data, int datalen, int remaining)
/* Examine first few bytes from an APP14.
 * Take appropriate action if it is an Adobe marker.
 * datalen is # of bytes at data[], remaining is length of rest of marker data.
 */
{
	int /*version, flags0, flags1, */transform;

	if (datalen >= APP14_DATA_LEN &&
			(data[0] & 0xFF) == 0x41 &&
			(data[1] & 0xFF) == 0x64 &&
			(data[2] & 0xFF) == 0x6F &&
			(data[3] & 0xFF) == 0x62 &&
			(data[4] & 0xFF) == 0x65)
	{
		/* Found Adobe APP14 marker */
//		version = ((data[5] & 0xFF) << 8) + (data[6] & 0xFF);
//		flags0 = ((data[7] & 0xFF) << 8) + (data[8] & 0xFF);
//		flags1 = ((data[9] & 0xFF) << 8) + (data[10] & 0xFF);
		transform = (data[11] & 0xFF);
//		TRACEMS4(cinfo, 1, JTRC_ADOBE, version, flags0, flags1, transform);
		cinfo.saw_Adobe_marker = true;
		cinfo.Adobe_transform = (byte) transform;
	} else {
		/* Start of APP14 does not match "Adobe", or too short */
//		TRACEMS1(cinfo, 1, JTRC_APP14, (int) (datalen + remaining));
	}
}

static boolean get_soi (jpeg_decompress_struct cinfo) /* Process an SOI marker */ {
	int i;
	
//	TRACEMS(cinfo, 1, JTRC_SOI);

	if (cinfo.marker.saw_SOI)
		error();
//		ERREXIT(cinfo, JERR_SOI_DUPLICATE);

	/* Reset all parameters that are defined to be reset by SOI */

	for (i = 0; i < NUM_ARITH_TBLS; i++) {
		cinfo.arith_dc_L[i] = 0;
		cinfo.arith_dc_U[i] = 1;
		cinfo.arith_ac_K[i] = 5;
	}
	cinfo.restart_interval = 0;

	/* Set initial assumptions for colorspace etc */

	cinfo.jpeg_color_space = JCS_UNKNOWN;
	cinfo.CCIR601_sampling = false; /* Assume non-CCIR sampling??? */

	cinfo.saw_JFIF_marker = false;
	cinfo.JFIF_major_version = 1; /* set default JFIF APP0 values */
	cinfo.JFIF_minor_version = 1;
	cinfo.density_unit = 0;
	cinfo.X_density = 1;
	cinfo.Y_density = 1;
	cinfo.saw_Adobe_marker = false;
	cinfo.Adobe_transform = 0;

	cinfo.marker.saw_SOI = true;

	return true;
}

static void jinit_input_controller (jpeg_decompress_struct cinfo)
{
	/* Initialize state: can't use reset_input_controller since we don't
	 * want to try to reset other modules yet.
	 */
	jpeg_input_controller inputctl = cinfo.inputctl = new jpeg_input_controller();
	inputctl.has_multiple_scans = false; /* "unknown" would be better */
	inputctl.eoi_reached = false;
	inputctl.inheaders = true;
}

static void reset_marker_reader (jpeg_decompress_struct cinfo) {
	jpeg_marker_reader marker = cinfo.marker;

	cinfo.comp_info = null;		/* until allocated by get_sof */
	cinfo.input_scan_number = 0;		/* no SOS seen yet */
	cinfo.unread_marker = 0;		/* no pending marker */
	marker.saw_SOI = false;		/* set internal state too */
	marker.saw_SOF = false;
	marker.discarded_bytes = 0;
//	marker.cur_marker = null;
}

static void reset_input_controller (jpeg_decompress_struct cinfo) {
	jpeg_input_controller inputctl = cinfo.inputctl;

	inputctl.has_multiple_scans = false; /* "unknown" would be better */
	inputctl.eoi_reached = false;
	inputctl.inheaders = true;
	/* Reset other modules */
	reset_marker_reader (cinfo);
	/* Reset progression state -- would be cleaner if entropy decoder did this */
	cinfo.coef_bits = null;
}

static void finish_output_pass (jpeg_decompress_struct cinfo) {
	jpeg_decomp_master master = cinfo.master;

	if (cinfo.quantize_colors) {
		error(SWT.ERROR_NOT_IMPLEMENTED);
//		(*cinfo.cquantize.finish_pass) (cinfo);
	}
	master.pass_number++;
}

static void jpeg_destroy (jpeg_decompress_struct cinfo) {
	/* We need only tell the memory manager to release everything. */
	/* NB: mem pointer is NULL if memory mgr failed to initialize. */
//	if (cinfo.mem != NULL)
//		(*cinfo.mem.self_destruct) (cinfo);
//	cinfo.mem = NULL;		/* be safe if jpeg_destroy is called twice */
	cinfo.global_state = 0;	/* mark it destroyed */
}

static void jpeg_destroy_decompress (jpeg_decompress_struct cinfo) {
	jpeg_destroy(cinfo); /* use common routine */
}

static boolean jpeg_input_complete (jpeg_decompress_struct cinfo) {
	/* Check for valid jpeg object */
	if (cinfo.global_state < DSTATE_START || cinfo.global_state > DSTATE_STOPPING)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	return cinfo.inputctl.eoi_reached;
}

static boolean jpeg_start_output (jpeg_decompress_struct cinfo, int scan_number) {
	if (cinfo.global_state != DSTATE_BUFIMAGE && cinfo.global_state != DSTATE_PRESCAN)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	/* Limit scan number to valid range */
	if (scan_number <= 0)
		scan_number = 1;
	if (cinfo.inputctl.eoi_reached && scan_number > cinfo.input_scan_number)
		scan_number = cinfo.input_scan_number;
	cinfo.output_scan_number = scan_number;
	/* Perform any dummy output passes, and set up for the real pass */
	return output_pass_setup(cinfo);
}

static boolean jpeg_finish_output (jpeg_decompress_struct cinfo) {
	if ((cinfo.global_state == DSTATE_SCANNING || cinfo.global_state == DSTATE_RAW_OK) && cinfo.buffered_image) {
		/* Terminate this pass. */
		/* We do not require the whole pass to have been completed. */
		finish_output_pass (cinfo);
		cinfo.global_state = DSTATE_BUFPOST;
	} else if (cinfo.global_state != DSTATE_BUFPOST) {
		/* BUFPOST = repeat call after a suspension, anything else is error */
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	}
	/* Read markers looking for SOS or EOI */
	while (cinfo.input_scan_number <= cinfo.output_scan_number && !cinfo.inputctl.eoi_reached) {
		if (consume_input (cinfo) == JPEG_SUSPENDED)
			return false;		/* Suspend, come back later */
	}
	cinfo.global_state = DSTATE_BUFIMAGE;
	return true;
}

static boolean jpeg_finish_decompress (jpeg_decompress_struct cinfo) {
	if ((cinfo.global_state == DSTATE_SCANNING || cinfo.global_state == DSTATE_RAW_OK) && ! cinfo.buffered_image) {
		/* Terminate final pass of non-buffered mode */
		if (cinfo.output_scanline < cinfo.output_height)
			error();
//			ERREXIT(cinfo, JERR_TOO_LITTLE_DATA);
		finish_output_pass (cinfo);
		cinfo.global_state = DSTATE_STOPPING;
	} else if (cinfo.global_state == DSTATE_BUFIMAGE) {
		/* Finishing after a buffered-image operation */
		cinfo.global_state = DSTATE_STOPPING;
	} else if (cinfo.global_state != DSTATE_STOPPING) {
		/* STOPPING = repeat call after a suspension, anything else is error */
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	}
	/* Read until EOI */
	while (! cinfo.inputctl.eoi_reached) {
		if (consume_input (cinfo) == JPEG_SUSPENDED)
			return false;		/* Suspend, come back later */
	}
	/* Do final cleanup */
//	(*cinfo.src.term_source) (cinfo);
	/* We can use jpeg_abort to release memory and reset global_state */
	jpeg_abort(cinfo);
	return true;
}


static int jpeg_read_header (jpeg_decompress_struct cinfo, boolean require_image) {
	int retcode;

	if (cinfo.global_state != DSTATE_START && cinfo.global_state != DSTATE_INHEADER)
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);

	retcode = jpeg_consume_input(cinfo);

	switch (retcode) {
		case JPEG_REACHED_SOS:
			retcode = JPEG_HEADER_OK;
			break;
		case JPEG_REACHED_EOI:
			if (require_image)		/* Complain if application wanted an image */
				error();
//				ERREXIT(cinfo, JERR_NO_IMAGE);
			/* Reset to start state; it would be safer to require the application to
			 * call jpeg_abort, but we can't change it now for compatibility reasons.
			 * A side effect is to free any temporary memory (there shouldn't be any).
			 */
			jpeg_abort(cinfo); /* sets state = DSTATE_START */
			retcode = JPEG_HEADER_TABLES_ONLY;
			break;
		case JPEG_SUSPENDED:
			/* no work */
			break;
	}

	return retcode;
}

static int dummy_consume_data (jpeg_decompress_struct cinfo) {
	return JPEG_SUSPENDED;	/* Always indicate nothing was done */
}

static int consume_data (jpeg_decompress_struct cinfo) {
	jpeg_d_coef_controller coef = cinfo.coef;
	int MCU_col_num;	/* index of current MCU within row */
	int blkn, ci, xindex, yindex, yoffset;
	int start_col;
//	short[][][][] buffer = new short[MAX_COMPS_IN_SCAN][][][];
	short[][] buffer_ptr;
	jpeg_component_info compptr;

//	/* Align the virtual buffers for the components used in this scan. */
//	for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
//		compptr = cinfo.cur_comp_info[ci];
//		buffer[ci] = coef.whole_image[compptr.component_index];
//		/* Note: entropy decoder expects buffer to be zeroed,
//		 * but this is handled automatically by the memory manager
//		 * because we requested a pre-zeroed array.
//		 */
//	}

	/* Loop to process one whole iMCU row */
	for (yoffset = coef.MCU_vert_offset; yoffset < coef.MCU_rows_per_iMCU_row; yoffset++) {
		for (MCU_col_num = coef.MCU_ctr; MCU_col_num < cinfo.MCUs_per_row; MCU_col_num++) {
			/* Construct list of pointers to DCT blocks belonging to this MCU */
			blkn = 0; /* index of current DCT block within MCU */
			for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
				compptr = cinfo.cur_comp_info[ci];
				start_col = MCU_col_num * compptr.MCU_width;
				for (yindex = 0; yindex < compptr.MCU_height; yindex++) {
//					buffer_ptr = buffer[ci][yindex+yoffset] + start_col;
					buffer_ptr = coef.whole_image[compptr.component_index][yindex+yoffset+cinfo.input_iMCU_row*compptr.v_samp_factor];
					int buffer_ptr_offset = start_col;
					for (xindex = 0; xindex < compptr.MCU_width; xindex++) {
						coef.MCU_buffer[blkn++] = buffer_ptr[buffer_ptr_offset++];
					}
				}
			}
			/* Try to fetch the MCU. */
			if (! cinfo.entropy.decode_mcu (cinfo, coef.MCU_buffer)) {
				/* Suspension forced; update state counters and exit */
				coef.MCU_vert_offset = yoffset;
				coef.MCU_ctr = MCU_col_num;
				return JPEG_SUSPENDED;
			}
		}
		/* Completed an MCU row, but perhaps not an iMCU row */
		coef.MCU_ctr = 0;
	}
	/* Completed the iMCU row, advance counters for next one */
	if (++(cinfo.input_iMCU_row) < cinfo.total_iMCU_rows) {
		coef.start_iMCU_row(cinfo);
		return JPEG_ROW_COMPLETED;
	}
	/* Completed the scan */
	finish_input_pass (cinfo);
	return JPEG_SCAN_COMPLETED;
}

static int consume_input (jpeg_decompress_struct cinfo) {
	switch (cinfo.inputctl.consume_input) {
		case COEF_CONSUME_INPUT:
			 switch (cinfo.coef.consume_data) {
				case CONSUME_DATA: return consume_data(cinfo);
				case DUMMY_CONSUME_DATA: return dummy_consume_data(cinfo);
				default: error();
			 }
			 break;
		case INPUT_CONSUME_INPUT:
			return consume_markers(cinfo);
		default:
			error();
	}
	return 0;	
}

static boolean fill_input_buffer(jpeg_decompress_struct cinfo) {
	try {
		InputStream inputStream = cinfo.inputStream;
		int nbytes = inputStream.read(cinfo.buffer);
		if (nbytes <= 0) {
			if (cinfo.start_of_file)	/* Treat empty input file as fatal error */
				error();
//				ERREXIT(cinfo, JERR_INPUT_EMPTY);
//			WARNMS(cinfo, JWRN_JPEG_EOF);
			/* Insert a fake EOI marker */
			cinfo.buffer[0] = (byte)0xFF;
			cinfo.buffer[1] = (byte)M_EOI;
			nbytes = 2;
		}
		cinfo.bytes_in_buffer = nbytes;
		cinfo.bytes_offset = 0;
		cinfo.start_of_file = false;
	} catch (IOException e) {
		error(SWT.ERROR_IO);
		return false;
	}
	return true;
}

static boolean first_marker (jpeg_decompress_struct cinfo) {
	/* Like next_marker, but used to obtain the initial SOI marker. */
	/* For this marker, we do not allow preceding garbage or fill; otherwise,
	 * we might well scan an entire input file before realizing it ain't JPEG.
	 * If an application wants to process non-JFIF files, it must seek to the
	 * SOI before calling the JPEG library.
	 */
	int c, c2;

	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
	c2 = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
	if (c != 0xFF || c2 != M_SOI)
		error();
//		ERREXIT2(cinfo, JERR_NO_SOI, c, c2);

	cinfo.unread_marker = c2;

	return true;
}

static boolean next_marker (jpeg_decompress_struct cinfo) {
	int c;

	for (;;) {
		if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
		c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		/* Skip any non-FF bytes.
		 * This may look a bit inefficient, but it will not occur in a valid file.
		 * We sync after each discarded byte so that a suspending data source
		 * can discard the byte from its buffer.
		 */
		while (c != 0xFF) {
			cinfo.marker.discarded_bytes++;
			if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
			c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		}
		/* This loop swallows any duplicate FF bytes.	Extra FFs are legal as
		 * pad bytes, so don't count them in discarded_bytes.	We assume there
		 * will not be so many consecutive FF bytes as to overflow a suspending
		 * data source's input buffer.
		 */
		do {
			 if (cinfo.bytes_offset == cinfo.bytes_in_buffer) fill_input_buffer(cinfo);
				c = cinfo.buffer[cinfo.bytes_offset++] & 0xFF;
		} while (c == 0xFF);
		if (c != 0)
			break;			/* found a valid marker, exit loop */
		/* Reach here if we found a stuffed-zero data sequence (FF/00).
		 * Discard it and loop back to try again.
		 */
		cinfo.marker.discarded_bytes += 2;
	}

	if (cinfo.marker.discarded_bytes != 0) {
//		WARNMS2(cinfo, JWRN_EXTRANEOUS_DATA, cinfo.marker.discarded_bytes, c);
		cinfo.marker.discarded_bytes = 0;
	}

	cinfo.unread_marker = c;

	return true;
}

static int read_markers (jpeg_decompress_struct cinfo) {
	/* Outer loop repeats once for each marker. */
	for (;;) {
		/* Collect the marker proper, unless we already did. */
		/* NB: first_marker() enforces the requirement that SOI appear first. */
		if (cinfo.unread_marker == 0) {
			if (! cinfo.marker.saw_SOI) {
				if (! first_marker(cinfo))
					return JPEG_SUSPENDED;
				} else {
					if (! next_marker(cinfo))
						return JPEG_SUSPENDED;
				}
		}
		/* At this point cinfo.unread_marker contains the marker code and the
		 * input point is just past the marker proper, but before any parameters.
		 * A suspension will cause us to return with this state still true.
		 */
		switch (cinfo.unread_marker) {
			case M_SOI:
				if (! get_soi(cinfo))
					return JPEG_SUSPENDED;
				break;

			case M_SOF0:		/* Baseline */
			case M_SOF1:		/* Extended sequential, Huffman */
				if (! get_sof(cinfo, false, false))
					return JPEG_SUSPENDED;
				break;

			case M_SOF2:		/* Progressive, Huffman */
				if (! get_sof(cinfo, true, false))
					return JPEG_SUSPENDED;
				break;

			case M_SOF9:		/* Extended sequential, arithmetic */
				if (! get_sof(cinfo, false, true))
					return JPEG_SUSPENDED;
				break;

			case M_SOF10:		/* Progressive, arithmetic */
				if (! get_sof(cinfo, true, true))
					return JPEG_SUSPENDED;
				break;

			/* Currently unsupported SOFn types */
			case M_SOF3:		/* Lossless, Huffman */
			case M_SOF5:		/* Differential sequential, Huffman */
			case M_SOF6:		/* Differential progressive, Huffman */
			case M_SOF7:		/* Differential lossless, Huffman */
			case M_JPG:			/* Reserved for JPEG extensions */
			case M_SOF11:		/* Lossless, arithmetic */
			case M_SOF13:		/* Differential sequential, arithmetic */
			case M_SOF14:		/* Differential progressive, arithmetic */
			case M_SOF15:		/* Differential lossless, arithmetic */
				error();
//				ERREXIT1(cinfo, JERR_SOF_UNSUPPORTED, cinfo.unread_marker);
				break;

			case M_SOS:
				if (! get_sos(cinfo))
					return JPEG_SUSPENDED;
				cinfo.unread_marker = 0;	/* processed the marker */
				return JPEG_REACHED_SOS;
	
			case M_EOI:
//				TRACEMS(cinfo, 1, JTRC_EOI);
				cinfo.unread_marker = 0;	/* processed the marker */
				return JPEG_REACHED_EOI;
		
			case M_DAC:
				if (! get_dac(cinfo))
					return JPEG_SUSPENDED;
				break;
		
			case M_DHT:
				if (! get_dht(cinfo))
					return JPEG_SUSPENDED;
				break;
		
			case M_DQT:
				if (! get_dqt(cinfo))
					return JPEG_SUSPENDED;
				break;
		
			case M_DRI:
				if (! get_dri(cinfo))
					return JPEG_SUSPENDED;
				break;
		
			case M_APP0:
			case M_APP1:
			case M_APP2:
			case M_APP3:
			case M_APP4:
			case M_APP5:
			case M_APP6:
			case M_APP7:
			case M_APP8:
			case M_APP9:
			case M_APP10:
			case M_APP11:
			case M_APP12:
			case M_APP13:
			case M_APP14:
			case M_APP15:
				if (! process_APPn(cinfo.unread_marker - M_APP0, cinfo))
					return JPEG_SUSPENDED;
				break;
		
			case M_COM:
				if (! process_COM(cinfo))
					return JPEG_SUSPENDED;
				break;

			case M_RST0:		/* these are all parameterless */
			case M_RST1:
			case M_RST2:
			case M_RST3:
			case M_RST4:
			case M_RST5:
			case M_RST6:
			case M_RST7:
			case M_TEM:
//				TRACEMS1(cinfo, 1, JTRC_PARMLESS_MARKER, cinfo.unread_marker);
				break;

			case M_DNL:			/* Ignore DNL ... perhaps the wrong thing */
				if (! skip_variable(cinfo))
					return JPEG_SUSPENDED;
				break;

			default:			/* must be DHP, EXP, JPGn, or RESn */
				/* For now, we treat the reserved markers as fatal errors since they are
				 * likely to be used to signal incompatible JPEG Part 3 extensions.
				 * Once the JPEG 3 version-number marker is well defined, this code
				 * ought to change!
				 */
				error();
 //		 		ERREXIT1(cinfo, JERR_UNKNOWN_MARKER, cinfo.unread_marker);
				break;
		}
		/* Successfully processed marker, so reset state variable */
		cinfo.unread_marker = 0;
	} /* end loop */
}

static long jdiv_round_up (long a, long b)
/* Compute a/b rounded up to next integer, ie, ceil(a/b) */
/* Assumes a >= 0, b > 0 */
{
	return (a + b - 1) / b;
}

static void initial_setup (jpeg_decompress_struct cinfo)
/* Called once, when first SOS marker is reached */
{
	int ci;
	jpeg_component_info compptr;

	/* Make sure image isn't bigger than I can handle */
	if (cinfo.image_height >	JPEG_MAX_DIMENSION || cinfo.image_width > JPEG_MAX_DIMENSION)
		error();
//		ERREXIT1(cinfo, JERR_IMAGE_TOO_BIG, (unsigned int) JPEG_MAX_DIMENSION);

	/* For now, precision must match compiled-in value... */
	if (cinfo.data_precision != BITS_IN_JSAMPLE)
		error(" [data precision=" + cinfo.data_precision + "]");
//		ERREXIT1(cinfo, JERR_BAD_PRECISION, cinfo.data_precision);

	/* Check that number of components won't exceed internal array sizes */
	if (cinfo.num_components > MAX_COMPONENTS)
		error();
//		ERREXIT2(cinfo, JERR_COMPONENT_COUNT, cinfo.num_components, MAX_COMPONENTS);

	/* Compute maximum sampling factors; check factor validity */
	cinfo.max_h_samp_factor = 1;
	cinfo.max_v_samp_factor = 1;
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		if (compptr.h_samp_factor<=0 || compptr.h_samp_factor>MAX_SAMP_FACTOR || compptr.v_samp_factor<=0 || compptr.v_samp_factor>MAX_SAMP_FACTOR)
			error();
//			ERREXIT(cinfo, JERR_BAD_SAMPLING);
		cinfo.max_h_samp_factor = Math.max(cinfo.max_h_samp_factor, compptr.h_samp_factor);
		cinfo.max_v_samp_factor = Math.max(cinfo.max_v_samp_factor, compptr.v_samp_factor);
	}

	/* We initialize DCT_scaled_size and min_DCT_scaled_size to DCTSIZE.
	 * In the full decompressor, this will be overridden by jdmaster.c;
	 * but in the transcoder, jdmaster.c is not used, so we must do it here.
	 */
	cinfo.min_DCT_scaled_size = DCTSIZE;

	/* Compute dimensions of components */
	for (ci = 0; ci < cinfo.num_components; ci++) {
		compptr = cinfo.comp_info[ci];
		compptr.DCT_scaled_size = DCTSIZE;
		/* Size in DCT blocks */
		compptr.width_in_blocks = (int)jdiv_round_up((long) cinfo.image_width * (long) compptr.h_samp_factor, (cinfo.max_h_samp_factor * DCTSIZE));
		compptr.height_in_blocks = (int)jdiv_round_up((long) cinfo.image_height * (long) compptr.v_samp_factor, (cinfo.max_v_samp_factor * DCTSIZE));
		/* downsampled_width and downsampled_height will also be overridden by
		 * jdmaster.c if we are doing full decompression.	The transcoder library
		 * doesn't use these values, but the calling application might.
		 */
		/* Size in samples */
		compptr.downsampled_width = (int)jdiv_round_up((long) cinfo.image_width * (long) compptr.h_samp_factor,	cinfo.max_h_samp_factor);
		compptr.downsampled_height = (int)jdiv_round_up((long) cinfo.image_height * (long) compptr.v_samp_factor, cinfo.max_v_samp_factor);
		/* Mark component needed, until color conversion says otherwise */
		compptr.component_needed = true;
		/* Mark no quantization table yet saved for component */
		compptr.quant_table = null;
	}

	/* Compute number of fully interleaved MCU rows. */
	cinfo.total_iMCU_rows = (int)jdiv_round_up( cinfo.image_height, (cinfo.max_v_samp_factor*DCTSIZE));

	/* Decide whether file contains multiple scans */
	if (cinfo.comps_in_scan < cinfo.num_components || cinfo.progressive_mode)
		cinfo.inputctl.has_multiple_scans = true;
	else
		cinfo.inputctl.has_multiple_scans = false;
}


static void per_scan_setup (jpeg_decompress_struct cinfo)
/* Do computations that are needed before processing a JPEG scan */
/* cinfo.comps_in_scan and cinfo.cur_comp_info[] were set from SOS marker */
{
	int ci, mcublks, tmp = 0;
	jpeg_component_info compptr;
	
	if (cinfo.comps_in_scan == 1) {
		
		/* Noninterleaved (single-component) scan */
		compptr = cinfo.cur_comp_info[0];
		
		/* Overall image size in MCUs */
		cinfo.MCUs_per_row = compptr.width_in_blocks;
		cinfo.MCU_rows_in_scan = compptr.height_in_blocks;
		
		/* For noninterleaved scan, always one block per MCU */
		compptr.MCU_width = 1;
		compptr.MCU_height = 1;
		compptr.MCU_blocks = 1;
		compptr.MCU_sample_width = compptr.DCT_scaled_size;
		compptr.last_col_width = 1;
		/* For noninterleaved scans, it is convenient to define last_row_height
		 * as the number of block rows present in the last iMCU row.
		 */
		tmp = (compptr.height_in_blocks % compptr.v_samp_factor);
		if (tmp == 0) tmp = compptr.v_samp_factor;
		compptr.last_row_height = tmp;
		
		/* Prepare array describing MCU composition */
		cinfo.blocks_in_MCU = 1;
		cinfo.MCU_membership[0] = 0;
		
	} else {
		
		/* Interleaved (multi-component) scan */
		if (cinfo.comps_in_scan <= 0 || cinfo.comps_in_scan > MAX_COMPS_IN_SCAN)
			error();
//			ERREXIT2(cinfo, JERR_COMPONENT_COUNT, cinfo.comps_in_scan, MAX_COMPS_IN_SCAN);
		
		/* Overall image size in MCUs */
		cinfo.MCUs_per_row = (int)jdiv_round_up( cinfo.image_width, (cinfo.max_h_samp_factor*DCTSIZE));
		cinfo.MCU_rows_in_scan = (int)jdiv_round_up( cinfo.image_height, (cinfo.max_v_samp_factor*DCTSIZE));
		
		cinfo.blocks_in_MCU = 0;
		
		for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
			compptr = cinfo.cur_comp_info[ci];
			/* Sampling factors give # of blocks of component in each MCU */
			compptr.MCU_width = compptr.h_samp_factor;
			compptr.MCU_height = compptr.v_samp_factor;
			compptr.MCU_blocks = compptr.MCU_width * compptr.MCU_height;
			compptr.MCU_sample_width = compptr.MCU_width * compptr.DCT_scaled_size;
			/* Figure number of non-dummy blocks in last MCU column & row */
			tmp = (compptr.width_in_blocks % compptr.MCU_width);
			if (tmp == 0) tmp = compptr.MCU_width;
			compptr.last_col_width = tmp;
			tmp = (compptr.height_in_blocks % compptr.MCU_height);
			if (tmp == 0) tmp = compptr.MCU_height;
			compptr.last_row_height = tmp;
			/* Prepare array describing MCU composition */
			mcublks = compptr.MCU_blocks;
			if (cinfo.blocks_in_MCU + mcublks > D_MAX_BLOCKS_IN_MCU)
				error();
//	ERREXIT(cinfo, JERR_BAD_MCU_SIZE);
			while (mcublks-- > 0) {
				cinfo.MCU_membership[cinfo.blocks_in_MCU++] = ci;
			}
		}
		
	}
}

static void latch_quant_tables (jpeg_decompress_struct cinfo) {
	int ci, qtblno;
	jpeg_component_info compptr;
	JQUANT_TBL qtbl;

	for (ci = 0; ci < cinfo.comps_in_scan; ci++) {
		compptr = cinfo.cur_comp_info[ci];
		/* No work if we already saved Q-table for this component */
		if (compptr.quant_table != null)
			continue;
		/* Make sure specified quantization table is present */
		qtblno = compptr.quant_tbl_no;
		if (qtblno < 0 || qtblno >= NUM_QUANT_TBLS || cinfo.quant_tbl_ptrs[qtblno] == null)
			error();
//			ERREXIT1(cinfo, JERR_NO_QUANT_TABLE, qtblno);
		/* OK, save away the quantization table */
		qtbl = new JQUANT_TBL();
		System.arraycopy(cinfo.quant_tbl_ptrs[qtblno].quantval, 0, qtbl.quantval, 0, qtbl.quantval.length);
		qtbl.sent_table = cinfo.quant_tbl_ptrs[qtblno].sent_table;
		compptr.quant_table = qtbl;
	}
}

static void jpeg_make_d_derived_tbl (jpeg_decompress_struct cinfo, boolean isDC, int tblno, d_derived_tbl dtbl) {
	JHUFF_TBL htbl;
	int p, i = 0, l, si, numsymbols;
	int lookbits, ctr;
	byte[] huffsize = new byte[257];
	int[] huffcode = new int[257];
	int code;

	/* Note that huffsize[] and huffcode[] are filled in code-length order,
	 * paralleling the order of the symbols themselves in htbl.huffval[].
	 */

	/* Find the input Huffman table */
	if (tblno < 0 || tblno >= NUM_HUFF_TBLS)
		error();
//		ERREXIT1(cinfo, JERR_NO_HUFF_TABLE, tblno);
	htbl = isDC ? cinfo.dc_huff_tbl_ptrs[tblno] : cinfo.ac_huff_tbl_ptrs[tblno];
	if (htbl == null)
		error();
//		ERREXIT1(cinfo, JERR_NO_HUFF_TABLE, tblno);

	/* Allocate a workspace if we haven't already done so. */
	dtbl.pub = htbl;		/* fill in back link */
	
	/* Figure C.1: make table of Huffman code length for each symbol */

	p = 0;
	for (l = 1; l <= 16; l++) {
		i = htbl.bits[l] & 0xFF;
		if (i < 0 || p + i > 256)	/* protect against table overrun */
			error();
//			ERREXIT(cinfo, JERR_BAD_HUFF_TABLE);
		while (i-- != 0)
			huffsize[p++] = (byte) l;
	}
	huffsize[p] = 0;
	numsymbols = p;
	
	/* Figure C.2: generate the codes themselves */
	/* We also validate that the counts represent a legal Huffman code tree. */
	
	code = 0;
	si = huffsize[0];
	p = 0;
	while ((huffsize[p]) != 0) {
		while (( huffsize[p]) == si) {
			huffcode[p++] = code;
			code++;
		}
		/* code is now 1 more than the last code used for codelength si; but
		 * it must still fit in si bits, since no code is allowed to be all ones.
		 */
		if (( code) >= (( 1) << si))
			error();
//			ERREXIT(cinfo, JERR_BAD_HUFF_TABLE);
		code <<= 1;
		si++;
	}

	/* Figure F.15: generate decoding tables for bit-sequential decoding */

	p = 0;
	for (l = 1; l <= 16; l++) {
		if ((htbl.bits[l] & 0xFF) != 0) {
			/* valoffset[l] = huffval[] index of 1st symbol of code length l,
			 * minus the minimum code of length l
			 */
			dtbl.valoffset[l] =	p -	huffcode[p];
			p += (htbl.bits[l] & 0xFF);
			dtbl.maxcode[l] = huffcode[p-1]; /* maximum code of length l */
		} else {
			dtbl.maxcode[l] = -1;	/* -1 if no codes of this length */
		}
	}
	dtbl.maxcode[17] = 0xFFFFF; /* ensures jpeg_huff_decode terminates */

	/* Compute lookahead tables to speed up decoding.
	 * First we set all the table entries to 0, indicating "too long";
	 * then we iterate through the Huffman codes that are short enough and
	 * fill in all the entries that correspond to bit sequences starting
	 * with that code.
	 */

	for (int j = 0; j < dtbl.look_nbits.length; j++) {
		dtbl.look_nbits[j] = 0;
	}

	p = 0;
	for (l = 1; l <= HUFF_LOOKAHEAD; l++) {
		for (i = 1; i <= (htbl.bits[l] & 0xFF); i++, p++) {
			/* l = current code's length, p = its index in huffcode[] & huffval[]. */
			/* Generate left-justified code followed by all possible bit sequences */
			lookbits = huffcode[p] << (HUFF_LOOKAHEAD-l);
			for (ctr = 1 << (HUFF_LOOKAHEAD-l); ctr > 0; ctr--) {
				dtbl.look_nbits[lookbits] = l;
				dtbl.look_sym[lookbits] = htbl.huffval[p];
				lookbits++;
			}
		}
	}

	/* Validate symbols as being reasonable.
	 * For AC tables, we make no check, but accept all byte values 0..255.
	 * For DC tables, we require the symbols to be in range 0..15.
	 * (Tighter bounds could be applied depending on the data depth and mode,
	 * but this is sufficient to ensure safe decoding.)
	 */
	if (isDC) {
		for (i = 0; i < numsymbols; i++) {
			int sym = htbl.huffval[i] & 0xFF;
			if (sym < 0 || sym > 15)
				error();
//				ERREXIT(cinfo, JERR_BAD_HUFF_TABLE);
		}
	}
}

static void start_input_pass (jpeg_decompress_struct cinfo) {
	per_scan_setup(cinfo);
	latch_quant_tables(cinfo);
	cinfo.entropy.start_pass(cinfo);
	cinfo.coef.start_input_pass (cinfo);
	cinfo.inputctl.consume_input = COEF_CONSUME_INPUT;
}

static void finish_input_pass (jpeg_decompress_struct cinfo) {
	cinfo.inputctl.consume_input = INPUT_CONSUME_INPUT;
}

static int consume_markers (jpeg_decompress_struct cinfo) {
	jpeg_input_controller inputctl = cinfo.inputctl;
	int val;

	if (inputctl.eoi_reached) /* After hitting EOI, read no further */
		return JPEG_REACHED_EOI;

	val = read_markers (cinfo);

	switch (val) {
	case JPEG_REACHED_SOS:	/* Found SOS */
		if (inputctl.inheaders) {	/* 1st SOS */
			initial_setup(cinfo);
			inputctl.inheaders = false;
			/* Note: start_input_pass must be called by jdmaster.c
			 * before any more input can be consumed.	jdapimin.c is
			 * responsible for enforcing this sequencing.
			 */
		} else {			/* 2nd or later SOS marker */
			if (! inputctl.has_multiple_scans)
				error();
//				ERREXIT(cinfo, JERR_EOI_EXPECTED); /* Oops, I wasn't expecting this! */
			start_input_pass(cinfo);
		}
		break;
	case JPEG_REACHED_EOI:	/* Found EOI */
		inputctl.eoi_reached = true;
		if (inputctl.inheaders) {	/* Tables-only datastream, apparently */
			if (cinfo.marker.saw_SOF)
				error();
//				ERREXIT(cinfo, JERR_SOF_NO_SOS);
		} else {
			/* Prevent infinite loop in coef ctlr's decompress_data routine
			 * if user set output_scan_number larger than number of scans.
			 */
			if (cinfo.output_scan_number > cinfo.input_scan_number)
				cinfo.output_scan_number = cinfo.input_scan_number;
		}
		break;
	case JPEG_SUSPENDED:
		break;
	}

	return val;
}

static void default_decompress_parms (jpeg_decompress_struct cinfo) {
	/* Guess the input colorspace, and set output colorspace accordingly. */
	/* (Wish JPEG committee had provided a real way to specify this...) */
	/* Note application may override our guesses. */
	switch (cinfo.num_components) {
		case 1:
			cinfo.jpeg_color_space = JCS_GRAYSCALE;
			cinfo.out_color_space = JCS_GRAYSCALE;
			break;
			
		case 3:
			if (cinfo.saw_JFIF_marker) {
				cinfo.jpeg_color_space = JCS_YCbCr; /* JFIF implies YCbCr */
			} else if (cinfo.saw_Adobe_marker) {
				switch (cinfo.Adobe_transform) {
					case 0:
						cinfo.jpeg_color_space = JCS_RGB;
						break;
					case 1:
						cinfo.jpeg_color_space = JCS_YCbCr;
						break;
					default:	
//						WARNMS1(cinfo, JWRN_ADOBE_XFORM, cinfo.Adobe_transform);
						cinfo.jpeg_color_space = JCS_YCbCr; /* assume it's YCbCr */
					break;
				}
			} else {
				/* Saw no special markers, try to guess from the component IDs */
				int cid0 = cinfo.comp_info[0].component_id;
				int cid1 = cinfo.comp_info[1].component_id;
				int cid2 = cinfo.comp_info[2].component_id;
	
				if (cid0 == 1 && cid1 == 2 && cid2 == 3)
					cinfo.jpeg_color_space = JCS_YCbCr; /* assume JFIF w/out marker */
				else if (cid0 == 82 && cid1 == 71 && cid2 == 66)
					cinfo.jpeg_color_space = JCS_RGB; /* ASCII 'R', 'G', 'B' */
				else {
//					TRACEMS3(cinfo, 1, JTRC_UNKNOWN_IDS, cid0, cid1, cid2);
					cinfo.jpeg_color_space = JCS_YCbCr; /* assume it's YCbCr */
				}
			}
			/* Always guess RGB is proper output colorspace. */
			cinfo.out_color_space = JCS_RGB;
			break;
			
		case 4:
			if (cinfo.saw_Adobe_marker) {
				switch (cinfo.Adobe_transform) {
					case 0:
						cinfo.jpeg_color_space = JCS_CMYK;
						break;
					case 2:
						cinfo.jpeg_color_space = JCS_YCCK;
						break;
					default:
//						WARNMS1(cinfo, JWRN_ADOBE_XFORM, cinfo.Adobe_transform);
						cinfo.jpeg_color_space = JCS_YCCK; /* assume it's YCCK */
						break;
				}
			} else {
				/* No special markers, assume straight CMYK. */
				cinfo.jpeg_color_space = JCS_CMYK;
			}
			cinfo.out_color_space = JCS_CMYK;
			break;
			
		default:
			cinfo.jpeg_color_space = JCS_UNKNOWN;
			cinfo.out_color_space = JCS_UNKNOWN;
			break;
	}

	/* Set defaults for other decompression parameters. */
	cinfo.scale_num = 1;		/* 1:1 scaling */
	cinfo.scale_denom = 1;
	cinfo.output_gamma = 1.0;
	cinfo.buffered_image = false;
	cinfo.raw_data_out = false;
	cinfo.dct_method = JDCT_DEFAULT;
	cinfo.do_fancy_upsampling = true;
	cinfo.do_block_smoothing = true;
	cinfo.quantize_colors = false;
	/* We set these in case application only sets quantize_colors. */
	cinfo.dither_mode = JDITHER_FS;
	cinfo.two_pass_quantize = true;
	cinfo.desired_number_of_colors = 256;
	cinfo.colormap = null;
	/* Initialize for no mode change in buffered-image mode. */
	cinfo.enable_1pass_quant = false;
	cinfo.enable_external_quant = false;
	cinfo.enable_2pass_quant = false;
}

static void init_source(jpeg_decompress_struct cinfo) {
	cinfo.buffer = new byte[INPUT_BUFFER_SIZE];
	cinfo.bytes_in_buffer = 0;
	cinfo.bytes_offset = 0;
	cinfo.start_of_file = true;
}

static int jpeg_consume_input (jpeg_decompress_struct cinfo) {
	int retcode = JPEG_SUSPENDED;

	/* NB: every possible DSTATE value should be listed in this switch */
	switch (cinfo.global_state) {
	case DSTATE_START:
		/* Start-of-datastream actions: reset appropriate modules */
		reset_input_controller(cinfo);
		/* Initialize application's data source module */
		init_source (cinfo);
		cinfo.global_state = DSTATE_INHEADER;
		/*FALLTHROUGH*/
	case DSTATE_INHEADER:
		retcode = consume_input(cinfo);
		if (retcode == JPEG_REACHED_SOS) { /* Found SOS, prepare to decompress */
			/* Set up default parameters based on header data */
			default_decompress_parms(cinfo);
			/* Set global state: ready for start_decompress */
			cinfo.global_state = DSTATE_READY;
		}
		break;
	case DSTATE_READY:
		/* Can't advance past first SOS until start_decompress is called */
		retcode = JPEG_REACHED_SOS;
		break;
	case DSTATE_PRELOAD:
	case DSTATE_PRESCAN:
	case DSTATE_SCANNING:
	case DSTATE_RAW_OK:
	case DSTATE_BUFIMAGE:
	case DSTATE_BUFPOST:
	case DSTATE_STOPPING:
		retcode = consume_input (cinfo);
		break;
	default:
		error();
//		ERREXIT1(cinfo, JERR_BAD_STATE, cinfo.global_state);
	}
	return retcode;
}


static void jpeg_abort (jpeg_decompress_struct cinfo) {
//	int pool;
//
//	/* Releasing pools in reverse order might help avoid fragmentation
//	 * with some (brain-damaged) malloc libraries.
//	 */
//	for (pool = JPOOL_NUMPOOLS-1; pool > JPOOL_PERMANENT; pool--) {
//		(*cinfo.mem.free_pool) (cinfo, pool);
//	}

	/* Reset overall state for possible reuse of object */
	if (cinfo.is_decompressor) {
		cinfo.global_state = DSTATE_START;
		/* Try to keep application from accessing now-deleted marker list.
		 * A bit kludgy to do it here, but this is the most central place.
		 */
//		((j_decompress_ptr) cinfo).marker_list = null;
	} else {
		cinfo.global_state = CSTATE_START;
	}
}


static boolean isFileFormat(LEDataInputStream stream) {
	try {
		byte[] buffer = new byte[2];
		stream.read(buffer);
		stream.unread(buffer);
		return (buffer[0] & 0xFF) == 0xFF && (buffer[1] & 0xFF) == M_SOI;
	} catch (Exception e) {
		return false;
	}
}
	
static ImageData[] loadFromByteStream(InputStream inputStream, ImageLoader loader) {
	jpeg_decompress_struct cinfo = new jpeg_decompress_struct();
	cinfo.inputStream = inputStream;
	jpeg_create_decompress(cinfo);
	jpeg_read_header(cinfo, true);
	cinfo.buffered_image = cinfo.progressive_mode && loader.hasListeners();
	jpeg_start_decompress(cinfo);
	PaletteData palette = null;
	switch (cinfo.out_color_space) {
		case JCS_RGB:
			palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			break;
		case JCS_GRAYSCALE:
			RGB[] colors = new RGB[256];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = new RGB(i, i, i);
			}
			palette = new PaletteData(colors);
			break;
		default:
			error();
	}
	int scanlinePad = 4;
	int row_stride = (((cinfo.output_width * cinfo.out_color_components * 8 + 7) / 8) + (scanlinePad - 1)) / scanlinePad * scanlinePad;
	byte[][] buffer = new byte[1][row_stride];
	byte[] data = new byte[row_stride * cinfo.output_height];
	ImageData imageData = ImageData.internal_new(
			cinfo.output_width, cinfo.output_height, palette.isDirect ? 24 : 8, palette, scanlinePad, data,
			0, null, null, -1, -1, SWT.IMAGE_JPEG, 0, 0, 0, 0);
	if (cinfo.buffered_image) {
		boolean done;
		do {
			int incrementCount = cinfo.input_scan_number - 1;
			jpeg_start_output(cinfo, cinfo.input_scan_number);
			while (cinfo.output_scanline < cinfo.output_height) {
				int offset = row_stride * cinfo.output_scanline;
				jpeg_read_scanlines(cinfo, buffer, 1);
				System.arraycopy(buffer[0], 0, data, offset, row_stride);
			}
			jpeg_finish_output(cinfo);
			loader.notifyListeners(new ImageLoaderEvent(loader, (ImageData)imageData.clone(), incrementCount, done = jpeg_input_complete(cinfo)));
		} while (!done);
	} else {
		while (cinfo.output_scanline < cinfo.output_height) {
			int offset = row_stride * cinfo.output_scanline;
			jpeg_read_scanlines(cinfo, buffer, 1);
			System.arraycopy(buffer[0], 0, data, offset, row_stride);
		}
	}
	jpeg_finish_decompress(cinfo);
	jpeg_destroy_decompress(cinfo);
	return new ImageData[]{imageData};
}

}
