package translator;

import java.util.ArrayList;

public class JavaInterpreter {

	public final boolean isEnum(Type t) {
		return t.getName().equals("enum");
	}

	public final boolean isPrimitiveType(Type t) {
		String sName = t.getName();
		if(sName.indexOf("[")!=-1) {
			sName = sName.substring(0,sName.indexOf("["));
		}
		return
			sName.equals("long")||
			sName.equals("void")||//not an actual primitive type, but can be returned
			sName.equals("boolean")||
			sName.equals("int")||
			sName.equals("short")||
			sName.equals("char")||
			sName.equals("double")||
			sName.equals("float")||
			sName.equals("byte");
	}

	public boolean isMainClass(ClassDeclaration c) {
		ArrayList<PublicMethod> aPublicMethods = c.getPublicMethods();
		for(int i=0,length=aPublicMethods.size();i<length;i++) {
			PublicMethod p = aPublicMethods.get(i);
			if(p.getStatic()&&p.getName().equals("main")) {
				return true;
			}
		}
		return false;
	}

	public boolean hasStaticProperty(ClassDeclaration c) {
		for(PrivateProperty pp : c.getPrivateProperties()) {
			if(pp.getStatic()) {
				return true;
			}
		}
		for(ProtectedProperty pp : c.getProtectedProperties()) {
			if(pp.getStatic()) {
				return true;
			}
		}
		for(PublicProperty pp : c.getPublicProperties()) {
			if(pp.getStatic()) {
				return true;
			}
		}
		return false;
	}

	private Type replaceGenerics(ArrayList<Type> baseTypeList,Type returnType,Type currentType) {

		if(baseTypeList!=null) {

			if(currentType!=null&&currentType.getTypeList()!=null) {

				for(int i=0,length=baseTypeList.size();i<length;i++) {
					if(returnType.getName().equals(baseTypeList.get(i).getName())) {
						return currentType.getTypeList().get(i);
					}
				}

				if(returnType.getTypeList()!=null) {
					for(int i=0,length=returnType.getTypeList().size();i<length;i++) {
						returnType.getTypeList().add(i,replaceGenerics(baseTypeList,returnType.getTypeList().remove(i),currentType));
					}
				}

			} else {
				//if they did not provide a generic type list for an interface
				//then we don't know what type they want and return Object
				return new Type("java.lang.Object");
			}

		}

		return returnType;
	}

	public final Type getMethodType(FunctionCall f,ClassDeclaration cd,Type at) {

		for(PublicMethod pm : cd.getPublicMethods()) {
			if(f.equals(pm)) {
				return replaceGenerics(cd.getClassName().getTypeList(),pm.getType(),at);
			}
		}

		for(PrivateMethod pm : cd.getPrivateMethods()) {
			if(f.equals(pm)) {
				return replaceGenerics(cd.getClassName().getTypeList(),pm.getType(),at);
			}
		}

		for(ProtectedMethod pm : cd.getProtectedMethods()) {
			if(f.equals(pm)) {
				return replaceGenerics(cd.getClassName().getTypeList(),pm.getType(),at);
			}
		}

		return null;

	}

}