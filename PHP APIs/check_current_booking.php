<?php require ('constants.php');
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$driver_id = $_POST['driver_id'];
$getCurrentRide = "SELECT ride_id, cab_id, src_lat, src_lng, dest_lat, dest_lng, fare, otp, user_name, users.phone customer_phone from rides, users, cabs, drivers WHERE rides.driver_id='$driver_id' and rides.user_id=users.user_id and cabs.cab_no=drivers.cab_no and rides.status=1 LIMIT 1";
$result = mysqli_query($con, $getCurrentRide);
if (mysqli_num_rows($result) > 0)
{
    $r = mysqli_fetch_assoc($result);
    $response = array(
        "status" => "1",
        "data" => array(
            "customerName" => $r['user_name'],
            "customerPhone" => $r['customer_phone'],
            "src_lat" => $r['src_lat'],
            "src_lng" => $r['src_lng'],
            "dest_lat" => $r['dest_lat'],
            "dest_lng" => $r['dest_lng'],
            "fare" => $r['fare'],
            "otp" => $r['otp'],
            "ride_id" => $r['ride_id'],
            "cab_id" => $r['cab_id']
        )
    );
    die(json_encode($response));
}
else
{
    $response = array(
        "status" => "0",
        "data" => "No active booking"
    );
    die(json_encode($response));
} ?>
