<?php require ('constants.php');
$con = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbName);
$email = mysqli_escape_string($con, $_POST['email']);
$password = mysqli_escape_string($con, $_POST['password']);
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
    $login_query = "SELECT * FROM drivers WHERE (email='$email' or phone='$email') and password='$password'";
    $result = mysqli_query($con, $login_query);
    if (mysqli_num_rows($result) > 0)
    {
        $r = mysqli_fetch_assoc($result);
        if ($r['is_verified'] == 1)
        {
            $response = array(
                "status" => "1",
                "data" => array(
                    "id" => $r['driver_id'],
                    "name" => $r['driver_name'],
                    "phone" => $r['phone'],
                    "email" => $r['email'],
                    "cab_no" => $r['cab_no']
                )
            );
            die(json_encode($response));
        }
        else
        {
            $response = array(
                "status" => "0",
                "data" => "Please verify your email"
            );
            die(json_encode($response));
        }
    }
    else
    {
        $response = array(
            "status" => "0",
            "data" => "Wrong Email or Password"
        );
        die(json_encode($response));
    }
} ?>
