<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api/calcDist/?inicio=-23.499924671569,-46.5803306057011&fim=-23.5770,-46.5230

function replace_space($str) 
{ 
  $a = array(' '); 
  $b = array('%20'); 
  return str_replace($a, $b, $str); 
}
function finalizaJson(){
	echo $json = '{"texto" : "", "valor" : -1}';
	exit();
}


function walkDistances($localInicio, $localFim){
$url = 'https://maps.googleapis.com/maps/api/distancematrix/json';
$endInicio = replace_space($localInicio);
$endFim = replace_space($localFim);


    $data = array (
        'origins' => $endInicio,
        'destinations' => $endFim,
        'mode' => "walking",
        /*'transit_mode' => "bus",
        'transit_routing_preference' => "less_walking",*/
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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 20 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);

$result = json_decode($result, true);

//var_dump($result[rows][0][elements][0][distance][text]);


$json = '{"texto" : "'.$result[rows][0][elements][0][distance][text].'", "valor" : '.$result[rows][0][elements][0][distance][value].'}';
//echo $json;
//exit();


return $json ;


}





//echo walkDistances("avenida das cerejeiras, 100, são paulo","rua padre saboia de medeiros, 1691, são paulo");

if(isset($_GET['inicio']) && isset($_GET['fim'])){
	$inicio = $_GET['inicio'];
	$fim = $_GET['fim'];
	
}else{
	finalizaJson();
}

	//var_dump($end);
	$json = walkDistances($inicio, $fim);
	if(strcmp($json,'{"texto" : "", "valor" : }') == 0){
		echo '{"texto" : "", "valor" : -1}';
		exit();
	}
	echo $json;
	







































?>