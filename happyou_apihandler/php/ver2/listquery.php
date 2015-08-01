<?php
/**
 * This is a sample to send query to happyou API and receive JSON result.
 *  @Author shogo okamoto(info@zaisoft.com)
*/

require_once('ApiHandlerv2.php');

//you need to application id to access
$h = new ApiHandlerv2('YOUR APP ID HERE');

echo "send query to server..\n";

//(1) send query and receive result as $article object via API 
$query = array('pubdatelast'=>'1000');
$resultlist = $h->listArticles($query);

// print result as article array
foreach ($resultlist['articles'] as $article) {
  echo $article['Article']['publishedDate']." : ".$article['Article']['orgname']." : ".$article['Article']['title']."\n";
  echo "---\n";
}

//(2) send query and receive result as json text.
$json = $h->sendQuery($query);

//print result as json
echo $json."\n";

?>