package translator;

public class InlineNumber {

	private String m_sValue = null;
	private Type m_tType = null;

	public InlineNumber(String line) {
		line = line.trim();
		if(line.indexOf("x")==-1) {
			if(line.endsWith("F")||line.endsWith("f")) {
				setType(new Type("float"));
				line = line.substring(0,line.length()-1);
			} else if(line.endsWith("D")||line.endsWith("d")) {
				setType(new Type("double"));
				line = line.substring(0,line.length()-1);
			} else if(line.endsWith("L")||line.endsWith("l")) {
				setType(new Type("long"));
				line = line.substring(0,line.length()-1);
			}
		}
		setValue(line);
	}

	public void setType(Type t) {
		m_tType = t;
	}

	public void setValue(String s) {
		m_sValue = s;
	}
	
	public String getValue() {
		return m_sValue;
	}

}