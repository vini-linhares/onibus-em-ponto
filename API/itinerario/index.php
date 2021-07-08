<?php header("Content-type: text/html; charset=iso-8859-1"); ?>
<?php

function finalizaJson(){
  //echo "{}";
  global $sentido;
  if($sentido == 1){
	  echo '{ "ida" : [ { "rua" : "Rua0000", "num_inicio" : 0, "num_fim" : 0 }]}';
	  exit();
  }
  echo '{ "ida" : [ { "rua" : "Rua0000", "num_inicio" : 0, "num_fim" : 0 }], "volta" : [ { "rua" : "", "num_inicio" : 0, "num_fim" : 0 }]}';
  exit();
}
function finalizaErroSP(){
//echo "{}";
  echo '{ "ida" : [ { "rua" : "Rua0001", "num_inicio" : 0, "num_fim" : 0 }], "volta" : [ { "rua" : "", "num_inicio" : 0, "num_fim" : 0 }]}';
  exit();
}


function codigoLinha($numLinha){
	$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/linhaselecionada.asp';

	$linha = $numLinha;

	$data = array (
		'Linha' => $linha
	);


        $params = '';
    	foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
        $params = trim($params, '&');

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 30); //Timeout after 7 seconds
	curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
	curl_setopt($ch, CURLOPT_HEADER, 0);

	$result = curl_exec($ch);
	curl_close($ch);

	$matches = array();
	$pattern = "/CdPjOID=([\s\S]){1,10}\" target=/";
	$resultado = preg_match($pattern, $result, $matches);
	$codigos= str_replace('CdPjOID=', '', $matches[0]);
	$codigos= str_replace('" target=', '', $codigos);

	return $codigos;
}


$sentido = -1;
if(isset($_GET['linha']) && isset($_GET['sentido'])){
	$numLinha = $_GET['linha'];
	$sentido = intval($_GET['sentido']);
}else if(isset($_GET['linha'])){
	$numLinha = $_GET['linha'];
}else{
	finalizaJson();
}

	

$codLinha = codigoLinha($numLinha);
if (strcmp($codLinha , "") == 0){
	sleep(5);
	$codLinha = codigoLinha($numLinha);
	if (strcmp($codLinha , "") == 0){
		sleep(5);
		$boolControle = false;
		$codLinhaControle = codigoLinha("1156-10");
		if (strcmp($codLinhaControle , "") == 0){
			$boolControle = true;
		}
		$codLinha = codigoLinha($numLinha);
		if (strcmp($codLinha , "") == 0){
			if($boolControle){
				finalizaErroSP();
			}else{
				finalizaJson();
			}
		}
	}
}

$resultado = pegarItinerario($codLinha);
if (strcmp($resultado, "") != 0){
	echo $resultado;
}else{
	sleep(5);
	$resultado = pegarItinerario($codLinha);
	if (strcmp($resultado, "") != 0){
		echo $resultado;
	}else{
		sleep(5);
		$resultado = pegarItinerario($codLinha);
		if (strcmp($resultado, "") != 0){
			echo $resultado;
		}else{
			finalizaErroSP();
		}
	}
}




