<?php

class java_util_HashMap extends java_lang_Object {

	public $aKeys = array();
	public $aValues;
	private $m_iNumKeys = 0;
	private $m_sValueType = null;
	private $m_sKeyType = null;

	public function __construct($sKeyType,$sValueType) {
		$this->m_sKeyType = $sKeyType;
		$this->m_sValueType = $sValueType;
		$this->aValues = new java_util_ArrayList($sValueType);
	}

	public function clear() {
		$this->aKeys = array();
		$this->aValues->clear();
		$this->m_iNumKeys = 0;
	}

	public function remove($key) {
		for($i=0;$i<$this->m_iNumKeys;$i++) {
			if($this->aKeys[$i]==$key) {
				$oValue = $this->aValues->get($i);
				array_splice($this->aKeys,$i,1);
				$this->m_iNumKeys--;
				$this->aValues->remove($i);
				return $oValue;
			}
		}
		return Translator_JavaBase::$null;
	}

	public function containsKey($key) {
		for($i=0;$i<$this->m_iNumKeys;$i++) {
			if($this->aKeys[$i]==$key) {
				return true;
			}
		}
		return false;
	}

	public function values() {
		return new java_util_HashMapValues($this);
	}

	public function put($sKey,$sValue) {
		if($this->m_sValueType==="Boolean") {
			$sValue = new java_lang_Boolean($sValue);
		}
		for($i=0;$i<$this->m_iNumKeys;$i++) {
			if($this->aKeys[$i]==$sKey) {
				$oValue = $this->aValues->get($i);
				$this->aValues->set($i,$sValue);
				return $oValue;
			}
		}
		$this->aKeys[] = $sKey;
		$this->aValues->add($sValue);
		$this->m_iNumKeys++;
		return Translator_JavaBase::$null;
	}

	public function get($key) {
		for($i=0;$i<$this->m_iNumKeys;$i++) {
			if($this->aKeys[$i]==$key) {
				return $this->aValues->get($i);
			}
		}
		return Translator_JavaBase::$null;
	}
	
	public function entrySet() {
		$aReturn = new java_util_Set("Map.Entry<".$this->m_sKeyType.",".$this->m_sValueType.">");
		for($i=0;$i<count($this->aKeys);$i++) {
			$aReturn->add(new java_util_Map_Entry($this->aKeys[$i],$this->aValues->get($i)));
		}
		return $aReturn;
	}

}


class java_util_HashMapValues implements java_util_Collection {
	private $m_hMap;
	function __construct(&$hashmap) {
		$this->m_hMap = $hashmap;
	}
	function remove($i) {
		$this->m_hMap->remove($this->m_hMap->aKeys[$i]);
	}
	function toArray() {
		return $this->m_hMap->aValues->toArray();
	}
	function add($o) {
		return $this->m_hMap->aValues->add($o);
	}
	function clear() {
		return $this->m_hMap->clear();
	}
	function iterator() {
		return new java_util_Iterator($this);
	}
	function size() {
		return $this->m_hMap->aValues->size();
	}
}


?>