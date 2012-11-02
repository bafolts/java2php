<?php

class java_io_ByteArrayInputStream extends java_io_InputStream {

	private $m_aInternal = "";
	private $m_oPosition = 0;
	private $m_iSize = 0;

	public function __construct($sInternal) {
		$this->m_aInternal = $sInternal;
		$this->m_iSize = count($this->m_aInternal);
	}

	public function read() {
		if($this->m_oPosition<=$this->m_iSize-1) {
			return $this->m_aInternal[$this->m_oPosition++];
		} else {
			return -1;
		}
	}
	
	public function close() {
	
	}

}

?>