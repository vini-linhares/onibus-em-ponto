<?php


function calcDistancia($lat_inicial, $long_inicial, $lat_final, $long_final)
{
    $d2r = 0.017453292519943295769236;

    $dlong = ($long_final - $long_inicial) * $d2r;
    $dlat = ($lat_final - $lat_inicial) * $d2r;

    $temp_sin = sin($dlat/2.0);
    $temp_cos = cos($lat_inicial * $d2r);
    $temp_sin2 = sin($dlong/2.0);

    $a = ($temp_sin * $temp_sin) + ($temp_cos * $temp_cos) * ($temp_sin2 * $temp_sin2);
    $c = 2.0 * atan2(sqrt($a), sqrt(1.0 - $a));

    return 6368.1 * $c;
}

function distancia1($lat1, $lon1, $lat2, $lon2) {

$lat1 = deg2rad($lat1);
$lat2 = deg2rad($lat2);
$lon1 = deg2rad($lon1);
$lon2 = deg2rad($lon2);

$dist = (6371 * acos( cos( $lat1 ) * cos( $lat2 ) * cos( $lon2 - $lon1 ) + sin( $lat1 ) * sin($lat2) ) );
//$dist = number_format($dist, 2, '.', '');
return $dist;
}

function distancia2($lat1, $lon1, $lat2, $lon2) {

$lat1 = deg2rad($lat1);
$lat2 = deg2rad($lat2);
$lon1 = deg2rad($lon1);
$lon2 = deg2rad($lon2);

$latD = $lat2 - $lat1;
$lonD = $lon2 - $lon1;

$dist = 2 * asin(sqrt(pow(sin($latD / 2), 2) +
cos($lat1) * cos($lat2) * pow(sin($lonD / 2), 2)));
$dist = $dist * 6371;
//$dist = number_format($dist, 2, '.', '');
return $dist;
}


function calcDistanciaSimples($lat_inicial, $long_inicial, $lat_final, $long_final)
{
    $deltaLat = $lat_inicial - $lat_final;
    $deltaLng = $long_inicial- $long_final;
    
    $dist = sqrt((($deltaLat*60*1.852)*($deltaLat*60*1.852)) + (($deltaLng*60*1.852)*($deltaLng*60*1.852)));

    return $dist;
}

function calcDistanciaSimples2($lat_inicial, $long_inicial, $lat_final, $long_final)
{
   $x = ($long_final - $long_inicial) * cos((($lat_inicial + $lat_final)/2) * M_PI / 180);
   $y = $lat_final - $lat_inicial;
   $dist = sqrt($x*$x + $y*$y) * 60*1.852;
   
   return $dist;
}


/*
echo calcDistancia(-23.486686, -46.568407, -23.493982, -46.575205);
echo "\n";
echo distancia1(-23.486686, -46.568407, -23.493982, -46.575205);
echo "\n";
echo distancia2(-23.486686, -46.568407, -23.493982, -46.575205);
echo "\n";
echo calcDistanciaSimples(-23.486686, -46.568407, -23.493982, -46.575205);
*/
echo calcDistancia(-23.489245, -46.573002, -23.491921, -46.574577);
echo "\n";
echo distancia1(-23.489245, -46.573002, -23.491921, -46.574577);
echo "\n";
echo distancia2(-23.489245, -46.573002, -23.491921, -46.574577);
echo "\n";
echo calcDistanciaSimples(-23.489245, -46.573002, -23.491921, -46.574577);
echo "\n";
echo calcDistanciaSimples2(-23.489245, -46.573002, -23.491921, -46.574577);














?>