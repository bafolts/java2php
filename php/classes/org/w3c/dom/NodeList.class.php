<?php

class org_w3c_dom_NodeList {

	private $m_aNodes;

	public function __construct($nL) {
		$this->m_aNodes = $nL;
	}

	public function item($i) {
		return new org_w3c_dom_Element($this->m_aNodes->item($i));
	}

	public function getLength() {
		return $this->m_aNodes->length;
	}

}

?>