package test;

import java.util.ArrayList;
import java.util.List;

import com.lgcns.test.AbstractWorker;

/* ----------------------------------------------------------------------------
 * 
 * Worker.java - removeExpiredStoreItems() ����, �� �� ���� ����
 * 
 * ----------------------------------------------------------------------------
 */
public class Worker extends AbstractWorker {
	
	/*
	 * �� Worker ����
	 * - <Queue ��ȣ>�� �Ķ���ͷ� �Ͽ� Worker �ν��Ͻ� ����
	 */
	public Worker(int queueNo) {
		super(queueNo);
	}
	
	/*
	 * �� ����� Store Item ����
	 * - �Էµ� Timestamp�� Store Item�� Timestamp���� ���̰� ����ð�(3000)�� �ʰ��ϸ� Store���� ����
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store) {
		for(int i = store.size()-1; i >= 0; i--) {
			String line = store.get(i);
			String[] element = splitToStringArray(line, "#", true);
			long timeout = Long.parseLong(element[0]);
			if (timestamp - timeout > 3000) {
				store.remove(i);
			}
		}
		//throw new UnsupportedOperationException("removeExpiredStoreItems()�� 3�� ���׿��� �����ϼ���.");
	}
	
	private String[] splitToStringArray(String str, String delimiter, boolean removeEmptyString) {
        String[] strArr = str.split(delimiter);
        ArrayList<String> strList = new ArrayList<String>();
        for (String s : strArr) {
            // 빈 문자열 제거 여부가 true이고 빈 문자열인 경우에는 리스트에 추가하지 않음
            if (removeEmptyString && s.isEmpty()) {
                continue;
            }
            strList.add(s);
        }
        strArr = strList.toArray(new String[strList.size()]);
        return strArr;
    }
}
