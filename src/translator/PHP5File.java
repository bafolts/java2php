package translator;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import java.util.HashMap;

public final class PHP5File extends JavaInterpreter {

	private StringBuilder fileWriter = null;

	private boolean m_includeMain = false;
	private JavaFile m_java = null;

	private ArrayList<String> m_aInlineStrings = new ArrayList<String>();
	
	private ArrayList<String> m_aSourceDirs = new ArrayList<String>();

	private ArrayList<PHPMethod> m_aPHPMethods = new ArrayList<PHPMethod>();

	private static ArrayList<String> m_aReservedWords = new ArrayList<String>(13);

	static {
		m_aReservedWords.add("for");
		m_aReservedWords.add("instanceof");
		m_aReservedWords.add("if");
		m_aReservedWords.add("class");
		m_aReservedWords.add("while");
		m_aReservedWords.add("return");
		m_aReservedWords.add("catch");
		m_aReservedWords.add("elseif");
		m_aReservedWords.add("else");
		m_aReservedWords.add("try");
		m_aReservedWords.add("break");
		m_aReservedWords.add("continue");
		m_aReservedWords.add("throw");
	}

	private ArrayList<EnumDeclaration> m_aIncludedEnums = new ArrayList<EnumDeclaration>();

	private HashMap<String,JavaFile> m_aJavaFiles = new HashMap<String,JavaFile>(100);
	private ArrayList<String> m_sIncludedFiles = new ArrayList<String>(100);
	private ArrayList<String> m_sIncludedDirs = new ArrayList<String>(100);

	private ArrayList<String> m_aSharedNames = new ArrayList<String>();

	private Method currentMethod = null;
	private ClassDeclaration curClass = null;
	private ArrayList<Assignment> m_aCurrentVariables = new ArrayList<Assignment>();
	private ArrayList<ClassDeclaration> m_aPrivateClasses = new ArrayList<ClassDeclaration>();
	private int m_iPrivateClasses = 0;

	public void setSourcePath(ArrayList<String> s) {
		m_aSourceDirs = s;
	}

	public boolean isIncludedClass(String s) {
		Type t = new Type(s);
		String sTypeName = t.getName();
		String sCheckTypeName = "."+sTypeName;
		if(isPrimitiveType(t)||s.equals("enum")) {
			return false;
		} else if(sTypeName.equals(curClass.getClassName().getName())) {
				return true;
		} else {
			for(String includedFile : m_sIncludedFiles) {
				if(includedFile.endsWith(sCheckTypeName)||includedFile.equals(s)) {
					return true;
				}
			}
			//see if the string itself is an actual class
			return findFile(s.replaceAll("\\.","/")+".java")!=null;
		}
	}

	public PHP5File(JavaFile javaFile) {

		m_java = javaFile;

		javaFile.addImport(new Import("import java.lang.*"));
		javaFile.addImport(new Import("import Translator.JavaBase"));

	}

	public String getFileContents() {
		return fileWriter.toString();
	}

