

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private int saveUnitLevel = 1;
	
	private boolean attackWithTank = false;
	private int stickToTankRadius = 0;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.saveUnitLevel = saveUnitLevel;
		
		this.attackWithTank = tankList.size() * 6 >= goliathList.size();
		if (this.attackWithTank) {
			this.stickToTankRadius = 140 + (int) (Math.log(goliathList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToTankRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit goliath) {
		if (!CommonUtils.executeUnitRotation(goliath, LagObserver.groupsize())) {
			return;
		}

		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(goliath, enemiesInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: attack
		KitingOption kOpt = KitingOption.defaultKitingOption();
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(goliath, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			boolean haveToFight = true;
			Unit closeTank = null;
			if (attackWithTank) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit tank : tankList) {
					int dist = goliath.getDistance(tank.getPosition());
					if (dist < closeDist) {
						closeTank = tank;
						closeDist = dist;
						// ����� ���� ��ũ�� ������ �ο��.
						if (closeDist < stickToTankRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			}
			if (haveToFight) {
				Unit enemy = MicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && goliath.isInWeaponRange(enemy)) {
					goliath.holdPosition();
				} else {
					kOpt.setGoalPosition(order.getPosition());
					MicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
				}
			} else {
				// ����� ���� ������ ��ũ�� �̵�
//				kOpt.setGoalPosition(closeTank.getPosition());
//				kOpt.setCooltimeAlwaysAttack(false);
				CommandUtil.move(goliath, closeTank.getPosition());
			}
			break;
			
		case 2: // attack move
			if (MicroSet.Common.versusMechanicSet()) {
				// �׶����� go
				int distToOrder = goliath.getDistance(order.getPosition());
				if (distToOrder <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition�� �ѷ��� ������ �����.
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtil.attackMove(goliath, randomPosition);
						}
					}
				} else {
					CommandUtil.attackMove(goliath, order.getPosition());
				}
				
			} else {
				Position movePosition = order.getPosition();
				
				// �̵��������� attackMove�� ����.
				if (goliath.getDistance(movePosition) > order.getRadius()) {
//					CommandUtil.attackMove(goliath, movePosition);
					CommandUtil.move(goliath, movePosition);
					
				} else { // ������ ����
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtil.attackMove(goliath, randomPosition);
						}
					}
				}
			}
			break;
		}
	}

}
