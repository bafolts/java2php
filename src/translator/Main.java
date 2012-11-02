package translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;


public final class Main {

	private static ArrayList<String> m_aFilesToParse = new ArrayList<String>();
	private static ArrayList<String> aSearchPaths = new ArrayList<String>();
	private static ArrayList<String> aClassPaths = new ArrayList<String>();
	
	

	private static void getFilesForDirectory(File f) {
		if(!f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()+" is not a directory.");
		}
		File[] aFiles = f.listFiles();
		for(int i=0;i<aFiles.length;i++) {
			if(aFiles[i].isDirectory()) {
				getFilesForDirectory(aFiles[i]);
			} else if(aFiles[i].getName().endsWith(".java")) {
				m_aFilesToParse.add(aFiles[i].getAbsolutePath());
			}
		}

	}

    public static void main(String[] args) {

		if(args.length==0) {

			System.out.println("Usage:");
			System.out.println("\tjava Translator FileToTranslate.java");

		} else {

			String sClassDir = "";
			for(int i=0;i<args.length;i++) {
				if(args[i].equals("-d")) {
					sClassDir = args[++i];
				} else if(args[i].equals("-sourcepath")) {
					String[] sSearchPaths = args[++i].split(";");
					for(int j=0;j<sSearchPaths.length;j++) {
						aSearchPaths.add(sSearchPaths[j]);
					}
				} else if(args[i].equals("-sourcedir")) {
					String sSourceDir = args[++i];
					File f = new File(sSourceDir);
					getFilesForDirectory(f);
				} else {
					translateFile(args[i],sClassDir);
				}
			}

			for(int j=0;j<m_aFilesToParse.size();j++) {
				translateFile(m_aFilesToParse.get(j),sClassDir);
			}

		}

    }
	
	private static void translateFile(String sName,String sClassDir) {

		//@TODO: Add a verbose option.
		//System.out.println("Attempting to translate: "+sName);

		FileTranslator file = new FileTranslator(sName);

		if(sName.lastIndexOf(".")!=-1) {
			sName = sName.substring(0,sName.lastIndexOf("."));
		}

		sName = sName + ".class.php";

		JavaFile f = file.translate();

		if(f.getPackage()!=null) {
			sName = sClassDir + "/" + f.getPackage().getPath().replaceAll("\\.","/");
		} else {
			sName = sClassDir;
		}

		if(sName.endsWith("/")) {
			sName = sName.substring(0,sName.length()-1);
		}

		File sDir = new File(sName);
		if(!sDir.exists()) {
			sDir.mkdirs();
		}

		sName += "/" + f.getMainClass().getClassName().getName() + ".class.php";

		try {
			PHP5File php5File = new PHP5File(f);
			php5File.setSourcePath(aSearchPaths);
			php5File.writeFile(sName);
		} catch(Exception e) {
			System.err.println("Error Translating: "+sName);
			e.printStackTrace();
		}

	}


}
