package translator;

import java.util.ArrayList;

import java.util.regex.*;

public class ClassDeclaration extends FileTranslator {

	private Type m_sClassName = null;
	private Type m_tExtends = null;

	private ArrayList<Constructor> m_aConstructors = new ArrayList<Constructor>();
	private ArrayList<ClassDeclaration> m_aPrivateClasses = new ArrayList<ClassDeclaration>();
	private ArrayList<PublicMethod> m_aPublicMethods = new ArrayList<PublicMethod>();
	private ArrayList<PrivateMethod> m_aPrivateMethods = new ArrayList<PrivateMethod>();
	private ArrayList<ProtectedMethod> m_aProtectedMethods = new ArrayList<ProtectedMethod>();
	private ArrayList<PrivateProperty> m_aPrivateProperties = new ArrayList<PrivateProperty>();
	private ArrayList<PublicProperty> m_aPublicProperties = new ArrayList<PublicProperty>();
	private ArrayList<ProtectedProperty> m_aProtectedProperties = new ArrayList<ProtectedProperty>();
	private ArrayList<StaticBlock> m_aStaticBlocks = new ArrayList<StaticBlock>();

	private boolean m_bPublicClass = false;
	private boolean m_bFinalClass = false;
	private boolean m_bAbstractClass = false;
	private boolean m_bInlineClass = false;
	private boolean m_bInterface = false;

