package com.aston.utils.sql.convert;

import com.aston.utils.sql.IConverter;

public class EnumStringConverter implements IConverter {

	@Override
	public void setFormat(String format) {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object bean2sql(Object val, Class<?> type) {
		if (val instanceof Enum)
			val = ((Enum) val).name();
		return val;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object sql2bean(Object val, Class<?> type) {
		if (val instanceof String)
			val = (Enum) Enum.valueOf((Class<Enum>) type, (String) val);
		return val;
	}

}
