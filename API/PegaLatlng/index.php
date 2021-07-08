<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api/PegaLatlng/?end=cerejeiras%201000

function replace_space($str) 
{ 
  $a = array(' '); 
  $b = array('%20'); 
  return str_replace($a, $b, $str); 
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

function finalizaJson(){
	$json = '{"lat" : 0, "lng" : 0, "rua" : "", "num" : "-1"}';
	echo $json;
	exit();
}
function finalizaErroSP(){
	$json = '{"lat" : 0, "lng" : 0, "rua" : "", "num" : "-2"}';
	echo $json;
	exit();
}




if(isset($_GET['rua']) && isset($_GET['num'])){
	$rua = $_GET['rua'];
	$num = intval($_GET['num']);
	$endCompleto = $rua . ", " . $num;
	$end = pegaEnd($endCompleto . ", são paulo");	
	
}if(isset($_GET['end'])){
	$endCompleto = $_GET['end'];
	$end = pegaEnd($endCompleto . ", são paulo");
	
}else{
	finalizaJson();
}

	//var_dump($end);
	$json = '{"lat" : '.$end[lat].', "lng" : '.$end[lng].', "rua" : "'.$end[rua].'", "num" : "'.$end[num].'"}';
	echo $json;
	



function pegaEnd($end){

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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 15 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    //echo $result;
    
    $endereco= object_to_array(json_decode($result )); 
    
    if(!isset($endereco["results"][0][address_components])){
    	return pegaEndKey($end);
    }
    
    if(isset($endereco["results"][0][geometry][location][lat])){
    	$retorno[lat] = $endereco["results"][0][geometry][location][lat];
    }
    if(isset($endereco["results"][0][geometry][location][lng])){
    	$retorno[lng] = $endereco["results"][0][geometry][location][lng];
    }
    
    $endereco = $endereco["results"][0][address_components];
    
    foreach ($endereco as &$value) {    
	  if($value[types][0] == "route"){
	  	$retorno[rua] = $value[long_name];
	  }
	  if($value[types][0] == "street_number"){
	  	$matches = array();
		$pattern = "/-([\s\S]){1,6}/";
		$resultado = preg_match($pattern, $value[long_name], $matches);
		//var_dump($matches);
		$retorno[num] = str_replace($matches[0], '', $value[long_name]);
	  }
	  if($value[types][0] == "political"){
	  	break;
	  }
	  
	}
    unset($value);
       
    
    return $retorno;
}

function pegaEndKey($end){

	$url = 'https://maps.googleapis.com/maps/api/geocode/json';
	 $data = array (
        'address' => $end,
        'key' => "AIzaSyCUOilBG9EYj3K3u6HuFsflMhL4NQTN3GY"
        );

        
        $params = '';
    foreach($data as $key=>$value)
                $params .= $key.'='.$value.'&';
         
        $params = trim($params, '&');

    $ch = curl_init();
    
    $url = replace_space($url.'?'.$params);
    
    //echo $url;

    curl_setopt($ch, CURLOPT_URL, $url ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    //echo $result;
    
    $endereco= object_to_array(json_decode($result )); 
    
    if(!isset($endereco["results"][0][address_components])){
    	finalizaJson();
    }
    
    
    if(isset($endereco["results"][0][geometry][location][lat])){
    	$retorno[lat] = $endereco["results"][0][geometry][location][lat];
    }
    if(isset($endereco["results"][0][geometry][location][lng])){
    	$retorno[lng] = $endereco["results"][0][geometry][location][lng];
    }
    
    $endereco = $endereco["results"][0][address_components];
    
    foreach ($endereco as &$value) {    
	  if($value[types][0] == "route"){
	  	$retorno[rua] = $value[long_name];
	  }
	  if($value[types][0] == "street_number"){
	  	$matches = array();
		$pattern = "/-([\s\S]){1,6}/";
		$resultado = preg_match($pattern, $value[long_name], $matches);
		//var_dump($matches);
		$retorno[num] = str_replace($matches[0], '', $value[long_name]);
	  }
	  if($value[types][0] == "political"){
	  	break;
	  }
	  
	}
    unset($value);
       
    
    return $retorno;
}




























































?>
