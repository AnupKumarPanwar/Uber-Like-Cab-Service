<?php require ('constants.php');
$cab_id = $_POST['cab_id'];
$ride_id = $_POST['ride_id'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$getRideStatus = "SELECT status FROM rides WHERE ride_id='$ride_id'";
$result = mysqli_query($con, $getRideStatus);
if ($result)
{
    $r = mysqli_fetch_assoc($result);
    if ($r['status'] == 0)
    {
        $getCurrentPosition = "SELECT cab_lat, cab_lng, cab_bearing FROM cabs WHERE cab_id='$cab_id'";
        $result2 = mysqli_query($con, $getCurrentPosition);
        if ($result2)
        {
            $r2 = mysqli_fetch_assoc($result2);
            $response = array(
                "status" => "1",
                "data" => array(
                    "cab_lat" => $r2['cab_lat'],
                    "cab_lng" => $r2['cab_lng'],
                    "cab_bearing" => $r2['cab_bearing']
                )
            );
            die(json_encode($response));
        }
    }
    else
    {
        $response = array(
            "status" => "2",
            "data" => "Trip started"
        );
        die(json_encode($response));
    }
}
$response = array(
    "status" => "0",
    "data" => "Unable to fetch location"
);
die(json_encode($response)); ?>
