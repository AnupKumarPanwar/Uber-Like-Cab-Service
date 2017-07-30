<?php
					
$one_signal_id='5becedef-1533-4cde-b3fe-449f305dff53';

					function sendMessage(){

						$heading=array(
							"en" => 'Near Cabs Booking'
							);

						$content = array(
							"en" => 'You have got a new ride booking.'
							);
						

								$fields = array(
									'app_id' => "cac81f77-8e1f-4589-a434-87a83f186f65",
									'include_player_ids' => array($GLOBALS['one_signal_id']),
						      'small_icon' => 'https://lh3.googleusercontent.com/TzYz9McRVy8fD_WnpKiCK5anw20So6eyPR9ti-LwTd_QIer8BpAg8cMkRoO4sUv2xCDw=w300-rw',
						      'large_icon' => 'https://lh3.googleusercontent.com/TzYz9McRVy8fD_WnpKiCK5anw20So6eyPR9ti-LwTd_QIer8BpAg8cMkRoO4sUv2xCDw',

						      		'headings' => $heading,
									'contents' => $content
								);
						
						$fields = json_encode($fields);
				    // print("\nJSON sent:\n");
				    // print($fields);
						
						$ch = curl_init();
						curl_setopt($ch, CURLOPT_URL, "https://onesignal.com/api/v1/notifications");
						curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json; charset=utf-8',
																   'Authorization: Basic OGIzOGE3MjgtYTQ4Ni00ODI1LWI5NjktMjRkZWM0ZjFhMjZl'));
						curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
						curl_setopt($ch, CURLOPT_HEADER, FALSE);
						curl_setopt($ch, CURLOPT_POST, TRUE);
						curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
						curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

						$response = curl_exec($ch);
						curl_close($ch);
						
						return $response;
					}


					$response = sendMessage();


echo $response;

?>