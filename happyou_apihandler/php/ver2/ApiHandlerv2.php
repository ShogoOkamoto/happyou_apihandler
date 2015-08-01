<?php 

  /**
   *  ApiHandler handles login and list articles 
   *  procedure of web API in happyou.info.
   *  @Author shogo okamoto(info@zaisoft.com)
   */
class ApiHandlerv2 {

  const PATH2API_JSON="https://happyou.info/webapp2/api2/index.json";
  const PATH2API_RSS="https://happyou.info/webapp2/api2/index.rss";

  public $appid;

  /**
   * constructor
   @param application id
  */
  function __construct($appid) {
    $this->appid = $appid;
  }

  /**
   * get articles by query
   * @param $queryarray array of queries(key1=>val1, key2=>val2..)
   * @return array of Articles object
   */
  public function listArticles($queryarray){

    $json = $this->sendQuery($queryarray);
    $articles = json_decode($json, true);
    
    return $articles;

  }

  /**
     send query to happyou API
     @param $queryarray array of queries(key1=>val1, key2=>val2..)
     @return json text
  */
  public function sendQuery($queryarray){

    //build query as str
    $querydata = http_build_query($queryarray);
    $querydata .="&app_id=".$this->appid;

    //build url to retrieve
    $url = self::PATH2API_JSON.'?'.$querydata;

    //do get
    $contents = file_get_contents($url, false);

    return $contents;
  }
}

?>