<?php require ('constants.php');
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
$token = $_GET['token'];
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
else
{
    $verify_email_query = "UPDATE users SET is_verified=1 WHERE verification_code='$token'";
    $result = mysqli_query($con, $verify_email_query);
    if ($result)
    {
        die('Email Verified Successfully :) ');
    }
    else
    {
        die('Failed to verify email :( ');
    }
} ?>
