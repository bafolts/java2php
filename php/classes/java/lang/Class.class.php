<?php


class java_lang_Class {

	private $m_sClass;
	public $m_bIsArray = false;

	public function __construct($s) {
		$s = $s->replaceAll("\\.","\\\\");
		$this->m_sClass = $s;
	}

	public function isArray() {
		return $this->m_bIsArray;
	}
	
	public function getConstructor($aClassTypes) {


		//Get the Methods for this class
		//TODO - might be a better way to do this?
		//this is not horrible
		$sF = create_function(null,'return '.$this->m_sClass->replaceAll("\\\\","_")->__toString().'::$___METHODS;');
		$aMethods = $sF();

		foreach($aMethods as $Method) {
			if(isset($Method['isConstructor'])) {
				for($i=0;$i<count($Method['parameters']);$i++) {
					if($i<count($aClassTypes)) {
						if($Method['parameters'][$i]['type']===$aClassTypes[$i]->getName()->__toString()) {
							if($i===count($Method['parameters'])-1) {
								return new java_lang_reflect_Constructor($this->m_sClass->replaceAll("\\\\","."),$Method);
							}
						}
					}
				}
			}
		}

		throw new java_lang_NoSuchMethodException();

	}
	
	public function getComponentType() {
		return new java_lang_Class($this->m_sClass->replaceAll("\\\\","."));
	}

	public function newInstance() {
		$c = str_replace("\\","_",$this->m_sClass->toString());
		class_exists($c);
		$c = new $c();
		return $c;
	}

	public function getName() {
		return new java_lang_String(str_replace("\\",".",$this->m_sClass->toString()));
	}
	
	public function getMethod() {
		$args = func_get_args();
		$sName = $args[0];//String
		$aMethods = get_class_methods(str_replace("\\","_",$this->m_sClass));
		foreach($aMethods as $method) {
			if($method==$sName->__toString()) {
				return new java_lang_reflect_Method($this,$method);
			}
		}
		throw new java_lang_NoSuchMethodException;
	}
	
	public function getMethods() {
		//@todo - this doesn't work when methods have same name
		$aMethods = get_class_methods(str_replace("\\","_",$this->m_sClass));
		$aReturn = array();
		foreach($aMethods as $method) {
			if(strpos($method,"_")!==0) {
				$aReturn[] = new java_lang_reflect_Method($this,$method);
			}
		}
		return $aReturn;
	}

	public static function forName($s) {
		return new java_lang_Class($s);
	}

	public function isInstance($c) {
		$class = str_replace("\\","_",$this->m_sClass->toString());
		if(is_bool($c)) {
			$c = new java_lang_Boolean($c);
		}
		return $c instanceof $class;
/*		if(get_class($c)==$class)
			return true;
		else 
			return is_subclass_of($c,$class);*/
	}

	public function getClassLoader() {
		return new java_lang_ClassLoader();
	}

}

?>