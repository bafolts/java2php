package translator;

import java.util.regex.*;
import java.util.ArrayList;

public class ForArgument extends FileTranslator {

	private ArrayList<Object> m_aLines = new ArrayList<Object>();
	private boolean m_bIterator = false;
	private Parameter m_pParameter = null;
	private Equals m_eEquals = null;

	private static final Pattern IS_ASSIGNMENT = Pattern.compile("^[ ]*[^ ({+]*[ ]*[^ ({+<>]+[ ]*=");

	public void setParameter(Parameter p) {
		m_pParameter = p;
	}
	
	public void setEquals(Equals e) {
		m_eEquals = e;
	}
	
	public Equals getEquals() {
		return m_eEquals;
	}

	public Parameter getParameter() {
		return m_pParameter;
	}

	public void setIterator(boolean i) {
		m_bIterator = i;
	}
	
	public boolean getIterator() {
		return m_bIterator;
	}

	public ForArgument(String line) {
		line = line.trim();

		setContents(line);

		goToNext("(");

		if(currentString().indexOf(";")>-1) {
			addLine(getAndSetNextSemi());
			addLine(getAndSetNextSemi());
			addLine(getContents().substring(getPosition(),getContents().length()-1));
		} else {
			setIterator(true);
			String[] aParts = currentString().split(":");
			setParameter(new Parameter(aParts[0]));
			setEquals(new Equals(aParts[1].substring(0,aParts[1].length()-1)));
		}

	}
	
	private void addLine(String s) {
		if(isAssignment(s)) {
			m_aLines.add(new Assignment(s));
		} else {
			m_aLines.add(new Equals(s));
		}
	}

	public ArrayList<Object> getLines() {
		return m_aLines;
	}

	public boolean isAssignment(String s) {
		return ForArgument.IS_ASSIGNMENT.matcher(s).lookingAt();
	}

}