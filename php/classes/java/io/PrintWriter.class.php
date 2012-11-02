<?php

class java_io_PrintWriter {


	public function close() {
	
	}

	public function __call($func,$args) {
		if($func==="print") {
			return $this->_print($args);
		}
		throw new Exception("Unknown function ".$func." in java.io.PrintWriter.");
	}

	public function _print($s) {
		if(is_array($s)) {
			foreach($s as $line) {
				echo $line;
			}
		}
	}

}

?>