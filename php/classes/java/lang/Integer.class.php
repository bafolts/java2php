<?php

class java_lang_Integer {
	public static $___METHODS = array();
	private $m_iInt;

	public function __construct($v) {
		if($v instanceof java_lang_String) {
			$this->m_iInt = intval($v->__toString());
		} else {
			$this->m_iInt = $v * 1;
		}
	}

	public static function toString($i) {
		return new java_lang_String("".$i);
	}
	
	public function &intValue() {
		return $this->m_iInt;
	}
	
	public static function parseInt(/*java.lang.String*/$string,/*int*/$radix = null) {
		if(is_numeric($string->__toString())) {
			return intval($string->__toString(),$radix);
		}
		throw new Exception("Integer.parseInt with radix parameter currently not implemented.");
	}

}

?>