<?php


$mysqli = new mysqli("HOST", 'USER','PASS',"DATABASE");
  if (mysqli_connect_errno()){
    trigger_error(mysqli_connect_error());
    echo "erro";
    exit();
  }
  
  



$sql = "CREATE TABLE `DATABASE`.`paradas` ( `id` INT UNSIGNED NOT NULL , `lat` DOUBLE NOT NULL , `lng` DOUBLE NOT NULL , `rua` VARCHAR(64) NOT NULL , `num` VARCHAR(12) NOT NULL , PRIMARY KEY (`id`), INDEX (`lat`)) ENGINE = MyISAM;" ;



$query = $mysqli->query($sql);













?>