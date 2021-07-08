<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api2/proximoBus?ponto=910000871&sl=1&linha=1156-10

function finalizaJson(){
  $json = "{\n\t".'"status" : "erro", '."\n\t".'"mensagem" : "Não há onibus registrado nesse sentido no momento.", '."\n\t".'"onibus" : '."[\n\t\t{\n\t\t\t".'"prefixo" : 0, '."\n\t\t\t".'"latitude" : 0, '."\n\t\t\t".'"longitude" : 0, '."\n\t\t\t".'"rua" : "", '."\n\t\t\t".'"numero" : "", '."\n\t\t\t".'"distancia_texto" : "0", '."\n\t\t\t".'"distancia_valor" : 0' . "\n\t\t}\n\t]\n}";
  
  echo $json;
  exit();
}
function finalizaErroJson(){
  $json = "{\n\t".'"status" : "erro1", '."\n\t".'"mensagem" : "Servidor da SPtrans está fora do ar.", '."\n\t".'"onibus" : '."[\n\t\t{\n\t\t\t".'"prefixo" : 0, '."\n\t\t\t".'"latitude" : 0, '."\n\t\t\t".'"longitude" : 0, '."\n\t\t\t".'"rua" : "", '."\n\t\t\t".'"numero" : "", '."\n\t\t\t".'"distancia_texto" : "0", '."\n\t\t\t".'"distancia_valor" : 0' . "\n\t\t}\n\t]\n}";
  
  echo $json;
  exit();
}




