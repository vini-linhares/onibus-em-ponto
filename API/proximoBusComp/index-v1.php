<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api/proximoBusComp/tester.php?latlng=-23.5109706,-46.5807412&sl=2&linha=2182

function remover_acento($str) 
{ 
  $a = array('À', 'Á', 'Â', 'Ã', 'Ä', 'É', 'Ê', 'Í', 'Î', 'Ó', 'Ô', 'Õ', 'Ú', 'Ü', 'à', 'á', 'â', 'ã', 'ä', 'é', 'ê', 'í', 'î', 'ó', 'ô', 'õ', 'ú', 'ü', 'Ç', 'ç'); 
  $b = array('A', 'A', 'A', 'A', 'A', 'E', 'E', 'I', 'I', 'O', 'O', 'O', 'U', 'U', 'a', 'a', 'a', 'a', 'a', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'u', 'u', 'C', 'c'); 
  return str_replace($a, $b, $str); 
}
function replace_space($str) 
{ 
  $a = array(' '); 
  $b = array('%20'); 
  return str_replace($a, $b, $str); 
}
function finalizaJson(){
  $json = "{\n\t".'"status" : "erro", '."\n\t".'"mensagem" : "Não há onibus registrado nesse sentido no momento.", '."\n\t".'"onibus" : '."[\n\t\t{\n\t\t\t".'"prefixo" : 0, '."\n\t\t\t".'"latitude" : 0, '."\n\t\t\t".'"longitude" : 0, '."\n\t\t\t".'"rua" : "", '."\n\t\t\t".'"numero" : "", '."\n\t\t\t".'"distancia_texto" : "0", '."\n\t\t\t".'"distancia_valor" : 0' . "\n\t\t}\n\t]\n}";
  
  echo $json;
 
  exit();
}
function finalizaErroJson(){
  $json = "{\n\t".'"status" : "erro", '."\n\t".'"mensagem" : "Servidor da SPtrans está fora do ar.", '."\n\t".'"onibus" : '."[\n\t\t{\n\t\t\t".'"prefixo" : 0, '."\n\t\t\t".'"latitude" : 0, '."\n\t\t\t".'"longitude" : 0, '."\n\t\t\t".'"rua" : "", '."\n\t\t\t".'"numero" : "", '."\n\t\t\t".'"distancia_texto" : "0", '."\n\t\t\t".'"distancia_valor" : 0' . "\n\t\t}\n\t]\n}";
  
  echo $json;
 
  exit();
}

function walkDistances($localInicio, $localFim){
$url = 'https://maps.googleapis.com/maps/api/distancematrix/json';
$endInicio = replace_space($localInicio);
$endFim = replace_space($localFim);


    $data = array (
        'origins' => $endInicio,
        'destinations' => $endFim,
        //'mode' => "bicycling",
        'mode' => "transit",
        'transit_mode' => "bus",
        'transit_routing_preference' => "less_walking",
        'language' => "pt-BR",
        //'key' => 'AIzaSyB1Xve8CT9oikothP7wUrJSx3GhPw_uTYY'
        'key' => 'AIzaSyDCnCK0MqAWQ7oRTc807l7Gttutf3I0Hzw'
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();
    
    echo $url.'?'.$params;
    exit();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);

$result = json_decode($result, true);


$jsonLinhas = '{"distancia":[';
foreach ($result[rows] as &$value) {    
    $jsonLinhas .= '{"valor":'.$value[elements][0][distance][value] . ", ";
    $jsonLinhas .= '"texto":"'.$value[elements][0][distance][text] . '", },';
}
unset($value); // break the reference with the last element
$jsonLinhas .= "]";
$jsonLinhas = str_replace(',]', ']}', $jsonLinhas );
$jsonLinhas = str_replace(", }", "}", $jsonLinhas );


//echo $jsonLinhas;

return $jsonLinhas;


//return $result[rows][0][elements][0][distance][value];
}


function object_to_array($data) {

    if (is_array($data) || is_object($data)) {
        $result = array();
        foreach ($data as $key => $value)
            $result[$key] = object_to_array($value);
        return $result;
    }

    return $data;

}
function getResult($accesspoint, $page, $postData, $cookie, $post = true) {
    $ch = curl_init();  

    $t = http_build_query($postData);

    $url = $accesspoint.$page."?".$t;

//  print $url."<br />";

    curl_setopt($ch, CURLOPT_URL, $url);  
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);  
    curl_setopt ($ch, CURLOPT_COOKIEJAR, $cookie);
    curl_setopt ($ch, CURLOPT_COOKIEFILE, $cookie);
//  curl_setopt($ch, CURLOPT_COOKIESESSION, true);

    if ($post == true) {
        curl_setopt($ch, CURLOPT_POST, true);  
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);  
    }
    curl_setopt($ch, CURLOPT_HEADER, 0);  

    $output = curl_exec($ch);  

    curl_close($ch);  

    $output = object_to_array(json_decode($output));    

    return $output;
}


