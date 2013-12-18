<?php

class javax_servlet_http_HttpServlet extends java_lang_Object {

	public function _initVars() {
	
	}

	public function service(javax_servlet_http_HttpServletRequest $oRequest, javax_servlet_http_HttpServletResponse $oResponse) {

		switch ($_SERVER['REQUEST_METHOD']) {
			case "GET":
				$this->doGet($oRequest, $oResponse);
			break;
			default:
				throw new Exception("Unsupported request type.");
		}

	}

}

?>