<?php

class java_lang_String extends java_lang_Object {

	private $m_sString = "";
	private $m_iLength = 0;
	public static $class = null;
	public static $___METHODS = array();

	public function __construct($s) {
		$this->m_sString = ($s instanceof java_lang_String ? $s->__toString() : $s);
		$this->m_iLength = strlen($this->m_sString);
		
		parent::__construct();

	}
	
	public static function format() {
		$args = func_get_args();
		if($args[0] instanceof java_lang_String) {
			for($i=0;$i<count($args);$i++) {
				$args[$i] = $args[$i]->__toString();
			}
			return new java_lang_String(call_user_func_array("sprintf",$args));
		} else {
			throw new Exception("java.lang.String (format) : this string format exception is not implemented yet.");
		}
	}

	public function trim() {
		return new java_lang_String(trim($this->m_sString));
	}

	public function concat($s) {
		return new java_lang_String($s->toString().$this->m_sString);
	}

	public function lastIndexOf($s) {
		if($s instanceof java_lang_String)
			$iPos = strrpos($this->m_sString,$s->__toString());
		else
			$iPos = strrpos($this->m_sString,$s);
		if($iPos===false) {
			return -1;
		} else {
			return $iPos;
		}
	}

	public function isEmpty() {
		return $this->m_iLength===0;
	}

	public function getBytes() {
		return str_split($this->m_sString);
	}

	public function split($s,$limit=-1) {
		$aReturn = preg_split("#$s#",$this->m_sString,$limit);
		foreach($aReturn as $i => $value) {
			$aReturn[$i] = new java_lang_String($value);
		}
		return $aReturn;
	}
	
	public function startsWith($s) {
		$s = $s->__toString();
		$iLength = strlen($s);
		if($iLength>$this->m_iLength)
			return false;
		for($i=0;$i<$iLength;$i++)
			if($s[$i]!==$this->m_sString[$i])
				return false;
		return true;
	}
	
	public function endsWith($s) {
		$s = $s->__toString();
		$iLength = strlen($s);
		if($iLength>$this->m_iLength)
			return false;
		for($i=0;$i<$iLength;$i++)
			if($s[$iLength-1-$i]!==$this->m_sString[$this->m_iLength-1-$i])
				return false;
		return true;
	}

	public function equals($s) {
		return $s->__toString()===$this->m_sString;
	}

	public function equalsIgnoreCase($s) {
		if(strlen($s)!==$this->m_iLength) {
			return false;
		} else {
			return strtoupper($s->__toString())===strtoupper($this->m_sString);
		}
	}
	
	public function indexOf($s,$i=0) {
		$s = $s->__toString();
		$iPos = strpos($this->m_sString,$s,$i);
		if($iPos===false) {
			return -1;
		} else {
			return $iPos;
		}
	}
	
	public function charAt($i) {
		return $this->m_sString[$i];
	}
	
	public function substring($s,$i=null) {
		if($i!==null) {
			return new java_lang_String(substr($this->m_sString,$s,$i-$s));
		} else {
			return new java_lang_String(substr($this->m_sString,$s));
		}
	}

	public static function valueOf($s) {
		if($s instanceof java_lang_String) {
			return $s;
		} else {
			return new java_lang_String($s);
		}
	}

	public function length() {
		return $this->m_iLength;
	}

	public function toString() {
		return new java_lang_String($this->m_sString);
	}
	
	public function __toString() {
		return $this->m_sString;
	}

	public function replaceAll($regex,$string) {
		return new java_lang_String(preg_replace("#$regex#s",$string,$this->m_sString));
	}

	public function replace($old,$new) {
		return new java_lang_String(str_replace($old,$new,$this->m_sString));
	}

	public function toLowerCase() {
		return new java_lang_String(strtolower($this->m_sString));
	}

	public function toUpperCase() {
		return new java_lang_String(strtoupper($this->m_sString));
	}

	public function contains(/*CharSequence */$charSequence) {
		if($charSequence===Translator_JavaBase::$null)
			return false;
		return strpos($this->m_sString,$charSequence->__toString())!==false;
	}

}

//TODO - This will have to be done for each class, generated or hand written
java_lang_String::$class = new java_lang_Class(new java_lang_String("java.lang.String"));
java_lang_String::$___METHODS[] = array('phpName'=>'__construct','isConstructor'=>true,'javaName'=>'String','parameters'=>array(array("type"=>"java.lang.String","name"=>"string","isArray"=>false)));

?>