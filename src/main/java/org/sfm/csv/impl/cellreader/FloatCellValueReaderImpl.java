package org.sfm.csv.impl.cellreader;

import org.sfm.csv.impl.ParsingContext;

public final class FloatCellValueReaderImpl implements FloatCellValueReader {

	@Override
	public Float read(char[] chars, int offset, int length, ParsingContext parsingContext) {
		if (length == 0) return null;
		return new Float(readFloat(chars, offset, length, parsingContext));
	}

	@Override
	public float readFloat(char[] chars, int offset, int length, ParsingContext parsingContext) {
		return parseFloat(chars, offset, length);
	}
	
	public static float parseFloat(char[] chars, int offset, int length) {
		return Float.parseFloat(StringCellValueReader.readString(chars, offset, length));
	}

    @Override
    public String toString() {
        return "FloatCellValueReaderImpl{}";
    }
}
