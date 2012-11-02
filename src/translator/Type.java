package translator;

import java.util.ArrayList;

public class Type extends FileTranslator {

	private String m_sName = null;
	private ArrayList<Type> m_aTypeList = null;
	private boolean m_bIsArray = false;

	public Type(String line) {
		setContents(line.trim());
		
		if(getContents().endsWith("[]")) {
			m_bIsArray = true;
			setName(getContents().substring(0,getContents().length()-2));
		} else if(getContents().endsWith(">")) {
			m_aTypeList = new ArrayList<Type>();
			setName(getAndSetNext("<"));

			while(currentCharacter()!='>') {
				m_aTypeList.add(new Type(getAndSetNextType()));
				if(currentCharacter()==',') {
					setPosition(getPosition()+1);
				}
			}
	
		} else {
			setName(getContents());
		}

	}
	
	public void setIsArray(boolean b) {
		m_bIsArray = b;
	}
	
	public boolean getIsArray() {
		return m_bIsArray;
	}

	public ArrayList<Type> getTypeList() {
		return m_aTypeList;
	}

	public void setName(String s) {
		m_sName = s;
	}
	
	public String getName() {
		return m_sName;
	}

}