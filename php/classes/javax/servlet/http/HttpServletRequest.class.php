<?php

class javax_servlet_http_HttpServletRequest {

	public function getServletPath() {
		return new java_lang_String($_SERVER['SCRIPT_NAME']);
	}

	public function getParameterMap() {
		return $GLOBALS['PARAMETER_MAP'];
	}

}

?>