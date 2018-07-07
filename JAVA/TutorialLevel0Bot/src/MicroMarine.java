
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class MicroMarine extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
		List<Unit> marines = getUnits();
		Unit bunker = null;
		Unit CC = null;

		if (targets.size() == 0) {
			return;
		}

		boolean DontGoFar = true;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Bunker && unit.isCompleted()) {
				bunker = unit;
			}
			if (unit.isUnderAttack()) {
				DontGoFar = false;
			}
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
				CC = unit;
			}
		}
		if(InformationManager.Instance().isGasRushed() == true){
			DontGoFar = false;
		}
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setCooltimeAlwaysAttack(true);
		kitingOption.setUnitedKiting(false); // TODO ���� �� ������?
		//kitingOption.setGoalPosition(order.getPosition());
		kitingOption.setFleeAngle(MicroSet.FleeAngle.NARROW_ANGLE);

		// ��Ŀ�� ���� ��� �ൿ
		if (bunker == null) {
			Position mineralpos = CombatManager.Instance().getBestPosition(CC);
			kitingOption.setGoalPosition(mineralpos);

			for (Unit marine : marines) {
				if (!CommonUtils.executeUnitRotation(marine, LagObserver.groupsize())) {
					continue;
				}

				Unit target = getTarget(marine, targets);
				if (target != null) {

					if (DontGoFar) {
						if (marine.getDistance(mineralpos) > 30) {
							CommandUtil.move(marine, mineralpos);
						}
					} else {
						MicroUtils.preciseKiting(marine, target, kitingOption);
					}
				} else {
					if (InformationManager.Instance().enemyRace == Race.Zerg) {
					// if we're not near the order position, go there
						if (marine.getDistance(mineralpos) > 30) {
							CommandUtil.move(marine, mineralpos);
						}
					}else{
						CommandUtil.move(marine, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
					}
				}
			}
		} else {// ��Ŀ�� ������
			
			Position bunkerpos = bunker.getPosition();
			kitingOption.setGoalPosition(bunkerpos);
			for (Unit marine : marines) {
				if (!CommonUtils.executeUnitRotation(marine, LagObserver.groupsize())) {
					continue;
				}
				
				Unit target = getTarget(marine, targets);
				if (target != null) {

					if (marine.getHitPoints() < 11) {
						bunker.load(marine);
						continue;
					}

					if (DontGoFar) {
						if (marine.getDistance(bunker) > 120) {
							marine.move(bunker.getPosition());
						}
						if (marine.getHitPoints() < 16) {
							if (marine.getDistance(bunker) > 50) {
								marine.move(bunker.getPosition());
							}
						}
					} else {
						if (marine.getDistance(target) <= marine.getType().groundWeapon().maxRange() * 3 / 4){
							if (marine.getDistance(bunker) < 40) {
								bunker.load(marine);
							} else {
								marine.move(bunker.getPosition());
							}
						} else {
							MicroUtils.preciseKiting(marine, target, kitingOption);
						}
					}
				} else {
					// if we're not near the order position, go there
					if (marine.getDistance(bunkerpos) > 30) {
						CommandUtil.move(marine, bunkerpos);
					}
				}
			}
		}
	}
	// public Unit getClosestMineral(Unit depot)
	// {
	// double bestDist = 99999;
	// Unit bestMineral = null;
	//
	// for (Unit mineral : MyBotModule.Broodwar.getAllUnits()){
	// if ((mineral.getType() == UnitType.Resource_Mineral_Field) &&
	// mineral.getDistance(depot) < 320){
	// double dist = mineral.getDistance(depot);
	// if (dist < bestDist){
	// bestMineral = mineral;
	// bestDist = dist;
	// }
	// }
	// }
	// return bestMineral;
	// }

	// public Position getBestPosition(Unit depot)
	// {
	// int x =0;
	// int y =0;
	// int finalx =0;
	// int finaly =0;
	// int minCnt = 0;
	// for (Unit mineral : MyBotModule.Broodwar.getAllUnits()){
	// if ((mineral.getType() == UnitType.Resource_Mineral_Field) &&
	// mineral.getDistance(depot) < 320){
	// x += mineral.getPosition().getX();
	// y += mineral.getPosition().getY();
	// minCnt++;
	// }
	// }
	// finalx = x/minCnt;
	// finaly = y/minCnt;
	// finalx = (finalx + depot.getPosition().getX())/2;
	// finaly = (finaly + depot.getPosition().getY())/2;
	//
	// Position res = new Position(finalx, finaly);
	// return res;
	// }
	//
	private Unit getTarget(Unit marine, List<Unit> targets) {
		int bestScore = -999999;
		Unit bestTarget = null;

		for (Unit target : targets) {
			if (!target.isDetected())
				continue;

			int priorityScore = TargetPriority.getPriority(marine, target); // �켱���� ����
			
			// int priority = 0; // 0..12
			int range = marine.getDistance(target); // 0..map size in pixels
			int toGoal = target.getDistance(order.getPosition()); // 0..map size
																	// in pixels

			// Let's say that 1 priority step is worth 160 pixels (5 tiles).
			// We care about unit-target range and target-order position
			// distance.
			// int score = 5 * 32 * priority - range - toGoal/2;
			int score = -range - toGoal / 2 + priorityScore;

			// Adjust for special features.
			// This could adjust for relative speed and direction, so that we
			// don't chase what we can't catch.
			if (marine.isInWeaponRange(target)) {
				score += 4 * 32;
			} else if (!target.isMoving()) {
				score += 24;
			} else if (target.isBraking()) {
				score += 16;
			} else if (target.getType().topSpeed() >= marine.getType().topSpeed()) {
				score -= 5 * 32;
			}

			// Prefer targets that are already hurt.
			// if (target.getType().getRace() == Race.Protoss &&
			// target.getShields() == 0) {
			// score += 32;
			// } else if (target.getHitPoints() <
			// target.getType().maxHitPoints()) {
			// score += 24;
			// }

			score += (target.getType().maxHitPoints() + target.getType().maxShields() - target.getHitPoints()
					+ target.getShields()) / target.getType().maxHitPoints() + target.getType().maxShields() * 32;

			if (score > bestScore) {
				bestScore = score;
				bestTarget = target;
			}
		}

		return bestTarget;
	}

	public static Position getFleePosition(Unit rangedUnit, Position targetPosition, int moveDistPerSec,
			boolean unitedKiting, Position goalPosition, Integer[] fleeAngle, Boolean bunker) {
		int reverseX = rangedUnit.getPosition().getX() - targetPosition.getX(); // Ÿ�ٰ�
																				// �ݴ��
																				// ����
																				// x��
		int reverseY = rangedUnit.getPosition().getY() - targetPosition.getY(); // Ÿ�ٰ�
																				// �ݴ��
																				// ����
																				// y��
		final double fleeRadian = Math.atan2(reverseY, reverseX); // ȸ�� ����

		Position safePosition = null; // 0.0 means the unit is facing east.
		int minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;
		double maximumdistanceToTarget = 0;

		// Integer[] FLEE_ANGLE =
		// MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType()); //
		// MicroData.FleeAngle�� ����� ����Ÿ�Կ� ���� ȸ�� �� ����(�񸮾� �������� �׶��ؼ� ���� ������ ���鳢��
		// ����)
		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // ȸ�� ��(radian)
		int moveCalcSize = moveDistPerSec; // �̵� ȸ�������� �Ÿ� = ������ �ʴ��̵��Ÿ�
		if (!bunker) {
			moveCalcSize = moveDistPerSec / 4;
		}
		while (safePosition == null && moveCalcSize > 10) {
			for (int i = 0; i < FLEE_ANGLE.length; i++) {
				Position fleeVector = new Position((int) (moveCalcSize * Math.cos(fleeRadianAdjust)),
						(int) (moveCalcSize * Math.sin(fleeRadianAdjust))); // �̵�����
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(),
						rangedUnit.getPosition().getY() + fleeVector.getY()); // ȸ������
				Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2,
						rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // ȸ���߰�����

				int risk = MicroUtils.riskOfFleePosition(rangedUnit.getType(), movePosition, moveCalcSize,
						unitedKiting); // ȸ������������ �������赵
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // ���赵��
																					// ����
																					// ���
																					// 2��°
																					// �������:
																					// ��ǥ����������
																					// �Ÿ�
				double distanceToTarget = movePosition.getDistance(targetPosition);

				if (bunker) {
					risk += distanceToGoal;
					// ȸ�������� ��ȿ�ϰ�, �ɾ�ٴ� �� �־�� �ϰ�, �����ؾ� �ϰ� ���
					if (MicroUtils.isValidGroundPosition(movePosition) && middlePosition.isValid()
							&& BWTA.getRegion(middlePosition) != null && (risk < minimumRisk
									|| (risk == minimumRisk && distanceToGoal < minimumDistanceToGoal))) {

						safePosition = movePosition;
						minimumRisk = risk;
						minimumDistanceToGoal = distanceToGoal;
					}
				} else {
					if (MicroUtils.isValidGroundPosition(movePosition) && middlePosition.isValid()
							&& BWTA.getRegion(middlePosition) != null && (risk < minimumRisk
									|| (risk == minimumRisk && distanceToTarget > maximumdistanceToTarget))) {

						safePosition = movePosition;
						minimumRisk = risk;
						maximumdistanceToTarget = distanceToTarget;
					}
				}
				fleeRadianAdjust = MicroUtils.rotate(fleeRadian, FLEE_ANGLE[i]); // ��������
			}
			if (safePosition == null) { // ȸ�������� ���� ��� 1) ȸ�ǰŸ� ª�� ��� �ٽ� ��ȸ
				// MyBotModule.Broodwar.sendText("safe is null : " +
				// moveCalcSize);
				moveCalcSize = moveCalcSize * 2;

				if (moveCalcSize <= 10 && FLEE_ANGLE.equals(MicroSet.FleeAngle.NARROW_ANGLE)) { // ȸ��������
																								// ����
																								// ���
																								// 2)
																								// ��
																								// ������
																								// ����
					FLEE_ANGLE = MicroSet.FleeAngle.WIDE_ANGLE;
					unitedKiting = false;
					moveCalcSize = moveDistPerSec;
				}
			}
		}
		if (safePosition == null) { // ȸ�������� ���� ��� 3) ��ǥ�������� ����. �� ���� ���� ����.
			safePosition = goalPosition;
		}

		return safePosition;
	}

}
