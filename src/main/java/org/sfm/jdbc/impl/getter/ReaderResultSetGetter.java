package org.sfm.jdbc.impl.getter;

import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sfm.reflect.Getter;

public final class ReaderResultSetGetter implements Getter<ResultSet, Reader> {
	private final int column;
	
	public ReaderResultSetGetter(final int column) {
		this.column = column;
	}

	public Reader get(final ResultSet target) throws SQLException {
		return target.getCharacterStream(column);
	}

}