function positLinhaString($linha, $sentido){
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
	//return $output[0][cl] ;
	$codigo = $output[0][cl];
		

    	$postData["codigoLinha"] = $codigo;
	$output = getResult("http://api.olhovivo.sptrans.com.br/v2.1", "/Posicao/Linha", $postData, $ckfile, false);
	//print_r ($output);
	unset($postData);
	
	$jsonLinhas = "";
	$i = 0;
	
	if(!isset($output [vs])){
		pegarProximaSaida();
	}
	foreach ($output [vs] as &$value) {    
	    //$jsonLinhas .= $value[py].",".$value[px]. "|";
	    $retorno[$i][0] = $value[p];
	    $retorno[$i][1] = $value[py];
	    $retorno[$i][2] = $value[px];
	    $i++;
	}
	unset($value); // break the reference ith the last element
	if(!isset($retorno)){
		pegarProximaSaida();
	}
	//$retorno[jsonLinhas] = $jsonLinhas;
	//print_r($retorno);
	return $retorno;
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


function object_to_array($data) {

    if (is_array($data) || is_object($data)) {
        $result = array();
        foreach ($data as $key => $value)
            $result[$key] = object_to_array($value);
        return $result;
    }

    return $data;

}


function conectaBd(){
  global $mysqli;
  $mysqli = new mysqli("HOST", 'USER','PASS',"DATABASE");
  if (mysqli_connect_errno()){
    trigger_error(mysqli_connect_error());
    echo "erro";
    exit();
  }
  $mysqli->set_charset('utf8');
}



function calcDist($lat_inicial, $long_inicial, $lat_final, $long_final)
{
   $x = ($long_final - $long_inicial) * cos(deg2rad(($lat_inicial + $lat_final)/2));
   $y = $lat_final - $lat_inicial;
   $dist = sqrt($x*$x + $y*$y) * 60*1.852;
   
   return $dist;
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




function walkDistances($localInicio, $localFim){
$url = 'https://maps.googleapis.com/maps/api/distancematrix/json';
$endInicio = $localInicio;
$endFim = $localFim;


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
    
    //echo $url.'?'.$params;
    //exit();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);

$result = json_decode($result, true);


$distanciaBus = array();
$i = 0;
foreach ($result[rows] as &$value) {
    $end = explode (" - ", $result[origin_addresses][$i]);
    if(strpos($end[0], ',') !== false){
    	$end = explode(", ", $end[0]);
    	if(strpos($end[1], '-') !== false){
    	   $numAux = explode("-", $end[1]);
    	   $end[1] = $numAux[0];
        }
    }else{
    	$end = explode(", ", $end[1]);
    	if(strpos($end[1], '-') !== false){
    	   $numAux = explode("-", $end[1]);
    	   $end[1] = $numAux[0];
        }
    }
    
    if(preg_match("/([1-9])+[a-z]?/", $end[1])){
       //echo $end[1];
    }else {
       $endAux = endPorLatKey($latlng);
       $end[0] = $endAux[rua];
       $end[1] = $endAux[num];
    }
    
    $distanciaBus[$i][0] = $end[0];
    $distanciaBus[$i][1] = $end[1];
    $distanciaBus[$i][2] = $value[elements][0][distance][text];
    $distanciaBus[$i][3] = $value[elements][0][distance][value];
    $i++;
}

//var_dump($distanciaBus);
//exit();
return $distanciaBus;

}



function cmp($a, $b) {
 return $a[6] > $b[6];
}
function ordenar($array){
$newArray  = $array;
usort($newArray, "cmp"); 
return $newArray ;
}





function pegarProximaSaida(){

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
	if(strcmp($obHoras[0]["horas"], "-2:00") == 0 || strcmp($obHoras[0]["horas"], "-1:00") == 0){
		finalizaJson();
	}
	$obHoras = prepararHora($obHoras);
	$obHoras = corrigeOutroDiaHora($obHoras);
	//var_dump($obHoras);
	//exit();
	$hora = "";
	foreach ($obHoras as &$value) {
		if($value["hora"] < $horaAtual){
			//echo "true";
		}else{
		   	$hora = $value["hora"];
		   	break;
		}
	}
	if($hora == ""){
		$hora = $obHoras[0]["hora"];
	}
	
	$json = "{\n\t".'"status" : "erro2", '."\n\t".'"mensagem" : "Ônibus no terminal", '."\n\t".'"proximo_onibus" : {'."\n\t\t".'"prefixo" : 0, '."\n\t\t".'"latitude" : 0, '."\n\t\t".'"longitude" : 0, '."\n\t\t".'"rua" : "", '."\n\t\t".'"numero" : "", '."\n\t\t".'"distancia_texto" : "'.recorrigeOutroDiaHora($hora).'", '."\n\t\t".'"distancia_valor" : 0' . "\n\t}\n}";
  echo $json;
	
	exit();
}

function prepararHora($obHoras){

	usort($obHoras["horas"], "compar");
	return $obHoras["horas"];
}
function compar($a, $b)
{   
    if ($a["hora"] == $b["hora"]) {
    return 0;
    }
    return ($a["hora"] < $b["hora"]) ? -1 : 1;
}
function corrigeOutroDiaHora($obHoras){
		$horaAr = explode(":", $obHoras[0]["hora"]);
		if(strcmp($horaAr[0], "00") == 0){
			$obHoras[count($obHoras)]["hora"] = "24:" . $horaAr[1];
		}
	return $obHoras;
}
function recorrigeOutroDiaHora($horas){
	$horaAr = explode(":", $horas);
	if(strcmp($horaAr[0], "24") == 0){
		return "00:" . $horaAr[1];
	}else{
		return $horas;
	}
}



















if(isset($_GET['ponto'])){
	$ponto = $_GET['ponto'];
}else{
	finalizaJson();
}

if(isset($_GET['linha']) && isset($_GET['sl'])){
	$linha = $_GET['linha'];
	$sentido = intval($_GET['sl']);
}else{
	finalizaJson();
}

$onibus = positLinhaString($linha, $sentido);
//var_dump($onibus);

  conectaBd();
  $sql = "SELECT idparada FROM paradalinha WHERE idlinha = '$linha' AND `sentido` = ".($sentido-1)." order by posicao";
  $query = $mysqli->query($sql);

  
  $pontos = array();
  while ($dados = $query->fetch_array(MYSQLI_ASSOC)) {
  
     $sql = "SELECT id, lat, lng FROM paradas WHERE id = ".$dados['idparada'];
     
     $query2 = $mysqli->query($sql);
     $dados2 = $query2->fetch_array(MYSQLI_ASSOC);
     
     $pontos[] = array($dados2['id'], $dados2['lat'], $dados2['lng']);
  
  }
//var_dump($pontos);
//exit();

 

$i = 1;
$euPos = 0;
foreach($pontos as &$parada){
	if($parada[0] == $ponto){
		$euPos = $i;
		break;
	}
	$i += 1;
}
$positString = "";
$maisProximo = 0;
foreach ($onibus as &$bus) {
	$distan = 100000;
	$i = 1;
	foreach($pontos as &$parada){
		$distanI =  calcDist($bus[1], $bus[2], $parada[1], $parada[2]);
		if($distanI < $distan){
			$distan = $distanI;
			$bus[ponto] = $i;
		}
		$i += 1; 
	}
	if ($bus[ponto] > $euPos){
		$bus[ponto] *= -1;
	}else if($bus[ponto] == $euPos){
		$midLat = ($pontos[$euPos][1] + $bus[1]) / 2;
		$midLng = ($pontos[$euPos][2] + $bus[2]) / 2;
		$midDist = calcDist($pontos[($euPos-1)][1], $pontos[($euPos-1)][2], $midLat, $midLng);
		$antDist = calcDist($pontos[($euPos-1)][1], $pontos[($euPos-1)][2], $bus[latidute], $bus[longitude]);
		if($antDist > $midDist){
			$bus[ponto] *= -1;
		}
	}
	if($bus[ponto] > $maisProximo){
		 $maisProximo = $bus[ponto];
	}
}

foreach ($onibus as &$bus) {
	if($bus[ponto] ==  $maisProximo){
		$proximosOnibus[] = $bus;
		$positString .= $bus[1] . "," . $bus[2] . "|";
	}
}


if($proximosOnibus != null){
	$json = '{ "status" : "ok", "mensagem" : "Ônibus encontrado", "proximo_onibus" : ';
	
	$positEu = $pontos[($euPos-1)][1].",".$pontos[($euPos-1)][2];
	$distanciaBus = walkDistances($positString, $positEu);
	
	$i = 0;
	foreach ($proximosOnibus as &$bus) {
		$bus[3] = $distanciaBus[$i][0];
		$bus[4] = $distanciaBus[$i][1];
		$bus[5] = $distanciaBus[$i][2];
		$bus[6] = $distanciaBus[$i][3];
		$i += 1;
	}
	
	$proximosOnibus = ordenar($proximosOnibus);
	
	
		$json .= '{"prefixo" : '.$proximosOnibus[0][0].', "latitude" : '.$proximosOnibus[0][1].', "longitude" : '.$proximosOnibus[0][2].', "rua" : "'.$proximosOnibus[0][3].'", "numero" : "'.$proximosOnibus[0][4].'", "distancia_texto" : "'.$proximosOnibus[0][5].'", "distancia_valor" : '.$proximosOnibus[0][6].'}';
		
	$json .= '}';
	
	echo $json;
}else{
	pegarProximaSaida();
}

?>