<?php header("Content-type: text/html; charset=iso-8859-1"); ?>
<?php
//http://onibusemponto.com/api/saidas/?linha=1156

function finalizaJson(){
  //echo "{}";
  echo '{"horas":[{"hora":"-1:00"}]}';
  exit();
}

function finalizaErroSP(){
  //echo "{}"; Quando o Site da SPtrans está com erro
  echo '{"horas":[{"hora":"-2:00"}]}';
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
if(isset($_GET['dia'])){
	if($_GET['dia'] == "1"){
		$codDia = $_GET['dia'];
	}else if($_GET['dia'] == "2"){
		$codDia = $_GET['dia'];
	}else{
		$codDia = "0";
	}
}else{
	$codDia = "0";
}
if(isset($_GET['sl'])){
	if($_GET['sl'] == "2"){
		$codSentido= 2;
	}else if($_GET['dia'] == "2"){
		$codSentido= "1";
	}
}else{
	$codSentido= "1";
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
global $codDia;
global $codSentido;
	$url = 'http://itinerarios.extapps.sptrans.com.br/PlanOperWeb/detalheLinha.asp';
	
	    $data = array (
	    	'TpDiaIDpar' => $codDia,
	        'TpDiaID' => '0',
	        'DfSenID' => $codSentido,
	        'CdPjOID' => $codPesquisa
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
	    
	
	$itens0 = explode('Partidas por sentido', $result);
	if(isset($itens0[1]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	$itens = explode('Tempo estimado de viagem', $itens0[1]);
	if(isset($itens[0]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	$itens = explode('tabelaHorarios', $itens[0]);
	if(isset($itens[1]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	$itens = explode('</table>', $itens[1]);
	if(isset($itens[0]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	$horas = explode('</tr>', $itens[0]);
	if(isset($horas [1]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	//var_dump($horas );
	
	$saida = "";
	$primeiraVez = true;
	foreach ($horas as &$value) {
		if($primeiraVez){
			$primeiraVez = false;
		}else{
		   	$hora = explode('<td>', $value);
		   	//echo $hora[3];
			$saida .= $hora[3];
		}
	}
	
	//echo $saida;
	
	
	$hora = explode('<td>', $horas [5]);
	if(isset($hora[3]) != 1 ){
		//finalizaJson();
		return "";
	}
	
	
	
	//echo $hora[3];
	//exit();
	
	
	$matches = array();
	$pattern = "/[\d][\d][:][\d][\d]/";
	$resultado = preg_match_all($pattern, $saida , $matches);
	
	
	//var_dump($matches[0]);
	//exit();
	
	
	$json = '{"horas":[';
	
	foreach ($matches[0] as &$value) {
		$json .= '{"hora":"'.$value.'"},';
	}
	$json .= "]}";
	$json = str_replace('},]}', '}]}', $json);
	return $json;
	
	
	
}

?>
