package be.olsson.bencoder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Bdecoder {

	private boolean stringAsByteArray = true;

	// This is to be groovy compatible
	private static final byte DICTIONARY = 'd';
	private static final byte LIST = 'l';
	private static final byte INTEGER = 'i';
	private static final byte END = 'e';
	public static final byte COLON = ':';
	public static final byte _0 = '0';
	public static final byte _1 = '1';
	public static final byte _2 = '2';
	public static final byte _3 = '3';
	public static final byte _4 = '4';
	public static final byte _5 = '5';
	public static final byte _6 = '6';
	public static final byte _7 = '7';
	public static final byte _8 = '8';
	public static final byte _9 = '9';

	protected static class MutableInt {
		int pos = 0;
	}

	public Number decodeInteger(byte[] bencodedData, MutableInt pos) throws IOException {
		byte atPos = bencodedData[pos.pos];
		if (atPos == INTEGER) {
			int index = indexOf(bencodedData, pos.pos, END);
			int recordLen = index - pos.pos - 1;
			pos.pos++;
			String s = new String(bencodedData, pos.pos, recordLen);
			Number result = Long.valueOf(s);
			pos.pos = index + 1;
			return result;
		}
		throw new IOException("decodeInteger: Not an Integer: " + atPos + " at position " + pos.pos);
	}

	protected int indexOf(byte[] haystack, int start, byte needle) {
		int index = start;
		while (index < haystack.length) {
			if (haystack[index] == needle) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public Object decodeString(byte[] bencodedData, MutableInt pos) throws IOException {
		byte atPos = bencodedData[pos.pos];
		int index = indexOf(bencodedData, pos.pos, COLON);
		if ((atPos >= _1 && atPos <= _9) || (atPos >= _0 && index <= pos.pos + 1)) {
			int recordLen = index - pos.pos;
			int arrayLength = Integer.parseInt(new String(bencodedData, pos.pos, recordLen));
			pos.pos = index + 1;
			Object result;
			if (isStringAsByteArray()) {
				byte[] res = new byte[arrayLength];
				System.arraycopy(bencodedData, pos.pos, res, 0, res.length);
				result = res;
			} else {
				result = new String(bencodedData, pos.pos, arrayLength);
			}
			pos.pos += arrayLength;
			return result;
		}
		throw new IOException("decodeString: Not a String: " + atPos + " at position " + pos.pos);
	}

	protected Object decode(byte[] bencodedData, MutableInt pos) throws IOException {
		Object result;
		switch (bencodedData[pos.pos]) {
			case _1:
			case _2:
			case _3:
			case _4:
			case _5:
			case _6:
			case _7:
			case _8:
			case _9: // Decode string
				result = decodeString(bencodedData, pos);
				break;
			case INTEGER:
				result = decodeInteger(bencodedData, pos);
				break;
			case DICTIONARY:
				result = decodeDic(bencodedData, pos);
				break;
			case LIST:
				result = decodeList(bencodedData, pos);
				break;
			default:
				throw new IOException("Unknown object type: " + (char)bencodedData[pos.pos] + " at position " + pos.pos);
		}
		return result;
	}

	public Object decode(byte[] bencodedData) throws IOException {
		return decode(bencodedData, new MutableInt());
	}


	public int compare(byte[] left, byte[] right) {
		int i = 0;
		int j = 0;
		while (i < left.length && j < right.length) {
			int a = (left[i] & 0xff);
			int b = (right[j] & 0xff);
			if (a != b) {
				return a - b;
			}
			i++;
			j++;
		}
		return left.length - right.length;
	}

	protected Map decodeDic(byte[] bencodedData, MutableInt pos) throws IOException {
		Map<Object, Object> result = new LinkedHashMap<Object, Object>();
		//byte[] last = new byte[0];
		Object temp;
		Object temp2;
		pos.pos++;
		while (pos.pos < bencodedData.length && bencodedData[pos.pos] != END) {
			temp = decodeString(bencodedData, pos);
/*			if (!(temp instanceof byte[])) {
				throw new IOException(temp + " is not a string, and can thus not be a key");
			}
			if (compare(last, temp) > 0) {
				throw new IOException("This data is not sorted: " + new String(temp) + " is before " + new String(last));
			}*/
			temp2 = decode(bencodedData, pos);
			result.put(temp, temp2);
		}
		pos.pos++;
		return result;
	}

	protected List decodeList(byte[] bencodedData, MutableInt pos) throws IOException {
		List<Object> result = new LinkedList<Object>();
		pos.pos++;
		while (pos.pos < bencodedData.length && bencodedData[pos.pos] != END) {
			result.add(decode(bencodedData, pos));
		}
		pos.pos++;
		return result;
	}

	protected List decodeList(byte[] bencodedData) throws IOException {
		return decodeList(bencodedData, new MutableInt());
	}

	public boolean isStringAsByteArray() {
		return stringAsByteArray;
	}

	public void setStringAsByteArray(boolean stringAsByteArray) {
		this.stringAsByteArray = stringAsByteArray;
	}
}
