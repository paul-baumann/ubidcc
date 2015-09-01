<?php

$db = new SQLite3('user/dbuser.db');
$query="CREATE TABLE IF NOT EXISTS questionnaire (id INTEGER primary key, age TEXT, gender TEXT, role TEXT, academia TEXT, deviceUID TEXT, macAddress TEXT, bluetoothID TEXT)";
$db-> exec($query);

//$timestamp="'" .$_POST["timestamp"] ."'";
$age="'" .$_POST["age"] ."'";
$gender="'" .$_POST["gender"] ."'";
$role="'" .$_POST["role"] ."'";
$academia="'" .$_POST["academiaorindustry"] ."'";
$deviceUID="'" .$_POST["deviceUID"] ."'";
$macAddress="'" .$_POST["macAddress"] ."'";
$bluetoothID="'" .$_POST["bluetoothAddress"] ."'";
$query="INSERT into questionnaire (age, gender, role, academia, deviceUID, macAddress, bluetoothID) VALUES ($age, $gender, $role, $academia, $deviceUID, $macAddress, $bluetoothID)";
$db-> exec($query);

echo("A new questionnaire has been added: ");
echo( 'Age: ' . $age . ' Gender: ' . $gender . ' Role: ' . $role . ' Academia / Industry: ' . $academia . ' DeviceUID: ' . $deviceUID);

?>