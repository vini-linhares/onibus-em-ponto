<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php
//http://onibusemponto.com/api2/pegaPontos/?lat=-23.559147&lng=-46.675521

function finalizaJson(){
  echo '{ "pontos" : [ { "id" : "-1", "lat" : 0, "lng" : 0, "rua" : "Nenhum ponto encontrado nas redondesas", "num" : "" } ] }';
  exit();
}
function finalizaJsonParam(){
  echo '{ "pontos" : [ { "id" : "-2", "lat" : 0, "lng" : 0, "rua" : "Faltam parâmetros necessarios para a busca.", "num" : "" } ] }';
  exit();
}

function conectaBd(){
  global $mysqli;
  $mysqli = new mysqli("HOST", 'USER','PASS',"DATABASE");
  if (mysqli_connect_errno()){
    trigger_error(mysqli_connect_error());
    echo "erro";
    exit();
  }
  $mysqli->set_charset('utf8');
}












if(isset($_GET['lat']) && isset($_GET['lng'])){
	$lat = $_GET['lat'];
	$lng = $_GET['lng'];
}else{
	finalizaJsonParam();
}
if(isset($_GET['raio'])){
	$prec = $_GET['raio'];
}else{
  $prec = 0.4;
}





$deltaLat = $prec / 111.12;
$deltaLng = $prec / (111.12*cos($lat * M_PI / 180));

conectaBd();
  

  $sql = "SELECT * FROM paradas where lat between ($lat - $deltaLat) and ($lat + $deltaLat) and lng between ($lng - $deltaLng) and ($lng + $deltaLng)" ;


//$time_start = microtime(true);
  $query = $mysqli->query($sql);
//$time_end = microtime(true);
//$time = $time_end - $time_start;
//echo $time;

  //echo 'Registros encontrados: ' . $query->num_rows;

  //exit();
  $pontos = array();
  $i = 0;
  while ($dados = $query->fetch_array(MYSQLI_ASSOC)) {
    $pontos[$i] = $dados;
    $pontos[$i]['dist'] = calcDistanciaSimples2($dados['lat'], $dados['lng'], $lat, $lng);
    $i++;
  }
  //echo $pontos[$i-1]['dist'];
  $pontos = ordenar($pontos);
  //exit();
  
  $json =  '{"pontos" : [';
  foreach ($pontos as &$ponto) {
      if($ponto['dist'] <= $prec){
        if (strcmp($ponto['rua'], "") == 0 || strcmp($ponto['num'], "") == 0){
          $end = endPorLatKey($ponto['lat'] . "," . $ponto['lng']);
          $ponto['rua'] = $end["rua"];
          $ponto['num'] = $end["num"];
          
          $sql = "UPDATE paradas SET rua = '".$ponto['rua']."', num = '".$ponto['num']."' WHERE id = '".$ponto['id']."'" ;
          $query = $mysqli->query($sql);
        }
        $json .= '{"id" : "'.$ponto['id'].'", "lat" : '.$ponto['lat'].', "lng" : '.$ponto['lng'].', "rua" : "'.$ponto['rua'].'", "num" : "'.$ponto['num'].'"}, ';
      }
  }


  $json .= ']}';
  $json = str_replace(", ]}", "]}", $json);

if(strcmp($json, '{"pontos" : []}') == 0){
  finalizaJson();
}

  echo $json;















  function calcDistanciaSimples2($lat_inicial, $long_inicial, $lat_final, $long_final)
{
   $x = ($long_final - $long_inicial) * cos((($lat_inicial + $lat_final)/2) * M_PI / 180);
   $y = $lat_final - $lat_inicial;
   $dist = sqrt($x*$x + $y*$y) * 60*1.852;

   return $dist;
}

function cmp($a, $b) {
 return $a['dist'] > $b['dist'];
}
function ordenar($array){
$newArray  = $array;
usort($newArray, "cmp");
return $newArray ;
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

    //echo $url.'?'.$params;
    //exit();
    curl_setopt($ch, CURLOPT_URL, $url.'?'.$params ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);

        $result = curl_exec($ch);
    curl_close($ch);
    
    //echo $result;
    //exit();


    $endereco= object_to_array(json_decode($result ));
    $endereco = $endereco["results"][0]["address_components"];

    foreach ($endereco as &$value) {
	  if($value["types"][0] == "route"){
	  	$retorno["rua"] = $value["long_name"];
	  }
	  if($value["types"][0] == "street_number"){
  		if(strpos($value["long_name"], "-") === false){
  			$retorno["num"] = $value["long_name"];
  			$retorno["numVar"] = "-1";
  		}else{
  			$pedacos = explode("-", $value["long_name"]);
  			$retorno["num"] = $pedacos[0];
  			$retorno["numVar"] = $pedacos[1];
  		}
	  }
	  if($value["types"][0] == "political"){
	  	break;
	  }
	}
    unset($value);
    
    //var_dump($retorno);
    //exit();

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
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);

        $result = curl_exec($ch);
    curl_close($ch);

    $endereco= object_to_array(json_decode($result ));

    if(!isset($endereco["results"][0]["address_components"])){
    	return endPorLatKey($latlng);
    }

    $endereco = $endereco["results"][0]["address_components"];

    foreach ($endereco as &$value) {
	  if($value["types"][0] == "route"){
	  	$retorno["rua"] = $value["long_name"];
	  }
	  if($value["types"][0] == "street_number"){
  		if(strpos($value["long_name"], "-") === false){
  			$retorno["num"] = $value["long_name"];
  			$retorno["numVar"] = "-1";
  		}else{
  			$pedacos = explode("-", $value["long_name"]);
  			$retorno["num"] = $pedacos[0];
  			$retorno["numVar"] = $pedacos[1];
  		}
	  }
	  if($value["types"][0] == "political"){
	  	break;
	  }
	}
    unset($value);

    return $retorno;
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


?>
