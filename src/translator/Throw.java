package translator;

public class Throw extends FileTranslator {

	private Equals m_oValue = null;
	private Try m_tParentTry = null;
	private Catch m_tParentCatch = null;
	private Finally m_tParentFinally = null;

	public Throw(String line) {
		m_oValue = new Equals(line.trim().substring(6));
	}
	
	public Equals getEquals() {
		return m_oValue;
	}
	
	public void setEquals(Equals o) {
		m_oValue = o;
	}

	public void setParentTry(Try t) {
		m_tParentTry = t;
	}
	
	public Try getParentTry() {
		return m_tParentTry;
	}

	public void setParentCatch(Catch c) {
		m_tParentCatch = c;
	}
	
	public Catch getParentCatch() {
		return m_tParentCatch;
	}

	public void setParentFinally(Finally f) {
		m_tParentFinally = f;
	}
	
	public Finally getParentFinally() {
		return m_tParentFinally;
	}

}