<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php

function replace_accent($str) 
{ 
  $a = array('À', 'Á', 'Â', 'Ã', 'Ä', 'É', 'Ê', 'Í', 'Î', 'Ó', 'Ô', 'Õ', 'Ú', 'Ü', 'à', 'á', 'â', 'ã', 'ä', 'é', 'ê', 'í', 'î', 'ó', 'ô', 'õ', 'ú', 'ü', 'Ç', 'ç', ' ', '+', '
'); 
  $b = array('%C0', '%C1', '%C2', '%C3', '%C4', '%C9', '%CA', '%CD', '%CE', '%D3', '%D4', '%D5', '%DA', '%DC', '%E0', '%E1', '%E2', '%E3', '%E4', '%E9', '%EA', '%ED', '%EE', '%F3', '%F4', '%F5', '%FA', '%FC', '%C7', '%E7', '%20', '%20', '%20'); 
  return str_replace($a, $b, $str); 
}
function finalizaJson(){
  echo '{ "linhas" : [ { "lt" : "", "tl" : 0 }] }';
  exit();
}
function finalizaErroSP(){
  echo '{ "linhas" : [ { "lt" : "erro1", "tl" : 0 }] }';
  exit();
}

//endPotLat("-23.506646,-46.620499");

$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/linhaselecionada.asp';



if(isset($_GET['latlng'])){
	$endnum = endPotLat($_GET['latlng']);
	$end= replace_accent($endnum[0]);
	//echo $end;
	//exit();
	$num = intval($endnum[1])-100;
}elseif(isset($_GET['rua']) && isset($_GET['num'])){
	$end = replace_accent($_GET['rua']);
	$num = intval($_GET['num'])-100;
} else{
	finalizaJson();
}

$resultado = pegaLinhas($end, $num, $numFim);
//echo $resultado;
//exit();
if (strcmp($resultado, '{ "linhas" : []}') == 0){
	sleep(4);
	$resultado = pegaLinhas($end, $num, $numFim);
	if (strcmp($resultado, '{ "linhas" : []}') == 0){
		sleep(4);
		$boolControle = false;
		$resultadoControle = pegaLinhas(replace_accent("Avenida Cerejeiras"), '766', '966');
		if (strcmp($resultadoControle, '{ "linhas" : []}') == 0){
			$boolControle = true;
		}
		$resultado = pegaLinhas($end, $num, $numFim);
		if (strcmp($resultado, '{ "linhas" : []}') == 0){
			if($boolControle){
				finalizaErroSP();
			}else{
				finalizaJson();
			}
		}
	}
}
echo $resultado;

function pegaLinhas($end, $num, $numFim){
global $url;
//$end = isset($_GET['end']) ? replace_accent($_GET['end']) : (finalizaJson());
//$num = isset($_GET['num']) ? (intval($_GET['num'])-100) : (finalizaJson());
$numFim = $num + 200;
//$end .= replace_accent("Avenida Cerejeiras");
//$num = '766';
//$numFim = '966';

    $data = array (
        'PPD' => '0',
        'NOT' => '0',
        'endereco' => $end,
        'numero' => $num,
        'numero_fim' => $numFim
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');
//echo $url.'?'.$params;
//exit();
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);

//echo $result;

$needle   = 'DadosDest_1';
$pos      = strripos($result, $needle);
if (!($pos === false)) {   
$matches = array();
$pattern = "/DadosDest_1([\s\S]){1,60}\" \/\>/";
$resultado = preg_match($pattern, $result, $matches);
$codigos= str_replace('DadosDest_1" value="', '', $matches[0]);
$codigos= str_replace('" />', '', $codigos);
}else{
$matches = array();
$pattern = "/GfTreID([\s\S]){1,60}\";/";
$resultado = preg_match($pattern, $result, $matches);
$codigos= str_replace('GfTreID=', '', $matches[0]);
$codigos= str_replace('&CODLOG=', ';', $codigos);
$codigos= str_replace('&GfNosID=', ';', $codigos);
$codigos= str_replace('";', '', $codigos);
}


$codigo = explode(";", $codigos);

//echo $codigo[2];
//var_dump($codigo);

    $data = array (
        'PPD' => '0',
        'NOT' => '0',
        'endereco' => $end,
        'numero' => $num,
        'numero_fim' => $numFim,
        'LOCALIZOU' => '1',
        'GfTreID' => $codigo[0],
        'CODLOG' => $codigo[1],
        'GfNosID' => $codigo[2]
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
    
//$result= str_replace('src="', 'src="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
//$result= str_replace('href="', 'href="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
//echo $result;




$matches = array();
$pattern = "/Linha: ([\s\S]){5,10}&nbsp;/";
$resultado = preg_match_all($pattern, $result, $matches);
//var_dump($matches);

$jsonLinhas = "{ ".'"linhas"'." : [";
foreach ($matches[0] as &$value) {
    $aux = str_replace('Linha: ', '', $value);
    $aux = str_replace('&nbsp;', '', $aux);
    $auxs = explode("-", $aux );

    //$jsonLinhas .= "\n\t\t{\n\t\t\t".'"lt" : "' . $auxs[0] . '"' . ", \n\t\t\t".'"tl" : ' . $auxs[1] . "\n\t\t}, ";
    $jsonLinhas .= "{".'"lt" : "' . $auxs[0] . '"' . ", ".'"tl" : ' . $auxs[1] . "}, ";
}
unset($value); // break the reference with the last element
$jsonLinhas .= "]";
$jsonLinhas = str_replace(", ]", "]", $jsonLinhas );
$jsonLinhas .= "}";

return $jsonLinhas;
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


//echo $result;

$matches = array();
$pattern = "/short_name([\s\S]){1,1000}route/";
$resultado = preg_match($pattern, $result, $matches);

if(count($matches[0]) == 0){
		return endPotLatKey($latlng);
}

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

function endPotLatKey($latlng){
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


	//echo $result;
	
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

?>
