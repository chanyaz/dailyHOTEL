package com.viewpagerindicator;

public interface Loopable {
	/**
	 * 
	 * @return �������� ��¥ ����
	 */
	int getRealCount();
	
	/**
	 * 
	 * @param fakePos ��¥ ��ġ��(������ ���Ͽ� ������ ���� �������� ����, fakePos�� pos�� ���)
	 * @return ��¥ ������ ��ġ ��갪 
	 */
	
	int getRealPos(int fakePos);
	
	/**
	 * 
	 * @return ��¥ �������� ���� ��ġ ��
	 */
	
	int getRealCurPos();
}