function pegarItinerario($codPesquisa){
global $sentido;
	$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/detalheLinha.asp';
	
	    $data = array (
	        'TpDiaID' => '0',
	        'CdPjOID' => $codPesquisa
	        );
	
	
	        $params = '';
	    foreach($data as $key=>$value)
	                $params .= $key.'='.$value.'&';
	
	        $params = trim($params, '&');
	
	    $ch = curl_init();
	
	    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
	    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
	    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 30); //Timeout after 7 seconds
	    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
	    curl_setopt($ch, CURLOPT_HEADER, 0);
	
	        $result = curl_exec($ch);
	    curl_close($ch);
	
	//$result= str_replace('src="', 'src="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
	//$result= str_replace('href="', 'href="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
	//echo $result;
	//exit();
	
	
	$auxs = explode('<li class="itemTitulo">Ponto inicial</li>', $result);
	if(isset($auxs[1]) != 1 ){
		//finalizaJson();
		return "";
	}
	$sentidos = explode('<li class="itemTitulo">Ponto Final</li>', $auxs[1]);
	//echo $sentidos[0];
	
	
	
	if($sentido != 2){
		$ida = $sentidos[0];
		$matches = array();
		$pattern = "/width=\"317\"([\s\S]){1,450}width=\"245\"/";
		$resultado = preg_match_all($pattern, $ida , $matches);
		//var_dump($matches);
	
		//$jsonLinhas = "{\n\t".'"linhas"'." : [";
		$jsonIda = "\t" . '"ida" : [';
		if (count($matches[0]) > 0){
		foreach ($matches[0] as &$value) {
		    $aux = str_replace('width="317">', '', $value);
		    $aux = str_replace('
		 ', '', $aux);
		    $aux = str_replace('</td>', '', $aux);
		    $aux = str_replace('(VIA Fora do municí­pio)', '', $aux);
		    $auxs = explode('<td width="140">', $aux );
		    $auxs[0] = trim($auxs[0]);
		    $auxs[1] = str_replace('<td width="245"', '', $auxs[1]);
		    $numeros = explode("-", $auxs[1]);
		    $numeros[0] = trim($numeros[0]);
		    $numeros[1] = trim($numeros[1]);
	
		    $jsonIda .= "\n\t\t{\n\t\t\t" . '"rua" : "' . $auxs[0] .'"' . ", \n\t\t\t" . '"num_inicio" : ' . $numeros[0] . ", \n\t\t\t" . '"num_fim" : ' . $numeros[1] . "\n\t\t}, ";
		}
		unset($value); // break the reference with the last element
		}else{
		 return "";
		}
		$jsonIda.= "]";
		$jsonIda= str_replace(", ]", "\n\t]", $jsonIda);
	}
	if($sentido != 1){
		$volta = $sentidos[1];
		$matches = array();
		$pattern = "/width=\"317\"([\s\S]){1,450}width=\"245\"/";
		$resultado = preg_match_all($pattern, $volta , $matches);
		$jsonVolta = "\t" . '"volta" : [';
		if (count($matches[0]) > 0){
		foreach ($matches[0] as &$value) {
		    $aux = str_replace('width="317">', '', $value);
		    $aux = str_replace('
		 ', '', $aux);
		    $aux = str_replace('</td>', '', $aux);
		    $auxs = explode('<td width="140">', $aux );
		    $auxs[0] = trim($auxs[0]);
		    $auxs[1] = str_replace('<td width="245"', '', $auxs[1]);
		    $numeros = explode("-", $auxs[1]);
		    $numeros[0] = trim($numeros[0]);
		    $numeros[1] = trim($numeros[1]);
	
		    $jsonVolta .= "\n\t\t{\n\t\t\t" . '"rua" : "' . $auxs[0] .'"' . ", \n\t\t\t" . '"num_inicio" : ' . $numeros[0] . ", \n\t\t\t" . '"num_fim" : ' . $numeros[1] . "\n\t\t}, ";
		}
		unset($value); // break the reference with the last element
		}else{
		 $jsonVolta .= "\n\t\t{\n\t\t\t" . '"rua" : ""' . ", \n\t\t\t" . '"num_inicio" : 0' . ", \n\t\t\t" . '"num_fim" : 0' . "\n\t\t}, ";
		}
		$jsonVolta .= "]";
		$jsonVolta = str_replace(", ]", "\n\t]", $jsonVolta);
		
	}
	if($sentido == 1){
		$jSon = "{\n" . $jsonIda . "\n}";
	}else if($sentido == 2){
		$jSon = "{\n" . $jsonVolta . "\n}";
	}else{
		$jSon = "{\n" . $jsonIda . ", \n" . $jsonVolta . "\n}";
	}
	return $jSon ;
}

?>
