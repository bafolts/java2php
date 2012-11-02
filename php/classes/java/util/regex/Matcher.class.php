<?php

class java_util_regex_Matcher {

	private $m_sInput;
	private $m_pPattern;
	private $m_aMatches;


	public function __construct($pattern,$sInput) {
		$this->m_sInput = $sInput;
		$this->m_pPattern = $pattern;
	}
	
	public function find() {

		if($this->m_sInput->length()>0) {
			$iHold = preg_match("/".$this->m_pPattern->pattern()."/s",$this->m_sInput,$this->m_aMatches)>0;

			if($iHold) {
				$this->m_iStart = strpos($this->m_sInput,$this->m_aMatches[0]);
				$this->m_iEnd = $this->m_iStart + strlen($this->m_aMatches[0]);
			}
			
			return $iHold;
		} else {
			return false;
		}

	}

	public function group($i=null) {
		if($i==null) {
			return new java_lang_String($this->m_aMatches[0]);
		} else {
			return new java_lang_String($this->m_aMatches[$i]);
		}
	}

	public function matches() {

		$iHold = preg_match("/".$this->m_pPattern->pattern()."/s",$this->m_sInput,$this->m_aMatches)>0;

		if($iHold) {
			$this->m_iStart = strpos($this->m_sInput,$this->m_aMatches[0]);
			$this->m_iEnd = $this->m_iStart + strlen($this->m_aMatches[0]);
			return $this->m_iEnd == $this->m_sInput->length();
		}

		return false;

	}

	public function lookingAt() {
		return preg_match("/".$this->m_pPattern->pattern()."/s",$this->m_sInput,$this->m_aMatches)>0;
	}
	
	public function start() {
		return $this->m_iStart;
	}
	
	public function end() {
		return $this->m_iEnd;
	}

}

?>