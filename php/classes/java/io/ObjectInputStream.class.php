<?php

class java_io_ObjectInputStream {

	private $m_fStream;

	public function __construct($fStream) {
		$this->m_fStream = $fStream;
	}

	public function readObject() {
		$sContents = array();
		$this->m_fStream->read($sContents,0,$this->m_fStream->available());
		return unserialize(implode($sContents));
	}
	
	public function close() {
		$this->m_fStream->close();
	}

}

?>