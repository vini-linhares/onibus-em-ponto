<?php header("Content-type: text/html; charset=UTF-8", true); ?>
<?


function finalizaJson(){
  echo '{ "ida" : [ { "rua" : "", "num_inicio" : 0, "num_fim" : 0 }';
  exit();
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


$url = 'http://virtualartsa.com.br/onibusemponto/api/itinerario/';


        if($sentido == -1){
	   $data = array (
	   	'linha' => $numLinha
	   );
	}else{
	   $data = array (
	   	'linha' => $numLinha,
	   	'sentido'=> $sentido
	   );
	}


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


echo utf8_encode($result);


?>