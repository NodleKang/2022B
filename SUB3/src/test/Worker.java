package test;

import java.util.List;
import com.lgcns.test.*;

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
			String[] element = MyString.splitToStringArray(line, "#", true);
			long timeout = Long.parseLong(element[0]);
			if (timestamp - timeout > 3000) {
				store.remove(i);
			}
		}
		// throw new UnsupportedOperationException("removeExpiredStoreItems()�� �����ϼ���.");
	}
}
