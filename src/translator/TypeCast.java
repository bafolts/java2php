package translator;

public class TypeCast extends FileTranslator {

	private Type m_sType = null;

	public TypeCast(String line) {
		setContents(line.trim());
		setType(new Type(getContents().substring(1,getContents().length()-1)));
	}

	public void setType(Type s) {
		m_sType = s;
	}

	public Type getType() {
		return m_sType;
	}

}