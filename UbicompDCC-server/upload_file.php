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
	echo "DeviceUID: " . $_POST["deviceUID"] . "<br>";

	// if (file_exists("data/" . $_FILES["uploadedfile"]["name"]))
	// {
	// echo $_FILES["uploadedfile"]["name"] . " already exists. ";
	// }
	// else {
	if (!file_exists("data/" . $_POST["deviceUID"])) { // $_POST["deviceUID"])
	    mkdir("data/" . $_POST["deviceUID"], 0777, true);
	}
	// move_uploaded_file($_FILES["uploadedfile"]["tmp_name"],"data/" . $_POST["deviceUID"] . "/" . $_FILES["uploadedfile"]["name"]);
	move_uploaded_file($_FILES["uploadedfile"]["tmp_name"],"data/" . $_POST["deviceUID"] . "/" . $_FILES["uploadedfile"]["name"]);
	echo "Stored in: " . "data/" . $_POST["deviceUID"] . "/" . $_FILES["uploadedfile"]["name"] .'<br>';
	  
	//Save log file
	$db = new SQLite3('user/uploadLogs.db');
	$query="CREATE TABLE IF NOT EXISTS log (timestamp FLOAT, timedevice FLOAT, deviceUUID TEXT, type TEXT)";
	$db-> exec($query);
	$filename = explode('_',$_FILES["uploadedfile"]["name"]);
	$time = time();
	$deviceUUID =  "'" . $_POST["deviceUID"] ."'";
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
	  // $default = 'default.db';
	  // if(strcasecmp($type, $default) == 0){
		  // Process data into one db for the user
		  // $dbname = explode('_',$_FILES["uploadedfile"]["name"]);
		  // echo "Device UUID: " .$dbname[0] .'<br>';
		  // $dbto = new SQLite3('user/' .$dbname[0] .'.db');
		  // $query="CREATE TABLE IF NOT EXISTS data (timestamp FLOAT, name TEXT, value TEXT)";
		  // $dbto-> exec($query);
		  
		  // Copy data from one db to another
		  // $dbfrom = new SQLite3('data/' . $_FILES["uploadedfile"]["name"]);
		  // $queryfrom="SELECT timestamp, name, value FROM data";
		  // $queryfrom="SELECT timestamp, name FROM data";
		  // $results = $dbfrom->query($queryfrom);
		  // while ($row = $results->fetchArray()) {
			// $timestamp = $row['timestamp'];
			// $name = $row['name'];
			// $value = $row['value'];
			// $queryinsert = "INSERT into data (timestamp, name, value) VALUES ($timestamp,'$name','$value')";
			// $queryinsert = "INSERT into data (timestamp, name) VALUES ($timestamp,'$name')";
			// $dbto->query($queryinsert);
		  // }

		  // }
	}
// }
	
/*
  }
else
  // {
  echo "Invalid uploadedfile";
  // }
*/
?> 