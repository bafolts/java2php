<?php

class java_lang_ClassLoader {

	public function __construct() {
	
	}

	public function _initVars() {
	
	}

	public function loadClass($sName) {

		return new java_lang_Class($sName);

	}

}

?>