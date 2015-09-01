<?php

$db = new SQLite3('user/registerForData.db');
$query="CREATE TABLE IF NOT EXISTS registerForDataset (id INTEGER primary key, name TEXT, email TEXT, affiliation TEXT)";
$db-> exec($query);

//$timestamp="'" .$_POST["timestamp"] ."'";
$affiliation="'" .$_POST["affiliation"] ."'";
$email ="'" .$_POST["email"] ."'";
$name="'" .$_POST["name"] ."'";
$query="INSERT into registerForDataset (affiliation, email, name) VALUES ($affiliation, $email, $name)";
$count = $db-> exec($query);

echo("A new user registration has been added");

?>