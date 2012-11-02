<?php

class java_io_DataInputStream {

	private $m_sStream;

	public function __construct($stream) {
		$this->m_sStream = $stream;
	}

	public function close() {
		$this->m_sStream->close();
	}

	public function read() {
		return $this->m_sStream->read();
	}

}
?>