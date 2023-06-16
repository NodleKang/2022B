* 콘솔로 Input Queue 입력받기

입력 형식 : <Queue 번호> + " " + <Value>
예> 0 VIEW_AD1 (<Queue 번호>: 0, <Value>: VIEW_AD1)

* 콘솔로 Output Queue 출력하기

Worker 실행 결과(리턴된 문자열)가 Null이 아니면 콘솔(Output Queue)로 출력

* 콘솔 입출력 예
C:\>SP_TEST<엔터키>        구현한 프로그램 실행 (Argument 없음)
0 VIEW_AD1                콘솔 입력
1 VIEW_AD2                콘솔 입력
0 CLICK_AD1               콘솔 입력
Worker(0):Matched AD1     콘솔 출력
1 CLICK_AD3               콘솔 입력
1 CLICK_AD2               콘솔 입력
Worker(1):Matched AD2     콘솔 출력
...
