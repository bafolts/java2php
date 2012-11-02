package translator;

import java.util.regex.*;
import java.util.ArrayList;

public class BlockBody extends FileTranslator {

	private static final Pattern IS_ASSIGNMENT = Pattern.compile("^[ \t]*([A-Za-z][^ \\{\\(\\=\\+\\*\\/\\-]*[ \t]+)?[A-Za-z][^ \\{\\(\\=\\+\\*\\/\\-\\>\\<\\,]*[ ]*[=;]");
	private static final Pattern IS_BLOCKBODY = Pattern.compile("^[ \t]*[{]");
	private static final Pattern IS_LABEL = Pattern.compile("^[A-Za-z][^\\(\\)\\[\\]\\*\\+ ]*[ \t]*:");
	private static final Pattern IS_WHILE = Pattern.compile("^while[( \t]");
	private static final Pattern IS_SYNCHRONIZED = Pattern.compile("^synchronized[( \t]");
	private static final Pattern IS_FOR = Pattern.compile("^for[( \t]");
	private static final Pattern IS_IF = Pattern.compile("^if[( \t]");
	private static final Pattern IS_ELSEIF = Pattern.compile("^else[ \t]*if[( \t]");
	private static final Pattern IS_ELSE = Pattern.compile("^else[ \t]*[{ \t\n]");
	private static final Pattern IS_SWITCH = Pattern.compile("^switch[( \t]");
	private static final Pattern IS_TRY = Pattern.compile("^try[ \t]*[{]");
	private static final Pattern IS_CATCH = Pattern.compile("^[ \t]*catch[ \t(]");
	private static final Pattern IS_FINALLY = Pattern.compile("^[ \t]*finally[ \t]*[{]");
	private static final Pattern IS_RETURN = Pattern.compile("^return[ ;\t]");
	
	private ArrayList<Object> m_aLines = new ArrayList<Object>();

	public BlockBody() {
	
	}

	public BlockBody(String line) {
		line = line.trim();

		setContents(line);
		goToNext("{");

		while(moreToParse()) {
			Object nextLine = getNextLine();
			if(nextLine!=null) {
				m_aLines.add(nextLine);
			} else {
				break;
			}
		}

	}

