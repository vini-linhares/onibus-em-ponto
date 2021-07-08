<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api/pegaLinhasComp/?latlng=-23.508081,-46.579270
//finalizaErroSP();
//sleep(70);
//echo "bla bla";
//exit();
function replace_accent($str) 
{ 
  $a = array('À', 'Á', 'Â', 'Ã', 'Ä', 'É', 'Ê', 'Í', 'Î', 'Ó', 'Ô', 'Õ', 'Ú', 'Ü', 'à', 'á', 'â', 'ã', 'ä', 'é', 'ê', 'í', 'î', 'ó', 'ô', 'õ', 'ú', 'ü', 'Ç', 'ç', ' ', '+', '
'); 
  $b = array('%C0', '%C1', '%C2', '%C3', '%C4', '%C9', '%CA', '%CD', '%CE', '%D3', '%D4', '%D5', '%DA', '%DC', '%E0', '%E1', '%E2', '%E3', '%E4', '%E9', '%EA', '%ED', '%EE', '%F3', '%F4', '%F5', '%FA', '%FC', '%C7', '%E7', '%20', '%20', '%20'); 
  return str_replace($a, $b, $str); 
}

function replace_j($str) 
{ 
  $a = array('
 ', ' '); 
  $b = array("", ""); 
  return str_replace($a, $b, $str); 
}

function finalizaJson(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0000"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Nenhuma linha encontrada nesse local."' ."\n\t\t}\n\t]\n}";
  echo $jsonLinhas;
  exit();
}
function finalizaErroParam(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0003"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Faltam parâmetros necessarios para a busca."' ."\n\t\t}\n\t]\n}";
  echo $jsonLinhas;
  exit();
}
function finalizaErroJson(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0001"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Nossos servidores não responderam."' ."\n\t\t}\n\t]\n}";
  $jsonLinhas = str_replace("\n", "", $jsonLinhas);
  $jsonLinhas = str_replace("\t", "", $jsonLinhas);
  echo $jsonLinhas;
  exit();
}
function finalizaErroSP(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0002"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Servidores da SpTrans não responderam"' ."\n\t\t}\n\t]\n}";
  $jsonLinhas = str_replace("\n", "", $jsonLinhas);
  $jsonLinhas = str_replace("\t", "", $jsonLinhas);
  echo $jsonLinhas;
  exit();
}


//endPotLat("-23.506646,-46.620499");




if(isset($_GET['latlng'])){
	$endnum = endPotLat($_GET['latlng']);
	$end= replace_accent($endnum[0]);
	$num = intval($endnum[1])-100;
}elseif(isset($_GET['rua']) && isset($_GET['num'])){
	$end = replace_accent($_GET['rua']);
	$num = intval($_GET['num'])-100;
}else{
	finalizaErroParam();
}

//echo $end . " " . replace_j($endnum[0]);
//exit();

//$end = isset($_GET['end']) ? replace_accent($_GET['end']) : (finalizaJson());
//$num = isset($_GET['num']) ? (intval($_GET['num'])-100) : (finalizaJson());
$numFim = $num + 200;
//$end .= replace_accent("Avenida Cerejeiras");
//$num = '766';
//$numFim = '966';

//echo $end;
//echo "<br>";
//echo $num;
//echo "<br>";
//echo $numFim;





if((strcmp(replace_j($endnum[0]), "AvenidaSãoJoão") == 0) && ($num < 600)){
		$cods = array("116231", "9173", "81666");
}else{
$cods = array();
$cods = pegaCodigos($end, $num, $numFim);
if (strcmp($cods[0], "") == 0){
	sleep(4);
	$cods = pegaCodigos($end, $num, $numFim);
	if (strcmp($cods[0], "") == 0){
		sleep(4);
		$boolControle = false;
		$codLinhaControle = pegaCodigos("Avenida%20das%20Cerejeiras", 892, 1092);
		if (strcmp($codLinhaControle [0], "") == 0){
			$boolControle = true;
			sleep(5);
		}
		$cods = pegaCodigos($end, $num, $numFim);
		if (strcmp($cods[0], "") == 0){
			if($boolControle){
				finalizaErroSP();
			}else{
				finalizaJson();
			}
		}
	}
}
}

//print_r($cods);

$linhas = pegaLinhas($cods, $end, $num, $numFim);
if (strcmp($linhas, "") != 0){
	echo $linhas;
}else{
	sleep(4);
	$linhas = pegaLinhas($cods, $end, $num, $numFim);
	if (strcmp($linhas, "") != 0){
		echo $linhas ;
	}else{
		sleep(4);
		$boolControle = false;
		$codLinhaControle = pegaCodigos($cods,"Avenida%20das%20Cerejeiras", 892, 1092);
		if (strcmp($codLinhaControle, "") == 0){
			$boolControle = true;
			sleep(5);
		}
		$linhas = pegaLinhas($cods, $end, $num, $numFim);
		if (strcmp($linhas, "") != 0){
			echo $linhas ;
		}else{
			if($boolControle){
				finalizaErroSP();
			}else{
				finalizaJson();
			}
		}
	}
}



function pegaCodigos($end, $num, $numFim){
/*
if($num != 892){
	echo "aqui";
	return array("", "", "");
}else{
	echo "fora";
}
*/
//return array("", "", "");
$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/linhaselecionada.asp';
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
    
    //echo $url.'?'.$params;

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
	
	//if(count($matches[0]) == 0){
	//	echo "aqui";
	//	//finalizaJson();
	//	return array("", "", "");
	//}
	//var_dump($matches[0]);
	if(!isset($matches[0])){
		//finalizaJson();
		return array("", "", "");
	}
	
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
	//var_dump($codigo);
	return $codigo;
}



function pegaLinhas($codigo, $end, $num, $numFim){
$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/linhaselecionada.asp';
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
    
   // echo $url.'?'.$params;

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
	
	$result = mb_convert_encoding($result, "UTF-8", "iso-8859-1");
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
		//finalizaJson();
		return "";
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
	    $aux3 = str_replace("</a>", "", $aux3);
	    
	
	    $jsonLinhas .= "\n\t\t{\n\t\t\t".'"numero" : "' . $auxs[0] . '"' . ", \n\t\t\t".'"operacao" : ' . $auxs[1] .", \n\t\t\t".'"sentido" : 2' . ", \n\t\t\t".'"nome" : "' . $aux3[0] . '"' ."\n\t\t}, ";
	    $jsonLinhas .= "\n\t\t{\n\t\t\t".'"numero" : "' . $auxs[0] . '"' . ", \n\t\t\t".'"operacao" : ' . $auxs[1] .", \n\t\t\t".'"sentido" : 1' . ", \n\t\t\t".'"nome" : "' . $aux3[1] . '"' ."\n\t\t}, ";
	}
	unset($value); // break the reference with the last element
	$jsonLinhas .= "]";
	$jsonLinhas = str_replace(", ]", "\n\t]", $jsonLinhas );
	$jsonLinhas .= "\n}";
	$jsonLinhas = str_replace("\r", "", $jsonLinhas );
	$jsonLinhas = str_replace("\n", "", $jsonLinhas );
	$jsonLinhas = str_replace("\t", "", $jsonLinhas );
	
	//sleep(10);
	//echo $jsonLinhas;
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
	
	//if(!isset($matches[0])){
	//    	return endPotLatKey($latlng);
	//}
	if(count($matches[0]) == 0){
		return endPotLatKey($latlng);
	}
	
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
	//print_r($retorno);
	//exit();
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
