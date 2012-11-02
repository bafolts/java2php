<?php

class java_io_FileOutputStream {

	private $m_sFilename = Translator_JavaBase::$null;
	private $m_iStream = Translator_JavaBase::$null;

	public function __construct($filename) {
		if($filename==Translator_JavaBase::$null) {
			throw new Exception('Currently not implemented');
		} else if($filename instanceof java_io_File) {
			$this->m_sFilename = $filename;
			$this->m_iStream = fopen($this->m_sFilename->toString(),'w+');
		} else if($filename instanceof java_io_FileDescriptor) {
			throw new Exception('Currently not implemented');
		} else if($filename instanceof java_lang_String) {
			$this->m_sFilename = new java_lang_String($filename);
			$this->m_iStream = fopen($this->m_sFilename->toString(),'w+');
		}
	}

	public function write($s) {
		fwrite($this->m_iStream,$s);
	}

	public function close() {
		fclose($this->m_iStream);
	}

}

?>