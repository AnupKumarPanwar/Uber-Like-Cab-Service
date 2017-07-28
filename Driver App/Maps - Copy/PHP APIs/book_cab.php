<?php
	require('constants.php');

	$src_lat=$_POST['src_lat'];
	$src_lng=$_POST['src_lng'];
	$dest_lat=$_POST['dest_lat'];
	$dest_lng=$_POST['dest_lng'];
	$user_id=$_POST['user_id'];

	$con=mysqli_connect($dbServer,$dbUsername,$dbPassword,$dbName);

	if (!$con) {
		$response=array(
			"status"=>"0",
			"data"=>"Error Connecting to Database!"
			);
		die(json_encode($response));
	}

	$getNearbyCabs="SELECT driver_id, driver_name, phone, cab_id, cabs.cab_no cab_no, cab_lat, cab_lng, cab_bearing FROM cabs, drivers WHERE on_trip=0 and on_duty=1 and drivers.cab_no=cabs.cab_no ORDER BY ((cab_lat-$src_lat)*(cab_lat-$src_lat) + (cab_lng-$src_lng)*(cab_lng-$src_lng)) LIMIT 1";

	$result=mysqli_query($con, $getNearbyCabs);

	if ($result) {
		$r=mysqli_fetch_assoc($result);

		$driver_id=$r['driver_id'];
		$cab_id=$r['cab_id'];
		// echo($driver_id);

		$otp=rand(1000, 9999);
		$fare=sqrt(($dest_lat-$src_lat)*($dest_lat-$src_lat)+($dest_lng-$src_lng)*($dest_lng-$src_lng))*111*15;
		$fare=(int)$fare;
		// echo($fare);
		// echo('<br>'.$otp);

		$booked_at=time();
		// echo('<br>'.$booked_at);

		$book_ride_query="INSERT INTO rides (driver_id, user_id, src_lat, src_lng, dest_lat, dest_lng, fare, otp, booked_at) values ('$driver_id', '$user_id', '$src_lat', '$src_lng', '$dest_lat', '$dest_lng', '$fare', '$otp', '$booked_at')";

		$change_on_trip_query="UPDATE cabs SET on_trip=1 WHERE cab_id='$cab_id'";

		$book_ride_result=mysqli_query($con, $book_ride_query);
		$change_on_trip_result=mysqli_query($con, $change_on_trip_query);

		if ($book_ride_result && $change_on_trip_result) {
			$get_ride_id="SELECT ride_id FROM rides WHERE driver_id='$driver_id' and user_id='$user_id' and booked_at='$booked_at'";

			$get_ride_id_result=mysqli_query($con, $get_ride_id);

			if ($get_ride_id_result) {
				$r2=mysqli_fetch_assoc($get_ride_id_result);
				$ride_id=$r2['ride_id'];

				$response=array(
					"status"=>"1",
					"data"=>array(
							"ride_id"=>$ride_id,
							"cab_lat"=>$r['cab_lat'],
							"cab_lng"=>$r['cab_lng'],
							"cab_id"=>$r['cab_id'],
							"driver_name"=>$r['driver_name'],
							"driver_phone"=>$r['phone'],
							"cab_no"=>$r['cab_no'],
							"fare"=>$fare,
							"otp"=>$otp
							)
					);
				die(json_encode($response));

			}
			else
			{
				$response=array(
					"status"=>"0",
					"data"=>"Unable to book ride"
					);
				die(json_encode($response));
			}
		}
		else
		{
			$response=array(
				"status"=>"0",
				"data"=>"Unable to book ride"
				);
			die(json_encode($response));	
		}

		
	}
	else
	{
		$response=array(
			"status"=>"0",
			"data"=>"Unable to book ride"
			);
		die(json_encode($response));	
	}



?>