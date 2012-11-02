<?php

class java_util_Stack {

	private $m_aInternal = null;

	public function __construct() {
		$this->m_aInternal = array();
	}

	public function push($o) {
		array_push($this->m_aInternal,$o);
		return $o;
	}
	
	public function __call($method,$args) {
		if($method==="empty") {
			return $this->_empty();
		} else {
			throw new Exception("Unknown method ".$method." in java.util.Stack");
		}
	}

	public function _empty() {
		return count($this->m_aInternal)===0;
	}
	
	public function size() {
		return count($this->m_aInternal);
	}
	
	public function pop() {
		if($this->size()===0) {
			throw new java_util_EmptyStackException();
		} else {
			return array_pop($this->m_aInternal);
		}
	}

}

?>