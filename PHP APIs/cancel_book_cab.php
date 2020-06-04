<?php require ('constants.php');
$ride_id = $_POST['ride_id'];
$cab_id = $_POST['cab_id'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$getRideStatus = "SELECT status FROM rides WHERE ride_id='$ride_id' and (status=1 or status=2)";
$result = mysqli_query($con, $getRideStatus);
if (mysqli_num_rows($result) == 0)
{
    $cancelCabQuery = "UPDATE rides SET status=4 WHERE ride_id='$ride_id'";
    $cancelCabQuery2 = "UPDATE cabs SET on_trip=0 WHERE cab_id='$cab_id'";
    $result2 = mysqli_query($con, $cancelCabQuery);
    $result3 = mysqli_query($con, $cancelCabQuery2);
    if ($result2 && $result3)
    {
        $response = array(
            "status" => "1",
            "data" => "Booking Cancelled"
        );
        die(json_encode($response));
    }
    else
    {
        $response = array(
            "status" => "0",
            "data" => "Unable to cancel ride"
        );
        die(json_encode($response));
    }
}
else
{
    $response = array(
        "status" => "0",
        "data" => "Unable to cancel ride"
    );
    die(json_encode($response));
} ?>
