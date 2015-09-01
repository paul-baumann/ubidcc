Content of user directory:<br><br>

<?php
$deviceUUID = '-';
$ending = '_Locator';

echo('<table border="1"><tr><th>Path</th><th>Name</th><th>Latitude</th><th>Longitude</th></tr>');

foreach(glob('./user/*.*') as $filename){

	if (strpos($filename,$deviceUUID) !== false and strpos($filename,$ending) !== false) {
			$dbdata = new SQLite3($filename);
			$querydata="SELECT datetime(timestamp/1000+7200, 'unixepoch') as date, longitude, latitude, name FROM contextplaces order by date desc Limit 10";
			$resultsdata = $dbdata->query($querydata);
			
			$db = new SQLite3('user/dbuser.db');
			$query="Select name, deviceUUID from user";
			$results2 = $db-> query($query);
			$user = "";
			while ($rowdata2 = $results2->fetchArray()) {
				if(strpos($filename, $rowdata2['deviceUUID'])){
					$user = $rowdata2['name'];
				}
			}
			$db->close();
			echo('<b>');
			echo('<tr><td><b>' .$filename .'</b></td><td><b>' .date ("F d Y H:i:s", filemtime($filename)) .'</b></td><td><b>' .$user .'</b></td><td></td></tr>');
			echo('</b>');	
			
			$filenameRaw = "https://maps.google.de/maps?q=";
			$filename = "";
			while ($rowdata = $resultsdata->fetchArray()) {
				$date = $rowdata['date'];
				$name = $rowdata['name'];
				$longitude = $rowdata['longitude'];
				$latitude = $rowdata['latitude'];
				$filename = $filenameRaw .$latitude ."," .$longitude;
				echo('<tr><td> <a href="' .$filename .'"target="_blank">Map</a></td><td>' .$name .'</td><td>' .$latitude .'</td><td>' .$longitude .'</td></tr>');
					
			}
			
			$dbdata->close();
	}
	
	else{
		echo('<tr><td>' .$filename . '</td><td>Wrong format</td></tr>');
	}
	
}

echo('</table>');
?>