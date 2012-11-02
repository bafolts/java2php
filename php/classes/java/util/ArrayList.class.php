<?php

class java_util_ArrayList extends java_lang_Object implements java_util_Collection,java_util_List {

	private $m_aInternal;
	private $m_iSize = 0;

	public function __construct($type,$s=0) {
		$this->m_aInternal = array();
	}
	
	public function add($i,$o=null) {
		if($o===null) {
			$this->m_aInternal[] = $i;
			$this->m_iSize++;
		} else {
			array_splice($this->m_aInternal,$i,0,"");
			$this->m_aInternal[$i] = $o;
			$this->m_iSize++;
		}
	}
	
	public function clear() {
		$this->m_aInternal = array();
		$this->m_iSize = 0;
	}

	public function iterator() {
		return new java_util_Iterator($this);
	}

	public function toArray() {
		return $this->m_aInternal;
	}
	
	public function size() {
		return $this->m_iSize;
	}
	
	public function remove($i) {
		if(is_int($i)) {
			$aRemoved = array_splice($this->m_aInternal,$i,1);
			$this->m_iSize--;
			return $aRemoved[0];
		} else {
			echo "NOT IMPLEMENTED YET";
			return false;
		}
	}
	
	public function contains($o) {
		return array_search($o,$this->m_aInternal)!==false;
	}

	public function indexOf($o) {
		$p = array_search($o,$this->m_aInternal);
		if($p===false) {
			return -1;
		} else {
			return $p;
		}
	}

	public function set($i,$o) {
		$aOld = $this->get($i);
		$this->m_aInternal[$i] = $o;
		return $aOld;
	}

	public function get($i) {
		return $this->m_aInternal[$i];
	}

	public function __toArray() {
		return $this->m_aInternal;
	}
	
	public function __toString() {
		return "java_util_Array";
	}

}

?>