<?php

class java_lang_reflect_Array {

	public static function newInstance($componentType,$iLength) {
		if(is_array($iLength)) {
			throw new RuntimeException("Currently not implemented, java.lang.reflect.Array::newInstance with multiple dimensions.");
		} else {
			//TODO - this might have to pull default values...
			return array_pad(array(),$iLength,null);
		}
	}
	
	public static function set($arr,$index,$value) {
		$arr[$index] = $value;
	}

}

?>