<?php
$userid=$_POST['userid'];
$uname=$_POST['username'];
$phone=$_POST['phone'];
$email=$_POST['email'];
$username='root';
$pwd='';
$server='127.0.0.1';
$database='my_project';
$flag['code']=0;
$conn=mysqli_connect($server,$username,$pwd,$database)or die("cannot connect with server");
if($row=mysqli_query($conn,"delete from userReg where uid=$userid"))
{
	$flag['code']=1;
}
print(json_encode($flag));
mysqli_close($conn);
?>
