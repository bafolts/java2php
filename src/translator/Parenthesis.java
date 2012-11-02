package translator;

public class Parenthesis extends FileTranslator {

	private Equals m_eEquals = null;

	public Parenthesis(String line) {
		line = line.trim();
		setEquals(new Equals(line.substring(1,line.length()-1)));
	
	}

	public Parenthesis() {
	
	}

	public void setEquals(Equals e) {
		m_eEquals = e;
	}

	public Equals getEquals() {
		return m_eEquals;
	}

}