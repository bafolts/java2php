<?php

class java_lang_reflect_Constructor {

	private $m_sClassType = null;
	private $m_mMethod = null;

	public function __construct($sClassType,$Method) {
		$this->m_sClassType = $sClassType;
		$this->m_mMethod = $Method;
	}

	public function newInstance($args) {
		return new java_lang_String($args[0]);//print_r($args);
		//die;
	}

}

?>