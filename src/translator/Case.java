package translator;

public class Case extends FileTranslator {

	private Equals m_eEquals = null;

	public Case(String line) {
		setEquals(new Equals(line.trim().substring(5)));
	}
	
	public void setEquals(Equals e) {
		m_eEquals = e;
	}
	
	public Equals getEquals() {
		return m_eEquals;
	}

}