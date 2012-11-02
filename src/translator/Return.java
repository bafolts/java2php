package translator;

public class Return extends FileTranslator {

	private Equals m_oValue = null;
	private Try m_tParentTry = null;
	private Catch m_tParentCatch = null;
	private Finally m_tParentFinally = null;

	public Return(String line) {
		line = line.trim().substring(6);
		if(line.length()>0)
			setEquals(new Equals(line));
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