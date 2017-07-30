<?php
	$server='localhost';
	$username='root';
	$password='';
	$dbname='spic';

	$con=mysqli_connect($server, $username, $password, $dbname);

	$token=$_POST['token'];

	if ($token=='Spic@Anup') {
		
	if (!$con) {
		die("Can not connect to the DB");
	}
	else
	{
		$login_query="SELECT * FROM users";
		$result=mysqli_query($con, $login_query);
		if ($result) {
			$r=array();
			while ($row=mysqli_fetch_assoc($result)) {
				$r[]=$row;
			}
			$response=array(
				"status"=>"1",
				"data"=>$r
					);
			echo(json_encode($response));
			}

		else{
			$response=array(
				"status"=>"0",
				"data"=>"Some error occured!"
				);
			echo(json_encode($response));
		}
	}
}
else
{
	$response=array(
		"status"=>"0",
		"data"=>"Some error occured!"
		);
	echo(json_encode($response));
}
?>