function positLinhaString($codigoLinha){

	$ckfile = tempnam ("cache/cookies", "spt.");

	$postData["token"] = "6c07ae90a397d9c48da1288cfeba0018da8c32449eedc506358dbdf0372c0924";
	$output = getResult("http://api.olhovivo.sptrans.com.br/v2.1", "/Login/Autenticar", $postData, $ckfile);
	//print_r ($output);
	unset($postData);	

    	$postData["codigoLinha"] = $codigoLinha;
	$output = getResult("http://api.olhovivo.sptrans.com.br/v2.1", "/Posicao/Linha", $postData, $ckfile, false);
	//print_r ($output);
	unset($postData);
	
	$jsonLinhas = "";
	$i = 0;
	
	if(!isset($output [vs])){
		finalizaErroJson();
	}
	foreach ($output [vs] as &$value) {    
	    $jsonLinhas .= $value[py].",".$value[px]. "|";
	    $retorno[cod][$i] = $value[p];
	    $retorno[lat][$i] = $value[py];
	    $retorno[lng][$i] = $value[px];
	    $i++;
	}
	unset($value); // break the reference ith the last element
	$retorno[jsonLinhas] = $jsonLinhas;
	//print_r($retorno);
	return $retorno;
	
}

function walkDistancesPorLinha($codigoLinha, $localAtual){
	$onibus = positLinhaString($codigoLinha);
	
	if(strcmp($onibus[jsonLinhas], "") == 0){
		//Pode ser uma indicação de que a linha não está operando nesse momento
		//finalizaJson();
		pegarProximaSaida();
	} 
	
	$distanciasJson = walkDistances($onibus[jsonLinhas], $localAtual);
	$distancias = object_to_array(json_decode($distanciasJson));    
	//$distancias = json_decode($distanciasJson);   
	//echo $distanciasJson;
	//print_r($distancias );
	
	$jsonRetorno = "{\n\t".'"onibus"'." : [";
	$i = 0;
	foreach ($onibus[cod] as &$value) {    
	  $jsonRetorno.= "\n\t\t{\n\t\t\t".'"prefixo" : '.$value.", \n\t\t\t".'"distancia" : '.$distancias[distancia][$i][valor].", \n\t\t\t".'"distancia_texto" : "'.$distancias[distancia][$i][texto].'"'.", \n\t\t\t".'"latidute" : '.$onibus[lat][$i].", \n\t\t\t".'"longitude" : '.$onibus[lng][$i]."\n\t\t}, ";
	    $retorno[cod][$i] = $value[p];
	    $i++;
	}
	unset($value); // break the reference ith the last element
	$jsonRetorno .= "]";
	$jsonRetorno = str_replace(", ]", "\n\t]", $jsonRetorno );
	$jsonRetorno .= "\n}";
	
	//echo $jsonRetorno;
	//exit();
	return $jsonRetorno;
}

function endPotLat($latlng){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'latlng' => $latlng
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 7); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
//
//------------------------------------------Substituir por acesso com array--------------------
//
$matches = array();
$pattern = "/short_name([\s\S]){1,1000}route/";
$resultado = preg_match($pattern, $result, $matches);
//print_r($matches);
$matches2 = array();
$pattern = "/long_name([\s\S]){1,1000}short_name/";
$resultado = preg_match($pattern, $matches[0], $matches2);
$endereco= str_replace('long_name" : "', '', $matches2[0]);
$endereco= str_replace('"short_name', '', $endereco);
$endereco= str_replace('",', '', $endereco);

$pattern = "/short_name([\s\S]){1,1000}street_number/";
$resultado = preg_match($pattern, $matches[0], $matches2);
$numero= str_replace('short_name" : "', '', $matches2[0]);
$numero= str_replace(' "types" : [ "street_number', '', $numero);
$numero= str_replace('",', '', $numero);

$retorno = array();
$retorno[0] = $endereco;
$retorno[1] = $numero;

