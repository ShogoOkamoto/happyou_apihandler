<?php 

  /**
   *  ApiHandler handles login and list articles 
   *  procedure of web API in happyou.info.
   *  @Author shogo.okamoto(info@zaisoft.com)
  */
class ApiHandler {

  const PATH2LOGIN='https://zaisoft.sakura.ne.jp/happyou/webappv1/authake/user/login';
  const PATH2API_TOPPAGE="https://zaisoft.sakura.ne.jp/happyou/webappv1/index.php";
  const PATH2API_JSON="https://zaisoft.sakura.ne.jp/happyou/webappv1/api/index.json";
  const PATH2API_RSS="https://zaisoft.sakura.ne.jp/happyou/webappv1/api/index.rss";

  public $email;
  public $pass;

  /**
   * constructor
   @param emailaddress
   @param password
  */
  function ApiHandler($emailaddress, $password) {
    $this->email = $emailaddress;
    $this->pass = $password;
  }

  /**
   * get articles by query
   * @param $query array array of queries(key1=>val1, key2=>val2..)
   * @param $cookie cookie string retrieved during login process.
   * @return array of Articles object
   */
  public function listArticles($queryarray, $cookie) {

    $json = $this->sendQuery($queryarray, $cookie);
    $articles = json_decode($json, true);
    
    return $articles;

  }

  /**
     send query to happyou API
     @param $queryarray key1=val1&key2=val2..
     @param $cookie which was retreived in login.
     @return json text
  */
  public function sendQuery($queryarray, $cookie) {

    $cookie = explode(';',$cookie)[0];
    $querydata = http_build_query($queryarray);

    $headers = array('Content-Type: application/x-www-form-urlencoded',
		     'Content-Length: '.strlen($querydata),
		     'Cookie: '.$cookie);

    $opts = array(
		  'http'=>array(
				'method'=>'POST',
				'header'=>implode("\r\n", $headers),
				'content'=>$querydata)
		  );
    $context = stream_context_create($opts);
    $contents = file_get_contents(self::PATH2API_JSON, false, $context);

    return $contents;
  }


  /** login happyouapi without form
   * @return retrieved cookie during login process.
   */
  public function login() {
  
    $cookie = $this->loadCookieFromLoginForm();
    $cookie = explode(';', $cookie)[0];

    $formdata = array('_method'=>'POST', 'data[User][login]'=>$this->email, 'data[User][password]'=>$this->pass);

    $headers = array('Content-Type: application/x-www-form-urlencoded',
		     'Content-Length: '.strlen(http_build_query($formdata)),
		     'Cookie: '.$cookie);
    $opts = array(
		  'http'=>array(
				'method'=>'POST',
				'header'=>implode("\r\n", $headers),
				'content'=>http_build_query($formdata)));

    $context = stream_context_create($opts);

    $contents = file_get_contents(self::PATH2LOGIN, false, $context);

    //inspect response header to login successed.
    foreach($http_response_header as $line) {
      $pos =  strpos($line, 'Location:');
      if ($pos===0) {
	$val = trim(substr($line, 9));
	if ($val==self::PATH2API_TOPPAGE) {
	  return $cookie;// success to login!
	}
      }
    }

    // login failed.
    return false;
  }


  /**
     load cookie from response header in form dialog
  */
  function loadCookieFromLoginForm() {

    //First, download form contents
    $opts = array(
		  'http'=>array(
				'method'=>'GET'));
    $context = stream_context_create($opts);
    $headers = get_headers(self::PATH2LOGIN);

    foreach($headers as $line) {
      $pos = strpos($line, ':');
      if (substr($line, 0,$pos)=='Set-Cookie') {
	$val = trim(substr($line, $pos+1));
	return $val;
      }
    }

    return "";
  }
  }

?>