<?php

class java_io_BufferedReader {

	private $m_sStream;
	private $m_sLastRead;

	public function __construct($stream) {
		$this->m_sStream = $stream;
	}

	public function readLine() {
		$s = "";

		while(true) {
			$c = $this->m_sStream->read();
			if($c === "\n" && $this->m_sLastRead === "\r") {
				continue;
			}
			if($c===-1||$c==="\n"||$c==="\r") {
				break;
			} else {
				$s.=$c;
			}
		}

		$this->m_sLastRead = $c;

		if($s === "" && $c === -1) {
			return Translator_JavaBase::$null;
		} else if($s === "" && ($c === "\n" || $c === "\r" )) {
			return new java_lang_String("");
		} else {
			return new java_lang_String($s);
		}

	}
	
	public function close() {
		$this->m_sStream->close();
	}


}

?>