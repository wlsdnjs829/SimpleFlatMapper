package org.sfm.csv.parser;

import java.io.IOException;
import java.io.InputStream;

public final class InputStreamCsvParser {
	
	private static final byte CARRIAGE_RETURN = '\n';
	private static final byte COMMA = ',';
	private static final byte QUOTES = '"';

	private byte[] buffer;

	private int bufferLength;
	private CsvParserState currentState = CsvParserState.NONE;

	private int currentStart =0;
	private int bufferOffset = 0;

	
	public InputStreamCsvParser(final int bufferSize) {
		buffer = new byte[bufferSize];
	}
	
	/**
	 * parse cvs from input stream assumes character encoding for '"', ',' and '\n' match utf8
	 * @param is
	 * @param handler
	 * @return
	 * @throws IOException
	 */
	public void parse(final InputStream is, final BytesCellHandler handler) throws IOException {
		byte c = 0;
		
		while((bufferLength = is.read(buffer, bufferOffset, buffer.length - bufferOffset)) != -1) {
			c = consumeBytes(handler);
		}
		
		if (bufferOffset > 0 || c == ',' ) {
			handler.newCell(buffer, 0, bufferOffset);
		}
		
		handler.end();
	}


	private byte consumeBytes(final BytesCellHandler handler) {
		bufferLength += bufferOffset;
		
		byte c = 0;
		for(int i = 0; i < bufferLength; i++) {
			c = buffer[i];
			handleByte(handler, c, i);
		}
		
		shiftBuffer();
		
		return c;
	}

	private void handleByte(final BytesCellHandler handler, final byte c, final int i) {
		if (c == QUOTES) {
			quote(i);
		} else if (c == COMMA) {
			if (currentState != CsvParserState.IN_QUOTE) {
				newCell(handler, i);
			}
		}else if (c == CARRIAGE_RETURN) {
			if (currentState != CsvParserState.IN_QUOTE) {
				newCell(handler, i);
				handler.endOfRow();
			}
		}
	}

	public void quote(final int i) {
		if (currentStart == i) {
			currentState = CsvParserState.IN_QUOTE;
		} else {
			if (currentState ==  CsvParserState.IN_QUOTE) {
				currentState = CsvParserState.QUOTE;
			} else if(currentState ==  CsvParserState.QUOTE) {
				currentState = CsvParserState.IN_QUOTE;
			}
		}
	}

	public void newCell(final BytesCellHandler handler, final int i) {
		handler.newCell(buffer, currentStart, i - currentStart);
		currentStart = i  + 1;
		currentState = CsvParserState.NONE;
	}

	private void shiftBuffer() {
		// shift buffer consumer data
		bufferOffset = bufferLength - currentStart;
		
		// if buffer tight double the size
		if (bufferOffset > bufferLength >> 1) {
			// double buffer size
			final byte[] newbuffer = new byte[buffer.length << 1];
			System.arraycopy(buffer, currentStart, newbuffer, 0, bufferOffset);
			buffer = newbuffer;
		} else {
			System.arraycopy(buffer, currentStart, buffer, 0, bufferOffset);
		}
		currentStart = 0;
	}
}
