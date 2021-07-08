<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php


function replace_space($str) 
{ 
  $a = array(' '); 
  $b = array('%20'); 
  return str_replace($a, $b, $str); 
}
function finalizaJson(){
  echo "{}";
  exit();
}

function walkDistances($localInicio, $localFim){
$url = 'https://maps.googleapis.com/maps/api/distancematrix/json';
$endInicio = replace_space($localInicio);
$endFim = replace_space($localFim);


    $data = array (
        'origins' => $endInicio,
        'destinations' => $endFim,
        'mode' => "transit",
        'transit_mode' => "bus",
        'transit_routing_preference' => "less_walking",
        'language' => "pt-BR",
        'key' => 'AIzaSyB1Xve8CT9oikothP7wUrJSx3GhPw_uTYY'
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

$result = json_decode($result, true);


$jsonLinhas = '{"distancia":[';
foreach ($result[rows] as &$value) {    
    $jsonLinhas .= $value[elements][0][distance][value] . ", ";
}
unset($value); // break the reference with the last element
$jsonLinhas .= "]";
$jsonLinhas = str_replace(', ]', ']}', $jsonLinhas );

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
	foreach ($output [vs] as &$value) {    
	    $jsonLinhas .= $value[py].",".$value[px]. "|";
	    $retorno[cod][$i] = $value[p];
	    $i++;
	}
	unset($value); // break the reference ith the last element
	$retorno[jsonLinhas] = $jsonLinhas;
	//print_r($retorno);
	return $retorno;
	
}

function walkDistancesPorLinha($codigoLinha, $localAtual){
	$onibus = positLinhaString($codigoLinha);
	
	$distanciasJson = walkDistances($onibus[jsonLinhas], $localAtual);
	$distancias = object_to_array(json_decode($distanciasJson));    
	//$distancias = json_decode($distanciasJson);   
	//print_r($distancias );
	
	$jsonRetorno = "{\n\t".'"onibus"'." : [";
	$i = 0;
	foreach ($onibus[cod] as &$value) {    
	    $jsonRetorno .= "\n\t\t{\n\t\t\t".'"prefixo" : '. $value.", \n\t\t\t" . '"distancia" : ' . $distancias[distancia][$i]. "\n\t\t}, ";
	    $retorno[cod][$i] = $value[p];
	    $i++;
	}
	unset($value); // break the reference ith the last element
	$jsonRetorno .= "]";
	$jsonRetorno = str_replace(", ]", "\n\t]", $jsonRetorno );
	$jsonRetorno .= "\n}";
	
	
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
	unset($postData);
	
	return $output[0][cl] ;
}




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


//echo positLinhaString("33427 ");
//echo walkDistancesPorLinha("33427", "Av. Guilherme Cotching, 1500");
echo walkDistancesPorLinha($codLinha, $endCompleto);






?>