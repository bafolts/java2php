<?php

class java_lang_Object {

	private $m_lHash;

	public function _initVars() {
		$this->m_lHash = time();
	}

	public function __construct() {

	}

	public function toString() {
		//@TODO - This could be wrong for classes that do have _ in their name.
		return new java_lang_String(str_replace("_",".",get_class($this))."@".$this->m_lHash);
	}

	public function getClass() {
		return new java_lang_Class(new java_lang_String(str_replace("_",".",get_class($this))));
	}

	public function hashCode() {
		return $this->m_lHash;
	}

	public function equals($o) {
		return $o==$this;
	}

}

?>