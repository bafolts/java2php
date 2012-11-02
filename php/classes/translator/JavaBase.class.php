<?php
class translator_JavaBase {

	public static function getArgs() {
	
		global $argv;
	
		$args = array();

		for($i=1,$length=count($argv);$i<$length;$i++) {
			$args[$i-1] = new java_lang_String($argv[$i]);
		}

		return $args;

	}

	public static function integerDivision($lhs,$rhs) {
		return (int)($lhs/$rhs);
	}

	public static function incrementBefore(&$a,$v) {
		$r = intval($a+$v);
		$a += $v;
		return $r;
	}
	
	public static function incrementAfter(&$a,$v) {
		$r = intval($a);
		$a += $v;
		return $r;
	}

	public static function decrementBefore(&$a,$v) {
		$r = intval($a-$v);
		$a -= $v;
		return $r;
	}
	
	public static function decrementAfter(&$a,$v) {
		$r = intval($a);
		$a -= $v;
		return $r;
	}

	public static function concat() {
		return new java_lang_String(implode(func_get_args()));
	}

	public static function unsignedRightShift($n,$s) {
		return ($n >= 0) ? ($n >> $s) :
			(($n & 0x7fffffff) >> $s) | 
				(0x40000000 >> ($s - 1));
	}

	public static $null;

}

Translator_JavaBase::$null = new JavaNull;

class JavaNull {

	function __call($a,$b) {
		if($a=="booleanValue") {
			return Translator_JavaBase::$null;
		}
		throw new java_lang_NullPointerException;
	}
	
	function __get($a) {
		throw new java_lang_NullPointerException;
	}

}

?>