<?php

class java_lang_Throwable extends Exception {

	private $m_tThrowable = null;

	public function initCause($tCause) {
		$this->m_tThrowable = $tCause;
		return $this->m_tThrowable;
	}
	
	public function getCause() {
		return $this->m_tThrowable;
	}

	public function printStackTrace() {
		echo "<br/>".$this->getMessage();
		if($this->m_tThrowable!=null) {
			echo "<br/>".$this->m_tThrowable;
		}
		//debug_print_backtrace();
	}

}

?>