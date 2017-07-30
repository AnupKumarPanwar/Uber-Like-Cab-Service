<?php
	require('constants.php');

	$ride_id=$_POST['ride_id'];

	$con=mysqli_connect($dbServer,$dbUsername,$dbPassword,$dbName);

	if (!$con) {
		$response=array(
			"status"=>"0",
			"data"=>"Error Connecting to Database!"
			);
		die(json_encode($response));
	}

	// $getRideStatus="SELECT status FROM rides WHERE ride_id='$ride_id' and (status=1 or status=2)";

	// $result=mysqli_query($con, $getRideStatus);
	// if (mysqli_num_rows($result)==0) {
		$startRideQuery="UPDATE rides SET status=1 WHERE ride_id='$ride_id'";
		
		$result2=mysqli_query($con, $startRideQuery);

		if ($result2) {
				$response=array(
					"status"=>"1",
					"data"=>"Ride started!"
					);
				die(json_encode($response));	
		}
		else
		{
			$response=array(
				"status"=>"0",
				"data"=>"Unable to start ride"
				);
			die(json_encode($response));	
		}

	// }
	// else
	// {
	// 	$response=array(
	// 		"status"=>"0",
	// 		"data"=>"Unable to cancel ride"
	// 		);
	// 	die(json_encode($response));	
	// }


	


?>