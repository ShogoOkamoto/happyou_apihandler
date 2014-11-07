<?php

require_once('ApiHandler.php');

//login with id and password

$h = new ApiHandler('youremailaddress', 'yourpassword');

$cookie = $h->login();
if ($cookie==false) {
  echo "Failed to login. Check your email address and password are correct.\n";
  exit;
}

echo "send query to server..\n";

//send query via API 
$query = array('pubdatelast'=>'1000');
$resultlist = $h->listArticles($query, $cookie);

// print result;
foreach ($resultlist['articles'] as $article) {
  echo $article['Article']['publishedDate']." : ".$article['Article']['orgname']." : ".$article['Article']['title']."\n";
  echo "---\n";
}
?>