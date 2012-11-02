package translator;

import java.util.ArrayList;
public class FunctionCall extends FileTranslator {

	private String m_sName = null;
	private ArrayList<Argument> m_aArguments = new ArrayList<Argument>();
	private FunctionCall m_fNextCall = null;

	public FunctionCall() {
	
	}

	public FunctionCall(String line) {
		setContents(line);

		setName(getAndSetNext("("));

		while(moreToParse()) {
			String sArgument = getAndSetNextArgument();
			if(sArgument.length()>0) {
				m_aArguments.add(new Argument(sArgument));
			}
		}

	}
	
	public FunctionCall getNextCall() {
		return m_fNextCall;
	}

	public void setNextCall(FunctionCall c) {
		m_fNextCall = c;
	}
	
	public boolean equals(Method m) {
		if(m.getName().equals(getName())||getName().endsWith("."+m.getName())) {
			return true;
		} else {
			return false;
		}
	}

	public void setName(String s) {
		m_sName = s;
	}
	
	public String getName() {
		return m_sName;
	}
	
	public ArrayList<Argument> getArguments() {
		return m_aArguments;
	}

	public void setArguments(ArrayList<Argument> args) {
		m_aArguments = args;
	}

}