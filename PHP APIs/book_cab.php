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
$src_lat = $_POST['src_lat'];
$src_lng = $_POST['src_lng'];
$dest_lat = $_POST['dest_lat'];
$dest_lng = $_POST['dest_lng'];
$user_id = $_POST['user_id'];
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
if (!$con)
{
    $response = array(
        "status" => "0",
        "data" => "Error Connecting to Database!"
    );
    die(json_encode($response));
}
$getUserNamePhoneQuery = "SELECT user_name, phone FROM users WHERE user_id='$user_id'";
$userNamePhone = mysqli_query($con, $getUserNamePhoneQuery);
$customer = mysqli_fetch_assoc($userNamePhone);
$customer_name = $customer['user_name'];
$customer_phone = $customer['phone'];
$getNearbyCabs = "SELECT driver_id, one_signal_id, driver_name, phone, cab_id, cabs.cab_no cab_no, cab_lat, cab_lng, cab_bearing FROM cabs, drivers WHERE on_trip=0 and on_duty=1 and drivers.cab_no=cabs.cab_no ORDER BY ((cab_lat-$src_lat)*(cab_lat-$src_lat) + (cab_lng-$src_lng)*(cab_lng-$src_lng)) LIMIT 1";
$result = mysqli_query($con, $getNearbyCabs);
if ($result)
{
    $r = mysqli_fetch_assoc($result);
    $driver_id = $r['driver_id'];
    $cab_id = $r['cab_id'];
    $one_signal_id = $r['one_signal_id'];
    $otp = rand(1000, 9999);
    $fare = sqrt(($dest_lat - $src_lat) * ($dest_lat - $src_lat) + ($dest_lng - $src_lng) * ($dest_lng - $src_lng)) * 111 * 20;
    $fare = (int)$fare;
    $booked_at = time();
    $book_ride_query = "INSERT INTO rides (driver_id, user_id, src_lat, src_lng, dest_lat, dest_lng, fare, otp, booked_at) values ('$driver_id', '$user_id', '$src_lat', '$src_lng', '$dest_lat', '$dest_lng', '$fare', '$otp', '$booked_at')";
    $change_on_trip_query = "UPDATE cabs SET on_trip=1 WHERE cab_id='$cab_id'";
    $book_ride_result = mysqli_query($con, $book_ride_query);
    $change_on_trip_result = mysqli_query($con, $change_on_trip_query);
    if ($book_ride_result && $change_on_trip_result)
    {
        $get_ride_id = "SELECT ride_id FROM rides WHERE driver_id='$driver_id' and user_id='$user_id' and booked_at='$booked_at'";
        $get_ride_id_result = mysqli_query($con, $get_ride_id);
        if ($get_ride_id_result)
        {
            $r2 = mysqli_fetch_assoc($get_ride_id_result);
            $ride_id = $r2['ride_id'];
            function sendMessage()
            {
                $heading = array(
                    "en" => 'Near Cabs Booking'
                );
                $content = array(
                    "en" => 'You have got a new ride booking.'
                );
                $fields = array(
                    'app_id' => "cac81f77-8e1f-4589-a434-87a83f186f65",
                    'include_player_ids' => array(
                        $GLOBALS['one_signal_id']
                    ) ,
                    'data' => array(
                        "customerName" => $GLOBALS['customer_name'],
                        "customerPhone" => $GLOBALS['customer_phone'],
                        "src_lat" => $GLOBALS['src_lat'],
                        "src_lng" => $GLOBALS['src_lng'],
                        "dest_lat" => $GLOBALS['dest_lat'],
                        "dest_lng" => $GLOBALS['dest_lng'],
                        "fare" => $GLOBALS['fare'],
                        "otp" => $GLOBALS['otp'],
                        "ride_id" => $GLOBALS['ride_id'],
                        "cab_id" => $GLOBALS['cab_id']
                    ) ,
                    'small_icon' => 'https://lh3.googleusercontent.com/TzYz9McRVy8fD_WnpKiCK5anw20So6eyPR9ti-LwTd_QIer8BpAg8cMkRoO4sUv2xCDw=w300-rw',
                    'large_icon' => 'https://lh3.googleusercontent.com/TzYz9McRVy8fD_WnpKiCK5anw20So6eyPR9ti-LwTd_QIer8BpAg8cMkRoO4sUv2xCDw',
                    'headings' => $heading,
                    'contents' => $content
                );
                $fields = json_encode($fields);
                $ch = curl_init();
                curl_setopt($ch, CURLOPT_URL, "https://onesignal.com/api/v1/notifications");
                curl_setopt($ch, CURLOPT_HTTPHEADER, array(
                    'Content-Type: application/json; charset=utf-8',
                    'Authorization: Basic OGIzOGE3MjgtYTQ4Ni00ODI1LWI5NjktMjRkZWM0ZjFhMjZl'
                ));
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                curl_setopt($ch, CURLOPT_HEADER, false);
                curl_setopt($ch, CURLOPT_POST, true);
                curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
                curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
                $response = curl_exec($ch);
                curl_close($ch);
                return $response;
            }
            $response = sendMessage();
            $response = array(
                "status" => "1",
                "data" => array(
                    "ride_id" => $ride_id,
                    "cab_lat" => $r['cab_lat'],
                    "cab_lng" => $r['cab_lng'],
                    "cab_id" => $r['cab_id'],
                    "driver_name" => $r['driver_name'],
                    "driver_phone" => $r['phone'],
                    "cab_no" => $r['cab_no'],
                    "fare" => $fare,
                    "otp" => $otp
                )
            );
            die(json_encode($response));
        }
        else
        {
            $response = array(
                "status" => "0",
                "data" => "Unable to book ride"
            );
            die(json_encode($response));
        }
    }
    else
    {
        $response = array(
            "status" => "0",
            "data" => "Unable to book ride"
        );
        die(json_encode($response));
    }
}
else
{
    $response = array(
        "status" => "0",
        "data" => "Unable to book ride"
    );
    die(json_encode($response));
} ?>
