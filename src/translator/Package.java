package translator;

public class Package {

	private String m_sPath = null;

	public Package(String line) {
		m_sPath = line.substring(8).trim();
	}

	public String getPath() {
		return m_sPath;
	}

}