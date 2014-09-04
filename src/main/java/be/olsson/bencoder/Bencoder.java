package be.olsson.bencoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

public class Bencoder {

	private Charset charset = Charset.forName("UTF-8");

	public void encode(Object[] input, OutputStream result) throws IOException {
		encode(Arrays.asList(input), result);
	}

	public void encode(Iterable<?> input, OutputStream result) throws IOException {
		result.write('l');
		for (Object o : input) {
			encode(o, result);
		}
		result.write('e');
	}

	public void encode(byte[] input, OutputStream result) throws IOException {
		writeLine(String.valueOf(input.length), result);
		result.write(':');
		result.write(input);
	}

	public void encode(SortedMap<?, ?> input, OutputStream result) throws IOException {
		result.write('d');
		for (Map.Entry<?, ?> e : input.entrySet()) {
			writeLine(e.getKey().toString(), result);
			encode(e.getValue(), result);
		}
		result.write('e');
	}

	public void encode(Number input, OutputStream result) throws IOException {
		result.write('i');
		writeLine(input.toString(), result);
		result.write('e');
	}

	public void encode(String input, OutputStream result) throws IOException {
		byte[] bytes = getBytes(input);
		writeLine(String.valueOf(bytes.length), result);
		result.write(':');
		result.write(bytes);
	}

	public byte[] getBytes(String input) {
		return input.getBytes(charset);
	}

	public void writeLine(String input, OutputStream result) throws IOException {
		byte[] bytes = getBytes(input);
		result.write(bytes);
	}

	public void encode(Object input, OutputStream result) throws IOException {
		if (input instanceof Number) {
			encode((Number) input, result);
		} else if (input instanceof String) {
			encode((String) input, result);
		} else if (input instanceof SortedMap) {
			encode((SortedMap) input, result);
		} else if (input instanceof Iterable) {
			encode((Iterable) input, result);
		} else if (input instanceof byte[]) {
			encode((byte[])input, result);
		} else {
			throw new IOException("Can't parse object " + input.toString());
		}
	}

	public byte[] encode(Object input) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			encode(input, baos);
			return baos.toByteArray();
		} catch (IOException e) {
			// Won't happen
			throw new RuntimeException(e);
		}
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}
}
