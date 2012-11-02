<?php

class java_util_Iterator {

	private $m_oInternal;
	private $m_oPosition = -1;

	function __construct(&$oInternal) {
		$this->m_oInternal = $oInternal;
	}

	function next() {
		if(!$this->hasNext()) {
			throw new NoSuchElementException("iteration has no more elements.");
		} else {
			$pArr = $this->m_oInternal->toArray();
			$this->m_oPosition++;
			return $pArr[$this->m_oPosition];
		}
	}
	
	function remove() {
		if($this->m_oPosition>-1) {
			$this->m_oInternal->remove($this->m_oPosition--);
		}
	}

	function hasNext() {
		return ($this->m_oPosition+1) < $this->m_oInternal->size();
	}

}

?>