import java.util.ArrayList;
import java.util.List;

import bwapi.Position;

/// �� ���α׷� ������ ���Ǽ� ����� ���� ���� ȭ�鿡 �߰� �������� ǥ���ϴ� class<br>
/// ���� Manager ��κ��� ������ ��ȸ�Ͽ� Screen Ȥ�� Map �� ������ ǥ���մϴ�
public class UXManager {
	
	private static UXManager instance = new UXManager();
	
	/// static singleton ��ü�� �����մϴ�
	public static UXManager Instance() {
		return instance;
	}
	
	/// ��Ⱑ ���۵� �� ��ȸ������ �߰� ������ ����մϴ�
	public void onStart() {
	}

	/// ��� ���� �� �� �����Ӹ��� �߰� ������ ����ϰ� ����� �Է��� ó���մϴ�
	public void update() {
	}
}