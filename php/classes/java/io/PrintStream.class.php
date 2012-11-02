<?php

class java_io_PrintStream {

	public function println($s) {
		echo $s."\n";
	}
	
	public function __call($name,$arguments) {
		if($name==="print") {
			return $this->_print($arguments[0]);
		}
	}

	public function _print($s) {
		echo $s;
	}

}

?>