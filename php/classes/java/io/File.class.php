<?php

class java_io_File {

	private $m_sFilename;
	
	public static $pathSeparatorChar = ';';

	public function __construct($filename) {
		if($filename===Translator_JavaBase::$null) {
			throw new java_lang_NullPointerException();
		}
		$this->m_sFilename = $filename->__toString();
	}
	
	public function getPath() {
		return $this->m_sFilename;
	}

	public function getName() {
		return new java_lang_String($this->m_sFilename);
	}

	public function getAbsolutePath() {
		return new java_lang_String($this->m_sFilename);
	}

	public function getAbsoluteFile() {
		return new java_io_File($this->getAbsolutePath());
	}

	public function isFile() {
		return is_file($this->m_sFilename);
	}

	public function isDirectory() {
		return is_dir($this->m_sFilename);
	}

	public function __call($name,$arguments) {
		if($name==="list") {
			return $this->_list();
		} else {
			throw new java_lang_Exception('Unknown Function '.$name.' in java.io.File');
		}
	}

	public function mkdirs() {
		return @mkdir($this->m_sFilename,0777,true);
	}

	public function lastModified() {
		$iLastModified = filemtime($this->m_sFilename);
		if($iLastModified===false) {
			throw new SecurityException("Access Denied");
			return 0;
		}
		return $iLastModified;
	}

	public function exists() {
		return file_exists($this->m_sFilename);
	}
	
	public function getParent() {
		$result = new String($this->m_sFilename);
		if($result->lastIndexOf(new java_lang_String("/"))>-1) {
			return $result->substring(0,$result->lastIndexOf(new String("/")));
		} else {
			return Translator_JavaBase::$null;
		}
	}

	public function listFiles() {
		$aFiles = scandir($this->m_sFilename);
		$aReturn = array();
		foreach($aFiles as $File) {
			if(is_file($this->m_sFilename.'/'.$File)) {
				$aReturn[] = new java_io_File(new java_lang_String($File));
			}
		}
		return $aReturn;
	}

	public function _list() {
		//@TODO - this should return directories
		$aFiles = scandir($this->m_sFilename);
		$aReturn = array();
		foreach($aFiles as $File) {
			if(is_file($this->m_sFilename.'/'.$File)) {
				$aReturn[] = new java_lang_String($File);
			}
		}
		return $aReturn;
	}
	
	public function delete() {
		return @unlink($this->m_sFilename);
	}
	
	public function toString() {
		//@TODO - check this
		return new java_lang_String($this->m_sFilename);
	}
	
}

?>