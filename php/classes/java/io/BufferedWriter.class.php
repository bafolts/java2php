<?php

class java_io_BufferedWriter {

	private $m_sStream;

	public function __construct($stream) {
		$this->m_sStream = $stream;
	}

	public function write($string) {
		$this->m_sStream->write($string);
	}
	
	public function close() {
		$this->m_sStream->close();
	}

}

?>