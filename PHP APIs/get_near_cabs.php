<?php require ('constants.php');
$user_lat = $_POST['user_lat'];
$user_lng = $_POST['user_lng'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$getNearbyCabs = "SELECT driver_name, phone, cab_id, cabs.cab_no cab_no, cab_lat, cab_lng FROM cabs, drivers WHERE on_trip=0 and on_duty=1 and drivers.cab_no=cabs.cab_no ORDER BY ((cab_lat-$user_lat)*(cab_lat-$user_lat) + (cab_lng-$user_lng)*(cab_lng-$user_lng)) LIMIT 1";
$result = mysqli_query($con, $getNearbyCabs);
if ($result)
{
    $r = mysqli_fetch_assoc($result);
    $response = array(
        "status" => "1",
        "data" => array(
            "cab_lat" => $r['cab_lat'],
            "cab_lng" => $r['cab_lng'],
            "cab_id" => $r['cab_id'],
            "driver_name" => $r['driver_name'],
            "driver_phone" => $r['phone'],
            "cab_no" => $r['cab_no']
        )
    );
    echo (json_encode($response));
} ?>
