<?php

class javax_servlet_http_HttpServletResponse {

	private $m_pWriter = null;

	public function __construct() {
		$this->m_pStream = new java_io_PrintWriter();
	}

	public function setContentType($sType) {
		header('Content-type: '.$sType->__toString());
	}

	public function getWriter() {
		return $this->m_pStream;
	}

}

?>