Content of data directory:<br><br>

<?php
$deviceUUID = '-';
$ending = '_default';

echo('<table border="1"><tr><th>Path</th><th>Number of sensor information</th></tr>');

foreach(glob('./data/*.*') as $filename){

	if (strpos($filename,$deviceUUID) !== false and strpos($filename,$ending) !== false) {
			$dbdata = new SQLite3($filename);
			$querydata="SELECT datetime(timestamp/1000+7200, 'unixepoch') as date, count(*) as number FROM data";
			echo('<tr><td>' .$filename .'</td><td>Test</td></tr>');
			$resultsdata = $dbdata->query($querydata);

			while ($rowdata = $resultsdata->fetchArray()) {
				$date = $rowdata['date'];
				$number = $rowdata['number'];
				echo('<tr><td>' .$filename .'</td><td>' .$rowdata['number'] .' ' .$rowdata['date'] .'</td></tr>');
			}
		
				
			$dbdata->close();
	}
	
	else{
		echo('<tr><td>' .$filename . '</td><td>Wrong format</td></tr>');
	}
	
}

echo('</table>');
?>