	public boolean hasPrivateClass(Type t) {
		for(ClassDeclaration cd : m_aPrivateClasses) {
			if(cd.getClassName().getName().equals(t.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public ClassDeclaration getPrivateClass(Type t) {
		for(ClassDeclaration cd : m_aPrivateClasses) {
			if(cd.getClassName().getName().equals(t.getName())) {
				return cd;
			}
		}
		return null;
	}

	public ArrayList<StaticBlock> getStaticBlocks() {
		return m_aStaticBlocks;
	}

	public ArrayList<ClassDeclaration> getPrivateClasses() {
		return m_aPrivateClasses;
	}

	public Type getPropertyType(String sName) {
		for(int i=0;i<m_aPrivateProperties.size();i++) {
			if(m_aPrivateProperties.get(i).getName().equals(sName)) {
				return m_aPrivateProperties.get(i).getType();
			}
		}
		for(int i=0;i<m_aPublicProperties.size();i++) {
			if(m_aPublicProperties.get(i).getName().equals(sName)) {
				return m_aPublicProperties.get(i).getType();
			}
		}
		for(int i=0;i<m_aProtectedProperties.size();i++) {
			if(m_aProtectedProperties.get(i).getName().equals(sName)) {
				return m_aProtectedProperties.get(i).getType();
			}
		}
		return null;
	}

	private boolean currentlyClass() {
		Pattern pMethodStart = Pattern.compile("^(static )?(final )?(class )[^ ]+[ ]*");
		Matcher mMethodStart = pMethodStart.matcher(getContents().substring(getPosition()));
		return mMethodStart.find();
	}

	private static final Pattern IS_CURRENTLY_METHOD = Pattern.compile("^(((final[ \t])?(static[ \t])?)|(((static[ \t])?(final[ \t])?)))?[^ (]+[ ]+[^ (]+[ ]*\\([^)]*\\)[ ]*(throws )?[^;{]*\\{");
	private boolean currentlyMethod(String s) {
		return ClassDeclaration.IS_CURRENTLY_METHOD.matcher(s).lookingAt();
	}

	private static final Pattern IS_ABSTRACT_METHOD = Pattern.compile("^(abstract )?[^ (]+ [^ (]+[ ]*\\(");
	private boolean currentlyAbstractMethod(String s) {
		return ClassDeclaration.IS_ABSTRACT_METHOD.matcher(s).lookingAt();
	}
	
	private static final Pattern IS_CURRENTLY_CONSTRUCTOR = Pattern.compile("^[ ]*[^ ]+[ ]*[\\(]");
	private boolean currentlyConstructor(String s) {
		return ClassDeclaration.IS_CURRENTLY_CONSTRUCTOR.matcher(s).lookingAt();
	}

	private static final Pattern IS_UNDECLARED_PUBLIC = Pattern.compile("^[A-Za-z][^ ]*[ \t]*[A-Za-z][^ ]*[ \t]*[(=;]");
	private boolean isUndeclaredPublic(String s) {
		return ClassDeclaration.IS_UNDECLARED_PUBLIC.matcher(s).lookingAt();
	}
	
	public Type getExtends() {
		return m_tExtends;
	}
	
	public void setExtends(Type t) {
		m_tExtends = t;
	}

	public void setPublic(boolean m) {
		m_bPublicClass = m;
	}
	
	public boolean getPublic() {
		return m_bPublicClass;
	}

	public void setFinal(boolean f) {
		m_bFinalClass = f;
	}
	
	public boolean getFinal() {
		return m_bFinalClass;
	}
	
	public void setAbstract(boolean a) {
		m_bAbstractClass = a;
	}
	
	public boolean getAbstract() {
		return m_bAbstractClass;
	}

	public ClassDeclaration(String body) {
		setContents(body);
	
		while(moreToParse()) {
			String curString = currentString();
			if(curString.startsWith("public ")) {
				setPublic(true);
				setPosition(getPosition()+7);
			} else if(curString.startsWith("final ")) {
				setFinal(true);
				setPosition(getPosition()+6);
			} else if(curString.startsWith("abstract ")) {
				setAbstract(true);
				setPosition(getPosition()+9);
			} else if(curString.startsWith("interface ")) {
				setInterface(true);
				setPosition(getPosition()+10);
				setupClassBody();
			} else if(curString.startsWith("class ")) {
				setPosition(getPosition()+6);
				setupClassBody();
				break;
			} else if(matches(curString,"^[ ]*[{]")) { 
				setupClassBody();
				break;
			} else {
				throw new RuntimeException("Error parsing class declaration in "+body);
			}
		}

	}

	private void setupClassBody() {

		if(!matches(currentString(),"^[ ]*[{]")) {
			setClassName(new Type(getAndSetNextType()));

			while(isFiller(currentCharacter())) {
				setPosition(getPosition()+1);
			}

			if(currently("extends ")) {
				goToNext(" ");
				setExtends(new Type(getAndSetNextType()));
			} else if(!getClassName().getName().equals("Object")) {
				setExtends(new Type("java.lang.Object"));
			}

		}

		goToNext("{");

		boolean abstractFound = false;
		boolean finalFound = false;
		boolean staticFound = false;

		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			
			if(isFiller(curChar)) {
				setPosition(getPosition()+1);
			} else if(curString.startsWith("@Deprecated")) {
				setPosition(getPosition()+11);
			} else if(curString.startsWith("@SuppressWarnings")) {
				getAndSetNextParen();
			} else if(curString.startsWith("abstract ")) {
				abstractFound = true;
				setPosition(getPosition()+9);
			} else if(curString.startsWith("final ")) {
				finalFound = true;
				setPosition(getPosition()+6);
			} else if(curString.startsWith("static ")||curString.startsWith("static{")) {
				staticFound = true;
				setPosition(getPosition()+6);
				if(matches(currentString(),"^[ ]*[{]")) {
					m_aStaticBlocks.add(new StaticBlock(getAndSetNextBlock()));
					staticFound = false;
				}
			} else if(curString.startsWith("private ")) {
				setPosition(getPosition()+8);
				curString = curString.substring(8);
				if(currentlyClass()) {
					goToNext("class ");
					setPosition(getPosition()-1);
					m_aPrivateClasses.add(new ClassDeclaration(getAndSetNextBlock()));
				} else if(currentlyMethod(curString)) {
					PrivateMethod privateMethod = new PrivateMethod(getAndSetNextBlock());
					if(!privateMethod.getStatic())
						privateMethod.setStatic(staticFound);
					m_aPrivateMethods.add(privateMethod);
				} else if(currentlyAbstractMethod(curString)) {
					m_aPrivateMethods.add(new PrivateMethod(getAndSetNextSemi()));
				} else {
					m_aPrivateProperties.add(new PrivateProperty(getAndSetNextSemi()));
				}
				abstractFound = false;
				finalFound = false;
				staticFound = false;
			} else if(curString.startsWith("public ")||isUndeclaredPublic(curString)) {
				if(curString.startsWith("public ")) {
					setPosition(getPosition()+7);
					curString = curString.substring(7);
				}
				if(currentlyClass()) {
					goToNext("class ");
					setPosition(getPosition()-1);
					m_aPrivateClasses.add(new ClassDeclaration(getAndSetNextBlock()));
				} else if(curString.startsWith("enum ")) {
					PublicProperty publicProperty = new PublicProperty(getAndSetNextBlock());
					publicProperty.setFinal(true);
					publicProperty.setStatic(true);
					m_aPublicProperties.add(publicProperty);
				} else if(currentlyMethod(curString)) {
					PublicMethod publicMethod = new PublicMethod(getAndSetNextBlock());
					if(!publicMethod.getFinal())
						publicMethod.setFinal(finalFound);
					if(!publicMethod.getStatic())
						publicMethod.setStatic(staticFound);
					m_aPublicMethods.add(publicMethod);
				} else if(abstractFound||currentlyAbstractMethod(curString)) {
					m_aPublicMethods.add(new PublicMethod(getAndSetNextSemi()));
				} else if(currentlyConstructor(curString)) {
					m_aConstructors.add(new Constructor(getAndSetNextBlock()));
				} else {
					PublicProperty publicProperty = new PublicProperty(getAndSetNextSemi());
					if(!publicProperty.getFinal())
						publicProperty.setFinal(finalFound);
					if(!publicProperty.getStatic())
						publicProperty.setStatic(staticFound);
					m_aPublicProperties.add(publicProperty);
				}
				abstractFound = false;
				finalFound = false;
				staticFound = false;
			} else if(curString.startsWith("protected ")) {
				setPosition(getPosition()+10);
				curString = curString.substring(10);
				if(currentlyMethod(curString)) {
					ProtectedMethod protectedMethod = new ProtectedMethod(getAndSetNextBlock());
					if(!protectedMethod.getFinal())
						protectedMethod.setFinal(finalFound);
					if(!protectedMethod.getStatic())
						protectedMethod.setStatic(staticFound);
					m_aProtectedMethods.add(protectedMethod);
				} else if(currentlyAbstractMethod(curString)) {
					m_aProtectedMethods.add(new ProtectedMethod(getAndSetNextSemi()));
				} else {
					ProtectedProperty protectedProperty = new ProtectedProperty(getAndSetNextSemi());
					if(!protectedProperty.getFinal())
						protectedProperty.setFinal(finalFound);
					if(!protectedProperty.getStatic())
						protectedProperty.setStatic(staticFound);
					m_aProtectedProperties.add(protectedProperty);
				}
				abstractFound = false;
				finalFound = false;
				staticFound = false;
			} else if(curString.startsWith("enum ")) {
				m_aPublicProperties.add(new PublicProperty(getAndSetNextBlock()));
			} else if(currentlyConstructor(curString)) {
				m_aConstructors.add(new Constructor(getAndSetNextBlock()));
			} else if(currentlyMethod(curString)) {
				m_aPublicMethods.add(new PublicMethod(getAndSetNextBlock()));
			} else if(curChar=='}') {
				setPosition(getPosition()+1);
				break;
			} else {
				System.err.println("Unknown in ClassDeclaration for "+getClassName().getName()+": "+currentString());
				setErrorParsing(true);
			}
		}
	}
	
	public void setClassName(Type s) {
		m_sClassName = s;
	}
	
	public Type getClassName() {
		return m_sClassName;
	}

	public void addConstructor(Constructor c) {
		m_aConstructors.add(c);
	}
	
	public ArrayList<Constructor> getConstructors() {
		return m_aConstructors;
	}
	
	public void addPublicMethod(PublicMethod p) {
		m_aPublicMethods.add(p);
	}
	
	public void addPrivateMethod(PrivateMethod p) {
		m_aPrivateMethods.add(p);
	}
	
	public ArrayList<PublicMethod> getPublicMethods() {
		return m_aPublicMethods;
	}
	
	public ArrayList<PrivateMethod> getPrivateMethods() {
		return m_aPrivateMethods;
	}

	public void addPrivateProperty(PrivateProperty p) {
		m_aPrivateProperties.add(p);
	}
	
	public ArrayList<PrivateProperty> getPrivateProperties() {
		return m_aPrivateProperties;
	}

	public ArrayList<PublicProperty> getPublicProperties() {
		return m_aPublicProperties;
	}

	public ArrayList<ProtectedProperty> getProtectedProperties() {
		return m_aProtectedProperties;
	}

	public boolean getInline() {
		return m_bInlineClass;
	}

	public void setInline(boolean inline) {
		m_bInlineClass = inline;
	}

	public ArrayList<ProtectedMethod> getProtectedMethods() {
		return m_aProtectedMethods;
	}

	public void setInterface(boolean b) {
		m_bInterface = b;
	}
	
	public boolean getInterface() {
		return m_bInterface;
	}

}