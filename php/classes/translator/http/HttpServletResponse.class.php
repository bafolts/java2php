<?php

class translator_http_HttpServletResponse implements javax_servlet_http_HttpServletResponse {

	private $m_pWriter = null;

	public function setContentType($sType) {
	
	}

	public function getWriter() {
		if ($this->m_pWriter === null) {
			$this->m_pWriter = new java_io_PrintStream();
		}
		return $this->m_pWriter;
	}


}

?>