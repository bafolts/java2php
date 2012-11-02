package translator;

import java.util.ArrayList;

public abstract class Method extends FileTranslator {

	private String m_sName = null;
	private Type m_sType = null;
	private boolean m_bStatic = false;
	private boolean m_bAbstract = false;
	private boolean m_bFinal = false;
	private ArrayList<Parameter> m_aParameters = new ArrayList<Parameter>();
	private BlockBody m_bBody = null;
	private Throws m_tThrows = null;

	public Method() {
	
	}
	
	public void setThrows(Throws t) {
		m_tThrows = t;
	}

	public Throws getThrows() {
		return m_tThrows;
	}
	
	public void setFinal(boolean b) {
		m_bFinal = b;
	}

	public boolean getFinal() {
		return m_bFinal;
	}

	public Method(String line) {
		line = line.trim();
		setContents(line);

		while(true) {
			String curString = currentString();
			if(curString.startsWith("final ")) {
				setPosition(getPosition()+6);
				setFinal(true);
			} else if(curString.startsWith("static ")) {
				setPosition(getPosition()+7);
				setStatic(true);
			} else if(curString.startsWith("abstract ")) {
				setPosition(getPosition()+9);
				setAbstract(true);
			} else {
				break;
			}
		}

		String sStart = getUntilNext("(").trim();

		String[] aStart = sStart.split("[ ]+");
		setType(new Type(aStart[0]));

		setName(aStart[1]);

		goToNext("(");

		StringBuilder sB = new StringBuilder();
		int iDepth = 0;
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(curChar=='<') {
				iDepth++;
				sB.append(curChar);
				setPosition(getPosition()+1);
			} else if(curChar=='>') {
				iDepth--;
				sB.append(curChar);
				setPosition(getPosition()+1);
			} else if(iDepth==0&&curChar==',') {
				m_aParameters.add(new Parameter(sB.toString()));
				setPosition(getPosition()+1);
				sB = new StringBuilder();
			} else if(curChar==')') {
				if(sB.length()>0) {
					m_aParameters.add(new Parameter(sB.toString()));
				}
				setPosition(getPosition()+1);
				break;
			} else {
				setPosition(getPosition()+1);
				sB.append(curChar);
			}
		}

		while(moreToParse()) {
			String curString = currentString();
			char curChar = currentCharacter();
			if(isFiller(curChar)) {
				setPosition(getPosition()+1);
			} else if(curString.startsWith("throws ")) {
				String sThrows = getUntilNext("{");
				setThrows(new Throws(sThrows));
				setPosition(getPosition()+sThrows.length());
			} else if(curChar=='{'||curChar==';') {
				break;
			} else {
				System.err.println("Error Parsing Method");
			}
		}

		if(!getAbstract())
			this.m_bBody = new BlockBody(currentString());

	}

	public void setAbstract(boolean b) {
		m_bAbstract = b;
	}
	
	public boolean getAbstract() {
		return m_bAbstract;
	}

	public void setStatic(boolean b) {
		m_bStatic = b;
	}
	
	public boolean getStatic() {
		return m_bStatic;
	}

	public void setBlockBody(BlockBody b) {
		m_bBody = b;
	}

	public BlockBody getBlockBody() {
		return m_bBody;
	}
	
	public void setName(String s) {
		if(s.equals("exit")) {
			System.err.println("Naming a method of a class 'exit' may cause PHP parse error.");
		}
		m_sName = s;
	}

	public String getName() {
		return m_sName;
	}
	
	public void setType(Type s) {
		m_sType = s;
	}
	
	public Type getType() {
		return m_sType;
	}
	
	public void addParameter(Parameter p) {
		m_aParameters.add(p);
	}
	
	public ArrayList<Parameter> getParameters() {
		return m_aParameters;
	}

}