<?php require ('constants.php');
$cab_no = $_POST['cab_id'];
$cab_lat = $_POST['lat'];
$cab_lng = $_POST['lng'];
$cab_bearing = $_POST['bearing'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$updateCabLocationQuery = "UPDATE cabs SET cab_lat='$cab_lat', cab_lng='$cab_lng', cab_bearing='$cab_bearing' WHERE cab_no='$cab_no'";
$result2 = mysqli_query($con, $updateCabLocationQuery);
if ($result2)
{
    $response = array(
        "status" => "1",
        "data" => "Location updated"
    );
    die(json_encode($response));
}
else
{
    $response = array(
        "status" => "0",
        "data" => "Unable to update location"
    );
    die(json_encode($response));
} ?>