return $retorno;
}
function endPorLatKey($latlng){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'latlng' => $latlng,
        'key' => "AIzaSyCUOilBG9EYj3K3u6HuFsflMhL4NQTN3GY"
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $endereco= object_to_array(json_decode($result )); 
    $endereco = $endereco["results"][0][address_components];
    
    
    //echo $result;
    //exit();
    
    foreach ($endereco as &$value) {    
	  if($value[types][0] == "route"){
	  	$retorno[rua] = $value[long_name];
	  }
	  if($value[types][0] == "street_number"){
	  	/*
	  	$matches = array();
		$pattern = "/-([\s\S]){1,6}/";
		$resultado = preg_match($pattern, $value[long_name], $matches);
		//var_dump($matches);
		$retorno[num] = str_replace($matches[0], '', $value[long_name]);
		*/
		if(strpos($value[long_name], "-") === false){
			$retorno[num] = $value[long_name];
			$retorno[numVar] = "-1";
		}else{
			$pedacos = explode("-", $value[long_name]);
			$retorno[num] = $pedacos[0];
			$retorno[numVar] = $pedacos[1];
		}
		//echo $retorno[numVar];
	  }
	  if($value[types][0] == "political"){
	  	break;
	  }
	  
	}
    unset($value);
    
    return $retorno;
}
function endPorLat($latlng){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'latlng' => $latlng
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $endereco= object_to_array(json_decode($result )); 
    
    if(!isset($endereco["results"][0][address_components])){
    	return endPorLatKey($latlng);
    }

    
    $endereco = $endereco["results"][0][address_components];
    
    
    //echo $result;
    //exit();
    
    foreach ($endereco as &$value) {    
	  if($value[types][0] == "route"){
	  	$retorno[rua] = $value[long_name];
	  }
	  if($value[types][0] == "street_number"){
	  	/*
	  	$matches = array();
		$pattern = "/-([\s\S]){1,6}/";
		$resultado = preg_match($pattern, $value[long_name], $matches);
		//var_dump($matches);
		$retorno[num] = str_replace($matches[0], '', $value[long_name]);
		*/
		if(strpos($value[long_name], "-") === false){
			$retorno[num] = $value[long_name];
			$retorno[numVar] = "-1";
		}else{
			$pedacos = explode("-", $value[long_name]);
			$retorno[num] = $pedacos[0];
			$retorno[numVar] = $pedacos[1];
		}
		//echo $retorno[numVar];
	  }
	  if($value[types][0] == "political"){
	  	break;
	  }
	  
	}
    unset($value);
    
    return $retorno;
}
function LatPorEnd($end){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'address' => $end
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();
    
    $url = replace_space($url.'?'.$params);

    curl_setopt($ch, CURLOPT_URL, $url ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $endereco= object_to_array(json_decode($result )); 
    
    if(!isset($endereco["results"][0][geometry][location][lat])){
    	return endPorLatKey($latlng);
    }
    
    
    
    if(isset($endereco["results"][0][geometry][location][lat])){
    	$retorno[lat] = $endereco["results"][0][geometry][location][lat];
    }
    if(isset($endereco["results"][0][geometry][location][lng])){
    	$retorno[lng] = $endereco["results"][0][geometry][location][lng];
    }
    return $retorno;
}
function LatPorEndKey($end){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'address' => $end
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();
    
    $url = replace_space($url.'?'.$params);

    curl_setopt($ch, CURLOPT_URL, $url ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $endereco= object_to_array(json_decode($result )); 
    
    
    
    if(isset($endereco["results"][0][geometry][location][lat])){
    	$retorno[lat] = $endereco["results"][0][geometry][location][lat];
    }
    if(isset($endereco["results"][0][geometry][location][lng])){
    	$retorno[lng] = $endereco["results"][0][geometry][location][lng];
    }
    return $retorno;
}


function pegaCodigoLinha($linha, $sentido){
	$ckfile = tempnam ("cache/cookies", "spt.");

	$postData["token"] = "6c07ae90a397d9c48da1288cfeba0018da8c32449eedc506358dbdf0372c0924";
	$output = getResult("http://api.olhovivo.sptrans.com.br/v2.1", "/Login/Autenticar", $postData, $ckfile);
	//print_r ($output);
	unset($postData);	

    	
	$postData["termosBusca"] = $linha;
	$postData["sentido"] = $sentido;
	$output = getResult("http://api.olhovivo.sptrans.com.br/v2.1", "/Linha/BuscarLinhaSentido", $postData, $ckfile, false);
	//print_r ($output);
	//exit();
	unset($postData);
	//echo $output[0][cl] ;
	//exit();
	return $output[0][cl] ;
}
function cmp($a, $b) {
 return $a[distancia] > $b[distancia];
}
function ordenar($array){
$newArray  = $array;
usort($newArray, "cmp"); 
return $newArray ;
}
function pegarItinerario($linha, $sentido){
	$postData["linha"] = $linha;
	$postData["sentido"] = $sentido;
	
	$ch = curl_init();  
	$t = http_build_query($postData);
	$url = "http://onibusemponto.com/api/itinerario/"."?".$t;
	//  print $url."<br />";
	curl_setopt($ch, CURLOPT_URL, $url);  
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);  
	//  curl_setopt($ch, CURLOPT_COOKIESESSION, true);
	curl_setopt($ch, CURLOPT_HEADER, 0);  
	$output = curl_exec($ch);  
	curl_close($ch);  
	return mb_convert_encoding($output, "UTF-8", "iso-8859-1");
}



/*
if(isset($_GET['rua']) && isset($_GET['num'])){
	$rua = $_GET['rua'];
	$num = intval($_GET['num']);
	$endCompleto = $rua . ", " . $num;
}else if(isset($_GET['latlng'])){
	$endnum = endPotLat($_GET['latlng']);
	$rua= $endnum[0];
	$num = intval($endnum[1]);
	$endCompleto = $rua . ", " . $num;
}else if(isset($_GET['end'])){
	$endCompleto = $_GET['end'];
}else{
	//finalizaJson();
}
if(isset($_GET['codLinha'])){
	$codLinha = $_GET['codLinha'];
}else if(isset($_GET['linha']) && isset($_GET['sl'])){
	$codLinha = pegaCodigoLinha($_GET['linha'], $_GET['sl']);
}else{
	//finalizaJson();
}
*/

function trocar_abrev($str) 
{ 

  $a = array('PRACA', 'RUA', 'AVENIDA', 'ACESSO', 'PASSARELO', 'VIADUTO', 'PONTE', 'LARGO', 'ALAMEDA', 'TERMINAL', 'TRAVESSA', 'ESTRADA', 'PARQUE', 'PASSAGEM', 'SUBTERRANEA', 'RODOVIA', 'SEM NOME'); 
  
  $b = array('PCA.', 'R.', 'AV.', 'AC.', 'PAS.', 'VIAD.', 'PTE.', 'LGO.', 'AL.', 'TERM.', 'TR.', 'ESTR.', 'PQ.', 'PAS', 'SUB', 'ROD', 'S/N'); 
  
  $aa = array('BRIGADEIRO', 'CAPITAO', 'SUBTERRANEO', 'ENGENHEIRO', 'SOLDADO', 'PRESIDENTE', 'CONDESSA', 'PADRE', 'PROFESSOR', 'DOUTOR', 'MAJOR', 'ARQUITETO', 'MAESTRO', 'DOM', 'TENENTE', 'SARGENTO', 'DUQUE', 'GENERAL', 'CORONEL', 'SANTO', 'BARAO', 'CONSELHEIRO', 'MINISTRO', 'SAO', 'MONSENHOR', 'SANTA', 'NOSSA', 'SENHORA', 'REVERENDO', 'CABO', 'VILA', 'JARDIM', 'TERMINAL', 'PRINCESA', 'PARQUE', 'CONJUNTO', 'HABITACIONAL', 'HOSPITAL', 'SHOPPING', 'CHACARA', 'SENADOR', 'PADRE', 'MARECHAL', 'ALMIRANTE', 'REGENTE', 'DONA'); 
  
  $bb = array('BRIG.', 'CAP.', 'SUB.', 'ENG.', 'SD.', 'PRES.', 'CDSSA.', 'PE.', 'PROF.', 'DR.', 'MJ.', 'ARQ.', 'MTO.', 'D.', 'TTE.', 'SG.', 'DQ.', 'GEN.', 'CEL.', 'STO.', 'BR.', 'CONS.', 'MIN.', 'S.', 'MONS.', 'STA.', 'NSA.', 'SRA.', 'REV.', 'CB.', 'VL.', 'JD.', 'TERM.', 'PRINC.', 'PQ.', 'CONJ.', 'HAB.', 'HOSP.', 'SHOP.', 'CHAC.', 'SEN.', 'PE.', 'MAL.', 'ALM.', 'REG.', 'DA.'); 
  
  $str = str_replace($a, $b, $str); 
  return str_replace($aa, $bb, $str); 
}
function formataEndereco($end){
	$end = remover_acento($end );
	$end = strtoupper($end );
	$end = trocar_abrev($end);
	return $end;
}
function formataItinerario($itinerario){
	$matches = array();
	$pattern = "/ \(([\s\S]){1,50}\)/";
	$resultado = preg_match_all($pattern, $itinerario , $matches);
	
	$substituir= array_unique($matches[0]);
	
	
	for($i=0; $i < count($substituir); $i++){
		$a[$i] = "";
	}
	$itinerario = str_replace($substituir, $a, $itinerario ); 
	$itinerario = remover_acento($itinerario);
	$itinerario = str_replace("ida", "sentido", $itinerario ); 
	$itinerario = str_replace("volta", "sentido", $itinerario ); 
	return $itinerario;
}


$proximaSaida = false;

if(isset($_GET['rua']) && isset($_GET['num'])){
	$rua = $_GET['rua'];
	$num = intval($_GET['num']);
	$endCompleto = $rua . ", " . $num;
	$latlng = latPorEnd($endCompleto . ", são paulo");
}else if(isset($_GET['latlng'])){
	$latlng = $_GET['latlng'];
	$endnum = endPorLat($latlng);
	$rua= $endnum[rua];
	$num = intval($endnum[num]);
	$numVar = intval($endnum[numVar]);
	$endCompleto = $rua . ", " . $num;
}else if(isset($_GET['end'])){
	$endCompleto = $_GET['end'];
	$latlng = latPorEnd($endCompleto . ", são paulo");
	$endnum = endPorLat($latlng);
	$rua= $endnum[rua];
	$num = intval($endnum[num]);
	$numVar = intval($endnum[numVar]);
}else{
	finalizaJson();
}

if(isset($_GET['codLinha'])){
	$codLinha = $_GET['codLinha'];
}else if(isset($_GET['linha']) && isset($_GET['sl'])){
	$codLinha = pegaCodigoLinha($_GET['linha'], $_GET['sl']);
	$sentido = $_GET['sl'];
	$proximaSaida = true;
}else{
	finalizaJson();
}




/*
$linha = "2182";
$sentido = 1;
$codLinha = pegaCodigoLinha($linha, $sentido);
*/
//$rua = "Av. Das Cerejeiras";
//$num = 2151;
//$endCompleto = $rua . ", " . $num;
//$endCompleto = "-23.502758,-46.573635";


//$saida = endPorLat($endCompleto);
//echo $saida[rua] . ", " . $saida[num];

//$saida = latPorEnd($endCompleto );
//var_dump($saida);
//exit();
 
//echo positLinhaString("33427 ");
//echo walkDistancesPorLinha("33427", "Av. Guilherme Cotching, 1500");
//echo "OI";


$json =  walkDistancesPorLinha($codLinha, $latlng);
//-------------------echo $json;
//exit();
$output = object_to_array(json_decode($json));    
$ordem = ordenar($output[onibus]);
//-----------------print_r($ordem);
//exit();


//$itinerario = pegarItinerario($linha, $sentido);
//$itinerario = formataItinerario($itinerario);
//$itinerario = object_to_array(json_decode($itinerario)); 
//--------------print_r($itinerario);
//exit();

function calcDist($lat_inicial, $long_inicial, $lat_final, $long_final)
{
   $x = ($long_final - $long_inicial) * cos(deg2rad(($lat_inicial + $lat_final)/2));
   $y = $lat_final - $lat_inicial;
   $dist = sqrt($x*$x + $y*$y) * 60*1.852;
   
   return $dist;
}


$pontos[0] = array(-23.542605,-46.635482);
$pontos[1] = array(-23.542164,-46.635699);
$pontos[2] = array(-23.542001,-46.635914);
$pontos[3] = array(-23.541636,-46.634545);
$pontos[4] = array(-23.543150,-46.635721);
$pontos[5] = array(-23.542566,-46.634650);
$pontos[6] = array(-23.536467,-46.633417);
$pontos[7] = array(-23.532794,-46.632538);
$pontos[8] = array(-23.530587,-46.631760);
$pontos[9] = array(-23.526896,-46.630360);
$pontos[10] = array(-23.525393,-46.627072);
$pontos[11] = array(-23.524361,-46.625203);
$pontos[12] = array(-23.520655,-46.623898);
$pontos[13] = array(-23.519698,-46.617935);
$pontos[14] = array(-23.520602,-46.616278);
$pontos[15] = array(-23.523596,-46.612171);
$pontos[16] = array(-23.523967,-46.607373);
$pontos[17] = array(-23.524296,-46.602647);
$pontos[18] = array(-23.526473,-46.599862);
$pontos[19] = array(-23.524365,-46.594160);
$pontos[20] = array(-23.522858,-46.593093);
$pontos[21] = array(-23.520743,-46.591433);
$pontos[22] = array(-23.518022,-46.589262);
$pontos[23] = array(-23.515836,-46.587562);
$pontos[24] = array(-23.513402,-46.585636);
$pontos[25] = array(-23.510690,-46.586865);
$pontos[26] = array(-23.509295,-46.587494);
$pontos[27] = array(-23.507186,-46.589209);
$pontos[28] = array(-23.505312,-46.591327);
$pontos[29] = array(-23.503309,-46.591604);
$pontos[30] = array(-23.501391,-46.590241);
$pontos[31] = array(-23.499934,-46.588268);
$pontos[32] = array(-23.497959,-46.586212);
$pontos[33] = array(-23.495620,-46.585261);
$pontos[34] = array(-23.494814,-46.583202);
$pontos[35] = array(-23.495030,-46.580862);
$pontos[36] = array(-23.495193,-46.578781);
$pontos[37] = array(-23.496566,-46.575964);
$pontos[38] = array(-23.494704,-46.575600);
$pontos[39] = array(-23.490836,-46.574455);
$pontos[40] = array(-23.489460,-46.572640);
$pontos[41] = array(-23.489401,-46.569860);
$pontos[42] = array(-23.486686,-46.568407);
//echo $pontos[42][1];
//$eu = array(-23.515836,-46.587562);
//euPos = 23;

$pontos[0] = array(-23.547245,-46.629620);
$pontos[1] = array(-23.550033,-46.631332);
$pontos[2] = array(-23.547871,-46.633165);
$pontos[3] = array(-23.544847,-46.633835);
$pontos[4] = array(-23.546566,-46.636392);
$pontos[5] = array(-23.545570,-46.638378);
$pontos[6] = array(-23.542753,-46.641526);
$pontos[7] = array(-23.542872,-46.643455);
$pontos[8] = array(-23.544576,-46.643782);
$pontos[9] = array(-23.546972,-46.642674);
$pontos[10] = array(-23.548915,-46.642340);
$pontos[11] = array(-23.550183,-46.639288);
$pontos[12] = array(-23.549267,-46.639310);
$pontos[13] = array(-23.549286,-46.639459);
$pontos[14] = array(-23.549309,-46.639763);
$pontos[15] = array(-23.550239,-46.641555);
$pontos[16] = array(-23.552112,-46.638943);
$pontos[17] = array(-23.551818,-46.632498);
$pontos[18] = array(-23.548977,-46.631206);
$pontos[19] = array(-23.546618,-46.632182);
$eu = array(-23.545570,-46.638378);
$euPos = 5;



foreach($pontos as &$ponto){
	//echo  $ponto[0] . "," . $ponto[1] . "\n";
	//echo calcDist($eu[0], $eu[1], $ponto[0], $ponto[1]) . "\n";
}
foreach ($ordem as &$bus) {
	$distan = 100000;
	$i = 0;
	foreach($pontos as &$ponto){
		$distanI =  calcDist($bus[latidute], $bus[longitude], $ponto[0], $ponto[1]);
		if($distanI < $distan){
			$distan = $distanI;
			$bus[ponto] = $i;
		}
		$i += 1; 
	}
	if ($bus[ponto] > $euPos){
		$bus[ponto] *= -1;
	}else if($bus[ponto] == $euPos){
		$midLat = ($pontos[$euPos][0] + $bus[latidute]) / 2;
		$midLng = ($pontos[$euPos][1] + $bus[longitude]) / 2;
		$midDist = calcDist($pontos[($euPos-1)][0], $pontos[($euPos-1)][1], $midLat, $midLng);
		$antDist = calcDist($pontos[($euPos-1)][0], $pontos[($euPos-1)][1], $bus[latidute], $bus[longitude]);
		if($antDist > $midDist){
			$bus[ponto] *= -1;
		}
	}
}
foreach ($ordem as &$bus) {
	//echo $bus[ponto] . "\n";
	echo $bus[prefixo] . " " . $bus[ponto] . " " . $bus[distancia] ."\n";
}

exit();




function gambLocal($localVerif){
	if(strcmp($localVerif, "LGO. PAISSANDU") == 0){
		$localVerif = "AV. S. JOAO";
	}
	$localVerif = str_replace("Z", "S", $localVerif);
	return $localVerif;
}


$localAtual[latlng] = $latlng;
$localAtual[rua] = formataEndereco($rua);
//echo $rua;
//echo "<br>";
//echo $localAtual[rua];
//exit();
$localAtual[num] = $num;
$localAtual[numVar] = $numVar;
$iLocalAtual = 0;
$i = 0;
foreach ($itinerario[sentido] as &$value) {
	  if(gambLocal($value[rua]) == gambLocal($localAtual[rua])){
	  	$min = intval(min($value[num_inicio], $value[num_fim]));
	  	$max = intval(max($value[num_inicio], $value[num_fim]));
	  	//echo " -- " . $min . "-" . $max . " -- ";
	  	//echo "$localAtual[num]";
	  	if(($localAtual[num] >= $min) && ($localAtual[num] <= $max)){
	  		$iLocalAtual = $i * -1;
	  		break;
	  	}else if($localAtual[numVar] != -1 && (($localAtual[numVar] >= $min) && ($localAtual[numVar] <= $max))){
	  		$iLocalAtual = $i * -1;
	  		break;
	  	}
	  }
	  $i--;
}
//echo "Meu local" . " : " . $iLocalAtual . "[". $localAtual[rua] . "," . $localAtual[num] . "]" . "(" . $localAtual[latlng] . ")" . "\n";
unset($value);
//echo $iLocalAtual;
//exit();
$existProximo = false;
$json = "{\n\t".'"status" : "ok", '."\n\t".'"mensagem" : "Ônibus encontrado", '."\n\t".'"proximo_onibus" : [';
foreach ($ordem as &$valor) {
	$proximoOnibus = endPorLat($valor[latidute] . "," . $valor[longitude]);
	$valor[rua] = $proximoOnibus[rua];
	$valor[num] = $proximoOnibus[num];
	$proximoOnibus[rua] = formataEndereco($proximoOnibus[rua]);
	$iProximoOnibus = -1;
	$i = 0;
	//echo $localAtual[rua] . ", " . $localAtual[num] . " - " . $proximoOnibus[rua] . ", " . $proximoOnibus[num] . " ";
	foreach ($itinerario[sentido] as &$value) {
		  if($value[rua] == $proximoOnibus[rua]){
		  	if ($iLocalAtual < (-1 * $i)){ // Onibus já passou da rua
		  		$iProximoOnibus = $i;
		  		break;
		  	}
		  	if($iLocalAtual == (-1 * $i)){ // Onibus está na rua
		  		if($value[num_inicio] < $value[num_fim]){
		  			$sentido = 1;
		  		}else{
		  			$sentido = -1;
		  		}
		  		if(($proximoOnibus[num] * $sentido) <= ($localAtual[num] * $sentido)){
		  			$iProximoOnibus = $i * -1;
		  			break;
		  		}else{
			  		$iProximoOnibus = $i;
			  		break;
		  		}
		  	}else{ // Onibus ainda não chegou na rua
		  		$min = intval(min($value[num_inicio], $value[num_fim]));
			  	$max = intval(max($value[num_inicio], $value[num_fim]));
			  	if(($proximoOnibus[num] >= $min) && ($proximoOnibus[num] <= $max)){
			  		$iProximoOnibus = $i * -1;
			  		break;
			  	}else if($proximoOnibus[numVar] != -1 && (($proximoOnibus[numVar] >= $min) && ($proximoOnibus[numVar] <= $max))){
			  		$iProximoOnibus = $i * -1;
			  		break;
			  	}
		  	}
		  }
		  $i--;
	}
	//echo $valor[prefixo] . " : " . $iProximoOnibus . "[". $valor[rua] . "," . $valor[num] . "]" . "(" . $valor[latidute] . "," . $valor[longitude] . ")" . "\n";
	unset($value);
	if($iProximoOnibus >=0 && $iProximoOnibus <= $iLocalAtual){
		//echo "Próximo ônibus é: " . $valor[prefixo];
		//calculaDistCorreta($valor, $iProximoOnibus, $localAtual, $iLocalAtual, $itinerario);
		//calculaDist($valor);
		$json = insereOnibus($valor, $json);
		$existProximo = true;
		//break;
	}
}
unset($valor);


if(!$existProximo){
	pegarProximaSaida();
	//finalizaJson();
	//echo "Não há próximos ônibus no momento";
	$json = "{\n\t".'"status" : "erro", '."\n\t".'"mensagem" : "Não há onibus registrado nesse sentido no momento."'."\n}";
	echo $json;
}else{
	$json .= "]\n}";
	$json = str_replace(",]", "\n\t]", $json); 
	
	echo $json;
}
//print_r($ordem);





function insereOnibus($proximoBus, $json){


	$json .= "\n\t\t{\n\t\t\t".'"prefixo" : ' . $proximoBus[prefixo] . ', '."\n\t\t\t".'"latitude" : ' . $proximoBus[latidute] . ', '."\n\t\t\t".'"longitude" : ' . $proximoBus[longitude] . ', '."\n\t\t\t".'"rua" : "' . $proximoBus[rua] . '", '."\n\t\t\t".'"numero" : "' . $proximoBus[num] . '", '."\n\t\t\t".'"distancia_texto" : "' . $proximoBus[distancia_texto] . '", '."\n\t\t\t".'"distancia_valor" : ' . $proximoBus[distancia] . "\n\t\t},";


//	$json = "{\n\t".'"status" : "ok", '."\n\t".'"mensagem" : "Ônibus encontrado", '."\n\t".'"proximo_onibus" : {'."\n\t\t".'"prefixo" : ' . $proximoBus[prefixo] . ', '."\n\t\t".'"latitude" : ' . $proximoBus[latidute] . ', '."\n\t\t".'"longitude" : ' . $proximoBus[longitude] . ', '."\n\t\t".'"rua" : "' . $proximoBus[rua] . '", '."\n\t\t".'"numero" : "' . $proximoBus[num] . ', '."\n\t\t".'"distancia_texto" : "' . $proximoBus[distancia_texto] . '", '."\n\t\t".'"distancia_valor" : ' . $proximoBus[distancia] . "\n\t}\n}";
	return $json;
}




function calculaDist($proximoBus, $localAtual){
	$json = "{\n\t".'"status" : "ok", '."\n\t".'"mensagem" : "Ônibus encontrado", '."\n\t".'"proximo_onibus" : {'."\n\t\t".'"prefixo" : ' . $proximoBus[prefixo] . ', '."\n\t\t".'"latitude" : ' . $proximoBus[latidute] . ', '."\n\t\t".'"longitude" : ' . $proximoBus[longitude] . ', '."\n\t\t".'"rua" : "' . $proximoBus[rua] . '", '."\n\t\t".'"numero" : "' . $proximoBus[num] . ', '."\n\t\t".'"distancia_texto" : "' . $proximoBus[distancia_texto] . '", '."\n\t\t".'"distancia_valor" : ' . $proximoBus[distancia] . "\n\t}\n}";
	print_r($json);
}


//
//
//Itinerários do site da sptrans podem estar errados (Ex. Rua Major Dantas Cortez nos números baixos indica 1300 - 1600
//Dependendo do Lugar pode ter erro por conta das rotas. Tem lugar que não da para ir a pé, e tem lugar que não da para ir de carro.
//
//
function calculaDistCorreta($proximoBus, $iProximoOnibus, $localAtual, $iLocalAtual, $itinerario){

	
	$waypoints = "";
	for($i = ($iProximoOnibus + 1); $i<= $iLocalAtual; $i++){
	
 	 	$ruaAtual = explode(". ", $itinerario[sentido][$i][rua]);
 	 	if(($ruaAtual[0] == 'R' || $ruaAtual[0] == 'AV' || $ruaAtual[0] == 'VIAD' || $ruaAtual[0] == 'TERM' || $ruaAtual[0] == 'ESTR') && ($itinerario[sentido][$i][rua] != "R. MJ. DANTAS CORTEZ")){
 	 		if($i== $iLocalAtual){
 	 			$waypoints .= "via:" .  $itinerario[sentido][$i][rua] . "," . intval(((intval($itinerario[sentido][$i][num_inicio]) + intval($localAtual[num])) / 2)) . ",são paulo|";
 	 		}else{
 	 			$waypoints .= "via:" .  $itinerario[sentido][$i][rua] . "," . intval(((intval($itinerario[sentido][$i][num_inicio]) + intval($itinerario[sentido][$i][num_fim])) / 2)) . ",são paulo|";
 	 		}
 	 	}
 	 	//$b = array('PCA.', 'R.', 'AV.', 'AC.', 'PAS.', 'VIAD.', 'PTE.', 'LGO.', 'AL.', 'TERM.', 'TR.', 'ESTR.', 'PQ.'); 
		
	}
	
	 
	
	

	$url = 'https://maps.googleapis.com/maps/api/directions/json';
	 $data = array (
        'mode' => "walking",
        'origin' => $proximoBus[latidute].",".$proximoBus[longitude],
        'destination' => $localAtual[latlng],
        'waypoints' => $waypoints,
        "language" => "pt-BR",
        'key' => "AIzaSyDAZJyX3WHFwdHJYayCB0e0nvVzG1MEVxw"
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');
        
        
        echo $url.'?'.$params;

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, replace_space($url.'?'.$params) ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $rota = object_to_array(json_decode($result)); 
    
    print_r($result );
    
    
    $json = '{"status" : "ok", "proximo_onibus" : {"prefixo" : ' . $proximoBus[prefixo] . ', "latitude" : ' . $proximoBus[latidute] . ', "longitude" : ' . $proximoBus[longitude] . ', "distancia_texto" : "' . $rota[routes][0][legs][0][distance][text] . '", "distancia_valor" : ' . $rota[routes][0][legs][0][distance][value] . '}';
    
    
echo $json;

}
	


function pegarProximaSaida(){
global $proximaSaida;
	if(!$proximaSaida){
		finalizaJson();
	}

	global $sentido;
	global $linha;
	
	if(date("N") == 7){
		$dia = 2;
	}else if(date("N") == 6){
		$dia = 1;
	}else{
		$dia = 0;
	}
	
	$url = 'http://onibusemponto.com/api/saidas/';
	
	    $data = array (
	    	'linha' => $linha,
	        'dia' => $dia,
	        'sl' => $sentido
	        );
	
	
	        $params = '';
	    foreach($data as $key=>$value)
	                $params .= $key.'='.$value.'&';
	
	        $params = trim($params, '&');
	
	    $ch = curl_init();
	    
	    //echo $url.'?'.$params;
	    //exit();
	
	    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
	    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
	    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 30); //Timeout after 30 seconds
	    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
	    curl_setopt($ch, CURLOPT_HEADER, 0);
	
	        $result = curl_exec($ch);
	    curl_close($ch);
	
	//echo $result;
	$horaAtual = date("H:i");	
	$obHoras = object_to_array(json_decode($result)); 
	$obHoras["horas"] = prepararHora($obHoras);
	$hora = "";
	foreach ($obHoras["horas"] as &$value) {
		if($value["hora"] < $horaAtual){
			//echo "true";
		}else{
		   	$hora = $value["hora"];
		   	break;
		}
	}
	if($hora == ""){
		$hora = $obHoras["horas"][0]["hora"];
	}
	
	$json = "{\n\t".'"status" : "erro2", '."\n\t".'"mensagem" : "Ônibus no terminal", '."\n\t".'"proximo_onibus" : [{'."\n\t\t".'"prefixo" : 0, '."\n\t\t".'"latitude" : 0, '."\n\t\t".'"longitude" : 0, '."\n\t\t".'"rua" : "", '."\n\t\t".'"numero" : "", '."\n\t\t".'"distancia_texto" : "'.$hora.'", '."\n\t\t".'"distancia_valor" : 0' . "\n\t}]\n}";
  echo $json;
	
	exit();
}

function prepararHora($obHoras){

	usort($obHoras["horas"], "compar");
	return $obHoras["horas"];
	//var_dump ($obHoras["horas"]);
	//exit();
/*
	$i = 0;
	while($i < count($obHoras["horas"])){
		if($i != 0){
			if($obHoras["horas"][$i]["hora"] < $obHoras["horas"][($i-1)]["hora"]){
				$arr= explode(":", $pizza);
			}
		}
	*/
}
function compar($a, $b)
{   
    if ($a["hora"] == $b["hora"]) {
    return 0;
    }
    return ($a["hora"] < $b["hora"]) ? -1 : 1;
}


//print_r($ordem);
//print_r($itinerario);
//echo $localAtual[rua] . ", " . $localAtual[num] . " - " . $proximoOnibus[rua] . ", " . $proximoOnibus[num] . " ";
//echo $iLocalAtual . "-" . $iProximoOnibus ;



//var_dump($substituir);


?>