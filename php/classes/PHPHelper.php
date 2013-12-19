<?php

set_time_limit(0);

function __parenthesis($s) {
	return $s;
}

$paths_to_check = explode(";",get_include_path());

function __fileExists($f) {
	global $paths_to_check;
	foreach($paths_to_check as $path) {
		if(file_exists($path.'/'.$f)) return true;
	}
	if(file_exists($f))return true;
	return false;
}


function __toArray($a) {
	if($a instanceof java_util_ArrayList) {
		return $a->__toArray();
	} else if($a===null) {
		return array();
	}
	return $a;
}

function __autoload($class_name) {

	$sFilename = str_replace('_','/',$class_name).'.class.php';

	if(__fileExists($sFilename)) {
		require_once($sFilename);
	} else {
		$sClassname = $class_name.'.class.php';
		while(strpos($sClassname,'_')!==false) {
			$sClassname = substr($sClassname,0,strpos($sClassname,'_')).'/'.substr($sClassname,strpos($sClassname,'_')+1);
			if(__fileExists($sClassname)) {
				require_once($sClassname);
				break;
			}
		}
	}

}

function __namespaced($sObjectName) {
	return _Class::forName(java_lang_String::valueOf($sObjectName))->newInstance();
}

function __concat() {
	return new java_lang_String(implode(func_get_args()));
}

function __attemptServletLoad($oServletClass) {

	if (isset($_SERVER['HTTP_HOST'])) {

		$oServlet = new $oServletClass();
		$oServlet->service(new translator_http_HttpServletRequest(), new translator_http_HttpServletResponse());

	}

}

?>