	private Object getNextLine() {
		Try m_currentTry = null;
		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(isFiller(curChar)) {
				setPosition(getPosition()+1);
			} else if(curChar=='w'&&isWhile(curString)) {
				While pWhile = new While(getAndSetNextParen());
				if(hasBlockBody(currentString())) {
					pWhile.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					pWhile.setBlockBody(b);
				}
				return pWhile;
			} else if(curChar=='s'&&isSynchronized(curString)) {
				return new Synchronized(getAndSetNextBlock());
			} else if(curChar=='f'&&isFor(curString)) {
				For pFor = new For(getAndSetNextParen());
				if(hasBlockBody(currentString())) {
					pFor.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					pFor.setBlockBody(b);
				}
				return pFor;
			} else if(curChar=='i'&&isIf(curString)) {
				If pIf = new If(getAndSetNextParen());
				if(hasBlockBody(currentString())) {
					pIf.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					pIf.setBlockBody(b);
				}
				return pIf;
			} else if(curChar=='s'&&isSwitch(curString)) {
				Switch pSwitch = new Switch(getAndSetNextParen());
				if(hasBlockBody(currentString())) {
					pSwitch.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					pSwitch.setBlockBody(b);
				}
				return pSwitch;
			} else if(curChar=='e'&&isElseIf(curString)) {
				ElseIf pEf = new ElseIf(getAndSetNextParen());
				if(hasBlockBody(currentString())) {
					pEf.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					pEf.setBlockBody(b);
				}
				return pEf;
			} else if(curChar=='e'&&isElse(curString)) {
				setPosition(getPosition()+4);
				Else e = new Else();
				if(hasBlockBody(currentString())) {
					e.setBlockBody(new BlockBody(getAndSetNextBlock()));
				} else {
					BlockBody b = new BlockBody();
					b.addLine(getNextLine());
					e.setBlockBody(b);
				}
				return e;
			} else if(curChar=='r'&&isReturn(curString)) {
				return new Return(getAndSetNextSemi());
			} else if(curChar=='c'&&curString.startsWith("continue")) {
				return new Continue(getAndSetNextSemi());
			} else if(curChar=='b'&&curString.startsWith("break")) {
				return new Break(getAndSetNextSemi());
			} else if(curChar=='d'&&curString.startsWith("default")) {
				return new Default(getAndSetNextColon());
			} else if(curChar=='c'&&curString.startsWith("case")) {
				return new Case(getAndSetNextColon());
			} else if(curChar=='t'&&curString.startsWith("throw ")) {
				return new Throw(getAndSetNextSemi());
			} else if(curChar=='t'&&isTry(curString)) {
				Try tTry = new Try(getAndSetNextBlock());
				if(m_currentTry!=null) {
					tTry.setParentTry(m_currentTry);
				}
				for(Object o : tTry.getBlockBody().getLines()) {
					if(o instanceof Throw) {
						((Throw)o).setParentTry(tTry);
					} else if(o instanceof Return) {
						((Return)o).setParentTry(tTry);
					} else if(o instanceof Try) {
						((Try)o).setParentTry(tTry);
					}
				}
				m_currentTry = tTry;
				if(!isCatch(currentString())&&!isFinally(currentString())) {
					return m_currentTry;
				}
			} else if(curChar=='c'&&isCatch(curString)) {
				Catch c = new Catch(getAndSetNextBlock());
				for(Object o : c.getBlockBody().getLines()) {
					if(o instanceof Throw) {
						((Throw)o).setParentTry(m_currentTry);
						((Throw)o).setParentCatch(c);
					} else if(o instanceof Return) {
						((Return)o).setParentTry(m_currentTry);
						((Return)o).setParentCatch(c);
					}
				}
				m_currentTry.getCatch().add(c);
				if(!isCatch(currentString())&&!isFinally(currentString())) {
					return m_currentTry;
				}
			} else if(curChar=='f'&&isFinally(curString)) {
				Finally f = new Finally(getAndSetNextBlock());
				for(Object o : f.getBlockBody().getLines()) {
					if(o instanceof Throw) {
						((Throw)o).setParentTry(m_currentTry);
						((Throw)o).setParentFinally(f);
					} else if(o instanceof Return) {
						((Return)o).setParentTry(m_currentTry);
						((Return)o).setParentFinally(f);
					}
				}
				m_currentTry.setFinally(f);
				return m_currentTry;
			} else if(curChar=='}') {
				setPosition(getPosition()+1);
				return null;
			} else if(curChar==';') {
				setPosition(getPosition()+1);
			} else if(curChar=='{') {
				return new BlockBody(getAndSetNextBlock());
			} else if(isLabel(curString)) {
				Label l = new Label(getUntilNext(":"));
				goToNext(":");
				return l;
			} else if(isAssignment(curString)) {
				return new Assignment(getAndSetNextSemi());
			} else {
				return new Equals(getAndSetNextSemi());
			}
		}
		return null;
	}

	public void addLine(Object o) {
		m_aLines.add(o);
	}

	public ArrayList<Object> getLines() {
		return m_aLines;
	}

	private static boolean isReturn(String s) {
		return IS_RETURN.matcher(s).lookingAt();
	}

	private static boolean isSwitch(String s) {
		return IS_SWITCH.matcher(s).lookingAt();
	}

	private static boolean isElseIf(String s) {
		return IS_ELSEIF.matcher(s).lookingAt();
	}

	private static boolean isTry(String s) {
		return IS_TRY.matcher(s).lookingAt();
	}

	private static boolean isElse(String s) {
		return IS_ELSE.matcher(s).lookingAt();
	}

	private static boolean isCatch(String s) {
		return IS_CATCH.matcher(s).lookingAt();
	}
	
	private static boolean isFinally(String s) {
		return IS_FINALLY.matcher(s).lookingAt();
	}

	private static boolean isIf(String s) {
		return IS_IF.matcher(s).lookingAt();
	}

	private static boolean isFor(String s) {
		return IS_FOR.matcher(s).lookingAt();
	}

	private static boolean isWhile(String s) {
		return IS_WHILE.matcher(s).lookingAt();
	}

	private static boolean isSynchronized(String s) {
		return IS_SYNCHRONIZED.matcher(s).lookingAt();
	}

	private static boolean isLabel(String s) {
		return IS_LABEL.matcher(s).lookingAt();
	}

	private static boolean hasBlockBody(String s) {
		return IS_BLOCKBODY.matcher(s).lookingAt();
	}

	public static boolean isAssignment(String s) {
		return IS_ASSIGNMENT.matcher(s).lookingAt();
	}

}