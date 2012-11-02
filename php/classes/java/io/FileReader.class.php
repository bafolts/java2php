<?php

class java_io_FileReader {

	private $m_sFilename;
	private $m_sStream;
	private $m_sContents;
	private $m_iCurrentPos = 0;
	private $m_iContentLength = 0;

	public function __construct($file) {
		$this->m_sFilename = $file->getPath();
		//$this->m_sStream = fopen($this->m_sFilename,"r");
		$this->m_sContents = file_get_contents($this->m_sFilename);
		$this->m_iContentLength = strlen($this->m_sContents);
	}

	public function read() {
		if($this->m_iCurrentPos>=$this->m_iContentLength) {
			return -1;
		} else {
			return $this->m_sContents[$this->m_iCurrentPos++];
		}
	}

	public function close() {
		//fclose($this->m_sStream);
	}

}

?>