	public void writeFile(String fileName) {
		getOutput(m_java);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(fileWriter.toString());
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private void getOutput(JavaFile m_javaFile) {

		m_java = m_javaFile;

		m_includeMain = false;

		fileWriter = new StringBuilder(1000);
		fileWriter.append("<?php\n");
    fileWriter.append("\tinclude_once(\"PHPHelper.php\");\n");
		writeClass(m_javaFile.getMainClass());
		fileWriter.append("?>");

	}
	
	private void writePropertyDeclarations() {
		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			writePropertyDeclaration(pp);
		}
		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			writePropertyDeclaration(pp);
		}
		for(PublicProperty pp : curClass.getPublicProperties()) {
			writePropertyDeclaration(pp);
		}
	}

	private void writePropertyDeclaration(Property pp) {

		fileWriter.append("\t");

		if(pp instanceof PrivateProperty)
			fileWriter.append("private");
		else if(pp instanceof PublicProperty)
			fileWriter.append("public");
		else if(pp instanceof ProtectedProperty)
			fileWriter.append("protected");

		fileWriter.append(" ");

		if(pp.getStatic()) {
			fileWriter.append("static ");
		}
		fileWriter.append("$"+pp.getName()+";\n");
	}

	private void writeInitStaticVarDeclaration(Property pp) {
		if(pp.getEquals()!=null&&pp.getStatic()) {
			fileWriter.append("\t\t\t");
			writeType(curClass.getClassName());
			fileWriter.append("::$");
			fileWriter.append(pp.getName()+" = ");
			if(isPrimitiveWrapper(pp.getType())) {
				fileWriter.append("new ");
				writeType(pp.getType());
				fileWriter.append("(");
			}
			writeEquals(pp.getEquals());
			if(isPrimitiveWrapper(pp.getType())) {
				fileWriter.append(")");
			}
			fileWriter.append(";\n");
		}
	}

	private void writeInitStaticVars() {
		fileWriter.append("\tpublic static function _initStaticVars() {\n");
		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			writeInitStaticVarDeclaration(pp);
		}
		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			writeInitStaticVarDeclaration(pp);
		}
		for(PublicProperty pp : curClass.getPublicProperties()) {
			writeInitStaticVarDeclaration(pp);
		}
		
		for(StaticBlock s : curClass.getStaticBlocks()) {
			writeBlockBody(s.getBlockBody(),3);
		}

		fileWriter.append("\t}\n");
	}

	private void writeInitVar(Property pp) {
		if(pp.getEquals()!=null&&!pp.getStatic()) {
			fileWriter.append("\t\t\t$this->"+pp.getName()+" = ");
			if(isPrimitiveWrapper(pp.getType())) {
				fileWriter.append("new ");
				writeType(pp.getType());
				fileWriter.append("(");
			}
			writeEquals(pp.getEquals());
			if(isPrimitiveWrapper(pp.getType())) {
				fileWriter.append(")");
			}
			fileWriter.append(";\n");
		}
	}

	private void writeInitVars() {
		fileWriter.append("\tpublic function _initVars() {\n");
		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			writeInitVar(pp);
		}
		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			writeInitVar(pp);
		}
		for(PublicProperty pp : curClass.getPublicProperties()) {
			writeInitVar(pp);
		}

		if(curClass.getExtends()!=null) {
			fileWriter.append("\t\t\tparent::_initVars();\n");
		}

		fileWriter.append("\t}\n");
	}

	private void writeClassMethods() {

		ArrayList<PublicMethod> aPublicMethods = curClass.getPublicMethods();
		for(int i=0,length=aPublicMethods.size();i<length;i++) {
			PublicMethod p = aPublicMethods.get(i);
			if(!m_includeMain&&p.getStatic()&&p.getName().equals("main")) {
				m_includeMain = true;
			}
			writeMethod(p,i);
		}

		ArrayList<PrivateMethod> aPrivateMethods = curClass.getPrivateMethods();
		for(int i=0,length=aPrivateMethods.size();i<length;i++) {
			PrivateMethod p = aPrivateMethods.get(i);
			writeMethod(p,i);
		}

		ArrayList<ProtectedMethod> aProtectedMethods = curClass.getProtectedMethods();
		for(int i=0,length=aProtectedMethods.size();i<length;i++) {
			ProtectedMethod p = aProtectedMethods.get(i);
			writeMethod(p,i);
		}

		for(String sName : m_aSharedNames) {
			writeMassCaller(sName);
		}
		
		m_aSharedNames.clear();
	
	}

	private void writeInlineStringFix() {
		fileWriter.append("private static $_aInlineStrings = array();\n");

		if(m_aInlineStrings.size()>0) {
			fileWriter.append("public static function _initInlineStrings() {\n");
			writeType(curClass.getClassName());
			fileWriter.append("::$_aInlineStrings = array(");
			for(int i=0,length=m_aInlineStrings.size();i<length;i++) {
				String sString = m_aInlineStrings.get(i);
				if(sString.indexOf("\\")!=-1) {
					fileWriter.append("new java_lang_String("+sString.replaceAll("\\$","\\\\\\$")+")");
				} else {
					sString = sString.substring(1,sString.length()-1);
					fileWriter.append("new java_lang_String('"+sString.replaceAll("\\'","\\\\'")+"')");
				}
				if(i<length-1) {
					fileWriter.append(",");
				}
			}
			fileWriter.append(");\n}\n");
		}
		fileWriter.append("}\n");

		if(m_aInlineStrings.size()>0) {
			writeType(curClass.getClassName());
			fileWriter.append("::_initInlineStrings();\n");
		}
	}

	private void writeClass(ClassDeclaration curClass) {

		this.curClass = curClass;

		if(curClass.getFinal()) {
			fileWriter.append("final ");
		}

		if(curClass.getAbstract()) {
			fileWriter.append("abstract ");
		}

		fileWriter.append("class ");
		writeType(curClass.getClassName());
		fileWriter.append(" ");

		if(curClass.getExtends()!=null) {
			fileWriter.append("extends ");
			writeType(curClass.getExtends());
		}

		fileWriter.append(" {\n");

		writePropertyDeclarations();

		if(hasStaticProperty(curClass)) {
			writeInitStaticVars();
		}

		writeInitVars();

		writeConstructors(curClass.getConstructors(),curClass,false);
		writeClassMethods();

		fileWriter.append("public static $___METHODS = array();\n");

		//@TODO - this closes the class tag, should be moved
		writeInlineStringFix();

		writeClassProperties();

		writeServletHook();

		if(hasStaticProperty(curClass)) {
			writeType(curClass.getClassName());
			fileWriter.append("::_initStaticVars();\n");
		}

		if(m_includeMain) {
			writeType(curClass.getClassName());
			fileWriter.append("::main(Translator_JavaBase::getArgs());\n");
		}

		writeInternalClasses();
		writeEnumDeclarations();

	}
	
	/**
	 * Attempts to load an HttpServlet manually.
	 * @todo - Add check if the base class extends HttpServlet.
	 */
	private void writeServletHook() {
		
		// Determine if this class extends javax.http.servlet.HttpServlet
		// TODO - this will only work directly for now.
		if (getFullClassName(curClass.getExtends()).equals("javax.servlet.http.HttpServlet")) {

			fileWriter.append("__attemptServletLoad('");
			writeType(curClass.getClassName());
			fileWriter.append("');");

		}
		
	}

	private void writeClassProperties() {
		for(PHPMethod pm : m_aPHPMethods) {
			writeType(curClass.getClassName());
			fileWriter.append("::$___METHODS[] = array(");
			fileWriter.append("'phpName'=>'"+pm.getName()+"',");
			if(pm.getMethod().getName()==null) {//constructor
				fileWriter.append("'javaName'=>'"+curClass.getClassName().getName()+"',");
				fileWriter.append("'isConstructor'=>true,");
			} else {
				fileWriter.append("'javaName'=>'"+pm.getMethod().getName()+"',");
			}
			fileWriter.append("'parameters'=>array(");
			ArrayList<Parameter> aParameters = pm.getMethod().getParameters();
			for(int i=0,length=aParameters.size();i<length;i++) {
				Parameter p = aParameters.get(i);
				fileWriter.append("array('type'=>'"+getFullClassName(p.getType())+"','name'=>'"+p.getName()+"','isArray'=>"+p.getType().getIsArray()+")");
				if(i<length-1) {
					fileWriter.append(",");
				}
			}
			fileWriter.append(")");
			fileWriter.append(");\n");
		}
	}
	
	private void writeInternalClasses() {

		ArrayList<ClassDeclaration> aPrivateClasses = curClass.getPrivateClasses();

		for(int i=0,length=m_aPrivateClasses.size();i<length;i++) {
			aPrivateClasses.add(m_aPrivateClasses.remove(0));
		}

		this.curClass = null;

		for(int i=0,length=aPrivateClasses.size();i<length;i++) {
			writeClass(aPrivateClasses.get(i));
		}

	}
	
	private void writeEnumDeclarations() {
		for(EnumDeclaration e : m_aIncludedEnums) {
			writeEnumDeclaration(e);
		}
	}

	public void writeEnumDeclaration(EnumDeclaration e) {
		fileWriter.append("class "+curClass.getClassName().getName()+"3num"+e.getName());
		fileWriter.append(" {\n");
		fileWriter.append("public $BOOK = 'BOOK';\n");
		fileWriter.append("}\n");
	}
	
	public void writeConstructors(ArrayList<Constructor> aConstructors,ClassDeclaration curClass,boolean parentCall) {
		int iNumConstructors = aConstructors.size();
		Type oExtends = curClass.getExtends();
		if(iNumConstructors==0) {
			if(oExtends!=null&&!oExtends.getName().equals("java.lang.Object")) {
				JavaFile j = getJavaFile(m_java,oExtends);
				if(j!=null) {
					writeConstructors(j.getMainClass().getConstructors(),j.getMainClass(),true);
				}
			} else {
				writeConstructor(null,"__construct",true,false);
			}
		} else if(iNumConstructors==1) {
			writeConstructor(aConstructors.get(0),"__construct",true,parentCall);
		} else if(iNumConstructors>1) {
			String sBaseName = "__construct_"+curClass.getClassName().getName()+"_";
			fileWriter.append("\tpublic function __construct() {\n\t\t$arg_list = func_get_args();\n\t\t$num_args = count($arg_list);\n\t\t$this->_initVars();\n");
			for(int i=0;i<iNumConstructors;i++) {
				ArrayList<Parameter> aParameters = aConstructors.get(i).getParameters();
				int iNumParameters = aParameters.size();
				if(iNumParameters>0) {
					fileWriter.append("\t\t");
					if(i>0) {
						fileWriter.append("else ");
					}
					fileWriter.append("if($num_args==="+iNumParameters+"&&(");
					String sArgList = new String("");
					for(int j=0;j<iNumParameters;j++) {
						sArgList = sArgList + "$arg_list["+j+"]";
						fileWriter.append("$arg_list["+j+"] instanceof ");
						writeType(aParameters.get(j).getType());
						if(j<iNumParameters-1) {
							fileWriter.append("&&");
							sArgList = sArgList + ",";
						}
					}
					fileWriter.append(")) {\n");
					if(parentCall) {
						fileWriter.append("\t\t\tparent::"+sBaseName+i+"("+sArgList+");\n");
					} else {
						fileWriter.append("\t\t\t$this->"+sBaseName+i+"("+sArgList+");\n");
					}
					fileWriter.append("\t\t}\n");
				} else {
					fileWriter.append("\t\t");
					if(i>0) {
						fileWriter.append("else ");
					}
					fileWriter.append("if($num_args===0) {\n");
					if(parentCall) {
						fileWriter.append("\t\t\tparent::"+sBaseName+i+"();\n");
					} else {
						fileWriter.append("\t\t\t$this->"+sBaseName+i+"();\n");
					}
					fileWriter.append("\t\t}\n");
				}
			}
			fileWriter.append("\t}\n");
			
			for(int i=0;i<iNumConstructors;i++) {
				writeConstructor(aConstructors.get(i),sBaseName+i,false,parentCall);
			}
		}
	}

	public void writeMassCallerMethod(Method m,String sName) {
		ArrayList<Parameter> aParameters = m.getParameters();
		String s = m.getName();
		int iNumParameters = aParameters.size();
		if(iNumParameters>0) {
			if(aParameters.get(iNumParameters-1).getUnlimited()) {
				fileWriter.append("\t\tif(");
				String sArgList = new String("");
				int j=0;
				for(;j<iNumParameters;j++) {
					sArgList += "$arg_list["+j+"]";
					if(aParameters.get(j).getType().getName().equals("char")) {
						fileWriter.append("is_string($arg_list["+j+"])");
					} else {
						fileWriter.append("$arg_list["+j+"] instanceof ");
						writeType(aParameters.get(j).getType());
					}
					if(j<iNumParameters-1) {
						fileWriter.append("&&");
						sArgList = sArgList + ",";
					}
				}
				fileWriter.append(") {\n");
				fileWriter.append("\t\t\t$arg_list["+(j-1)+"]=array_splice($arg_list,"+(j-1)+",($num_args-"+(j-1)+"));\n");
				fileWriter.append("\t\t\treturn ");
				if(m.getStatic()) {
					writeType(curClass.getClassName());
					fileWriter.append("::");
				} else {
					fileWriter.append("$this->");
				}
				fileWriter.append(sName+"("+sArgList+");\n");
				fileWriter.append("\t\t}\n");
			} else {
				fileWriter.append("\t\t");
				fileWriter.append("if($num_args==="+iNumParameters+"&&(");
				String sArgList = new String("");
				for(int j=0;j<iNumParameters;j++) {
					sArgList += "$arg_list["+j+"]";
					if(aParameters.get(j).getType().getName().equals("char")) {
						fileWriter.append("is_string($arg_list["+j+"])");
					} else if(aParameters.get(j).getType().getName().equals("int")) {
						fileWriter.append("is_int($arg_list["+j+"])");
					} else {
						fileWriter.append("$arg_list["+j+"] instanceof ");
						writeType(aParameters.get(j).getType());
					}
					if(j<iNumParameters-1) {
						fileWriter.append("&&");
						sArgList += ",";
					}
				}
				fileWriter.append(")) {\n");
				fileWriter.append("\t\t\treturn ");
				if(m.getStatic()) {
					writeType(curClass.getClassName());
					fileWriter.append("::");
				} else {
					fileWriter.append("$this->");
				}
				fileWriter.append(sName+"("+sArgList+");\n");
				fileWriter.append("\t\t}\n");
			}
		} else {
			fileWriter.append("\t\t");
			fileWriter.append("if($num_args===0) {\n");
			fileWriter.append("\t\t\treturn ");
			if(m.getStatic()) {
				writeType(curClass.getClassName());
				fileWriter.append("::");
			} else {
				fileWriter.append("$this->");
			}
			fileWriter.append(sName+"();\n");
			fileWriter.append("\t\t}\n");
		}
	}
	
	public void writeMassCaller(String s) {
		
		fileWriter.append("\tpublic function "+s+"() {\n\t\t$arg_list = func_get_args();\n\t\t$num_args = count($arg_list);\n");

		ArrayList<PublicMethod> aPublicMethods = m_java.getMainClass().getPublicMethods();
		for(int i=0,length=aPublicMethods.size();i<length;i++) {
			if(aPublicMethods.get(i).getName().equals(s)) {
				writeMassCallerMethod(aPublicMethods.get(i),s+"_pu_"+i);
			}
		}

		ArrayList<PrivateMethod> aPrivateMethods = m_java.getMainClass().getPrivateMethods();
		for(int i=0,length=aPrivateMethods.size();i<length;i++) {
			if(aPrivateMethods.get(i).getName().equals(s)) {
				writeMassCallerMethod(aPrivateMethods.get(i),s+"_pv_"+i);
			}
		}

		ArrayList<ProtectedMethod> aProtectedMethods = m_java.getMainClass().getProtectedMethods();
		for(int i=0,length=aProtectedMethods.size();i<length;i++) {
			if(aProtectedMethods.get(i).getName().equals(s)) {
				writeMassCallerMethod(aProtectedMethods.get(i),s+"_pr_"+i);
			}
		}

		fileWriter.append("\t}\n");
	}

	private boolean sharedName(String name) {

		int matches = 0;
		ArrayList<PrivateMethod> aPrivateVars = curClass.getPrivateMethods();
		ArrayList<ProtectedMethod> aProtectedVars = curClass.getProtectedMethods();
		ArrayList<PublicMethod> aPublicVars = curClass.getPublicMethods();
		int i = 0;
		int length = aPrivateVars.size();

		for(;i<length;i++) {
			if(name.equals(aPrivateVars.get(i).getName())) {
				if(++matches>1)
					return true;
			}
		}

		for(i=0,length=aProtectedVars.size();i<length;i++) {
			if(name.equals(aProtectedVars.get(i).getName())) {
				if(++matches>1)
					return true;
			}
		}

		for(i=0,length=aPublicVars.size();i<length;i++) {
			if(name.equals(aPublicVars.get(i).getName())) {
				if(++matches>1)
					return true;
			}
		}

		return false;
	}

	private void writeMethod(Method m,int i) {

		if(m instanceof PublicMethod) {
			fileWriter.append("\tpublic ");
		} else if(m instanceof ProtectedMethod) {
			fileWriter.append("\tprotected ");
		} else if(m instanceof PrivateMethod) {
			fileWriter.append("\tprivate ");
		} else {
			throw new RuntimeException("Unknown Method Type "+m);
		}

		if(m.getStatic()) {
			fileWriter.append("static ");
		}
		if(m.getAbstract()) {
			fileWriter.append("abstract ");
		}

		String sName = m.getName();
		if(sharedName(sName)) {
			if(!m_aSharedNames.contains(sName)) {
				m_aSharedNames.add(sName);
			}
			if(m instanceof PublicMethod) {
				sName += "_pu_";
			} else if(m instanceof PrivateMethod) {
				sName += "_pv_";
			} else if(m instanceof ProtectedMethod) {
				sName += "_pr_";
			} else {
				throw new RuntimeException("Unknown Method Type "+m);
			}
			sName += i;
		}

		currentMethod = m;
		m_aCurrentVariables.clear();

		fileWriter.append("function "+sName+"(");
		writeParameters(m.getParameters());
		fileWriter.append(")");

		if(!m.getAbstract()) {
			fileWriter.append("{\n");
			
			for(Parameter p : m.getParameters()) {
				if(isPrimitiveWrapper(p.getType())) {
					fileWriter.append("\t\t$"+p.getName()+" = new "+getFullClassName(p.getType())+"($"+p.getName()+");\n");
				}
			}

			writeBlockBody(m.getBlockBody(),2);
			fileWriter.append("\t}\n");
		} else {
			fileWriter.append(";\n");
		}

		PHPMethod pMethod = new PHPMethod(currentMethod,sName);
		m_aPHPMethods.add(pMethod);

		currentMethod = null;
		m_aCurrentVariables.clear();
	}

	private void writeParameters(ArrayList<Parameter> parameters) {

		for(int i=0,length=parameters.size();i<length;i++) {
			fileWriter.append("$");
			fileWriter.append(parameters.get(i).getName());
			if(i<length-1) {
				fileWriter.append(",");
			}
		}
	}

	private boolean isStaticProperty(String s) {

		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			if(pp.getStatic()&&s.equals(pp.getName())) {
				return true;
			}
		}

		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			if(pp.getStatic()&&s.equals(pp.getName())) {
				return true;
			}
		}
		
		for(PublicProperty pp : curClass.getPublicProperties()) {
			if(pp.getStatic()&&s.equals(pp.getName())) {
				return true;
			}
		}

		return false;

	}


	private boolean isMemberProperty(String s) {

		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			if(s.equals(pp.getName())) {
				return true;
			}
		}

		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			if(s.equals(pp.getName())) {
				return true;
			}
		}

		for(PublicProperty pp : curClass.getPublicProperties()) {
			if(s.equals(pp.getName())) {
				return true;
			}
		}

		return false;

	}

	private void writeBlockBody(BlockBody b,int depth) {
		ArrayList<Object> aLines = b.getLines();
		int iNumLines = aLines.size();
		for(int i=0;i<iNumLines;i++) {
			Object line = aLines.get(i);
			if(line instanceof Assignment) {
				writeTabs(depth);
				writeAssignment((Assignment)line);
				fileWriter.append(";\n");
			} else if(line instanceof Equals) {
				writeTabs(depth);
				writeEquals((Equals)line);
				fileWriter.append(";\n");
			} else if(line instanceof Case) {
				Case w = (Case)line;
				writeTabs(depth);
				fileWriter.append("case ");
				writeEquals(w.getEquals());
				fileWriter.append(":\n");
			} else if(line instanceof Return) {
				Return ret = (Return)line;

				writeTabs(depth);

				if(ret.getParentTry()!=null) {
					fileWriter.append("$t_wants_to_return_"+currentMethod.hashCode()+"=");
				} else {
					fileWriter.append("return ");
				}

				if(isPrimitiveWrapper(currentMethod.getType())) {
					fileWriter.append("new ");
					writeType(currentMethod.getType());
					fileWriter.append("(");
				}
				Return r = (Return)line;
				writeEquals(r.getEquals());
				if(isPrimitiveWrapper(currentMethod.getType())) {
					fileWriter.append(")");
				}
				fileWriter.append(";\n");

			} else if(line instanceof For) {
				For w = (For)line;
				writeTabs(depth);
				fileWriter.append("for");
				ForArgument fa = w.getArguments();
				if(fa.getIterator()) {
					fileWriter.append("each(__toArray(");
					writeEquals(fa.getEquals());
					fileWriter.append(") as $");
					fileWriter.append(fa.getParameter().getName());

					m_aCurrentVariables.add(new Assignment(fa.getParameter()));

				} else {
					fileWriter.append("(");
					ArrayList<Object> arguments = w.getArguments().getLines();
					for(int j=0,length=arguments.size();j<length;j++) {
						Object argument = arguments.get(j);
						if(argument instanceof Assignment) {
							writeAssignment((Assignment)argument);
						} else if(argument instanceof Equals) {
							writeEquals((Equals)argument);
						}
						if(j<length-1) {
							fileWriter.append(";");
						}
					}
				}
				fileWriter.append(") {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof While) {
				While w = (While)line;
				writeTabs(depth);
				fileWriter.append("while(");
				writeEquals((Equals)w.getArgument());
				fileWriter.append(") {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof Label) {
				Label w = (Label)line;
				writeTabs(depth);
				fileWriter.append(w.getName());
				fileWriter.append(":\n");
			} else if(line instanceof If) {
				If w = (If)line;
				writeTabs(depth);
				fileWriter.append("if(");
				Equals ifTest = (Equals)w.getArgument();
				writeEquals(ifTest);
				fileWriter.append(") {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof Switch) {
				Switch w = (Switch)line;
				writeTabs(depth);
				fileWriter.append("switch(");
				writeEquals((Equals)w.getArgument());
				fileWriter.append(") {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof ElseIf) {
				ElseIf w = (ElseIf)line;
				writeTabs(depth);
				fileWriter.append("else if(");
				writeEquals((Equals)w.getArgument());
				fileWriter.append(") {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof Else) {
				Else w = (Else)line;
				writeTabs(depth);
				fileWriter.append("else {\n");
				writeBlockBody(w.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			} else if(line instanceof Try) {
				writeTry((Try)line,depth);
			} else if(line instanceof Break) {
				writeTabs(depth);
				fileWriter.append("break;\n");
			} else if(line instanceof Continue) {
				writeTabs(depth);
				fileWriter.append("continue;\n");
			} else if(line instanceof Throw) {
				Throw w = (Throw)line;
				writeTabs(depth);
				
				//Check for a throw which is not caught.
				if(w.getParentCatch()!=null&&w.getParentTry().getFinally()!=null) {
					fileWriter.append("$t_uncaught_throw_"+w.getParentTry().hashCode()+"=");
					writeEquals(w.getEquals());
					fileWriter.append(";\n");
				} else if(w.getParentTry()!=null&&w.getEquals().getValues().size()==1) {
					Type t = getTypeOf(w.getEquals().getValues().get(0),m_java,curClass,null);
					if(t!=null) {
						Try parentTry = w.getParentTry();
						boolean bCaught = false;
						if(parentTry.getCatch()!=null) {
							for(Catch c : parentTry.getCatch()) {
								if(getFullClassName(c.getParameter().getType()).equals(getFullClassName(t))) {
									bCaught = true;
									break;
								}
							}
						}
						if(parentTry.getParentTry()!=null) {
							Try grandParentTry = parentTry.getParentTry();
							if(grandParentTry.getCatch()!=null) {
								for(Catch c : grandParentTry.getCatch()) {
									if(getFullClassName(c.getParameter().getType()).equals(getFullClassName(t))) {
										bCaught = true;
										break;
									}
								}
							}
						}
						if(currentMethod.getThrows()!=null) {
							for(Type t1 : currentMethod.getThrows().getTypes()) {
								if(getFullClassName(t1).equals(getFullClassName(t))) {
									bCaught = true;
									break;
								}
							}
						}
						if(!bCaught) {
							fileWriter.append("$t_uncaught_throw_"+w.getParentTry().hashCode()+"=");
							writeEquals(w.getEquals());
							fileWriter.append(";\n");
						} else {
							fileWriter.append("throw ");
							writeEquals(w.getEquals());
							fileWriter.append(";\n");
						}
					} else {
						System.err.println("Error finding return type of exception in "+curClass.getClassName().getName());
					}
				} else {
					fileWriter.append("throw ");
					writeEquals(w.getEquals());
					fileWriter.append(";\n");
				}
			} else if(line instanceof Synchronized) {
				writeSynchronized((Synchronized)line,depth);
			}  else {
				writeTabs(depth);
				fileWriter.append(line);
				fileWriter.append("\n");
			}
		}
	}

	private void writeTry(Try w,int depth) {
		writeTabs(depth);
		fileWriter.append("try {\n");
		writeTabs(depth+1);
		fileWriter.append("$t_uncaught_throw_"+w.hashCode()+"=null;\n");
		writeTabs(depth+1);
		fileWriter.append("$t_wants_to_return_"+currentMethod.hashCode()+"=null;\n");
		writeBlockBody(w.getBlockBody(),depth+1);
		writeTabs(depth);
		fileWriter.append("}\n");

		if(w.getCatch().size()>0) {
			for(Catch c : w.getCatch()) {
				writeTabs(depth);
				Parameter p = c.getParameter();
				fileWriter.append("catch(");
				writeType(p.getType());
				fileWriter.append(" $");
				fileWriter.append(p.getName());
				m_aCurrentVariables.add(new Assignment(p.getType().getName()+" "+p.getName()));
				fileWriter.append(") {\n");
				writeBlockBody(c.getBlockBody(),depth+1);
				writeTabs(depth);
				fileWriter.append("}\n");
			}
		}

		if(w.getFinally()!=null) {
			writeTabs(depth);
			fileWriter.append("catch(Exception $e) {\n");
			writeTabs(depth+1);
			fileWriter.append("$t_uncaught_throw_"+w.hashCode()+"=$e;\n");
			writeTabs(depth);
			fileWriter.append("}\n");
		}

		if(w.getFinally()!=null) {
			writeTabs(depth);
			fileWriter.append("//finally {\n");
			writeBlockBody(w.getFinally().getBlockBody(),depth+1);
			writeTabs(depth+1);
			fileWriter.append("if($t_wants_to_return_"+currentMethod.hashCode()+"===null&&$t_uncaught_throw_"+w.hashCode()+"!==null)throw $t_uncaught_throw_"+w.hashCode()+";\n");
			writeTabs(depth);
			fileWriter.append("//}\n");
		}

		if(w.getParentTry()==null) {
			writeTabs(depth);
			fileWriter.append("if($t_wants_to_return_"+currentMethod.hashCode()+"!==null)return $t_wants_to_return_"+currentMethod.hashCode()+";\n");
		}

	}

	private void writeConstructor(Constructor c,String n,boolean b,boolean parentCall) {

		if(c!=null)
			currentMethod = (Method)c;
		m_aCurrentVariables.clear();
		fileWriter.append("\tpublic function "+n+"(");

		if(c!=null)
			writeParameters(c.getParameters());
		fileWriter.append(") {\n");

		if(c!=null) {
			for(Parameter p : c.getParameters()) {
				if(isPrimitiveWrapper(p.getType())) {
					fileWriter.append("\t\t$"+p.getName()+" = new ");
					writeType(p.getType());
					fileWriter.append("($"+p.getName()+");\n");
				}
			}
		}

		if(b) {
			fileWriter.append("\t\t$this->_initVars();\n");
		}

		if(!parentCall) {
			if(c!=null)
				writeBlockBody(c.getBlockBody(),2);
		}
		else {
			fileWriter.append("\t\t\tparent::"+n+"(");
			writeParameters(c.getParameters());
			fileWriter.append(");\n");
		}

		fileWriter.append("\t}\n");

		if(c!=null)
			m_aPHPMethods.add(new PHPMethod(currentMethod,n));

		currentMethod = null;
		m_aCurrentVariables.clear();

	}

	private void writeTabs(int depth) {
		for(int i=0;i<depth;i++) {
			fileWriter.append("\t");
		}
	}

	private void writeAssignment(Assignment a) {
		if(a.getType()!=null) {
			m_aCurrentVariables.add(a);
		}
		Type pType = a.getType();
		if(pType==null) {
			pType = getTypeOf(a.getVariable(),m_java,curClass,null);
		}
		writeVariable(a.getVariable());
		if(a.getEquals()!=null) {
			fileWriter.append("=");
			if(pType!=null) {
				if(isPrimitiveWrapper(pType)) {
					fileWriter.append("new ");
					writeType(pType);
					fileWriter.append("(");
					writeEquals(a.getEquals());
					fileWriter.append(")");
				} else {
					writeEquals(a.getEquals());
				}
			} else {
				writeEquals(a.getEquals());
			}
		}
	}

	private boolean isStaticMethod(String methodName) {

		for(PublicMethod pp : curClass.getPublicMethods()) {
			if(pp.getStatic()&&pp.getName().equals(methodName)) {
				return true;
			}
		}

		for(PrivateMethod pp : curClass.getPrivateMethods()) {
			if(pp.getStatic()&&pp.getName().equals(methodName)) {
				return true;
			}
		}
		
		for(ProtectedMethod pp : curClass.getProtectedMethods()) {
			if(pp.getStatic()&&pp.getName().equals(methodName)) {
				return true;
			}
		}

		return false;

	}

	private void addIncludedFile(String s) {
		if(!m_sIncludedFiles.contains(s)) {
			m_sIncludedFiles.add(s);
		}
	}

	/**
	 * Returns the full class name for a given type relative to this class.
	 * This allows the PHP to know the class that is being used and removes
	 * any need for namespacing. Namespacing can be hit or miss within different
	 * versions of PHP.
	 * @param t The type to get the PHP namespaced class name for.
	 * @returns String The PHP namespaced class name.
	 */
	private String getFullClassName(Type t) {
		String sName = t.getName();
		if(m_java.getMainClass().getClassName().getName().equals(sName)) {
			if(m_java.getPackage()!=null) {
				return m_java.getPackage().getPath() + "." + sName;
			} else {
				return sName;
			}
		} else {
			for(ClassDeclaration cd : m_java.getMainClass().getPrivateClasses()) {
				if(cd.getClassName().getName().equals(sName)) {
					return sName;
				}
			}
		}
		if(!isPrimitiveType(t)&&!sName.equals("enum")) {
			JavaFile j = getJavaFile(m_java,t);
			if(j.getPackage()!=null) {
				String sClassCheck = "."+j.getMainClass().getClassName().getName();
				if(j.getPackage().getPath().equals("PLACEHOLDERS")) {
					for(String includedFile : m_sIncludedFiles) {
						if(includedFile.endsWith(sClassCheck)) {
							return includedFile;
						}
					}
				}
				return j.getPackage().getPath()+sClassCheck;
			} else {
				return j.getMainClass().getClassName().getName();
			}

		}

		return sName;

	}

	private void writeType(Type t) {
		String s = t.getName();

		if(s.indexOf(".")>-1) {
			fileWriter.append(s.replaceAll("\\.","_"));
		} else {

			String sFullName = getFullClassName(t);
			if(m_aReservedWords.contains(sFullName.toLowerCase())) {
				fileWriter.append("_");
			}
			fileWriter.append(sFullName.replaceAll("\\.","_"));

		}
	}

	private boolean isDelimeter(Object value) {
		return value instanceof AdditionalAssignment ||
			value instanceof LogicalNotEqual ||
			value instanceof InstanceOf ||
			value instanceof LogicalEqual ||
			value instanceof Addition ||
			value instanceof IncrementorAssignment ||
			value instanceof Multiply ||
			value instanceof Subtraction ||
			value instanceof LogicalLessThanOrEqualTo ||
			value instanceof LessThan ||
			value instanceof LogicalGreaterThanOrEqualTo ||
			value instanceof GreaterThan ||
			value instanceof LogicalOr ||
			value instanceof BitwiseOr ||
			value instanceof LogicalAnd ||
			value instanceof BitwiseAnd ||
			value instanceof ConditionalStarter ||
			value instanceof ConditionalElse ||
			value instanceof Return||
			value instanceof Not||
			value instanceof Assigner||
			value instanceof Remainder||
			value instanceof Divide;
	}

	private ArrayList<Object> getSequence(ArrayList<Object> ori,int iStart,boolean headRight) {
		ArrayList<Object> aResults = new ArrayList<Object>(1);
		if(headRight) {
			for(int i=iStart+1,length=ori.size();i<length;i++) {
				Object o = ori.get(i);
				if(isDelimeter(o)) {
					break;
				} else {
					aResults.add(o);
				}
			}
		} else {
			for(int i=iStart-1;i>=0;i--) {
				Object o = ori.get(i);
				if(isDelimeter(o)) {
					break;
				} else {
					aResults.add(0,o);
				}
			}
		}
		return aResults;
	}

	private Property getPropertyByName(String s) {

		for(PrivateProperty pp : curClass.getPrivateProperties()) {
			if(s.equals(pp.getName())) {
				return pp;
			}
		}

		for(ProtectedProperty pp : curClass.getProtectedProperties()) {
			if(s.equals(pp.getName())) {
				return pp;
			}
		}

		for(PublicProperty pp : curClass.getPublicProperties()) {
			if(s.equals(pp.getName())) {
				return pp;
			}
		}

		return null;

	}
	
	private String findPackage(String s) {
		for(String path : m_aSourceDirs) {
			File f = new File(path + "/" + s);
			if(f.exists()&&f.isDirectory()) {
				return path + "/" + s;
			}
		}
		throw new RuntimeException("Could not find package for: "+s);
	}
	
	private String findFile(String s) {
		for(String path : m_aSourceDirs) {
			File f = new File(path + "/" + s);
			if(f.exists()&&f.isFile()) {
				return path + "/" + s;
			}
		}
		return null;
	}

	private void getIncludedFilesFromJavaFile(JavaFile j) {

		if(j.getPackage()!=null) {
			String sPackageName = j.getPackage().getPath();
			if(!sPackageName.equals("PLACEHOLDERS")) {
				String sPackageDir = findPackage(sPackageName.replaceAll("\\.","/"));
				if(!m_sIncludedDirs.contains(sPackageDir)) {
					File sDir = new File(sPackageDir);
					if(sDir.isDirectory()) {
						m_sIncludedDirs.add(sPackageDir);
						String[] sFiles = sDir.list();
						for(int i=0;i<sFiles.length;i++) {
							if(sFiles[i].endsWith(".java")) {
								addIncludedFile(sPackageName.replaceAll("/",".")+"."+sFiles[i].substring(0,sFiles[i].length()-5));
							}
						}
					} else {
						System.err.println(sPackageName.replaceAll("\\.","/")+" is not a directory!");
					}
				}
			}

		}

		for(Import imp : j.getImports()) {
			String sPath = imp.getPath();
			if(sPath.endsWith("*")) {
				String sDire = sPath.substring(0,sPath.length()-1).replaceAll("\\.","/");
				String sBase = findPackage(sDire);
				if(!m_sIncludedDirs.contains(sBase)) {
					File sDir = new File(sBase);
					if(sDir.isDirectory()) {
						m_sIncludedDirs.add(sBase);
						String[] sFiles = sDir.list();
						for(int i=0;i<sFiles.length;i++) {
							if(sFiles[i].endsWith(".java")) {
								String sT = sDire+sFiles[i].substring(0,sFiles[i].length()-5);
								addIncludedFile(sT.replaceAll("/","."));
							}
						}
					} else {
						System.err.println("Unable to find import "+sPath);
					}
				}
			} else {
				addIncludedFile(sPath.replaceAll("/","."));
			}
		}

	}

	private JavaFile getJavaFile(JavaFile j, Type t) {

		if(curClass.hasPrivateClass(t)) {
			return null;
		}

		String sClassName = t.getName();

		JavaFile jCached = m_aJavaFiles.get(sClassName);

		if(jCached!=null) {
			return jCached;
		} else if(!(t.getName().equals("String")||t.getName().equals("java.lang.String"))&&isPrimitiveType(t)) {
			return null;
		}

		getIncludedFilesFromJavaFile(j);
		
		for(String sIncludedFile : m_sIncludedFiles) {
			if(sIncludedFile.endsWith("."+sClassName)||sIncludedFile.equals(sClassName)) {
				FileTranslator file = new FileTranslator(findFile(sIncludedFile.replaceAll("\\.","/")+".java"));
				JavaFile jf = file.translate();
				getIncludedFilesFromJavaFile(jf);
				m_aJavaFiles.put(t.getName(),jf);
				return jf;
			}
		}

		String sName = t.getName().replaceAll("\\.","/");
		String sFile = findFile(sName+".java");

		if(sFile==null) {
			throw new RuntimeException("Could not find file "+sName+".java in source path.");
		}

		File sCheck = new File(findFile(sName+".java"));
		if(sCheck.isFile()) {
			FileTranslator file = new FileTranslator(findFile(sName+".java"));
			JavaFile jf = file.translate();
			getIncludedFilesFromJavaFile(jf);
			m_aJavaFiles.put(t.getName(),jf);
			return jf;
		}

		throw new RuntimeException("Unable to find java file for type "+t.getName()+" in "+j.getMainClass().getClassName().getName());
	}

	private boolean isAString(Object s) {
		Type tCheck = getTypeOf(s,m_java,curClass,null);
		if(tCheck!=null) {
			String sType = tCheck.getName();
			return sType.equals("String")||sType.equals("Character")||sType.equals("java.lang.String")||sType.equals("java.lang.Character");
		} else {
			return false;
		}
	}

	private void fixUnsignedShiftRight(ArrayList<Object> aValues) {
		for(int i=0;i<aValues.size();i++) {
			if(aValues.get(i) instanceof BitwiseUnsignedShiftRight) {
				int j=0;
				int k=0;
				ArrayList<Argument> args = new ArrayList<Argument>(2);

				for(j=i-1;j>=0;j--) {
					Object value = aValues.get(j);
					if(isDelimeter(value))
						break;
				}

				int iStartFirst = j+1;
				int iEndFirst = 0;
				Argument a1 = new Argument();
				for(k=iStartFirst;k<i;k++) {
					a1.addValue(aValues.remove(iStartFirst));
				}
				args.add(a1);

				boolean bEndWithAdd = true;

				while(bEndWithAdd) {

					bEndWithAdd = false;
					aValues.remove(iStartFirst);
					for(j=iStartFirst;j<aValues.size();j++) {
						Object value = aValues.get(j);
						if(isDelimeter(value)) {
							if(value instanceof BitwiseUnsignedShiftRight) {
								bEndWithAdd = true;
							}
							break;
							}
						}
						iEndFirst = j;
						Argument a3 = new Argument();
						for(k=iStartFirst;k<iEndFirst;k++) {
							a3.addValue(aValues.remove(iStartFirst));
						}
						args.add(a3);
					}

					FunctionCall fc = new FunctionCall();
					fc.setName("JavaBase.unsignedRightShift");
					fc.setArguments(args);
					aValues.add(iStartFirst,fc);

			}
		}
	}

	private void fixStrings(ArrayList<Object> aValues) {
		for(int i=0;i<aValues.size();i++) {
			if(aValues.get(i) instanceof Addition) {
				if(isAString(getSequence(aValues,i,false))||(i+1<aValues.size()&&isAString(getSequence(aValues,i,true)))) {
					int j=0;
					int k=0;
					ArrayList<Argument> args = new ArrayList<Argument>(2);

					for(j=i-1;j>=0;j--) {
						Object value = aValues.get(j);
						if(isDelimeter(value))
							break;
					}
					
					int iStartFirst = j+1;
					int iEndFirst = 0;
					Argument a1 = new Argument();
					for(k=iStartFirst;k<i;k++) {
						a1.addValue(aValues.remove(iStartFirst));
					}
					args.add(a1);

					boolean bEndWithAdd = true;

					while(bEndWithAdd) {

						bEndWithAdd = false;
						aValues.remove(iStartFirst);
						for(j=iStartFirst;j<aValues.size();j++) {
							Object value = aValues.get(j);
							if(isDelimeter(value)) {
								if(value instanceof Addition) {
									bEndWithAdd = true;
								}
								break;
							}
						}
						iEndFirst = j;
						Argument a3 = new Argument();
						for(k=iStartFirst;k<iEndFirst;k++) {
							a3.addValue(aValues.remove(iStartFirst));
						}
						args.add(a3);
					}

					FunctionCall fc = new FunctionCall();
					fc.setName("JavaBase.concat");
					fc.setArguments(args);
					aValues.add(iStartFirst,fc);

				}
			}
		}
	}

	private boolean isParameter(String sName) {
		if(currentMethod!=null) {
			for(Parameter p : currentMethod.getParameters()) {
				if(p.getName().equals(sName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Type getParameterType(String s) {
		if(currentMethod!=null) {
			for(Parameter p : currentMethod.getParameters()) {
				if(p.getName().equals(s)) {
					return p.getType();
				}
			}
		}
		return null;
	}

	private void writeVariable(Variable v) {
		String vName = v.getName();
		boolean bProperty = false;
		if(vName.startsWith("this.")) {
			vName = vName.substring(5);
			bProperty = true;
		}
		if(vName.indexOf(".")!=-1) {
			if(vName.endsWith(".length")) {
				fileWriter.append("count($"+vName.substring(0,vName.lastIndexOf("."))+")");
			} else {
				vName = vName.replaceAll("\\.","\\->");
				String sFirst = vName.substring(0,vName.indexOf("->"));
				if(isStaticProperty(sFirst)) {
					writeType(m_java.getMainClass().getClassName());
					fileWriter.append("::$");
					writeType(new Type(sFirst));
					fileWriter.append("->"+vName.substring(vName.indexOf("->")+2));
				} else if(isMemberProperty(sFirst)) {
					fileWriter.append("$this->"+vName);
				} else if(isIncludedClass(sFirst)) {
					writeType(new Type(sFirst));
					fileWriter.append("::$"+vName.substring(vName.indexOf("->")+2));
				} else {
					fileWriter.append("$"+vName);
				}
			}
		} else if(bProperty) {
			if(isStaticProperty(vName)) {
				writeType(m_java.getMainClass().getClassName());
				fileWriter.append("::$"+vName);
			} else {
				fileWriter.append("$this->"+vName);
			}
		} else if(isLocalVariable(vName)||isParameter(vName)) {
			fileWriter.append("$"+vName);
		} else if(isStaticProperty(vName)) {
			writeType(m_java.getMainClass().getClassName());
			fileWriter.append("::$"+vName);
		} else if(isMemberProperty(vName)) {
			fileWriter.append("$this->"+vName);
		} else {
			fileWriter.append("$"+vName);
		}
	}

	private boolean isLocalVariable(String vName) {
		return getLocalVariable(vName)!=null;
	}
	
	private Assignment getLocalVariable(String vName) {

		for(int x=m_aCurrentVariables.size()-1;x>=0;x--) {
			Assignment a = m_aCurrentVariables.get(x);
			if(a.getVariable().getName().equals(vName)) {
				return a;
			} else if(a.getEquals()!=null) {
				for(int i=0;i<a.getEquals().getValues().size();i++) {
					if(a.getEquals().getValues().get(i) instanceof AdditionalAssignment) {
						AdditionalAssignment aa = (AdditionalAssignment)a.getEquals().getValues().get(i);
						if(aa.getAssignment().getVariable().getName().equals(vName)) {
							aa.getAssignment().setType(a.getType());
							return aa.getAssignment();
						}
					}
				}
			}
		}

		if(vName.indexOf("[")>-1) {
			return getLocalVariable(vName.substring(0,vName.indexOf("[")));
		}

		return null;
	}

	private void writeArrayDeclaration(ArrayDeclaration a) {
		ArrayList<Object> items = a.getItems();
		fileWriter.append("array(");
		for(int i=0,length=items.size();i<length;i++) {
			Object item = items.get(i);
			if(item instanceof ArrayDeclaration) {
				writeArrayDeclaration((ArrayDeclaration)item);
			} else {
				writeEquals((Equals)item);
			}
			if(i<length-1)
				fileWriter.append(",");
		}
		fileWriter.append(")");
	}

	private Type getTypeOf(Object o,JavaFile currentFile,ClassDeclaration currentScope,Type activeType) {
		if(o instanceof FunctionCall) {

			FunctionCall f = (FunctionCall)o;

			String vName = f.getName();

			int iEndPeriod = vName.lastIndexOf(".");
			if(iEndPeriod!=-1) {
				String sVariable = vName.substring(0,iEndPeriod);
				if(!sVariable.isEmpty()) {
					activeType = getTypeOf(new Variable(sVariable),currentFile,currentScope,activeType);
					if(activeType!=null) {
						JavaFile jf2 = getJavaFile(currentFile,activeType);
						currentScope = jf2.getMainClass();
						currentFile = jf2;
					} else {
						throw new RuntimeException("Error checking type of variable "+sVariable+" of class "+currentScope.getClassName().getName());
					}
				}
			} else if(vName.equals("super")) {
				return new Type("void");
			}

			while(true) {
				activeType = getMethodType(f,currentScope,activeType);

				if(activeType==null&&currentScope.getExtends()!=null) {
					JavaFile j = getJavaFile(currentFile,currentScope.getExtends());
					currentScope = j.getMainClass();
					currentFile = j;
				} else if(activeType!=null) {

					if(isPrimitiveType(activeType)) {
						return activeType;
					}
					String rType = activeType.getName();
					if(f.getNextCall()!=null) {
						JavaFile jf2 = getJavaFile(currentFile,activeType);
						if(jf2!=null) {
							return getTypeOf(f.getNextCall(),jf2,jf2.getMainClass(),activeType);
						} else if(curClass.hasPrivateClass(activeType)) {
							return getTypeOf(f.getNextCall(),jf2,curClass.getPrivateClass(activeType),activeType);
						} else {
							throw new RuntimeException("Could not find "+activeType.getName()+" in "+curClass.getClassName().getName()+".");
						}
					} else {
						return activeType;
					}
				} else {
					throw new RuntimeException("Unable to find method "+vName+" in class "+curClass.getClassName().getName()+" with package "+currentFile.getPackage().getPath()+" "+currentScope.getClassName().getName());
				}
			}

		} else if(o instanceof InlineCharacter) {
			return new Type("char");
		} else if(o instanceof InlineNumber) {
			return new Type("int");
		} else if(o instanceof InlineString) {
			return new Type("String");
		} else if(o instanceof ObjectDeclaration) {
			return ((ObjectDeclaration)o).getType();
		} else if(o instanceof Parenthesis) {
			Parenthesis p = (Parenthesis)o;
			ArrayList<Object> eValues = p.getEquals().getValues();
			if(eValues.size()==1) {
				return getTypeOf(eValues.get(0),currentFile,currentScope,activeType);
			} else if(eValues.size()==0) {
				return new Type("void");
			} else {

				if(valuesContainsPrimitive(eValues,"String","java_lang_String"))
					return new Type("String");
				else if(valuesContainsPrimitive(eValues,"boolean","java_lang_Boolean"))
					return new Type("boolean");
				else if(valuesContainsPrimitive(eValues,"double","java_lang_Double"))
					return new Type("double");
				else if(valuesContainsPrimitive(eValues,"float","java_lang_Float"))
					return new Type("float");
				else if(valuesContainsPrimitive(eValues,"long","java_lang_Long"))
					return new Type("long");
				else if(valuesContainsPrimitive(eValues,"int","java_lang_Integer"))
					return new Type("int");
				else if(valuesContainsPrimitive(eValues,"short","java_lang_Short"))
					return new Type("short");
				else if(valuesContainsPrimitive(eValues,"byte","java_lang_Byte"))
					return new Type("byte");
				else if(valuesContainsPrimitive(eValues,"char","java_lang_Char"))
					return new Type("char");
				else if(eValues.get(0) instanceof TypeCast) {
					return getTypeOf(eValues.get(0),currentFile,currentScope,activeType);
				} else if(eValues.size()>2&&eValues.get(0) instanceof Variable&&eValues.get(1) instanceof Assigner) {
					return getTypeOf(eValues.get(0),currentFile,currentScope,activeType);
				} else if(valuesContainsInstanceOf(eValues)) {
					return new Type("boolean");
				} else {
					throw new RuntimeException("Unknown parenthesis return type in "+curClass.getClassName().getName());
				}
			}
		} else if(o instanceof Variable) {
			String vName = ((Variable)o).getName();

			String[] aParts = vName.split("\\.");
			String sFirstPart = aParts[0];
			int iDepth = 0;

			if(sFirstPart.equals("this")) {
				iDepth++;
				if(currentScope.getInline()) {
					activeType = currentScope.getExtends();
				} else {
					activeType = currentScope.getClassName();
				}
			} else if(sFirstPart.equals("super")) {
				iDepth++;
				activeType = currentScope.getExtends();
			} else if(vName.endsWith(".class")) {
				return new Type("java.lang.Class");
			} else if(sFirstPart.isEmpty()) {
				iDepth++;
			} else if(isParameter(sFirstPart)) {
				activeType = getParameterType(sFirstPart);
				if(isIncludedClass(activeType.getName())) {
					JavaFile jf2 = getJavaFile(currentFile,activeType);
					currentFile = jf2;
					currentScope = jf2.getMainClass();
					iDepth++;
				} else if(isPrimitiveType(activeType)) {
					iDepth++;
				} else {
					throw new RuntimeException("Error checking type of parameter "+activeType.getName()+" "+aParts[iDepth]+" of class "+currentScope.getClassName().getName());
				}
			} else if(isLocalVariable(sFirstPart)) {
				Assignment v = getLocalVariable(sFirstPart);
				activeType = v.getType();
				JavaFile jf2 = getJavaFile(currentFile,activeType);
				currentFile = m_java;
				if(jf2!=null) {
					currentScope = jf2.getMainClass();
					iDepth++;
				} else if(isPrimitiveType(activeType)) {
					iDepth++;
				} else {
					System.err.println("Error checking type of local variable "+activeType.getName()+" "+aParts[iDepth]+" of class "+curClass.getClassName().getName());
					return null;
				}
			} else {
				//Check for some static call
				String sCheck = sFirstPart;

				int iDepth2 = iDepth;
				while(iDepth2 < aParts.length) {
					if(isIncludedClass(sCheck)) {
						JavaFile jf2 = getJavaFile(currentFile,new Type(sCheck));
						currentScope = jf2.getMainClass();
						currentFile = jf2;
						activeType = new Type(sCheck);
						iDepth = iDepth2+1;
						break;
					} else {
						++iDepth2;
						if(iDepth2>=aParts.length) {
							break;
						}
						sCheck = sCheck + "." + aParts[iDepth2];
					}
				}

			}

			while(iDepth < aParts.length) {
				String sPart = aParts[iDepth];
				//Check for the property
				while(currentScope.getPropertyType(sPart)==null) {
					if(currentScope.getExtends()!=null&&isIncludedClass(currentScope.getExtends().getName())) {
						JavaFile jf2 = getJavaFile(currentFile,currentScope.getExtends());
						currentFile = jf2;
						currentScope = jf2.getMainClass();
					} else if(sPart.equals("length")) {
						//TODO: why is this only an int?
						return new Type("int");
					} else {
						throw new RuntimeException("Error finding property "+vName+" "+sPart+" of class "+curClass.getClassName().getName()+" "+currentScope.getClassName().getName());
					}
				}
				activeType = currentScope.getPropertyType(sPart);
				JavaFile jf2 = getJavaFile(currentFile,activeType);
				if(jf2!=null) {
					currentScope = jf2.getMainClass();
					iDepth++;
				} else if(currentScope.hasPrivateClass(activeType)) {
					return activeType;
				} else if(isPrimitiveType(activeType)) {
					return activeType;
				} else if(isEnum(activeType)) {
					return new Type("enum");
				} else {
					throw new RuntimeException("Error checking type of local property "+sPart+" of class "+curClass.getClassName().getName());
				}
			}
			return activeType;
		} else if(o instanceof TypeCast) {
			return ((TypeCast)o).getType();
		} else if(o instanceof Null) {
			return null;
		} else if(o instanceof PrimitiveBoolean) {
			return new Type("boolean");
		} else if(o instanceof List) {
			//SEQUENCE OF FUNCTION CALLS ETCEDRA
			List aSequence = (List)o;
			for(int i=0,length=aSequence.size();i<length;i++) {
				if(!(aSequence.get(i) instanceof ArrayCaller||aSequence.get(i) instanceof Incrementor||aSequence.get(i) instanceof Decrementor)) {
					if(aSequence.get(i) instanceof TypeCast) {
						return ((TypeCast)aSequence.get(i)).getType();
					} else {
						activeType = getTypeOf(aSequence.get(i),currentFile,currentScope,activeType);
						if(activeType!=null&&!isPrimitiveType(activeType)) {
							currentFile = getJavaFile(currentFile,activeType);
							if(currentFile!=null)
								currentScope = currentFile.getMainClass();
							else if(currentScope.hasPrivateClass(activeType)) {
								currentScope = currentScope.getPrivateClass(activeType);
							}
						}
					}
				}
			}
			return activeType;
		} else if(o instanceof Assignment) {
			return ((Assignment)o).getType();
		} else if(o instanceof Type) {
			return (Type)o;
		} else {
			System.out.println("Get Type Of Unknown: "+o);
			Thread.dumpStack();
		}
		return null;
	}

	private boolean valuesContainsInstanceOf(ArrayList<Object> values) {
		for(Object value : values) {
			if(value instanceof InstanceOf) {
				return true;
			}
		}
		return false;
	}

	private boolean valuesContainsPrimitive(ArrayList<Object> values,String primitive,String wrapper) {
		for(int i=0,length=values.size();i<length;i++) {
			if(!isDelimeter(values.get(i))) {
				ArrayList<Object> seq = getSequence(values,i-1,true);
				Type tCheck = getTypeOf(seq,m_java,curClass,null);
				while(i<length&&!isDelimeter(values.get(i))) {
					i++;
				}
				if(tCheck!=null&&(tCheck.getName().equals(primitive)||getFullClassName(tCheck).equals(wrapper))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isPrimitiveWrapper(Type t) {
		String s = getFullClassName(t);
		return
			s.startsWith("java.lang.")&&(
				s.equals("java.lang.Byte")||
				s.equals("java.lang.Double")||
				s.equals("java.lang.Integer")||
				s.equals("java.lang.Short")||
				s.equals("java.lang.Float")||
				s.equals("java.lang.Long")||
				s.equals("java.lang.Boolean")||
				s.equals("java.lang.Character")
			);
	}
	
	private String getDefaultReturnForWrapper(Type t) {
		String s = getFullClassName(t);
		if(s.equals("java.lang.Byte")) {
			return "byteValue";
		} else if(s.equals("java.lang.Double")) {
			return "doubleValue";
		} else if(s.equals("java.lang.Integer")) {
			return "intValue";
		} else if(s.equals("java.lang.Short")) {
			return "shortValue";
		} else if(s.equals("java.lang.Float")) {
			return "floatValue";
		} else if(s.equals("java.lang.Long")) {
			return "longValue";
		} else if(s.equals("java.lang.Boolean")) {
			return "booleanValue";
		} else if(s.equals("java.lang.Character")) {
			return "charValue";
		} else {
			return null;
		}
	}

	private void writeFunctionCall(FunctionCall f) {

		String fName = f.getName();
		if(fName.indexOf(".")!=-1) {

			//Check for some static call
			String[] aParts = fName.split("\\.");
			int iDepth = 0;
			String sCheck = aParts[iDepth];
			boolean bStatic = false;
			while(iDepth < aParts.length -1) {
				if(isIncludedClass(sCheck)) {
					writeType(new Type(sCheck));
					fileWriter.append("::");
					if(!aParts[aParts.length-1].equals(fName.substring(sCheck.length()+1))) {
						fileWriter.append("$");
					}
					fileWriter.append(fName.substring(sCheck.length()+1).replaceAll("\\.","->"));
					bStatic = true;
					break;
				} else {
					sCheck = sCheck + "." + aParts[++iDepth];
				}
			}

			if(!bStatic) {
				fName = fName.replaceAll("\\.","->");
				if(fName.startsWith("->")) {
					fileWriter.append(fName);
				} else {
					String s = fName.substring(0,fName.indexOf("->"));
					if(isIncludedClass(s)) {
						writeType(new Type(s));
						fileWriter.append("::");
						String whatsLeft = fName.substring(fName.indexOf("->")+2);
						if(whatsLeft.indexOf("->")>-1) {
							fileWriter.append("$");
						}
						fileWriter.append(whatsLeft);
					} else if(fName.startsWith("System->out")) {
						fileWriter.append("System::$out");
						fName = fName.substring(11);
						fileWriter.append(fName);
					} else if(fName.startsWith("JavaBase->concat")) {
						fileWriter.append("__concat");
					} else if(fName.startsWith("Math->min")) {
						fileWriter.append("Math::min");
					} else if(fName.startsWith("System->err")) {
						fileWriter.append("System::$err");
						fName = fName.substring(11);
						fileWriter.append(fName);
					} else {
						String sFirst = fName.substring(0,fName.indexOf("->"));
						if(isStaticProperty(sFirst)) {
							writeType(curClass.getClassName());
							fileWriter.append("::$"+sFirst);
						} else if(isLocalVariable(sFirst)||isParameter(sFirst)) {
							fileWriter.append("$"+sFirst);
						} else if(isMemberProperty(sFirst)) {
							fileWriter.append("$this->"+sFirst);
						} else {
							fileWriter.append("$"+sFirst);
						}
						fName = fName.substring(fName.indexOf("->"));
						fileWriter.append(fName);
					}
				}
			}
		} else {
			if(fName.equals("super")) {
				fileWriter.append("parent::__construct");
			} else {
				if(isStaticMethod(fName)||isStaticProperty(fName)) {
					writeType(m_java.getMainClass().getClassName());
					fileWriter.append("::"+fName);
				} else {
					fileWriter.append("$this->"+fName);
				}
			}
		}

		fileWriter.append("(");
		ArrayList<Argument> arguments = f.getArguments();
		for(int j=0;j<arguments.size();j++) {
			writeEquals((Equals)arguments.get(j));
			if(j<arguments.size()-1)
				fileWriter.append(",");
		}
		fileWriter.append(")");

		if(f.getNextCall()!=null) {
			writeFunctionCall(f.getNextCall());
		}
	}

	private void fixPrimitiveWrappers(ArrayList<Object> values) {
		for(int i=0,length=values.size();i<length;i++) {
			if(!isDelimeter(values.get(i))) {
				ArrayList<Object> seq = getSequence(values,i-1,true);
				Type tCheck = getTypeOf(seq,m_java,curClass,null);
				while(i<length&&!isDelimeter(values.get(i))) {
					i++;
				}
				Object value = values.get(i-1);
				if(tCheck!=null&&isPrimitiveWrapper(tCheck)) {
					String sWrapperFunc = getDefaultReturnForWrapper(tCheck);
					if(value instanceof Variable) {
						if(i+1>=length) {
							values.add(new FunctionCall("."+sWrapperFunc+"()"));
						} else {
							values.add(i++,new FunctionCall("."+sWrapperFunc+"()"));
						}
						length++;
					} else if(value instanceof FunctionCall) {
						appendFunctionCall((FunctionCall)value,new FunctionCall("."+sWrapperFunc+"()"));
					} else {
						
					}
				}
			}
		}
	}

	private void appendFunctionCall(FunctionCall b, FunctionCall n) {
		while(b.getNextCall()!=null) {
			b = b.getNextCall();
		}
		b.setNextCall(n);
	}


	private void fixParenthesis(ArrayList<Object> aValues) {
		for(int i=0;i<aValues.size();i++) {
			if(aValues.get(i) instanceof Parenthesis) {
				Parenthesis p = (Parenthesis)aValues.get(i);
				if(p.getEquals().getValues().size()==1&&!(p.getEquals().getValues().get(0) instanceof ObjectDeclaration)) {
					aValues.remove(i);
					aValues.add(i,p.getEquals().getValues().get(0));
					i--;
				}
			}
		}
	}

	private void fixIntegerDivision(ArrayList<Object> aValues) {
		for(int i=1;i<aValues.size();i++) {
			if(aValues.get(i) instanceof Divide) {
				Type leftType = getTypeOf(aValues.get(i-1),m_java,curClass,null);
				Type rightType = getTypeOf(aValues.get(i+1),m_java,curClass,null);
				if(
					(leftType.getName().equals("int")||getFullClassName(leftType).equals("java.lang.Integer")) &&
					(rightType.getName().equals("int")||getFullClassName(rightType).equals("java.lang.Integer"))
				) {

					FunctionCall f = new FunctionCall();
					f.setName("JavaBase.integerDivision");

					Argument a1 = new Argument();
					a1.addValue(aValues.get(i-1));
					f.getArguments().add(a1);
					
					Argument a2 = new Argument();
					a2.addValue(aValues.get(i+1));
					f.getArguments().add(a2);

					aValues.remove(i-1);
					aValues.remove(i-1);
					aValues.remove(i-1);

					aValues.add(i-1,f);

					i--;

				}
			}
		}
	}

	private void fixIncrementor(ArrayList<Object> aValues) {
		for(int i=0;i<aValues.size();i++) {
			int iNextIndex = i;
			String sType = "";

			if(aValues.get(i) instanceof Incrementor && i>0 && (aValues.get(i-1) instanceof Variable || aValues.get(i-1) instanceof Parenthesis)) {
				iNextIndex = i-1;
				sType = "increment";
			} else if(aValues.get(i) instanceof Incrementor && i+1 < aValues.size() && (aValues.get(i+1) instanceof Variable || aValues.get(i+1) instanceof Parenthesis)) {
				iNextIndex = i+1;
				sType = "increment";
			} else if(aValues.get(i) instanceof Decrementor && i>0 && (aValues.get(i-1) instanceof Variable || aValues.get(i-1) instanceof Parenthesis)) {
				iNextIndex = i-1;
				sType = "decrement";
			} else if(aValues.get(i) instanceof Decrementor && i+1 < aValues.size() && (aValues.get(i+1) instanceof Variable || aValues.get(i+1) instanceof Parenthesis)) {
				iNextIndex = i+1;
				sType = "decrement";
			}

			if(iNextIndex!=i&&isPrimitiveWrapper(getTypeOf(aValues.get(iNextIndex),m_java,curClass,null))) {
				ArrayList<Argument> args = new ArrayList<Argument>(2);
				
				Argument a1 = new Argument();
				a1.addValue(aValues.get(iNextIndex));

				args.add(a1);
				args.add(new Argument("1"));

				FunctionCall fc = new FunctionCall();
				fc.setArguments(args);

				if(iNextIndex==i+1) {
					fc.setName("JavaBase."+sType+"Before");
					aValues.remove(i);
					aValues.remove(i);
					aValues.add(i,fc);
				} else {
					fc.setName("JavaBase."+sType+"After");
					aValues.remove(i-1);
					aValues.remove(i-1);
					aValues.add(i-1,fc);
				}

			} else if((aValues.get(i) instanceof BitwiseXOrAssignment || aValues.get(i) instanceof BitwiseOrAssignment || aValues.get(i) instanceof BitwiseAndAssignment || aValues.get(i) instanceof BitwiseUnsignedShiftRightAssignment || aValues.get(i) instanceof BitwiseShiftLeftAssignment || aValues.get(i) instanceof BitwiseShiftRightAssignment || aValues.get(i) instanceof DividorAssignment || aValues.get(i) instanceof MultipliorAssignment || aValues.get(i) instanceof IncrementorAssignment ||aValues.get(i) instanceof DecrementorAssignment) && i>0 && (aValues.get(i-1) instanceof Variable || aValues.get(i-1) instanceof Parenthesis)) {

				Parenthesis p = new Parenthesis();

				Equals e = new Equals();

				Assignment a = new Assignment();
				a.setVariable((Variable)aValues.get(i-1));

				e.addValue(a);

				Equals e1 = new Equals();

				Parenthesis p2 = new Parenthesis();
				Equals ep2 = new Equals();
				ep2.addValue(aValues.get(i-1));
				p2.setEquals(ep2);
				e1.addValue(p2);

				if(aValues.get(i) instanceof IncrementorAssignment)
					e1.addValue(new Addition());
				else if(aValues.get(i) instanceof DecrementorAssignment)
					e1.addValue(new Subtraction());
				else if(aValues.get(i) instanceof MultipliorAssignment)
					e1.addValue(new Multiply());
				else if(aValues.get(i) instanceof DividorAssignment)
					e1.addValue(new Divide());
				else if(aValues.get(i) instanceof BitwiseShiftRightAssignment)
					e1.addValue(new BitwiseShiftRight());
				else if(aValues.get(i) instanceof BitwiseShiftLeftAssignment)
					e1.addValue(new BitwiseShiftLeft());
				else if(aValues.get(i) instanceof BitwiseUnsignedShiftRightAssignment)
					e1.addValue(new BitwiseUnsignedShiftRight());
				else if(aValues.get(i) instanceof BitwiseAndAssignment)
					e1.addValue(new BitwiseAnd());
				else if(aValues.get(i) instanceof BitwiseOrAssignment)
					e1.addValue(new BitwiseOr());
				else if(aValues.get(i) instanceof BitwiseXOrAssignment)
					e1.addValue(new BitwiseXOr());
				
				Parenthesis p3 = new Parenthesis();
				Equals ep3 = new Equals();
				while(i+1<aValues.size()) {
					ep3.addValue(aValues.remove(i+1));
				}
				p3.setEquals(ep3);
				e1.addValue(p3);

				a.setEquals(e1);

				p.setEquals(e);

				aValues.remove(i-1);
				aValues.remove(i-1);
				aValues.add(i-1,p);

			}

		}
	}

	private void writeEquals(Equals e) {
		if(e!=null) {
		
			ArrayList<Object> aValues = e.getValues();

			fixParenthesis(aValues);
			fixUnsignedShiftRight(aValues);
			fixIncrementor(aValues);
			fixIntegerDivision(aValues);
			fixStrings(aValues);
			fixPrimitiveWrappers(aValues);

			int iNumValues = aValues.size();

			for(int i=0;i<iNumValues;i++) {
				Object value = aValues.get(i);
				if(value instanceof InlineNumber) {
					fileWriter.append(((InlineNumber)value).getValue());
				} else if(value instanceof AdditionalAssignment) {
					AdditionalAssignment a = (AdditionalAssignment)value;
					fileWriter.append(",");
					writeAssignment(a.getAssignment());
				} else if(value instanceof ArrayDeclaration) {
					writeArrayDeclaration((ArrayDeclaration)value);
				} else if(value instanceof Assigner) {
					fileWriter.append("=");
				} else if(value instanceof Variable) {
					writeVariable((Variable)value);
				} else if(value instanceof FunctionCall) {
					writeFunctionCall((FunctionCall)value);
				} else if(value instanceof InlineString) {
					InlineString s = (InlineString)value;
					if(!m_aInlineStrings.contains(s.getValue())) {
						m_aInlineStrings.add(s.getValue());
					}
					writeType(curClass.getClassName());
					fileWriter.append("::$_aInlineStrings["+m_aInlineStrings.indexOf(s.getValue())+"]");
				} else if(value instanceof InlineCharacter) {
					InlineCharacter c = (InlineCharacter)value;
					String sValue = c.getValue();
					if(!sValue.equals("'\\''")&&sValue.length()>3) {
						fileWriter.append('"');
						fileWriter.append(sValue.substring(1,3));
						fileWriter.append('"');
					} else {
						fileWriter.append(c.getValue());
					}
				} else if(value instanceof ArrayCaller) {
					fileWriter.append("[");
					writeEquals(((ArrayCaller)value).getEquals());
					fileWriter.append("]");
				} else if(value instanceof LogicalOr) {
					fileWriter.append("||");
				} else if(value instanceof LogicalAnd) {
					fileWriter.append("&&");
				} else if(value instanceof LogicalEqual) {
					fileWriter.append("==");
				} else if(value instanceof LogicalNotEqual) {
					fileWriter.append("!=");
				} else if(value instanceof LogicalLessThanOrEqualTo) {
					fileWriter.append("<=");
				} else if(value instanceof LessThan) {
					fileWriter.append("<");
				} else if(value instanceof GreaterThan) {
					fileWriter.append(">");
				} else if(value instanceof InstanceOf) {
					fileWriter.append(" instanceof ");
				} else if(value instanceof Type ) {
					writeType((Type)value);
				} else if(value instanceof Addition) {
					fileWriter.append("+");
				} else if(value instanceof PrimitiveBoolean) {
					PrimitiveBoolean pb = (PrimitiveBoolean)value;
					if(pb.getValue()) {
						fileWriter.append("true");
					} else {
						fileWriter.append("false");
					}
				} else if(value instanceof TypeCast) {
					fileWriter.append("");
				} else if(value instanceof Not) {
					fileWriter.append("!");
				} else if(value instanceof Subtraction) {
					fileWriter.append("-");
				} else if(value instanceof Multiply) {
					fileWriter.append("*");
				} else if(value instanceof BitwiseAnd) {
					fileWriter.append("&");
				} else if(value instanceof BitwiseOr) {
					fileWriter.append("|");
				} else if(value instanceof BitwiseXOr) {
					fileWriter.append("^");
				} else if(value instanceof BitwiseComplement) {
					fileWriter.append("~");
				} else if(value instanceof Remainder) {
					fileWriter.append("%");
				} else if(value instanceof BitwiseShiftRight) {
					fileWriter.append(">>");
				} else if(value instanceof BitwiseShiftLeft) {
					fileWriter.append("<<");
				} else if(value instanceof Assignment) {
					writeAssignment((Assignment)value);
				} else if(value instanceof Divide) {
					fileWriter.append("/");
				} else if(value instanceof ObjectDeclaration) {
					ObjectDeclaration o = (ObjectDeclaration)value;
					if(o.isArrayDeclaration()) {
						writeArrayDeclaration(o.getArrayDeclaration());
					} else {
						fileWriter.append("__parenthesis(");
						ArrayList<Argument> arguments = o.getArguments();
						fileWriter.append("new ");
						if(o.getInlineClass()!=null) {
							writeType(m_java.getMainClass().getClassName());
							fileWriter.append("_");
							writeType(o.getType());
							fileWriter.append("_"+m_iPrivateClasses);
						} else {
							writeType(o.getType());
						}
						fileWriter.append("(");
						if(o.getType().getTypeList()!=null) {
							for(int j=0,length=o.getType().getTypeList().size();j<length;j++) {
								fileWriter.append("\""+o.getType().getTypeList().get(j).getName()+"\"");
								if(j<length-1) {
									fileWriter.append(",");
								}
							}
							if(arguments!=null&&arguments.size()>0) {
								fileWriter.append(",");
							}
						}
						for(int j=0;j<arguments.size();j++) {
							writeEquals((Equals)arguments.get(j));
							if(j<arguments.size()-1) {
								fileWriter.append(",");
							}
						}
						fileWriter.append(")");
						
						fileWriter.append(")");
						if(isPrimitiveWrapper(o.getType())) {
							fileWriter.append("->"+getDefaultReturnForWrapper(o.getType())+"()");
						}
						if(o.getInlineClass()!=null) {
							ClassDeclaration c = o.getInlineClass();
							c.setClassName(new Type(getFullClassName(m_java.getMainClass().getClassName())+"_"+getFullClassName(o.getType())+"_"+(m_iPrivateClasses++)));
							c.setExtends(o.getType());
							m_aPrivateClasses.add(c);
						}
					}
				} else if(value instanceof Incrementor) {
					fileWriter.append("++");
				} else if(value instanceof Parenthesis) {
					fileWriter.append("__parenthesis(");
					writeEquals(((Parenthesis)value).getEquals());
					fileWriter.append(")");
				} else if(value instanceof Decrementor) {
					fileWriter.append("--");
				} else if(value instanceof IncrementorAssignment) {
					fileWriter.append("+=");
					//@TODO - This should never get displayed, since we supposedly fix them all earlier.
				} else if(value instanceof DecrementorAssignment) {
					fileWriter.append("-=");
				} else if(value instanceof ConditionalStarter) {
					fileWriter.append("?");
				} else if(value instanceof EnumDeclaration) {
					EnumDeclaration en = (EnumDeclaration)value;
					m_aIncludedEnums.add(en);
					fileWriter.append("new "+curClass.getClassName().getName()+"3num"+en.getName()+"()");
				} else if(value instanceof ConditionalElse) {
					fileWriter.append(":");
				} else if(value instanceof LogicalGreaterThanOrEqualTo) {
					fileWriter.append(">=");
				} else if(value instanceof Null) {
					writeNull((Null)value);
				} else {
					fileWriter.append(value);
				}
			}
		}
	}

	private void writeSynchronized(Synchronized s,int i) {
		writeBlockBody(s.getBlockBody(),i);
	}

	private void writeNull(Null n) {
		fileWriter.append("Translator_JavaBase::$null");
	}

}