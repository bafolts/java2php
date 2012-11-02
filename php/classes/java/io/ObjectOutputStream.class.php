<?php

class java_io_ObjectOutputStream {

	private $m_fStream = Translator_JavaBase::$null;

	public function __construct($fStream) {
		$this->m_fStream = $fStream;
	}

	public function writeObject($o) {
		$this->m_fStream->write(serialize($o));
	}
	
	public function close() {
		$this->m_fStream->close();
	}

}

?>