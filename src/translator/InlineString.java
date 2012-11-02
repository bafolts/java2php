package translator;

public class InlineString {

	private String m_sValue = null;

	public InlineString(String line) {
		setValue(line);
	}

	public void setValue(String s) {
		m_sValue = s;
	}
	
	public String getValue() {
		return m_sValue;
	}

}