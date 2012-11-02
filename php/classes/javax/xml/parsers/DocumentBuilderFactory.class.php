<?php

$GLOBALS['included_files'][]="javax/xml/parsers/DocumentBuilder.class.php";

class javax_xml_parsers_DocumentBuilderFactory {

	public static function newInstance() {
		return new javax_xml_parsers_DocumentBuilderFactory();
	}

	public function newDocumentBuilder() {
		return new javax_xml_parsers_DocumentBuilder();
	}
	
	public function setNamespaceAware($b) {
		//maybe do something here?
	}

}
?>