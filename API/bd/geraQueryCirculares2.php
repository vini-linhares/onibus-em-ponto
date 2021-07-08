<?php header("Content-type: text/html; charset=utf-8"); header("Refresh:2");?>
<?php


conectaBd();
$file = 'queryCircularesErro.txt';

$lineAr = "";

$conteudo = "";
$pular = true;
$handle = fopen("circularesErro.txt", "r");
if ($handle) {
    while (($line = fgets($handle)) !== false) {
        if(!$pular){
           $conteudo .= $line;
        }else{
           //$conteudo .= $line;
           $lineAr = explode(";", $line);
           $pular = false;
        }
    }

    fclose($handle);
} else {
    // error opening the file.
} 
$file = 'circularesErro.txt';
file_put_contents($file, $conteudo);



        $linha = $lineAr[0];
        $destino = $lineAr[1] . ", São Paulo";



$destinoCoord = LatPorEnd($destino);

//var_dump($destinoCoord);

  $sql = "SELECT idparada FROM paradalinha WHERE idlinha = '$linha' order by posicao";
  $query = $mysqli->query($sql);

  
  $pontos = array();
  while ($dados = $query->fetch_array(MYSQLI_ASSOC)) {
  	
     $sql = "SELECT id, lat, lng FROM paradas WHERE id = ".$dados['idparada'];
     
     $query2 = $mysqli->query($sql);
     $dados2 = $query2->fetch_array(MYSQLI_ASSOC);
     
     $dist = calcDist($destinoCoord["lat"], $destinoCoord["lng"], $dados2['lat'], $dados2['lng']);
     
     $pontos[] = array($dados2['id'], $dist);
  }

  $maisProximo = 0;
  for($i = 0; $i < count($pontos); $i++){
     if($pontos[$i][1] < $pontos[$maisProximo][1]){
     	 $maisProximo = $i;
     }
  }
//var_dump($pontos);
//echo $pontos[$maisProximo][0];

  $pos = 1;
  for($i = $maisProximo; $i < count($pontos); $i++){
     $sql = "UPDATE paradalinha SET sentido = 1, posicao = ". $pos ." WHERE idparada = " . $pontos[$i][0] . " and idlinha LIKE '" . $linha . "' and posicao > " . $maisProximo . ";\r\n";
     echo $sql . "<br>";
     //$query = $mysqli->query($sql);
     
     $file = 'queryCircularesErro.txt';
     $current = file_get_contents($file);
     $current .= $sql;
     file_put_contents($file, $current);

     $pos += 1;
  }
        
        
        
     $file = 'queryCircularesErro.txt';
     $current = file_get_contents($file);
     $current .= "\r\n";
     file_put_contents($file, $current);








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

function calcDist($lat_inicial, $long_inicial, $lat_final, $long_final)
{
   $x = ($long_final - $long_inicial) * cos(deg2rad(($lat_inicial + $lat_final)/2));
   $y = $lat_final - $lat_inicial;
   $dist = sqrt($x*$x + $y*$y) * 60*1.852;
   
   return $dist;
}

function LatPorEnd($end){

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

    curl_setopt($ch, CURLOPT_URL, $url ); //Url together with parameters
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); //Return data instead printing directly in Browser
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT , 20); //Timeout after 7 seconds
    curl_setopt($ch, CURLOPT_USERAGENT , "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    curl_setopt($ch, CURLOPT_HEADER, 0);
    
        $result = curl_exec($ch);
    curl_close($ch);
    
    $endereco= object_to_array(json_decode($result )); 
    //echo $result;
    //exit();
    
    
    if(isset($endereco["results"][0][geometry][location][lat])){
    	$retorno[lat] = $endereco["results"][0][geometry][location][lat];
    }
    if(isset($endereco["results"][0][geometry][location][lng])){
    	$retorno[lng] = $endereco["results"][0][geometry][location][lng];
    }
    return $retorno;
}

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


?>