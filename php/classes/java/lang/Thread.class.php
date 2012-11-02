<?php

class java_lang_Thread {

	public static function dumpStack() {
		$a = debug_backtrace();
		for($i=0;$i<count($a);$i++) {
			echo $a[$i]["file"]."(".$a[$i]["function"].") on line ".$a[$i]["line"]."\n";
		}
	}

}

?>