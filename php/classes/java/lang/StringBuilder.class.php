<?php

class java_lang_StringBuilder {

	private $m_sString;

	public function __construct($o=null) {
		if($o===null) {
			$this->m_sString = '';
		} else if($o instanceof java_lang_String) {
			$this->m_sString = $o->__toString();
		} else if($o instanceof java_lang_CharSequence) {
		
		} else {
			//int - nothing needed
		}
	}
	
	public function append($s) {
		if($s instanceof java_lang_String) {
			$this->m_sString .= $s->toString();
		} else if(is_string($s)) {
			$this->m_sString .= $s;
		}
		return $this;

	}

	public function delete($start,$end) {
		$sResult = new java_lang_String($this->m_sString);

		$sReturn = new java_lang_StringBuilder();

		$sReturn->append($sResult->substring(0,$start));
		$sReturn->append($sResult->substring($end));

		return $sReturn;

	}

	public function length() {
		return strlen($this->m_sString);
	}

	public function toString() {
		return new java_lang_String($this->m_sString);
	}
	
	public function __toString() {
		return $this->m_sString;
	}

}

?>