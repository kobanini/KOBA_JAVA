

import java.util.Arrays;

import bwapi.Position;

public class KitingOption {
	
	public KitingOption(boolean cooltimeAlwaysAttack, boolean unitedKiting, Integer[] fleeAngle, Position goalPosition) {
		this.cooltimeAlwaysAttack = cooltimeAlwaysAttack;
		this.unitedKiting = unitedKiting;
		this.fleeAngle = fleeAngle;
		this.goalPosition = goalPosition;
	}
	public static KitingOption defaultKitingOption() {
		return new KitingOption(true, true, MicroSet.FleeAngle.NARROW_ANGLE, null);
	}
	public static KitingOption vultureKitingOption() {
		return new KitingOption(false, false, MicroSet.FleeAngle.WIDE_ANGLE, null);
	}
	
	private boolean cooltimeAlwaysAttack; // cooltimeAlwaysAttack : true�� ��� ��Ÿ���� ���ƿ��� �� �׻� ������ �Ѵ�. ��ó, ���̽����� ������ ��� �� ���� true�� �ϸ� �ȵȴ�.
	private boolean unitedKiting; // unitedKiting : ȸ�������� risk�� ����, united���� true�� ��� �ڽ��� ���� �������¸� ������ �������� �Ǵ����� �����Ѵ�.(�ش簪�� false�̸� ������� kiting�� �Ѵ�.)
	private Position goalPosition; // goalPosition : ��ǥ����. ȸ�������� �����Ҷ� risk�� ������ ��ǥ������ ����Ѵ�.
	private Integer[] fleeAngle; // ȸ�ǰ���
	private boolean fleeGoalPosition; // haveToFlee = true�� ��� goalPosition�� �ݵ�� ��������� �Ѵ�.
	
	public boolean isCooltimeAlwaysAttack() {
		return cooltimeAlwaysAttack;
	}
	public void setCooltimeAlwaysAttack(boolean cooltimeAlwaysAttack) {
		this.cooltimeAlwaysAttack = cooltimeAlwaysAttack;
	}
	public boolean isUnitedKiting() {
		return unitedKiting;
	}
	public void setUnitedKiting(boolean unitedKiting) {
		this.unitedKiting = unitedKiting;
	}
	public Position getGoalPosition() {
		return goalPosition;
	}
	public void setGoalPosition(Position goalPosition) {
		this.goalPosition = goalPosition;
	}
	public Integer[] getFleeAngle() {
		return fleeAngle;
	}
	public void setFleeAngle(Integer[] fleeAngle) {
		this.fleeAngle = fleeAngle;
	}
	@Override
	public String toString() {
		return "KitingOption [cooltimeAlwaysAttack=" + cooltimeAlwaysAttack + ", unitedKiting=" + unitedKiting
				+ ", goalPosition=" + goalPosition + ", fleeAngle=" + Arrays.toString(fleeAngle) + ", fleeGoalPosition="
				+ fleeGoalPosition + "]";
	}
	
}
