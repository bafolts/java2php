package translator;

public class Assignment extends FileTranslator {

	private Variable m_vVariable = null;
	private Type m_tType = null;
	private Equals m_eEqual = null;

	public Assignment(Parameter p) {
		setType(p.getType());
		setVariable(new Variable(p.getName()));
	}

	public Assignment() {
	
	}

	public Assignment(String line) {

		line = line.trim();
		setContents(line);
		
		if(currentString().indexOf("=")!=-1) {
			String sStart = getAndSetNext("=").trim();

			if(sStart.indexOf(" ")!=-1) {
				String[] aParts = sStart.split(" ");
				setType(new Type(aParts[0]));
				if(aParts[1].endsWith("[]")) {
					aParts[1] = aParts[1].substring(0,aParts[1].length()-2);
					m_tType.setIsArray(true);
				}
				setVariable(new Variable(aParts[1]));
			} else {
				setVariable(new Variable(sStart));
			}
			
			if(matches(currentString(),"^[ ]*[{]")) {
				Equals e = new Equals();
				e.addValue(new ArrayDeclaration(getAndSetNextBlock()));
				setEquals(e);
			} else {
				setEquals(new Equals(currentString()));
			}
		} else {
			String sStart = getContents();

			if(sStart.indexOf(" ")!=-1) {
				String[] aParts = sStart.split(" ");
				setType(new Type(aParts[0]));
				if(aParts[1].endsWith("[]")) {
					aParts[1] = aParts[1].substring(0,aParts[1].length()-2);
					m_tType.setIsArray(true);
				}
				setVariable(new Variable(aParts[1]));
			} else {
				setVariable(new Variable(sStart));
			}
		}
	}

	public void setVariable(Variable sName) {
		m_vVariable = sName;
	}
	
	public Variable getVariable() {
		return m_vVariable;
	}
	
	public void setType(Type sType) {
		m_tType = sType;
	}
	
	public Type getType() {
		return m_tType;
	}

	public void setEquals(Equals e) {
		m_eEqual = e;
	}

	public Equals getEquals() {
		return m_eEqual;
	}

}