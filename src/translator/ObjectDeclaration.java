package translator;

import java.util.ArrayList;
import java.util.regex.*;

public class ObjectDeclaration extends FileTranslator {

	private Type m_tType = null;
	private ArrayList<Argument> m_aArguments = new ArrayList<Argument>();
	private ArrayDeclaration m_aDeclaration = null;
	private ClassDeclaration m_aInlineClass = null;

	public void setInlineClass(ClassDeclaration c) {
		m_aInlineClass = c;
	}
	
	public ClassDeclaration getInlineClass() {
		return m_aInlineClass;
	}

	public boolean isArrayDeclaration() {
		return m_aDeclaration!=null;
	}

	public void setArrayDeclaration(ArrayDeclaration b) {
		m_aDeclaration = b;
	}

	public ArrayDeclaration getArrayDeclaration() {
		return m_aDeclaration;
	}

	public ObjectDeclaration(String line) {

		line = line.trim();

		setContents(line);

		if(currentString().endsWith("}")) {

			setType(new Type(getUntilNext("{")));//@TODO: Should be getAndSetNextBracket()
			goToNext("{");
			setPosition(getPosition()-1);

			setArrayDeclaration(new ArrayDeclaration(currentString()));

		} else if(currentString().endsWith("]")) {
			setType(new Type(getUntilNext("[")));
			goToNext("[");
			setPosition(getPosition()-1);
			setArrayDeclaration(new ArrayDeclaration(currentString()));
		} else {

			setType(new Type(getUntilNext("(")));

			goToNext("(");

			while(moreToParse()) {
				String sArgument = getAndSetNextArgument();

				if(sArgument.length()>0) {
					m_aArguments.add(new Argument(sArgument));
				}
			}
		}

	}


	private void parseArguments(String line) {
		line = line.trim();
		m_aArguments.add(new Argument(line));
	}

	public void setType(Type t) {
		m_tType = t;
	}

	public Type getType() {
		return m_tType;
	}

	public ArrayList<Argument> getArguments() {
		return m_aArguments;
	}

}