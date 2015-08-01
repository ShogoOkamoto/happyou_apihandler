/**
This script uses CakePHP model and controller from outsite of cake php world.

Send query and retrieve articles of happyou.info.
*/

<?php

  ob_start();//suppress messages.
;
// load cakePHP classes.
require_once ('../webappv1/index.php');

include(WWW_ROOT.'/../commons/TagQueryParser/TagQueryParser.php');
include(WWW_ROOT.'/../commons/SolrQuery/SolrQuery.php');
require_once(WWW_ROOT.'/../commons/commons.php');
;
$articleModel = ClassRegistry::init('Article');

//build queries
$queries = array(
		 'pubdatelast'=>'100000'
		 );

//execute query 
$offset=0;
$maxCount=20;
$conditions = $articleModel->buildQuery4api($queries);
$mode='AND';

  $params = array('limit'=>$maxCount, 'offset'=>$offset, 'conditions'=>array($mode=>$conditions), 'order'=> array('publishedDate'=>'desc'));
      $articles = $articleModel->find('all', $params);

$articles = $articleModel->find('all', $params);

ob_end_clean();//end of suppressing

//echo "---\n";
echo "count:".count($articles);
foreach($articles as $a) {
  echo $a['Article']['orgname'].":".$a['Article']['title']."\n";
  echo "---\n";
}
echo "\nend\n";

?>
