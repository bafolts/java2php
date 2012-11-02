<?php

class com_sun_tools_javac_Main {

	public function compile($args) {

		$file = new Translator_FileTranslator($args[count($args)-1]);

		$javaFile = $file->translate();

		$sFile = new java_lang_String($args[count($args)-1]);
		$sFile = __concat($sFile->substring(0,$sFile->length()-5),".class.php");
	

		//@TODO - Namespace this better
		$php5File = new Translator_PHP5File($javaFile);

		$aSearchPaths = new java_util_ArrayList("String");
		
		//@TODO - I will need these in an environment variable
		$aSearchPaths->add(new java_lang_String("C:/tomcat/test/work/"));
		$aSearchPaths->add(new java_lang_String("C:/Users/bfolts/Desktop/s/codotos/src/"));
		$aSearchPaths->add(new java_lang_String("C:/Users/bfolts/Desktop/translator/src/"));

		$php5File->setSourcePath($aSearchPaths);

		try {
			$php5File->writeFile($sFile);
		} catch(Exception $e) {
			echo $e;
			die("WTF");
			return -1;
		}
		return 0;
	}

}

?>