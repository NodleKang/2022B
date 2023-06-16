* 멀티스레드 비동기(asynchronous) 프로그램 구현

* HTTP 서버에 요청해서 Input Queue 입력받기

request)
GET http://127.0.0.1:8080/queueInfo

response 예)
{ "inputQueueCount":3,
  "inputQueueURIs":["http://127.0.0.1:8010/input", → Input Queue(0)의 URI
  "http://127.0.0.1:8011/input", → Input Queue(1)의 URI
  "http://127.0.0.1:8012/input"], → Input Queue(2)의 URI
  "outputQueueURI":"http://127.0.0.1:9010/output" }

* HTTP 서버로 Output Queue 출력하기

Worker 실행 결과(리턴된 문자열)가 Null이 아닐 때만 outputQueueURI에 POST로 전송

request)
POST http://127.0.0.1:9010/output
request 본문)
{"result":"<Worker 실행 결과>"}


* 콘솔 입출력 예
C:\>SP_TEST<엔터키>                 구현한 프로그램 실행 (Argument 없음)
1000 0 VIEW_AD1                    콘솔 입력
1500 0 VIEW_AD2                    콘솔 입력
2000 1 VIEW_AD3                    콘솔 입력
2500 1 VIEW_AD4                    콘솔 입력
4000 0 CLICK_AD1                   콘솔 입력
Worker(0):Matched AD1              콘솔 출력
5000 1 CLICK_AD4                   콘솔 입력
Worker(1):Matched AD4              콘솔 출력
6000 0 CLICK_AD2                   콘솔 입력
7000 1 CLICK_AD3                   콘솔 입력
...
<Timestamp> <Queue 번호> <Value>


 콘솔 입력              | 콘솔 출력               | Worker(0) Store | Worker(1) Store
----------------------|-----------------------|-----------------|-----------------
1000 0 VIEW_AD1                                   1000#VIEW_AD1
----------------------|-----------------------|-----------------|-----------------
1500 0 VIEW_AD2                                   1000#VIEW_AD1
                                                  1500#VIEW_AD2
----------------------|-----------------------|-----------------|-----------------
2000 1 VIEW_AD3                                                    2000#VIEW_AD3
----------------------|-----------------------|-----------------|-----------------
2500 1 VIEW_AD4                                                    2000#VIEW_AD3
                                                                   2500#VIEW_AD4
----------------------|-----------------------|-----------------|-----------------
4000 0 CLICK_AD1        Worker(0):Matched AD1     1000#VIEW_AD1
                                                  1500#VIEW_AD2
                                                  4000#CLICK_AD1
----------------------|-----------------------|-----------------|-----------------
5000 1 CLICK_AD4        Worker(1):Matched AD4                      2000#VIEW_AD3
                                                                   2500#VIEW_AD4
                                                                   5000#CLICK_AD4
----------------------|-----------------------|-----------------|-----------------
6000 0 CLICK_AD2                                  1000#VIEW_AD1 입력된 Timestamp(6000)와 Store Item의 Timestamp(1000,1500)의 차이가 만료시간(3000)을 초과하여 제거됨
                                                  1500#VIEW_AD2 입력된 Timestamp(6000)와 Store Item의 Timestamp(1000,1500)의 차이가 만료시간(3000)을 초과하여 제거됨
                                                  4000#CLICK_AD1
                                                  6000#CLICK_AD2
----------------------|-----------------------|-----------------|-----------------
7000 1 CLICK_AD3                                                  2000#VIEW_AD3 삭제됨
                                                                  2500#VIEW_AD4 삭제됨
                                                                  5000#CLICK_AD4
                                                                  7000#CLICK_AD3
