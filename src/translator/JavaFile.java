package translator;

import java.util.ArrayList;

public class JavaFile extends FileTranslator {

	private ClassDeclaration m_mainClass = null;
	private Package m_sPackage = null;
	private ArrayList<Import> m_aImports = new ArrayList<Import>();

	public JavaFile(String contents) {
		setContents(contents);

		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(isFiller(curChar)) {
				setPosition(getPosition()+1);
			} else if(curString.startsWith("public ")||curString.startsWith("class ")||curString.startsWith("final ")||curString.startsWith("abstract ")) {
				setMainClass(new ClassDeclaration(getAndSetNextBlock()));
				continue;
			} else if(curString.startsWith("import ")) {
				addImport(new Import(getAndSetNextSemi()));
				continue;
			} else if(curString.startsWith("package ")) {
				setPackage(new Package(getAndSetNextSemi()));
				continue;
			} else {
				System.err.println("Error Parsing Java File: "+curString);
				break;
			}
		}

	}
	
	public ClassDeclaration getMainClass() {
		return m_mainClass;
	}

	public void setMainClass(ClassDeclaration cd) {
		m_mainClass = cd;
	}
	
	public void setPackage(Package p) {
		m_sPackage = p;
	}
	
	public Package getPackage() {
		return m_sPackage;
	}
	
	public void addImport(Import s) {
		m_aImports.add(s);
	}
	
	public ArrayList<Import> getImports() {
		return m_aImports;
	}

}