package com.thoughtcomplex.hairpin.classhackery
/**
 * Created by Falkreon on 6/29/2014.
 */
class ClassConstant {
	private ClassConstantType constType;
	private int referenceType;
	/** The location at which this constant resides in the constant pool **/
	private final int baseLoc;

	//For types which refer to UTF-8 data, its location in the constant pool and eventually, its value.
	private int refLoc;
	private String data = null;

	//For FIELD_REF, METHOD_REF, and INTERFACE_METHOD_REF
	private int classRefLoc = -1;
	private int nameAndTypeRefLoc = -1;

	//For NAME_AND_TYPE
	private int descriptorLoc;

	public ClassConstant(ClassConstantType constType, int baseLoc) {
		this(constType, baseLoc, -1);
	}

	public ClassConstant(ClassConstantType constType, int baseLoc, int refLoc) {
		this.baseLoc = baseLoc;
		this.refLoc = refLoc;
		this.constType = constType;
	}

	private static final ClassConstant INVALID = new ClassConstant(ClassConstantType.INVALID, -1);
	public static ClassConstant dereference(ClassConstant[] table, int ref) {
		if (ref-1<0 || ref-1>=table.length) return INVALID;
		else return table[ref-1];
	}

	public void fillIn(ClassConstant[] table) {
		switch(constType) {
			case ClassConstantType.STRING:
				this.data = dereference(table,refLoc).getData();
				break;
			case ClassConstantType.CLASS:
				ClassConstant refTarget = dereference(table, refLoc);
				//System.out.println("Resolved Class reference ID "+refLoc+" to "+refTarget.toString());
				this.data = refTarget.data;
				break;
			case ClassConstantType.FIELD_REF:
			case ClassConstantType.METHOD_REF:
			case ClassConstantType.INTERFACE_METHOD_REF:
				ClassConstant classObject = dereference(table, classRefLoc);
				ClassConstant nameAndTypeObject = dereference(table, nameAndTypeRefLoc);
				if (classObject.data!=null && nameAndTypeObject.data!=null) {
					this.data = classObject.data + " | " + nameAndTypeObject.data + "";
				}
				break;
			case ClassConstantType.NAME_AND_TYPE:
				ClassConstant descriptorTarget = dereference(table, descriptorLoc);
				ClassConstant target = dereference(table,refLoc);
				if (descriptorTarget.data!=null && target.data!=null) {
					this.data = descriptorTarget.data+" | "+target.data;
				}
			default:
				break;
		}

	}

	public void setData(String s) {
		this.data = s;
	}

	public String getData() {
		return data?:"";
	}

	@Override
	public String toString() {
		return constType.name()+" : "+(data?:"NULL");
	}
}
