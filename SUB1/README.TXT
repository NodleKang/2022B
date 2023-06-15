하기 내용을 반드시 숙지하고 시험 응시 바랍니다. 

+--------------------------------------------------------------+
|     JAVA 언어 제공 라이브러리 - 소스코드는 제공되지 않음     |
+--------------------------------------------------------------+

package com.lgcns.test;

public class Worker {
	
	/*
	 * ※ Worker 생성
	 * - 구현 프로그램이 시작되면 <Queue 번호(0, 1)>를 파라미터로 하여 2개의 Worker 인스턴스 생성
	 */
	public Worker(int queueNo);

	/*
	 * ※ Worker 실행
	 * - 콘솔을 통해 Input Queue 데이터가 입력되면 <Value>를 파라미터로 하여 <Queue 번호>에 해당하는 Worker 실행
	 */
	public synchronized String run(String value);
}
