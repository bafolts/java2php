<?php

class org_w3c_dom_NamedNodeMap {

	private $m_pNodeMap;

	public function __construct($pNodeName) {
		$this->m_pNodeMap = $pNodeName;
	}

	public function getLength() {
		return $this->m_pNodeMap->length;
	}
	
	public function item($i) {
		return new org_w3c_dom_Node($this->m_pNodeMap->item($i));
	}

}

?>