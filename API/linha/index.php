<?php header("Content-Type: text/html; charset=UTF-8", true);?>
<?php
//http://onibusemponto.com/api/linha/?busca=119

$site["sptrans"]["accesspoint"] = "http://api.olhovivo.sptrans.com.br/v2.1";
$site["sptrans"]["page"]["Login"] = "/Login/Autenticar";
$site["sptrans"]["page"]["Linha"] = "/Linha/Buscar";

$site["sptrans"]["token"] = "6c07ae90a397d9c48da1288cfeba0018da8c32449eedc506358dbdf0372c0924"; //This should contain your token.

error_reporting(E_ALL);
ini_set('display_errors', 1);

function object_to_array($data) {

    if (is_array($data) || is_object($data)) {
        $result = array();
        foreach ($data as $key => $value)
            $result[$key] = object_to_array($value);
        unset($value);
        return $result;
    }

    return $data;

}

function arrayToJson($obj){
	
	$retorno = "[";
	
	foreach ($obj as &$linha) {  
	    $retorno .= "{";  
	    foreach($linha as $key=>$data) {
	    	if((strcmp($key, "lc") == 0)){
	        	$retorno .= '"'.$key . '" : ' . (($data) ? 'true' : 'false') . ',';
	    	}else if((strcmp($key, "lt") == 0) || (strcmp($key, "tp") == 0) || (strcmp($key, "ts") == 0)){
	        	$retorno .= '"'.$key . '" : "' . $data . '",';
	    	}else{
	    		$retorno .= '"'.$key . '" : ' . $data . ",";
	    	}
	    	//echo($key . '" : ' . $data);
	    }
	    $retorno .= "},";
	}
	$retorno .= "]";
	$retorno = str_replace(',}', '}', $retorno);
	$retorno = str_replace(',]', ']', $retorno);
	
	
	
	return $retorno;
	
}


function getResult($accesspoint, $page, $postData, $cookie, $post = true) {
    $ch = curl_init();  

    $t = http_build_query($postData);

    $url = $accesspoint.$page."?".$t;

//  print $url."<br />";

    curl_setopt($ch, CURLOPT_URL, $url);  
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);  
    curl_setopt ($ch, CURLOPT_COOKIEJAR, $cookie);
    curl_setopt ($ch, CURLOPT_COOKIEFILE, $cookie);
//  curl_setopt($ch, CURLOPT_COOKIESESSION, true);

    if ($post == true) {
        curl_setopt($ch, CURLOPT_POST, true);  
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);  
    }
    curl_setopt($ch, CURLOPT_HEADER, 0);  

    $output = curl_exec($ch);  

    curl_close($ch);  

    //$output = object_to_array(json_decode($output));    

    return $output;
}

function retirarRedundancia($output){
	$output = object_to_array(json_decode($output)); 
	
	$array = array();
	$i = 0;
	$aux = "0000";
	$aux2 = "00";
	foreach ($output as &$value) {    
	    if(strcmp($value['lt'], $aux) == 0 && strcmp($value['tl'], $aux2) == 0){
	    	array_push($array, $i);
	    }else{
		$aux = $value['lt'];
		$aux2 = $value['tl'];
	    }
	    $i++;
	}
	unset($value); // break the reference ith the last element
	foreach ($array as &$value) {    
	    unset($output[$value]);
	}
	unset($value); 
	
//var_dump($output);
//exit();
return arrayToJson($output);
}




function finalizaJson(){
  //echo "{}";
  echo '{"linhas":[{"cl" : 0,"lc" : false,"lt" : "0000","sl" : 0,"tl" : 0,"tp" : "","ts" : ""}]}';
  exit();
}


if(isset($_GET['busca'])){
	$busca= $_GET['busca'];
}else{
	finalizaJson();
}

//Create a cookie for the duration of the page.
$ckfile = tempnam ("cache/cookies", "spt.");

//print "Authentication<br />";
$postData["token"] = $site["sptrans"]["token"];
$output = getResult($site["sptrans"]["accesspoint"], $site["sptrans"]["page"]["Login"], $postData, $ckfile);
//print_r ($output);
unset($postData);

//print "<hr />";

//print "Linha<br />";
$postData["termosBusca"] = $busca;
$output = getResult($site["sptrans"]["accesspoint"], $site["sptrans"]["page"]["Linha"], $postData, $ckfile, false);
if(strcmp($output, "[]") == 0){
$output = '{"linhas":[{"cl":0,"lc":false,"lt":"0000","sl":1,"tl":0,"tp":"ida","ts":"volta"}]}';
}else{
$output = retirarRedundancia($output);
$output = '{"linhas":' . $output . '}';
}
echo $output;
unset($postData);

//print "<hr />";

//Delete the cookie
unlink($ckfile);

?>