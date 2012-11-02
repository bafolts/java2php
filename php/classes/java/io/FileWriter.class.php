<?php

class java_io_FileWriter {

	private $m_sFilename;
	private $m_sStream;

	public function __construct($string) {
		$this->m_sFilename = $string;
		$this->m_sStream = fopen($string,"w");
	}

	public function write($s) {
		fwrite($this->m_sStream,$s);
	}

	public function close() {
		fclose($this->m_sStream);
	}

}

?>