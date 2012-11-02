package translator;

public class Import {

	private String m_sPath = null;

	public Import(String line) {
		line = line.substring(7);
		m_sPath = line.trim();
	}

	public String getPath() {
		return m_sPath;
	}

}