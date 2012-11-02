package translator;

public class PHPMethod {

	private Method m_mMethod = null;
	private String m_sName = null;
	
	public PHPMethod(Method m, String s) {
		m_mMethod = m;
		m_sName = s;
	}
	
	public Method getMethod() {
		return m_mMethod;
	}
	
	public String getName() {
		return m_sName;
	}

}