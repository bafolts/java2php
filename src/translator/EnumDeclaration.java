package translator;

import java.util.ArrayList;

public class EnumDeclaration extends FileTranslator {

	private String m_sName = null;
	private ArrayList<String> m_sConstants = new ArrayList<String>();

	public ArrayList<String> getConstants() {
		return m_sConstants;
	}
	
	public void setConstants(ArrayList<String> constants) {
		m_sConstants = constants;
	}

	public void setName(String s) {
		m_sName = s;
	}
	
	public String getName() {
		return m_sName;
	}

	public EnumDeclaration(String line) {
		line = line.trim();
		line = line.substring(1,line.length()-1);
		setContents(line.trim());
		while(moreToParse()) {
			m_sConstants.add(getAndSetNextArgument());
		}
	}

}