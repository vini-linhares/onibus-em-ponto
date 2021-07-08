<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php

function replace_accent($str) 
{ 
  $a = array('À', 'Á', 'Â', 'Ã', 'Ä', 'É', 'Ê', 'Í', 'Î', 'Ó', 'Ô', 'Õ', 'Ú', 'Ü', 'à', 'á', 'â', 'ã', 'ä', 'é', 'ê', 'í', 'î', 'ó', 'ô', 'õ', 'ú', 'ü', 'Ç', 'ç', ' ', '+', '
'); 
  $b = array('%C0', '%C1', '%C2', '%C3', '%C4', '%C9', '%CA', '%CD', '%CE', '%D3', '%D4', '%D5', '%DA', '%DC', '%E0', '%E1', '%E2', '%E3', '%E4', '%E9', '%EA', '%ED', '%EE', '%F3', '%F4', '%F5', '%FA', '%FC', '%C7', '%E7', '%20', '%20', '%20'); 
  return str_replace($a, $b, $str); 
}

function tirarAcentos($string){
    
    $unwanted_array = array(    'Š'=>'S', 'š'=>'s', 'Ž'=>'Z', 'ž'=>'z', 'À'=>'A', 'Á'=>'A', 'Â'=>'A', 'Ã'=>'A', 'Ä'=>'A', 'Å'=>'A', 'Æ'=>'A', 'Ç'=>'C', 'È'=>'E', 'É'=>'E',
                            'Ê'=>'E', 'Ë'=>'E', 'Ì'=>'I', 'Í'=>'I', 'Î'=>'I', 'Ï'=>'I', 'Ñ'=>'N', 'Ò'=>'O', 'Ó'=>'O', 'Ô'=>'O', 'Õ'=>'O', 'Ö'=>'O', 'Ø'=>'O', 'Ù'=>'U',
                            'Ú'=>'U', 'Û'=>'U', 'Ü'=>'U', 'Ý'=>'Y', 'Þ'=>'B', 'ß'=>'Ss', 'à'=>'a', 'á'=>'a', 'â'=>'a', 'ã'=>'a', 'ä'=>'a', 'å'=>'a', 'æ'=>'a', 'ç'=>'c',
                            'è'=>'e', 'é'=>'e', 'ê'=>'e', 'ë'=>'e', 'ì'=>'i', 'í'=>'i', 'î'=>'i', 'ï'=>'i', 'ð'=>'o', 'ñ'=>'n', 'ò'=>'o', 'ó'=>'o', 'ô'=>'o', 'õ'=>'o',
                            'ö'=>'o', 'ø'=>'o', 'ù'=>'u', 'ú'=>'u', 'û'=>'u', 'ý'=>'y', 'þ'=>'b', 'ÿ'=>'y' );
return $str = strtr( $string, $unwanted_array );
    
}


function finalizaJson(){
  //echo "{}";
  $jsonLinhas .= "{".'"linhas"'." : [{".'"numero" : "0000"' . ", ".'"operacao" : 0' .", ".'"sentido" : 0' . ", ".'"nome" : ""' ."}]}";
  echo $jsonLinhas;
  exit();
}

//endPotLat("-23.506646,-46.620499");

$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/linhaselecionada.asp';



if(isset($_GET['latlng'])){
	$endnum = endPotLat($_GET['latlng']);
	$end= replace_accent($endnum[0]);
	$num = intval($endnum[1])-100;
}elseif(isset($_GET['rua']) && isset($_GET['num'])){
	$end = replace_accent($_GET['rua']);
	$num = intval($_GET['num'])-100;
} else{
	finalizaJson();
}

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

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 7); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);



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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 7); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
//$result= str_replace('src="', 'src="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
//$result= str_replace('href="', 'href="http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/', $result);
//echo $result;

//$result = mb_convert_encoding($result, "UTF-8", "iso-8859-1");
$result = utf8_encode($result);

$result = str_replace('	', '', $result );
//echo $result;
//exit();


$matches = array();
$pattern = "/Linha: ([\s\S]){5,70}<\/a>/";
$resultado = preg_match_all($pattern, $result, $matches);
//var_dump($matches);

//echo isset($matches[0]);
//echo count($matches[0]);
if(count($matches[0]) == 0){
finalizaJson();
}

$jsonLinhas = "{\n\t".'"linhas"'." : [";
foreach ($matches[0] as &$value) {

    
$value= str_replace('
</a>', '', $value);
    
    $aux = str_replace('Linha: ', '', $value);
    //$aux = str_replace('&nbsp;', '', $aux);
    $auxs2 = explode("&nbsp;", $aux );
    $auxs = explode("-", $auxs2[0] );
    $aux3 = explode(" / ", $auxs2[1] );
    

    $jsonLinhas .= "{".'"numero" : "' . $auxs[0] . '"' . ", ".'"operacao" : ' . $auxs[1] .", ".'"sentido" : 2' . ", ".'"nome" : "' . $aux3[0] . '"' ."}, ";
    $jsonLinhas .= "{".'"numero" : "' . $auxs[0] . '"' . ", ".'"operacao" : ' . $auxs[1] .", ".'"sentido" : 1' . ", ".'"nome" : "' . $aux3[1] . '"' ."}, ";
}
unset($value); // break the reference with the last element
$jsonLinhas .= "]";
$jsonLinhas = str_replace(", ]", "\n\t]", $jsonLinhas );
$jsonLinhas .= "\n}";
$jsonLinhas = str_replace("\r", "", $jsonLinhas );
$jsonLinhas = str_replace("\n", "", $jsonLinhas );
$jsonLinhas = str_replace("\t", "", $jsonLinhas );

//sleep(10);
echo $jsonLinhas;



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
