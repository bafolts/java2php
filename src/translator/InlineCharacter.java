package translator;


public class InlineCharacter extends FileTranslator {

	private String m_sValue = null;

	public InlineCharacter(String line) {
		setContents(line);
		setValue(line);
	}
	
	public void setValue(String s) {
		m_sValue = s;
	}
	
	public String getValue() {
		return m_sValue;
	}
	
}