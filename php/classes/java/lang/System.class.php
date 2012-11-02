<?php

class java_lang_System {

	public static $out;
	public static $err;
	
	public static function _initStaticVars() {
		java_lang_System::$out = new java_io_PrintStream();
		java_lang_System::$err = new java_io_PrintStream();
	}

	public static function getProperty($sProperty) {
		if(isset($GLOBALS['SYSTEM_PROPERTIES'][$sProperty->__toString()])) {
			return new java_lang_String($GLOBALS['SYSTEM_PROPERTIES'][$sProperty->__toString()]);
		}
		return null;
	}

}

java_lang_System::_initStaticVars();
?>