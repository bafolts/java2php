<?php

class java_lang_reflect_Method {

	private $m_sName;
	private $m_cClass;

	public function __construct($class,$method) {
		$this->m_cClass = $class;
		$this->m_sName = $method;
	}
	
	public function getName() {
		return new java_lang_String($this->m_sName);
	}

	public function getParameterTypes() {
		$aReturn = array();

		$sClassName = $this->m_cClass->getName()->__toString();

		//Get the Methods for this class
		//TODO - might be a better way to do this?
		//this is not horrible
		$sF = create_function(null,'return '.$sClassName.'::$___METHODS;');
		
		$aMethods = $sF();

		foreach($aMethods as $Method) {
			if($Method['javaName']===$this->m_sName) {
				foreach($Method['parameters'] as $Parameter) {
					$cClass = new java_lang_Class(new java_lang_String($Parameter['type']));
					if($Parameter['isArray']) {
						$cClass->m_bIsArray = true;
					}
					$aReturn[] = $cClass;
				}
				return $aReturn;
			}
		}

		//if we did not find a parameter it must be an issue with the translator, this java method throws no exceptions.
		throw new Exception('Unable to find method parameter types for '.$this->m_sName.' of '.$sClassName.', must be issue with translator.');

	}

	public function invoke() {
		$args = func_get_args();
		if(func_num_args()==2) {
			return call_user_func_array(array($args[0],$this->m_sName),$args[1]);
		} else {
			return call_user_func_array(array($args[0],$this->m_sName),array());
		}
	}

}

?>