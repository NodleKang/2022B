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
	 * �� Worker ����
	 * - <Queue ��ȣ>�� ����� <Store>�� �Ķ���ͷ� �Ͽ� Worker �ν��Ͻ� ����
	 */
	public Worker(int queueNo, List<String> store) {
		super(queueNo, store);
	}
	
	/*
	 * �� ����� Store Item ����
	 * - �Էµ� Timestamp�� Store Item�� Timestamp���� ���̰� ����ð�(3000)�� �ʰ��ϸ� Store���� ����
	 */
	public void removeExpiredStoreItems(long timestamp, List<String> store) {
		// �Ʒ� ������ ����� ����� Store Item ���� ����� �����ϼ���.
		throw new UnsupportedOperationException("removeExpiredStoreItems()�� 4�� ���׿��� �����ϼ���.");
	}
}
