package translator;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Equals extends FileTranslator {

	private ArrayList<Object> m_mValue = new ArrayList<Object>();

	private static final Pattern PATTERN_FUNCTION_CALL = Pattern.compile("^[.A-Za-z][^ =:;|+<>)(&\\[\\]\\~\\^\\*\\/\\-\\+\\%]*[(]");
	private static final Pattern PATTERN_VARIABLE = Pattern.compile("^[A-Za-z][^\\[ \\);=\\|\\+&\\-<\\+\\(\\>\\!\\:\\?\\*~^\\/\\%]*[ ]*");
	private static final Pattern PATTERN_TYPECAST = Pattern.compile("^[\\(][A-Za-z][^ \\:\\);=|\\+&\\-\\+\\(\\%]*[\\)][^+-]");
	private static final Pattern PATTERN_NUMBER = Pattern.compile("^[0-9]");

	public Equals() {}

	private static boolean isNumber(String s) {
		return PATTERN_NUMBER.matcher(s).find();
	}

	private static boolean isCurrentlyFunctionCall(String s) {
		return PATTERN_FUNCTION_CALL.matcher(s).find();
	}
	
	private static boolean isTypeCast(String s) {
		return PATTERN_TYPECAST.matcher(s).find();
	}

	public static boolean isVariable(String s) {
		return PATTERN_VARIABLE.matcher(s).find();
	}

	public Equals(String line) {

		setContents(line.trim());

		while(moreToParse()) {
			char curChar = currentCharacter();
			String curString = currentString();
			if(isFiller(curChar)) {
				setPosition(getPosition()+1);
			} else if(curChar=='=') {
				if(curString.startsWith("==")) {
					addValue(new LogicalEqual());
					setPosition(getPosition()+2);
				} else {
					addValue(new Assigner());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='^') {
				if(curString.startsWith("^=")) {
					addValue(new BitwiseXOrAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new BitwiseXOr());
					setPosition(getPosition()+1);
				}
			} else if(curChar==',') {
				addValue(new AdditionalAssignment(getAndSetNextSemi()));
			} else if(curChar=='"') {
				addValue(new InlineString(getAndSetNextString()));
			} else if(curChar=='\'') {
				addValue(new InlineCharacter(getAndSetNextChar()));
			} else if(curChar=='!') {
				if(curString.startsWith("!=")) {
					addValue(new LogicalNotEqual());
					setPosition(getPosition()+2);
				} else {
					addValue(new Not());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='f'&&curString.startsWith("false")) {
				addValue(new PrimitiveBoolean(false));
				setPosition(getPosition()+5);
			} else if(curChar=='i'&&curString.startsWith("instanceof ")) {
				addValue(new InstanceOf());
				setPosition(getPosition()+11);
				addValue(new Type(getAndSetNextType()));
			} else if(curChar=='t'&&curString.startsWith("true")) {
				addValue(new PrimitiveBoolean(true));
				setPosition(getPosition()+4);
			} else if(curChar=='n'&&curString.startsWith("null")) {
				addValue(new Null());
				setPosition(getPosition()+4);
			} else if(curChar=='n'&&curString.startsWith("new ")) {
				setPosition(getPosition()+4);
				ObjectDeclaration oD = null;
				if(curString.indexOf("{")!=-1&&curString.indexOf("{")<curString.indexOf("(")) {
					oD = new ObjectDeclaration(getAndSetNextBlock());
				} else {
					oD = new ObjectDeclaration(getAndSetNextParen());
				}
				if(matches(currentString(),"^[ ]*[{]")) {
					ClassDeclaration cd = new ClassDeclaration(getAndSetNextBlock());
					cd.setInline(true);
					oD.setInlineClass(cd);
				}
				addValue(oD);
			} else if(curChar=='+') {
				if(curString.startsWith("+=")) {
					addValue(new IncrementorAssignment());
					setPosition(getPosition()+2);
				} else if(curString.startsWith("++")) {
					addValue(new Incrementor());
					setPosition(getPosition()+2);
				}  else {
					addValue(new Addition());
					setPosition(getPosition()+1);
				}	
			} else if(curChar=='*') {
				if(curString.startsWith("*=")) {
					addValue(new MultipliorAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new Multiply());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='/') {
				if(curString.startsWith("/=")) {
					addValue(new DividorAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new Divide());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='<') {
				if(curString.startsWith("<=")) {
					addValue(new LogicalLessThanOrEqualTo());
					setPosition(getPosition()+2);
				} else if(curString.startsWith("<<=")) {
					addValue(new BitwiseShiftLeftAssignment());
					setPosition(getPosition()+3);
				} else if(curString.startsWith("<<")) {
					addValue(new BitwiseShiftLeft());
					setPosition(getPosition()+2);
				} else {
					addValue(new LessThan());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='>') {
				if(curString.startsWith(">=")) {
					addValue(new LogicalGreaterThanOrEqualTo());
					setPosition(getPosition()+2);
				} else if(curString.startsWith(">>>=")) {
					addValue(new BitwiseUnsignedShiftRightAssignment());
					setPosition(getPosition()+4);
				} else if(curString.startsWith(">>=")) {
					addValue(new BitwiseShiftRightAssignment());
					setPosition(getPosition()+3);
				} else if(curString.startsWith(">>>")) {
					addValue(new BitwiseUnsignedShiftRight());
					setPosition(getPosition()+3);
				} else if(curString.startsWith(">>")) {
					addValue(new BitwiseShiftRight());
					setPosition(getPosition()+2);
				} else {
					addValue(new GreaterThan());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='[') {
				addValue(new ArrayCaller(getAndSetNextSquare()));
			} else if(curChar=='|') {
				if(curString.startsWith("||")) {
					addValue(new LogicalOr());
					setPosition(getPosition()+2);
				} else if(curString.startsWith("|=")) {
					addValue(new BitwiseOrAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new BitwiseOr());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='&') {
				if(curString.startsWith("&&")) {
					addValue(new LogicalAnd());
					setPosition(getPosition()+2);
				} else if(curString.startsWith("&=")) {
					addValue(new BitwiseAndAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new BitwiseAnd());
					setPosition(getPosition()+1);
				}
			} else if(curChar=='?') {
				addValue(new ConditionalStarter());
				setPosition(getPosition()+1);
			} else if(curChar==':') {
				addValue(new ConditionalElse());
				setPosition(getPosition()+1);
			} else if(curChar=='~') {
				addValue(new BitwiseComplement());
				setPosition(getPosition()+1);
			} else if(curChar=='%') {
				addValue(new Remainder());
				setPosition(getPosition()+1);
			} else if(curChar=='(') {
				if(isTypeCast(curString)) {
					addValue(new TypeCast(getAndSetNextParen()));
				} else {
					addValue(new Parenthesis(getAndSetNextParen()));
				}
			} else if(curChar=='-') {
				if(curString.startsWith("--")) {
					addValue(new Decrementor());
					setPosition(getPosition()+2);
				} else if(curString.startsWith("-=")) {
					addValue(new DecrementorAssignment());
					setPosition(getPosition()+2);
				} else {
					addValue(new Subtraction());
					setPosition(getPosition()+1);
				}
			} else if(isNumber(curString)) {
				addValue(new InlineNumber(getAndSetNextNumber()));
			} else if(isCurrentlyFunctionCall(curString)) {
				FunctionCall fc = new FunctionCall(getAndSetNextParen());
				if(m_mValue.size()>0&&m_mValue.get(m_mValue.size()-1) instanceof FunctionCall) {
					FunctionCall pc = (FunctionCall)m_mValue.get(m_mValue.size()-1);
					while(pc.getNextCall()!=null) {
						pc = pc.getNextCall();
					}
					pc.setNextCall(fc);
				} else {
					addValue(fc);
				}
			} else if(isVariable(curString)) {
				String s = getAndSetNextVariable();
				addValue(new Variable(s));
			} else {
				throw new RuntimeException("Unknown Equals type :"+curString);
			}
		}

	}

	public ArrayList<Object> getValues() {
		return m_mValue;
	}
	
	public void addValue(Object o) {
		m_mValue.add(o);
	}

}