<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api/pegaEnd/?latlng=-23.499924671569,-46.5803306057011

function object_to_array($data) {

    if (is_array($data) || is_object($data)) {
        $result = array();
        foreach ($data as $key => $value)
            $result[$key] = object_to_array($value);
        return $result;
    }

    return $data;

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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 7); //Timeout after 7 seconds
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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 7); //Timeout after 7 seconds
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



if(isset($_GET['latlng'])){
	$latlng = $_GET['latlng'];
	$array = endPorLat($latlng);
	$json = '{"num" : "'.$array[num].'", "rua" : "'.$array[rua].'"}';
	echo $json;
}else{
	echo $json = '{"num" : "-1", "rua" : ""}';
}

//var_dump(endPorLat("-23.494943,-46.580476"));































?>

