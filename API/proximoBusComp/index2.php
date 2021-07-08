<?php header("Content-type: text/html; charset=UTF-8", true); ?>
<?


function finalizaJson(){
  $json = "{\n\t".'"status" : "erro", '."\n\t".'"mensagem" : "Não há onibus registrado nesse sentido no momento.", '."\n\t".'"onibus" : '."[\n\t\t{\n\t\t\t".'"prefixo" : 0, '."\n\t\t\t".'"latitude" : 0, '."\n\t\t\t".'"longitude" : 0, '."\n\t\t\t".'"rua" : "", '."\n\t\t\t".'"numero" : "", '."\n\t\t\t".'"distancia_texto" : "0", '."\n\t\t\t".'"distancia_valor" : 0' . "\n\t\t}\n\t]\n}";
  $json = str_replace("\r", "", $result);
  $json = str_replace("\n", "", $json);
  $json = str_replace("\t", "", $json);
  exit();
}


if(isset($_GET['latlng'])){
	$latlng = $_GET['latlng'];
}else{
	finalizaJson();
}

if(isset($_GET['linha']) && isset($_GET['sl'])){
	$linha = $_GET['linha'];
	$sentido = $_GET['sl'];
}else{
	finalizaJson();
}


$url = 'http://virtualartsa.com.br/onibusemponto/api/proximoBusComp/';


       
	   $data = array (
	   	'linha' => $linha,
	   	'sl'=> $sentido,
	   	'latlng' => $latlng
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


     $json = str_replace("\r", "", $result);
     $json = str_replace("\n", "", $json);
     $json = str_replace("\t", "", $json);
     echo $json;

//echo utf8_encode($result);


?>