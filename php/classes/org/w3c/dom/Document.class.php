<?php

class org_w3c_dom_Document {

	private $m_dDocument;
	
	public function __construct($d) {
		$this->m_dDocument = $d;
	}

	public function getElementsByTagName($sTagName) {

		if($sTagName->indexOf(new java_lang_String(":"))===-1) {
			return new org_w3c_dom_NodeList($this->m_dDocument->getElementsByTagName($sTagName));
		} else {
			$aDeal = explode(":",$sTagName->__toString());
			return new org_w3c_dom_NodeList($this->m_dDocument->getElementsByTagNameNS($this->m_dDocument->lookupNamespaceURI($aDeal[0]),$aDeal[1]));
		}
	}

	public function getElementsByTagNameNS($sNamespace,$sTag) {
		return new org_w3c_dom_NodeList($this->m_dDocument->getElementsByTagNameNS($sNamespace,$sTag));
	}
	
	public function getFirstChild() {
		return new org_w3c_dom_Node($this->m_dDocument->firstChild);
	}

}

?>