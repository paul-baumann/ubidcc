Content of user directory:<br><br>

<?php
$deviceUUID = '-';
$ending = '_Locator';

echo('<table border="1"><tr><th>Path</th><th>From</th><th>To</th><th>Name</th></tr>');

foreach(glob('./user/*.*') as $filename){

	if (strpos($filename,$deviceUUID) !== false and strpos($filename,$ending) !== false) {
			$dbdata = new SQLite3($filename);
			$querydata="SELECT datetime(timestampfrom/1000+7200, 'unixepoch') as datefrom, datetime(timestampto/1000+7200, 'unixepoch') as dateto, name FROM diary order by datefrom desc Limit 10";
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
			
			$filename = "";
			while ($rowdata = $resultsdata->fetchArray()) {
				$datefrom = $rowdata['datefrom'];
				$dateto = $rowdata['dateto'];
				$name = $rowdata['name'];
				echo('<tr><td>' .$filename .'</td><td>' .$rowdata['datefrom'] .'</td><td>' .$rowdata['dateto'] .'</td><td>' .$rowdata['name'] .'</td></tr>');
					
			}
			
			$dbdata->close();
	}
	
	else{
		echo('<tr><td>' .$filename . '</td><td>Wrong format</td></tr>');
	}
	
}

echo('</table>');
?>