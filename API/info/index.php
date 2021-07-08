<?php header("Content-type: text/html; charset=iso-8859-1"); ?>
<?php

function finalizaJson(){
  //echo "{}";
  echo '{"ida" : "Linha0000-00", "volta" : "", "semana" : { "inicial" : "", "fim" : ""}, "sabado" : { "inicial" : "", "fim" : ""}, "domingo" : { "inicial" : "", "fim" : ""}}';
  exit();
}

function finalizaErroSP(){
  //echo "{}";
  echo '{"ida" : "Linha0000-10", "volta" : "", "semana" : { "inicial" : "", "fim" : ""}, "sabado" : { "inicial" : "", "fim" : ""}, "domingo" : { "inicial" : "", "fim" : ""}}';
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
	if($resultado == 0){
		return "";
		//finalizaJson();
	}
	$codigos= str_replace('CdPjOID=', '', $matches[0]);
	$codigos= str_replace('" target=', '', $codigos);
	
	//echo $codigos;
	//exit();

	return $codigos;
}


if(isset($_GET['linha'])){
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

$resultado = pegaInformacoes($codLinha);
if (strcmp($resultado, "") != 0){
	echo $resultado;
}else{
	sleep(5);
	$resultado = pegaInformacoes($codLinha);
	if (strcmp($resultado, "") != 0){
		echo $resultado;
	}else{
		sleep(5);
		$resultado = pegaInformacoes($codLinha);
		if (strcmp($resultado, "") != 0){
			echo $resultado;
		}else{
			finalizaErroSP();
		}
	}
}


function pegaInformacoes($codPesquisa){
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
	    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 30); //Timeout after 30 seconds
	    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
	    curl_setopt($ch, CURLOPT_HEADER, 0);
	
	        $result = curl_exec($ch);
	    curl_close($ch);
	
	$itens = explode('itemTitulo', $result);
	if(isset($itens[1]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	$matches = array();
	$pattern = "/<\/strong>([\s\S]){1,55}<\/td><\/li>/";
	$resultado = preg_match_all($pattern, $itens[1] , $matches);
	$ida = str_replace('</strong> ', '', $matches[0][0]);
	$ida = str_replace('</td></li>', '', $ida);
	$volta = str_replace('</strong> ', '', $matches[0][1]);
	$volta = str_replace('</td></li>', '', $volta);
	
	
	$itens[2] = str_replace(' ', '', $itens[2]);
	//echo $itens[2];
	
	$matches = array();
	$pattern = "/sexta([\s\S]){1,80}<\/tr>/";
	$resultado = preg_match($pattern, $itens[2] , $matches);
	$matches2 = array();
	$pattern = "/<tdwidth=\"2([\s\S]){1,20}<\/td>/";
	$resultado = preg_match_all($pattern, $matches[0] , $matches2);
	$semana[inicial] = str_replace('<tdwidth="226">', '', $matches2[0][0]);
	$semana[inicial] = str_replace('</td>', '', $semana[inicial]);
	$semana[finalP] = str_replace('<tdwidth="238">', '', $matches2[0][1]);
	$semana[finalP] = str_replace('</td>', '', $semana[finalP]);
	
	$matches = array();
	$pattern = "/bado([\s\S]){1,80}<\/tr>/";
	$resultado = preg_match($pattern, $itens[2] , $matches);
	$matches2 = array();
	$pattern = "/<tdwidth=\"2([\s\S]){1,20}<\/td>/";
	$resultado = preg_match_all($pattern, $matches[0] , $matches2);
	$sabado[inicial] = str_replace('<tdwidth="226">', '', $matches2[0][0]);
	$sabado[inicial] = str_replace('</td>', '', $sabado[inicial]);
	$sabado[finalP] = str_replace('<tdwidth="238">', '', $matches2[0][1]);
	$sabado[finalP] = str_replace('</td>', '', $sabado[finalP]);
	
	$matches = array();
	$pattern = "/Feriado([\s\S]){1,80}<\/tr>/";
	$resultado = preg_match($pattern, $itens[2] , $matches);
	$matches2 = array();
	$pattern = "/<tdwidth=\"2([\s\S]){1,20}<\/td>/";
	$resultado = preg_match_all($pattern, $matches[0] , $matches2);
	$domingo[inicial] = str_replace('<tdwidth="226">', '', $matches2[0][0]);
	$domingo[inicial] = str_replace('</td>', '', $domingo[inicial]);
	$domingo[finalP] = str_replace('<tdwidth="238">', '', $matches2[0][1]);
	$domingo[finalP] = str_replace('</td>', '', $domingo[finalP]);
	
	
	//var_dump($domingo);
	
	//echo $matches[0];
	
	
	$json = '{"ida" : "'.$ida.'", "volta" : "'.$volta.'", "semana" : { "inicial" : "'.$semana[inicial].'", "fim" : "'.$semana[finalP].'"}, "sabado" : { "inicial" : "'.$sabado[inicial].'", "fim" : "'.$sabado[finalP].'"}, "domingo" : { "inicial" : "'.$domingo[inicial].'", "fim" : "'.$domingo[finalP].'"}}';
	
	return $json;
}

?>
