package translator;

import java.util.ArrayList;

public class Throws extends FileTranslator {

	private ArrayList<Type> m_aTypes = new ArrayList<Type>(0);

	public Throws(String line) {

		line = line.substring(7).trim();

		String[] aTypes = line.split(",");
		for(int i=0;i<aTypes.length;i++) {
			m_aTypes.add(new Type(aTypes[i]));
		}

	}
	
	public ArrayList<Type> getTypes() {
		return m_aTypes;
	}

}