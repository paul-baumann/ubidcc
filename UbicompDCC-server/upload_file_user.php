<?php
/*
$allowedExts = array("gif", "jpeg", "jpg", "png");
$a = explode(".", $_FILES["uploadedfile"]["name"]);
$extension = end($a);
echo "Filetype: " . $_FILES["uploadedfile"]["type"];

if ((($_FILES["uploadedfile"]["type"] == "image/gif")
|| ($_FILES["uploadedfile"]["type"] == "image/jpeg")
|| ($_FILES["uploadedfile"]["type"] == "image/jpg")
|| ($_FILES["uploadedfile"]["type"] == "image/pjpeg")
|| ($_FILES["uploadedfile"]["type"] == "image/x-png")
|| ($_FILES["uploadedfile"]["type"] == "image/png")
|| ($_FILES["uploadedfile"]["type"] == "image/png"))
&& ($_FILES["uploadedfile"]["size"] < 1000000)
&& in_array($extension, $allowedExts))
  {
  
*/

if ($_FILES["uploadedfile"]["error"] > 0){
	echo "Return Code: " . $_FILES["uploadedfile"]["error"] . "<br>";
}
else { 
	echo "Upload: " . $_FILES["uploadedfile"]["name"] . "<br>";
	echo "Type: " . $_FILES["uploadedfile"]["type"] . "<br>";
	echo "Size: " . ($_FILES["uploadedfile"]["size"] / 1024) . " kB<br>";
	echo "Temp uploadedfile: " . $_FILES["uploadedfile"]["tmp_name"] . "<br>";

	move_uploaded_file($_FILES["uploadedfile"]["tmp_name"],"user/" . $_FILES["uploadedfile"]["name"]);
	echo "Stored in: " . "user/" . $_FILES["uploadedfile"]["name"] .'<br>';
	  
	//Save log file
	$db = new SQLite3('user/dbuser.db');
	$query="CREATE TABLE IF NOT EXISTS log (timestamp FLOAT, timedevice FLOAT, deviceUUID TEXT, type TEXT)";
	$db-> exec($query);
	$filename = explode('_',$_FILES["uploadedfile"]["name"]);
	$time = time();
	$deviceUUID =  "'" .$filename[0] ."'";
	if(sizeof($filename) == 3){
		$type =  "'" .$filename[2] ."'";
		$timedevice = $filename[1];
	}
	elseif(sizeof($filename) == 2){ 
		$type =  "'" .$filename[1] ."'";
		$timedevice = $time;
	}
	elseif(sizeof($filename) == 1){
		$type =  "'Unkown'";
		$timedevice = $time;
	}
	else{
		$type =  "'Unkown'";
		$timedevice = $time;
	}

	$query="INSERT into log (timestamp, timedevice, deviceUUID, type) VALUES ($time, $timedevice, $deviceUUID, $type)";
	$db-> exec($query);
	$db-> close();

	}
?> 