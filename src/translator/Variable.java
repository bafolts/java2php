package translator;

public class Variable extends FileTranslator {

	private String m_sName = null;

	public Variable(String line) {
		m_sName = line;
	}

	public String getName() {
		return m_sName;
	}
	
	public void setName(String s) {
		m_sName = s;
	}

}