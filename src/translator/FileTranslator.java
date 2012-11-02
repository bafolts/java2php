package translator;

import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FileTranslator {

	protected String s_Filename = null;
	private boolean b_errorParsing = false;
	private String m_sContents = null;
	private int i_currentPos = 0;
	private int m_iLength;

	private final Pattern PATTERN_NEXT_VARIABLE = Pattern.compile("^[ \t]*[A-Za-z][^\\[ =\\(\\+\\-\\)\\&\\<\\|\\>\\!\\:\\?,*\\/\\^\\~\\%]*");
	private final Pattern PATTERN_NEXT_NUMBER = Pattern.compile("(-)?[0-9][0-9L]*([.][0-9L]+)?");

	public FileTranslator() {
	
	}

	public FileTranslator(String fileName) {
		s_Filename = fileName;
		setContents(readFile());
	}

	protected String readFile() {
		StringBuilder sContents = new StringBuilder(10000);
		try {
			FileInputStream fstream = new FileInputStream(s_Filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String s = "";
			char[] contents = new char[2048];
			int iRead = 0;
			while((iRead = br.read(contents,0,2048))!=-1) {
				sContents.append(contents,0,iRead);
			}
			fstream.close();
		} catch (IOException e) {
			System.err.println("Error reading file: "+e.getMessage());
			setErrorParsing(true);
		}
		return sContents.toString();
	}

	protected void RemoveComments() {
		boolean bFoundStartBlock = false;
		boolean bInString = false;
		int bStartBlockPosition = 0;
		StringBuilder sb = new StringBuilder(m_sContents.length());
		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(!bFoundStartBlock&&!bInString&&curChar=='"') {
				bInString = true;
				sb.append("\"");
				i_currentPos++;
			} else if(bInString&&curChar=='\\') {
				sb.append(m_sContents.substring(i_currentPos,i_currentPos+2));
				i_currentPos+=2;
			} else if(bInString&&curChar=='"') {
				bInString = false;
				sb.append("\"");
				i_currentPos++;
			} else if(!bInString&&!bFoundStartBlock&&curChar=='\'') {
				sb.append(getAndSetNextChar());
			} else if(!bInString&&!bFoundStartBlock&&curChar=='/'&&curString.startsWith("/*")) {
				bStartBlockPosition = i_currentPos;
				bFoundStartBlock = true;
				i_currentPos+=2;
			} else if(bFoundStartBlock&&!bInString&&curChar=='*'&&curString.startsWith("*/")) {
				bFoundStartBlock = false;
				i_currentPos+=2;
			} else if(!bFoundStartBlock&&!bInString&&curChar=='/'&&curString.startsWith("//")) {
				int iNextPosition = m_sContents.indexOf("\n",i_currentPos);
				i_currentPos = iNextPosition;
			} else if(!bInString&&!bFoundStartBlock&&(curChar=='<'||curChar==','||curChar=='>')) {
				sb.append(curChar);
				i_currentPos++;
				if(curChar==','||curChar=='>') {
					int iStart = i_currentPos;
					i_currentPos -= 2;
					while(currentCharacter()==' ') {
						i_currentPos--;
					}
					if(iStart>i_currentPos+2) {
						sb = sb.delete(sb.length() - 1 - (iStart - 2 - i_currentPos),sb.length() - 1);
					}
					i_currentPos = iStart;
				}
				if(curChar=='<'||curChar==',') {
					while(currentCharacter()==' ') {
						i_currentPos++;
					}
				}
			} else if(!bInString&&(curChar=='\n'||curChar=='\r')) {
				i_currentPos++;
			} else {
				if(!bFoundStartBlock) {
					sb.append(curChar);
				}
				i_currentPos++;
			}
		}
		setContents(sb.toString());
		i_currentPos = 0;
	}

    public JavaFile translate() {

		i_currentPos = 0;
		RemoveComments();
		return new JavaFile(getContents());
    }

	public boolean isFiller(char s) {
		return s==' '||s=='\t'||s=='\n'||s=='\r';
	}

	public boolean moreToParse() {
		return !b_errorParsing && i_currentPos < m_iLength;
	}

	public boolean currently(String s) {
		return currentString().startsWith(s);
	}

	public boolean matches(String s,String reg) {
		return Pattern.compile(reg).matcher(s).find();
	}

	public String getAndSetNextType() {

		StringBuilder sOut = new StringBuilder(10);
		boolean bFoundStartBlock = false;
		int iDepth = 0;
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(!bFoundStartBlock&&curChar=='<') {
				bFoundStartBlock = true;
			} else if(bFoundStartBlock&&curChar=='<') {
				iDepth++;
			} else if(bFoundStartBlock&&curChar=='>'&&iDepth>0) {
				iDepth--;
			} else if(bFoundStartBlock&&curChar=='>'&&iDepth==0) {
				i_currentPos++;
				sOut.append(curChar);
				break;
			} else if(bFoundStartBlock&&curChar==',') {
				//commas are acceptable inside generic type list
			} else if(matches(currentString(),"^[ >,=\\(\\+\\-\\)\\&|]")) {
				break;
			}
			i_currentPos++;
			sOut.append(curChar);
		}
		return sOut.toString();
	}

	public String getAndSetNextVariable() {
		Matcher mEndName = PATTERN_NEXT_VARIABLE.matcher(currentString());
		mEndName.find();
		i_currentPos += mEndName.end();
		return mEndName.group().trim();
	}
	
	public String getAndSetNextNumber() {
		Matcher mEndName = PATTERN_NEXT_NUMBER.matcher(currentString());
		mEndName.find();
		i_currentPos += mEndName.end();
		return mEndName.group().trim();	
	}

	public void setContents(String contents) {
		m_sContents = contents;
		m_iLength = contents.length();
	}
	
	public void setErrorParsing(boolean b) {
		b_errorParsing = b;
	}
	
	public String getContents() {
		return m_sContents;
	}

	public void goToNext(String s) {
		i_currentPos = m_sContents.indexOf(s,i_currentPos)+1;
	}
	
	public String getAndSetNext(String s) {
		String curString = currentString();
		int iIndex = curString.indexOf(s);
		if(iIndex==-1) {
			return curString;
		} else {
			i_currentPos = i_currentPos + iIndex + 1;
			return curString.substring(0,iIndex);
		}
	}
	
	public String getUntilNext(String s) {
		String curString = currentString();
		int iIndex = curString.indexOf(s);
		if(iIndex==-1) {
			return curString;
		} else {
			return curString.substring(0,iIndex);
		}
	}

	public void setPosition(int position) {
		i_currentPos = position;
	}
	
	public int getPosition() {
		return i_currentPos;
	}

	public String getAndSetNextArgument() {
		StringBuilder sOut = new StringBuilder(10);
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(curChar=='"') {
				sOut.append(getAndSetNextString());
			} else if(curChar=='\'') {
				sOut.append(getAndSetNextChar());
			} else if(curChar=='(') {
				sOut.append(getAndSetNextParen());
			} else if(curChar==',') {
				i_currentPos++;
				break;
			} else if(curChar==')') {
				i_currentPos++;
				break;
			} else {
				sOut.append(curChar);
				i_currentPos++;
			}
		}
		return sOut.toString();
	}

	public String getAndSetNextSquare() {
		int iStartPos = i_currentPos;
		boolean bFoundOpener = false;
		boolean bInString = false;
		int iDepth = 0;
		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(!bFoundOpener&&curChar=='[') {
				bFoundOpener = true;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar=='[') {
				iDepth++;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar==']') {
				iDepth--;
				i_currentPos++;
				if(iDepth==-1) {
					break;
				}
			} else if(!bInString&&curChar=='\'') {
				getAndSetNextChar();
			} else if(!bInString&&curChar=='"') {
				bInString=true;
				i_currentPos++;
			} else if(bInString&&curChar=='\\') {
				i_currentPos = i_currentPos + 2;
			} else if(bInString&&curChar=='"') {
				bInString = false;
				i_currentPos++;
			} else {
				i_currentPos++;
			}
		}
		return getContents().substring(iStartPos,i_currentPos);
	}

	public String getAndSetNextColon() {
		StringBuilder sOut = new StringBuilder(10);
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(curChar=='"') {
				sOut.append(getAndSetNextString());
			} else if(curChar=='\'') {
				sOut.append(getAndSetNextChar());
			} else if(curChar=='(') {
				sOut.append(getAndSetNextParen());
			} else if(curChar==':') {
				i_currentPos++;
				break;
			} else {
				sOut.append(curChar);
				i_currentPos++;
			}
		}
		return sOut.toString();
	}

	public String getAndSetNextSemi() {
		StringBuilder sOut = new StringBuilder(10);
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(curChar=='"') {
				sOut.append(getAndSetNextString());
			} else if(curChar=='{') {
				sOut.append(getAndSetNextBlock());
			} else if(curChar=='\'') {
				sOut.append(getAndSetNextChar());
			} else if(curChar=='(') {
				sOut.append(getAndSetNextParen());
			} else if(curChar==';') {
				i_currentPos++;
				break;
			} else {
				sOut.append(curChar);
				i_currentPos++;
			}
		}
		return sOut.toString();
	}

	public String getAndSetNextChar() {
		StringBuilder sOut = new StringBuilder();
		sOut.append(currentCharacter());
		i_currentPos++;
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(curChar=='\\') {
				sOut.append(curChar);
				i_currentPos++;
				sOut.append(currentCharacter());
				i_currentPos++;
			} else if(curChar=='\'') {
				sOut.append(curChar);
				i_currentPos++;
				break;
			} else {
				sOut.append(curChar);
				i_currentPos++;
			}
		}
		return sOut.toString();
	}

	public String getAndSetNextString() {
		int iStartPos = i_currentPos;
		boolean bInString = false;
		while(moreToParse()) {
			char curChar = currentCharacter();
			if(!bInString&&curChar=='"') {
				bInString=true;
				i_currentPos++;
			} else if(bInString&&curChar=='\\') {
				i_currentPos = i_currentPos + 2;
			} else if(bInString&&curChar=='"') {
				bInString = false;
				i_currentPos++;
				break;
			} else if(!bInString&&curChar=='\'') {
				getAndSetNextChar();
			} else {
				i_currentPos++;
			}
		}
		return getContents().substring(iStartPos,i_currentPos);	
	}

	public String getAndSetNextParen() {
		int iStartPos = i_currentPos;
		boolean bFoundOpener = false;
		boolean bInString = false;
		int iDepth = 0;
		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(!bInString&&!bFoundOpener&&curChar=='(') {
				bFoundOpener = true;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar=='(') {
				iDepth++;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar==')') {
				iDepth--;
				i_currentPos++;
				if(iDepth==-1) {
					break;
				}
			} else if(!bInString&&curChar=='\'') {
				getAndSetNextChar();
			} else if(!bInString&&curChar=='"') {
				bInString=true;
				i_currentPos++;
			} else if(bInString&&curChar=='\\') {
				i_currentPos = i_currentPos + 2;
			} else if(bInString&&curChar=='"') {
				bInString = false;
				i_currentPos++;
			} else {
				i_currentPos++;
			}
		}
		return getContents().substring(iStartPos,i_currentPos);
	}

	public String getAndSetNextBlock() {
		int iStartPos = i_currentPos;
		boolean bFoundOpener = false;
		boolean bInString = false;
		int iDepth = 0;
		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(!bFoundOpener&&!bInString&&curChar=='{') {
				bFoundOpener = true;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar=='{') {
				iDepth++;
				i_currentPos++;
			} else if(bFoundOpener&&!bInString&&curChar=='}') {
				iDepth--;
				i_currentPos++;
				if(iDepth==-1) {
					break;
				}
			} else if(!bInString&&curChar=='\'') {
				getAndSetNextChar();
			} else if(!bInString&&curChar=='"') {
				bInString=true;
				i_currentPos++;
			} else if(bInString&&curChar=='\\') {
				i_currentPos=i_currentPos+2;
			} else if(bInString&&curChar=='"') {
				bInString = false;
				i_currentPos++;
			} else {
				i_currentPos++;
			}
		}
		return getContents().substring(iStartPos,i_currentPos);
	}

	public String currentString() {
		return m_sContents.substring(i_currentPos);
	}

	public char currentCharacter() {
		return m_sContents.charAt(i_currentPos);
	}

}
