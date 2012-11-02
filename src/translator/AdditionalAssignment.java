package translator;

public class AdditionalAssignment extends FileTranslator {

	private Assignment m_aAssignment = null;

	public AdditionalAssignment(String line) {
		line = line.trim();
		setContents(line);
		setPosition(getPosition()+1);
		
		setAssignment(new Assignment(currentString()));

	}

	public void setAssignment(Assignment a) {
		m_aAssignment = a;
	}
	
	public Assignment getAssignment() {
		return m_aAssignment;
	}

}