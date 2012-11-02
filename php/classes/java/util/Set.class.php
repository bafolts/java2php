<?php

class java_util_Set implements java_util_Collection {

	private $m_aInternal = array();

	public function iterator() {
		return new java_util_Iterator($this);
	}
	
	public function add($o) {
		$this->m_aInternal[] = $o;
	}
	
	public function clear() {
	
	}
	
	public function toArray() {
		return $this->m_aInternal;
	}

	public function size() {
		return count($this->m_aInternal);
	}

}

?>