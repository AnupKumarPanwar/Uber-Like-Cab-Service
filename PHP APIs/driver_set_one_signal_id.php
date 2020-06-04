<?php require ('constants.php');
$driver_id = $_POST['driver_id'];
$one_signal_id = $_POST['one_signal_id'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$setOneSignalId = "UPDATE drivers SET one_signal_id='$one_signal_id' WHERE driver_id='$driver_id'";
$result2 = mysqli_query($con, $setOneSignalId);
if ($result2)
{
    $response = array(
        "status" => "1",
        "data" => "Driver registered for notificatons"
    );
    die(json_encode($response));
}
else
{
    $response = array(
        "status" => "0",
        "data" => "Unable to register driver for notifications"
    );
    die(json_encode($response));
} ?>
