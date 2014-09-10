package com.thoughtcomplex.hairpin.classhackery

/**
 * Created by Falkreon on 6/29/2014.
 */
enum ClassConstantType {
	UTF8(1),
	INTEGER(3),
	FLOAT(4),
	LONG(5),
	DOUBLE(6),
	CLASS(7),
	STRING(8),
	FIELD_REF(9),
	METHOD_REF(10),
	INTERFACE_METHOD_REF(11),
	NAME_AND_TYPE(12),
	METHOD_HANDLE(15),
	METHOD_TYPE(16),
	INVOKE_DYNAMIC(18),
	INVALID(-1);

	private final int id;

	ClassConstantType(int id) {
		this.id = id;
	}

	public static ClassConstantType get(int id) {
		ClassConstantType result = values().find { it.id == id };
		return result ?: INVALID;
	}
}
