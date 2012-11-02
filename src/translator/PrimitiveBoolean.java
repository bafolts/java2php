package translator;

public class PrimitiveBoolean {

	private boolean m_bValue;

	public PrimitiveBoolean(boolean bType) {
		setValue(bType);
	}
	
	public void setValue(boolean b) {
		m_bValue = b;
	}
	
	public boolean getValue() {
		return m_bValue;
	}

}