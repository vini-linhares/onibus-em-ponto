<?php header("Content-type: text/html; charset=utf-8"); ?>
<?php

function finalizaJson(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0000"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Nenhuma linha encontrada nesse local."' ."\n\t\t}\n\t]\n}";
  echo $jsonLinhas;
  exit();
}
function finalizaErroParam(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0003"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Faltam parâmetros necessarios para a busca."' ."\n\t\t}\n\t]\n}";
  echo $jsonLinhas;
  exit();
}
function finalizaErroJson(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0001"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Nossos servidores não responderam."' ."\n\t\t}\n\t]\n}";
  $jsonLinhas = str_replace("\n", "", $jsonLinhas);
  $jsonLinhas = str_replace("\t", "", $jsonLinhas);
  echo $jsonLinhas;
  exit();
}
function finalizaErroSP(){
  //echo "{}";
  $jsonLinhas .= "{\n\t".'"linhas"'." : [\n\t\t{\n\t\t\t".'"numero" : "0002"' . ", \n\t\t\t".'"operacao" : 0' .", \n\t\t\t".'"sentido" : 0' . ", \n\t\t\t".'"nome" : "Servidores da SpTrans não responderam"' ."\n\t\t}\n\t]\n}";
  $jsonLinhas = str_replace("\n", "", $jsonLinhas);
  $jsonLinhas = str_replace("\t", "", $jsonLinhas);
  echo $jsonLinhas;
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





if(isset($_GET['ponto'])){
	$ponto = $_GET['ponto'];
}else{
	finalizaErroParam();
}




  conectaBd();

  $sql = "SELECT idlinha, sentido FROM paradalinha WHERE idparada = '$ponto'" ;
  //$sql = "SELECT linha.nome1, paradalinha.sentido FROM linha, paradalinha  WHERE linha.num IN (SELECT paradalinha.idlinha FROM paradalinha WHERE paradalinha.idparada = '301714')";

//$time_start = microtime(true);
  $query = $mysqli->query($sql);
//$time_end = microtime(true);
//$time = $time_end - $time_start;
//echo $time;

  //echo 'Registros encontrados: ' . $query->num_rows;

  $json =  '{"linhas" : [';

  //exit();
  $pontos = array();
  while ($dados = $query->fetch_array(MYSQLI_ASSOC)) {
  
     if($dados['sentido'] == 0){
     	$sql = "SELECT nome2 as nome FROM linha WHERE num = '".$dados['idlinha']."'" ;
     }else{
     	$sql = "SELECT nome1 as nome FROM linha WHERE num = '".$dados['idlinha']."'" ;
     }
     $query2 = $mysqli->query($sql);
     $dados2 = $query2->fetch_array(MYSQLI_ASSOC);
     
     $nome = $dados2['nome'];
     $linhaAr = explode("-", $dados['idlinha']);
     $num = $linhaAr[0];
     $op =  $linhaAr[1];
     $sentido = $dados['sentido'] + 1;
     
  
     $json .= '{"numero" : "'.$num.'", "operacao" : '.$op.', "sentido" : '.$sentido.', "nome" : "'.$nome.'"}, ';
  }


  $json .= ']}';
  $json = str_replace(", ]}", "]}", $json);

if(strcmp($json, '{"linhas" : []}') == 0){
  finalizaJson();
}

  echo $json;

?>
