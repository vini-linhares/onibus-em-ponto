<?php

exit();
conectaBd();

$handle = fopen("queryParadasLinhas.txt", "r");
if ($handle) {
    while (($line = fgets($handle)) !== false) {
        //echo $line;
        //$sql = "SELECT idparada FROM paradalinha WHERE idlinha = '$linha' order by posicao";
  	$query = $mysqli->query($line);
    }

    fclose($handle);
} else {
    // error opening the file.
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




?>