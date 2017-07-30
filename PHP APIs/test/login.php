<?php
	$server='localhost';
	$username='root';
	$password='';
	$dbname='spic';

	$con=mysqli_connect($server, $username, $password, $dbname);

	$email=$_POST['email'];
	$password=$_POST['password'];

	if (!$con) {
		die("Can not connect to the DB");
	}
	else
	{
		$login_query="SELECT * FROM users WHERE email='$email' and password='$password'";
		$result=mysqli_query($con, $login_query);
		if (mysqli_num_rows($result)>0) {
			$r=mysqli_fetch_assoc($result);
			$response=array(
				"status"=>"1",
				"data"=>array(
					"id"=>$r['id'],
					"name"=>$r['name'],
					"phone"=>$r['phone'],
					"email"=>$r['email']
					)
				);
			echo(json_encode($response));
		}
		else{
			$response=array(
				"status"=>"0",
				"data"=>"Wrong Email/Password"
				);
			echo(json_encode($response));
		}
	}
?>