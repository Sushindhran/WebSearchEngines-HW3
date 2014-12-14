package edu.nyu.cs.cs2580;

//import hdt.util.Mutable;

import gnu.trove.list.array.TByteArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Typical implementation of Variable-Byte encoding for integers.
 * http://nlp.stanford.edu/IR-book/html/htmledition/variable-byte-codes-1.html
 *
 * The first bit of each byte specifies whether there are more bytes available.
 * Numbers from 0 to 126 are encoded using just one byte.
 * Numbers from 127 to 16383 are encoded using two bytes.
 * Numbers from 16384 to 2097151 are encoding using three bytes.
 *
 *
 */
public class VByte {
    public static void encode(OutputStream out, int value) throws IOException {
        while( value > 127) {
            out.write(value & 127);
            value>>>=7;
        }
        out.write(value|0x80);
    }

    public static int decode(InputStream in) throws IOException {
        int out = 0;
        int shift=0;
        byte readbyte = (byte)in.read();
        while( (readbyte & 0x80)==0) {
            out |= (readbyte & 127) << shift;
            readbyte = (byte)in.read();
            shift+=7;
        }
        out |= (readbyte & 127) << shift;
        return out;
    }

    public static TByteArrayList encode(List<Integer> values) {

        TByteArrayList data = new TByteArrayList();
        for(int value : values) {
            while( value > 127) {
                byte d = (byte)(value & 127);
                data.add(d);
                value>>>=7;
            }
            byte d = (byte)(value|0x80);
            data.add(d);
        }


        return data;
    }
/*
	public static int decode(byte[] data, int offset, Mutable<Integer> value) {
		int out = 0;
		int i=0;
		int shift=0;
		while( (0x80 & data[offset+i])==0) {
			out |= (data[offset+i] & 127) << shift;
			i++;
			shift+=7;
		}
		out |= (data[offset+i] & 127) << shift;
		i++;
		value.setValue(out);
		return i;
	}*/

    public static List<Integer> decode(TByteArrayList data) {
        int out = 0;
        int i=0;
        int shift=0;
        List<Integer> integerList = new ArrayList<Integer>();
        while(data.size() != i) {
            out=0;
            shift=0;
            while( (0x80 & data.get(i))==0) {
                out |= (data.get(i) & 127) << shift;
                i++;
                shift+=7;
            }
            out |= (data.get(i) & 127) << shift;
            i++;
            integerList.add(out);
        }

        return integerList;
    }

}