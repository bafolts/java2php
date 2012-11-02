package translator;

public class Parameter extends FileTranslator {

	private Type m_sType = null;
	private String m_sName = null;
	private boolean m_bUnlimited = false;

	public void setUnlimited(boolean b) {
		m_bUnlimited = b;
	}
	
	public boolean getUnlimited() {
		return m_bUnlimited;
	}

	public Parameter(String line) {
		line = line.trim();

		if(line.indexOf("...")>-1) {
			line = line.replaceAll("[ ]*\\.\\.\\.[ ]*"," ");
			setUnlimited(true);
		}

		String[] aParts = line.split(" ");

		if(aParts[1].endsWith("[]")) {
			aParts[0] += "[]";
			aParts[1] = aParts[1].substring(0,aParts[1].length()-2);
		}

		setType(new Type(aParts[0]));
		setName(aParts[1]);
	}

	public void setName(String s) {
		m_sName = s;
	}
	
	public String getName() {
		return m_sName;
	}
	
	public void setType(Type s) {
		m_sType = s;
	}
	
	public Type getType() {
		return m_sType;
	}

}