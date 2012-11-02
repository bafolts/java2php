package translator;

public abstract class Property extends FileTranslator {

	private String m_sName = null;
	private Type m_sType = null;
	private Equals m_eEqual = null;
	private boolean m_bStatic = false;
	private boolean m_bFinal = false;

	public Property(String line) {
		setContents(line);
		
		while(currently("static ")||currently("final ")) {
			if(currently("static ")) {
				setStatic(true);
				setPosition(getPosition()+7);
			} else if(currently("final ")) {
				setFinal(true);
				setPosition(getPosition()+6);
			}
		}

		setType(new Type(getAndSetNextType()));
		setName(getAndSetNextVariable());

		if(getType().getName().equals("enum")) {
			Equals e = new Equals();
			EnumDeclaration en = new EnumDeclaration(currentString());
			en.setName(getName());
			e.addValue(en);
			setEquals(e);
		} else {
			if(getContents().indexOf("=")!=-1) {
				goToNext("=");
				setEquals(new Equals(currentString()));
			}
		}
	}

	public boolean getStatic() {
		return m_bStatic;
	}

	public void setStatic(boolean bStatic) {
		m_bStatic = bStatic;
	}
	
	public void setFinal(boolean bFinal) {
		m_bFinal = bFinal;
	}
	
	public boolean getFinal() {
		return m_bFinal;
	}

	public void setName(String sName) {
		m_sName = sName;
	}
	
	public void setType(Type sType) {
		m_sType = sType;
	}
	
	public String getName() {
		return m_sName;
	}
	
	public Type getType() {
		return m_sType;
	}

	public void setEquals(Equals e) {
		m_eEqual = e;
	}

	public Equals getEquals() {
		return m_eEqual;
	}

}