Statistics of upload per device of the last 7 days:<br><br>

<?php

	$db = new SQLite3('user/dbuser.db');
	echo('<table border="1"><tr><th>Date</th><th>User</th><th>DeviceUUID</th><th>Count</th></tr>');
	
	for($count = 0; $count <7; $count++)
	{
		$day = date("d.m.y", mktime(0, 0, 0, date("m"), date("d")-$count, date("Y")));   
		// $daystart = date("d.m.y H:i:s" , mktime(0, 0, 0, date("m"), date("d")-$count, date("Y")));
		// $dayend = date("d.m.y H:i:s" , mktime(23, 59, 59, date("m"), date("d")-$count, date("Y")));
		// echo($day .' goes from ' .$daystart .' to ' .$dayend .'<br>');
		$timefrom = mktime(0, 0, 0, date("m"), date("d")-$count, date("Y"));
		$timeto = mktime(23, 59, 59, date("m"), date("d")-$count, date("Y"));
		$query="Select deviceUUID, COUNT(deviceUUID) as devicecount from log where timestamp >= $timefrom and timestamp <= $timeto group by deviceUUID";
		$results = $db-> query($query);
		
		
		while ($rowdata = $results->fetchArray()) {
			$deviceUUID = $rowdata['deviceUUID'];
			
			$query="Select name from user where deviceUUID = '" .$deviceUUID ."'";
			$results2 = $db-> query($query);
			$user = "";
			while ($rowdata2 = $results2->fetchArray()) {
				$user = $rowdata2['name'];
			}

			$devicecount = $rowdata['devicecount'];
			echo('<tr><td>' .$day . '</td><td>' .$user . '</td><td>' .$rowdata['deviceUUID'] . '</td><td>' .$rowdata['devicecount'] .'</td></tr>');
		}
		
	}

	echo('</table>');
			
?>