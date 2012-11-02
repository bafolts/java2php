package translator;

public class Label extends FileTranslator {

	private String m_sName = null;

	public Label(String line) {
		line = line.trim();
		setContents(line);

		setName(getContents());

	}

	public void setName(String s) {
		m_sName = s;
	}

	public String getName() {
		return m_sName;
	}

}