package translator;

import java.util.ArrayList;

public class ArrayDeclaration extends FileTranslator {

	private ArrayList<Object> m_aItems = new ArrayList<Object>();

	public ArrayDeclaration(String line) {
		line = line.trim();
		setContents(line.substring(1,line.length()-1));

		while(moreToParse()) {
			if(isFiller(currentCharacter())) {
				setPosition(getPosition()+1);
			} else if(currentCharacter()=='{') {
				addItem(new ArrayDeclaration(getAndSetNextArgument()));
			} else {
				addItem(new Equals(getAndSetNextArgument()));
			}
		}
	}

	public ArrayList<Object> getItems() {
		return m_aItems;
	}

	public void addItem(Object o) {
		m_aItems.add(o);
	}

}