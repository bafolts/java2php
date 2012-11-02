<?php
class java_io_FileInputStream {

	private $m_sFilename;
	private $m_iStream;
	private $m_sContents;
	private $m_iCurrentPos = 0;

	public function __construct($filename) {
		if($filename==Translator_JavaBase::$null) {
			
		}
		if($filename instanceof java_io_File) {
			$filename = $filename->toString();
		}
		if($filename instanceof java_lang_String) {
			if(!file_exists($filename->toString())) {
				throw new java_io_IOException("Error: ".$filename->toString()." (The system cannot find the file specified)");
			} else {
				$this->m_sFilename = $filename;
				$this->m_sContents = file_get_contents($this->m_sFilename);
				$this->m_iContentLength = strlen($this->m_sContents);
				//$this->m_iStream = fopen($this->m_sFilename->toString(),"r");
			}
		}
	}

	public function available() {
		return $this->m_iContentLength;
	}

	public function read(&$buffer=null,$off=null,$len=null) {
		if($off===null&&$len===null) {
			if($buffer===null) {
				if($this->m_iCurrentPos>=$this->m_iContentLength) {
					return -1;
				} else {
					return $this->m_sContents[$this->m_iCurrentPos++];
				}
			}
		} else if($buffer===null) {
			throw new java_lang_NullPointerException();
		} else {
			$buffer = str_split($this->m_sContents);
			return $this->m_iContentLength;
		}
	}

	public function close() {
		//fclose($this->m_iStream);
	}

}
?>