<?php

class java_io_InputStreamReader {

	private $m_sStream;

	public function __construct($stream) {
		$this->m_sStream = $stream;
	}
	
	public function read() {
		return $this->m_sStream->read();
	}

}

?>