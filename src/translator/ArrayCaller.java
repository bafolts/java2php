package translator;

public class ArrayCaller extends FileTranslator {

	private Equals m_sEquals = null;

	public ArrayCaller(String line) {
		line = line.trim();
		setEquals(new Equals(line.substring(1,line.length()-1)));

	}

	public void setEquals(Equals e) {
		m_sEquals = e;
	}

	public Equals getEquals() {
		return m_sEquals;
	}

}