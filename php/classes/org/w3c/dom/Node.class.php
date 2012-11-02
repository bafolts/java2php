<?php

class org_w3c_dom_Node {

	public static $ELEMENT_NODE = 1;

	private $m_nNode;
	
	public function __construct($n) {
		$this->m_nNode = $n;
	}
	
	public function hasAttribute($s) {
	
		return $this->m_nNode->hasAttribute($s);

	}
	
	public function getElementsByTagName($sTagName) {
		return new org_w3c_dom_NodeList($this->m_nNode->getElementsByTagName($sTagName));
	}

	public function getLocalName() {
		return new java_lang_String($this->m_nNode->localName);
	}

	public function getFirstChild() {
		return new org_w3c_dom_Node($this->m_nNode->firstChild);
	}

	public function getTextContent() {
		return new java_lang_String($this->m_nNode->textContent);
	}

	public function hasChildNodes() {
		return $this->m_nNode->hasChildNodes();
	}

	public function getChildNodes() {
		return new org_w3c_dom_NodeList($this->m_nNode->childNodes);
	}

	public function getPrefix() {
		return new java_lang_String($this->m_nNode->prefix);
	}

	public function getAttributes() {
		return new org_w3c_dom_NamedNodeMap($this->m_nNode->attributes);
	}

	public function getTagName() {
		return new java_lang_String($this->m_nNode->nodeName);
	}

	public function getNodeType() {
		return $this->m_nNode->nodeType;
	}

	public function getAttribute($s) {
	
		$sResult = $this->m_nNode->getAttribute($s);
		
		if($sResult === Translator_JavaBase::$null) {
			return new java_lang_String("");
		} else {
			return new java_lang_String($sResult);
		}

	}

	public function getName() {
		return new java_lang_String($this->m_nNode->name);
	}

	public function getValue() {
		return new java_lang_String($this->m_nNode->value);
	}

}

?>