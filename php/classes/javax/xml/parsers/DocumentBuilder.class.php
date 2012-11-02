<?php

class javax_xml_parsers_DocumentBuilder {

	public function parse($f) {

		if($f instanceof java_io_File) {
			$doc = new DOMDocument();
			$doc->load($f->getPath());
			return new org_w3c_dom_Document($doc);
		} else {
			$sInput = "";
			while(true) {
				$sChar = $f->read();
				if($sChar==-1) {
					break;
				} else {
					$sInput.=$sChar;
				}
			}
			$doc = new DOMDocument();
			@$doc->loadXML($sInput);
			return new org_w3c_dom_Document($doc);
		}

	}

}


?>