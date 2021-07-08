<?php



conectaBd();



$sql = "SELECT `num`, `nome2` FROM `linha` WHERE `iscric` = 1";
 $query = $mysqli->query($sql);

  
  $linhas = "";
  while ($dados = $query->fetch_array(MYSQLI_ASSOC)) {
  
     $linhas .= $dados['num'] . ";" . $dados['nome2'] . "\r\n";
  }
  
$file = 'circulares.txt';
$current = file_get_contents($file);
$current .= $linhas;
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











?>