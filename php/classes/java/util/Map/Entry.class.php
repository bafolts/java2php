<?php

class java_util_Map_Entry {

	private $m_oKey = null;
	private $m_oValue = null;

	public function __construct($key,$value) {
		$this->m_oKey = $key;
		$this->m_oValue = $value;
	}
	
	public function getValue() {
		return $this->m_oValue;
	}
	
	public function getKey() {
		return $this->m_oKey;
	}

}

?>