<?php

class java_lang_Boolean extends java_lang_Object {
	public static $___METHODS = array();
	public function __construct($b) {
		if($b instanceof java_lang_Boolean) {
			$this->m_bBool = $b->booleanValue();
		} else {
			$this->m_bBool = $b;
		}
	}

	public function booleanValue() {
		return $this->m_bBool;
	}

	public function toString($b) {
		return new java_lang_String($b ? "true" : "false");
	}

	public static function valueOf($s) {
		if($s instanceof java_lang_String) {
			return new java_lang_Boolean($s->equalsIgnoreCase(new java_lang_String("true")));
		} else {
			return new java_lang_Boolean($s);
		}
	}

}

java_lang_Boolean::$___METHODS[] = array('phpName'=>'__construct','isConstructor'=>true,'javaName'=>'Boolean','parameters'=>array(array("type"=>"java.lang.Boolean","name"=>"bool","isArray"=>false)));


?>