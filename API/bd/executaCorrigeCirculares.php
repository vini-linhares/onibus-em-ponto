<?php

//exit();
conectaBd();

$handle = fopen("queryCircularesCerto.txt", "r");
if ($handle) {
    while (($line = fgets($handle)) !== false) {
        if(strcmp($line, "") !== 0){
  		$query = $mysqli->query($line);
  		//echo "1" . $line;
  	}
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