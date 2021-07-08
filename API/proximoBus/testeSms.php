<?php

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


function object_to_array($data) {

    if (is_array($data) || is_object($data)) {
        $result = array();
        foreach ($data as $key => $value)
            $result[$key] = object_to_array($value);
        return $result;
    }

    return $data;

}






$ckfile = tempnam ("cache/cookies", "spt.");

	$postData["token"] = "6c07ae90a397d9c48da1288cfeba0018da8c32449eedc506358dbdf0372c0924";
	$postData["message"] = "Teste de envio pela API";
	$postData["message_type"] = "promotional";
	$postData["subject"] = "11951558967";
	$postData["subject_type"] = "number";
	$output = getResult("https://api.enviarsms.io/v1", "/send", $postData, $ckfile);
	//print_r ($output);
	unset($postData);	

    	
	


?>











