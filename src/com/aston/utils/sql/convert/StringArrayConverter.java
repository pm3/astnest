package com.aston.utils.sql.convert;

import com.aston.utils.sql.IConverter;

public class StringArrayConverter implements IConverter {

	String delim = "\n";

	@Override
	public void setFormat(String format) {
		if (format != null && format.length() > 0) {
			delim = format;
		}
	}

	@Override
	public Object bean2sql(Object val, Class<?> type) {

		if (val instanceof String[]) {
			return toString((String[]) val, delim);
		}
		return null;
	}

	@Override
	public Object sql2bean(Object val, Class<?> type) throws Exception {
		if (val instanceof String) {
			return ((String) val).split(delim);
		}
		return null;
	}

	public static String toString(String[] arr, String delim) {
		if (arr == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (String s : arr) {
			if (s == null)
				continue;
			s = s.trim();
			if (s.length() == 0)
				continue;
			if (sb.length() > 0)
				sb.append(delim);
			sb.append(s);
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

}
