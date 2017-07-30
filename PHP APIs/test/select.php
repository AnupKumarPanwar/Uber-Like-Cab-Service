<?php
$user='root';
$pass='';
$database='my_project';
$server='127.0.0.1';
$conn=mysqli_connect($server,$user,$pass,$database) or die('cannot be connected to the server');
$myquery=mysqli_query($conn,"select * from userReg");
while($row=mysqli_fetch_array($myquery))
{
	print $row['uid']." ".$row['uname']." ".$row['phone']." ".$row['email']."<br>";
}
?>