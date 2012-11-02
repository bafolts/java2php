<?php

class java_util_regex_Pattern {

	private $m_sRegex;

	public function __construct($sRegex) {
		$this->m_sRegex = $sRegex;
	}
	
	public static function compile($sRegEx) {
		return new java_util_regex_Pattern($sRegEx);
	}
	
	public function pattern() {
		return $this->m_sRegex;
	}
	
	public function matcher($sInput) {
		return new java_util_regex_Matcher($this,$sInput);
	}

}
?>