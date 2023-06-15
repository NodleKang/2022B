package test;

import java.util.ArrayList;
import java.util.List;

/* ----------------------------------------------------------------------------
 * 
 * Worker.java - removeExpiredStoreItems() 구현, 그 외 변경 금지
 * 
 * ----------------------------------------------------------------------------
 */
public class Worker extends AbstractWorker {
	
	/*
	 * ※ Worker 생성
	 * - <Queue 번호>를 파라미터로 하여 Worker 인스턴스 생성
	 */
	public Worker(int queueNo) {
		super(queueNo);
	}

	/*
	 * ※ 만료된 Store Item 제거
	 * - 입력된 Timestamp와 Store Item의 Timestamp간의 차이가 만료시간(3000)을 초과하면 Store에서 제거
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store) {
		for(int i = store.size()-1; i >= 0; i--) {
			String line = store.get(i);
			String[] element = MyString.splitToStringArray(line, "#", true);
			long timeout = Long.parseLong(element[0]);
			if (timestamp - timeout > 3000) {
				store.remove(i);
			}
		}
		// throw new UnsupportedOperationException("removeExpiredStoreItems()를 구현하세요.");
	}
}
