package com.thoughtcomplex.hairpin.classhackery
/**
 * Created by Falkreon on 7/2/2014.
 */
class ClassProfile {
	private String humanReadable = "Object { /* There was a parsing error */}";
	private final AccessModifier accessModifier;
	private final String thisclass;
	private final String superclass;
	private final List<String> interfaces = [];
	private final ClassConstant[] constants = [];
	private List<String> classReferences = null;
	private List<String> stringConstants = null;
	private String sourceFileName;

	public ClassProfile(byte[] data) {
		DataInputStream buf = new DataInputStream(new ByteArrayInputStream(data));
		//ByteBuffer buf = ByteBuffer.wrap(data).order( ByteOrder.BIG_ENDIAN );
		int magic = buf.readInt();
		if (Integer.toHexString(magic).equals("cafebabe")) {
			//System.out.println("File is a valid Class.")
		} else {
			System.out.println("Class magic invalid: "+Integer.toHexString(magic));
		}

		int minorVersion = buf.readUnsignedShort();
		int majorVersion = buf.readUnsignedShort();
		int constantPoolCount = buf.readUnsignedShort();
		constants = new ClassConstant[constantPoolCount];
		EnumSet<ClassConstantType> usedTypes = EnumSet.noneOf(ClassConstantType.class);

		int numInvalidConstants = 0;
		int constNum = 0;
		for( i in 0..<constantPoolCount-1) {
			ClassConstantType constType = ClassConstantType.get(buf.readUnsignedByte());
			ClassConstant result = new ClassConstant( constType, constNum );
			if (constType!=ClassConstantType.INVALID) usedTypes.add(constType);
			switch ( constType ) {
				case ClassConstantType.STRING:
					result.refLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.INTEGER:
					result.setData( buf.readInt() as String );
					break;
				case ClassConstantType.FLOAT:
					result.setData( buf.readFloat() as String );
					break;
				case ClassConstantType.LONG:
					result.setData( buf.readLong() as String );
					break;
				case ClassConstantType.DOUBLE:
					result.setData( buf.readDouble() as String );
					break;
				case ClassConstantType.CLASS:
					result.refLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.FIELD_REF:
					result.classRefLoc = buf.readUnsignedShort();
					result.nameAndTypeRefLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.METHOD_REF:
					result.classRefLoc = buf.readUnsignedShort();
					result.nameAndTypeRefLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.INTERFACE_METHOD_REF:
					result.classRefLoc = buf.readUnsignedShort();
					result.nameAndTypeRefLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.NAME_AND_TYPE:
					result.refLoc = buf.readUnsignedShort();
					result.descriptorLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.UTF8:
					int len = buf.readUnsignedShort();
					byte[] bytes = new byte[len];
					buf.read( bytes );
					result.data = new String( bytes, "UTF8" );
					break;
				case ClassConstantType.METHOD_HANDLE:
					result.referenceType = buf.readUnsignedByte();
					result.refLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.METHOD_TYPE:
					result.refLoc = buf.readUnsignedShort();
					break;
				case ClassConstantType.INVOKE_DYNAMIC:
					result.refLoc = buf.readUnsignedShort();
					result.nameAndTypeRefLoc = buf.readUnsignedShort();
					break;
				default:
					buf.readUnsignedShort(); //minimum 2 bytes of data
					numInvalidConstants++;
					break;
			}
			if (constNum>=constants.size()) {
				System.out.println("Can't place constant at index "+constNum+" of "+constants.length)
			} else {
				constants[constNum] = result;
			}
			if (constType==ClassConstantType.LONG || constType==ClassConstantType.DOUBLE) constNum++;
			constNum++;
			if (constNum>=constantPoolCount-1) break;
		}
		3.times {
			for ( int i in 0..<constants.size() ) {
				if (constants[i]!=null) constants[i].fillIn( constants );
			}
		}

		if (numInvalidConstants>0) System.out.println("There were "+numInvalidConstants+" invalid constants specified out of "+constants.length+" constants. Assuming 2 bytes of data reserved for each.");
		//System.out.println(constants.toList().toListString());

		accessModifier = AccessModifier.forClass(buf.readUnsignedShort());

		int thisClassLocation = buf.readUnsignedShort();
		ClassConstant thisClassConstant = ClassConstant.dereference( constants, thisClassLocation );
		if (thisClassConstant==null) {
			System.out.println("'this' Class tried to reference nonexistant constant #"+thisClassLocation);
		} else {
			if (thisClassConstant.constType.equals(ClassConstantType.CLASS)) {
				thisclass = thisClassConstant.data;
			} else {
				System.out.println("Invalid 'this' Class specified at location "+thisClassLocation+". Found instead: "+thisClassConstant.toString());
			}
		}
		int superClassLocation = buf.readUnsignedShort();
		ClassConstant superClassConstant = ClassConstant.dereference( constants, superClassLocation );
		if (superClassConstant==null) {
			System.out.println("superclass tried to reference nonexistant constant #"+superClassLocation);
		} else {
			if (superClassConstant.constType.equals(ClassConstantType.CLASS)) {
				superclass = superClassConstant.data;
			} else {
				System.out.println("Invalid 'super' Class specified at location "+superClassLocation+". Found instead: "+superClassConstant.toString());
			}
		}

		int numInterfaces = buf.readUnsignedShort();
		//List<String> interfaces = [];
		for(int i in 0..<numInterfaces) {
			ClassConstant curInterface = ClassConstant.dereference(constants, buf.readUnsignedShort());
			if (curInterface==null) {
				System.out.println("Invalid interface specified.");
			} else {
				interfaces.add(curInterface.getData()?.replace("/","."));
			}
		}

		String packageDeclaration = DeclarationHelper.getPackageDeclaration( thisclass );
		String baseClassDeclaration = accessModifier.toString()+" "+DeclarationHelper.getUnqualifiedName(thisclass);
		//String baseClassDeclaration = DeclarationHelper.getClassDeclaration( accessModifier, thisclass );

		int numFields = buf.readUnsignedShort();
		numFields.times {
			String value = null;
			int fieldAccess = buf.readUnsignedShort();
			String name = ClassConstant.dereference(constants, buf.readUnsignedShort())?.data?:"INVALID_FIELD";
			String descriptor = ClassConstant.dereference(constants, buf.readUnsignedShort())?.data?:"INVALID_DESCRIPTOR";
			int numAttributes = buf.readUnsignedShort();
			List<String> attributes = [];
			numAttributes.times {
				String attributeName = ClassConstant.dereference(constants, buf.readUnsignedShort())?.data?:"INVALID_ATTRIBUTE";

				int attributeLength = buf.readInt();
				if (attributeName=="ConstantValue") {
					ClassConstant constValue = ClassConstant.dereference(constants, buf.readUnsignedShort());
					if (constValue!=null) value = constValue.data;
				} else {
					if ( attributeLength > 0 ) {
						byte[] attributeData = new byte[attributeLength];
						buf.read( attributeData );
					}
					attributes.add( attributeName + "(" + attributeLength + "bytes)" );
				}
			}
			String declaration = DeclarationHelper.translateDescriptor( descriptor )+" "+name;
			declaration = DeclarationHelper.getFieldAccessFlags( fieldAccess ) + declaration;
			if (value!=null) {
				declaration+=" = "+value;
			}
			if (!attributes.isEmpty()) declaration+=" : "+attributes.toListString();
			//System.out.println(declaration+";");
		}

		int methodCount = buf.readUnsignedShort();
		methodCount.times {
			int methodAccessFlags = buf.readUnsignedShort();
			String accessText = DeclarationHelper.getFieldAccessFlags( methodAccessFlags );
			String methodName = ClassConstant.dereference(constants, buf.readUnsignedShort())?.data?:"INVALID_METHOD";
			ClassConstant methodDescriptor = ClassConstant.dereference(constants, buf.readUnsignedShort());
			//String arguments = DeclarationHelper.translateMethodDescriptor(methodDescriptor);
			int numAttributes = buf.readUnsignedShort();
			List<String> attributes = [];
			numAttributes.times {
				String attributeName = ClassConstant.dereference(constants, buf.readUnsignedShort())?.data?:"INVALID_ATTRIBUTE";
				String value = null;
				int attributeLength = buf.readInt();
				if (attributeName=="ConstantValue") {
					ClassConstant constValue = ClassConstant.dereference(constants, buf.readUnsignedShort());
					if (constValue!=null) value = constValue.data;
				} else {
					if ( attributeLength > 0 ) {
						byte[] attributeData = new byte[attributeLength];
						buf.read( attributeData );
					}
					attributes.add( attributeName + "(" + attributeLength + "bytes)" );
				}
			}
			//System.out.println(accessText.trim() + " " + DeclarationHelper.translateMethodDescriptor(methodDescriptor, methodName) + " //"+attributes.toListString());
		}



		int numClassAttributes = buf.readUnsignedShort();
		List<String> attributes = [];
		numClassAttributes.times {
			String attributeName = ClassConstant.dereference( constants, buf.readUnsignedShort() )?.data ?: "INVALID_ATTRIBUTE";
			String value = null;
			int attributeLength = buf.readInt();
			if ( attributeName == "SourceFile" ) {
				int sourceFileLocation = buf.readUnsignedShort();
				sourceFileName = ClassConstant.dereference(constants, sourceFileLocation).data;
			} else {
				if ( attributeLength > 0 ) {
					byte[] attributeData = new byte[attributeLength];
					buf.read( attributeData );
				}
				attributes.add( attributeName + "(" + attributeLength + "bytes)" );
			}
		}

		if (sourceFileName==null) sourceFileName = "(none)";

		humanReadable = thisclass.replace("/",".");
		if (superclass!="java/lang/Object") humanReadable += " extends " + superclass.replace("/",".");
		if (!interfaces.isEmpty()) {
			humanReadable+= " implements "+interfaces.toListString().replace("[","").replace("]","").replace("/",".");
		}

		humanReadable += " { /* "+data.length+" bytes */ }";
	}

	private final boolean hasFlag(int bitmask, int flagsField) {
		return (flagsField & bitmask) == bitmask;
	}

	@Override
	public String toString() {
		return accessModifier.toString()+" "+humanReadable;
	}

	public List<String> getClassReferences() {
		if (classReferences==null) {
			classReferences = [];
			for(ClassConstant cur : constants) {
				if (cur.constType==ClassConstantType.CLASS) {
					classReferences.add(cur.data.replace("/","."));
				}
			}
		}
		return classReferences.asImmutable();
	}

	public List<String> getStringConstants() {
		if (stringConstants==null) {
			stringConstants = [];
			for(ClassConstant cur : constants) {
				if (cur==null) continue;
				if (cur.constType==ClassConstantType.STRING) {
					stringConstants.add(cur.data);
				}
			}
		}
		return stringConstants.asImmutable();
	}

	public boolean isEnum() {
		return accessModifier.isEnum();
	}

	public boolean isInterface() {
		return accessModifier.isInterface();
	}

	public boolean isAbstract() {
		return accessModifier.isAbstract();
	}

	public boolean doesImplement(String s) {
		return interfaces.contains(s);
	}

	public String getName() {
		return thisclass.replace("/",".");
	}
}
