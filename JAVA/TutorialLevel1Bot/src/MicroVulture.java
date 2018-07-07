

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class MicroVulture extends MicroManager {
	
	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> vultures = getUnits();
		List<Unit> vultureTargets = MicroUtils.filterTargets(targets, false);
		
		final boolean cooltimeAlwaysAttack = false;
		final boolean unitedKiting = false;
		final Integer[] fleeAngle = MicroSet.FleeAngle.WIDE_ANGLE;
		final Position retreatPosition = order.getType() == SquadOrderType.WATCH ?
				InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition() : order.getPosition();

		for (Unit vulture : vultures) {
			
			// ���θż��� ����� ��ó��� �ż�����
			Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
			if (positionToMine != null) {
				CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
				continue;
			}

			Unit target = getTarget(vulture, vultureTargets);
			
			if (target != null) {
				KitingOption kitingOption = new KitingOption(cooltimeAlwaysAttack, unitedKiting, fleeAngle, retreatPosition);
				
				// checker ��ó���� ������ orderPosition�� ������.
				if (order.getType() == SquadOrderType.CHECK) {
					BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
					if (travelBase != null) {
						kitingOption.setGoalPosition(travelBase.getPosition());
					}
				}

				List<Unit> nearTanks = new ArrayList<>();
				List<Unit> nearGoliaths = new ArrayList<>();
				List<Unit> nearVultures = new ArrayList<>();
				List<Unit> units = MapGrid.Instance().getUnitsNear(vulture.getPosition(), MicroSet.Common.MAIN_SQUAD_COVERAGE, true, false, null);
				for (Unit unit : units) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						nearTanks.add(unit);
					} else if (unit.getType() == UnitType.Terran_Goliath) {
						nearGoliaths.add(unit);
					} else if (unit.getType() == UnitType.Terran_Vulture) {
						nearVultures.add(unit);
					}
				}
				Position centerPosition = null;
				if (!nearTanks.isEmpty()) {
					centerPosition = MicroUtils.centerOfUnits(nearTanks);
				} else if (!nearGoliaths.isEmpty()) {
					centerPosition = MicroUtils.centerOfUnits(nearGoliaths);
				}
				
				// ��ũ, �񸮾��� ��ó�� ������ ��Ÿ���� ���ƿö� ������ ������.
				// TODO �������̽� Ȯ�� �ʿ� (�ʹ� ���� �ټ� ��)
				if (centerPosition != null) {
					kitingOption.setCooltimeAlwaysAttack(true);
					kitingOption.setFleeAngle(MicroSet.FleeAngle.NARROW_ANGLE);
					kitingOption.setGoalPosition(centerPosition);
				} else {
					// �ڽ��� ������ ���������� ���������� �ο��� �Ѵ�.
					Region region = BWTA.getRegion(vulture.getPosition());
					for (Region occupied : InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer)) {
						if (region == occupied) {
							break;
						}
					}
				}
				
				MicroUtils.preciseKiting(vulture, target, kitingOption);
//				CommandUtil.attackUnit(vulture, target);
				
			} else {
				// 1. ���θż� ��ġ üũ
				int spiderMineNumPerPosition = MicroSet.Vulture.spiderMineNumPerPosition;
				Position minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture, spiderMineNumPerPosition);
				if (order.getType() == SquadOrderType.WATCH) {
					if (minePosition == null) {
						BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
						if (BWTA.getRegion(base.getPosition()) != BWTA.getRegion(vulture.getPosition())) {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, spiderMineNumPerPosition);
						}
					}
				}
				if (minePosition != null) { // �ż��� ������ �ִٸ� ����
					continue;
				}
				
				// 2. ��ó ���� ���� ����, �̵����� ����
				// watcher : ��ǥ����(��base)���� �̵�. �տ� ������ �ʴ� ���� ������ ����base�� ����.
				// checker : ������ ��ǥ����(travelBase)���� �̵�.
				Position movePosition = order.getPosition();
				if (order.getType() == SquadOrderType.WATCH) { // watcher ������ �ʴ� ���� ���� ����.
					if (!MicroUtils.isSafePlace(vulture)) {
						movePosition = retreatPosition;
					}
				} else if (order.getType() == SquadOrderType.CHECK) { // checker ��ó���� ������ orderPosition�� ������.
					BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
					if (travelBase != null) {
						movePosition = travelBase.getPosition();
					}
				}
				
				// �̵��������� attackMove�� ����.
				if (vulture.getDistance(movePosition) > order.getRadius()) {
					CommandUtil.attackMove(vulture, movePosition);
					
				} else { // ������ ����
					if (vulture.isIdle() || vulture.isBraking()) {
						Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), order.getRadius());
						CommandUtil.attackMove(vulture, randomPosition);
					}
				}
			}
		}
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			if (!target.isDetected()) continue;
			
			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // �켱���� ����
			
			int distanceScore = 0; // �Ÿ� ����
			int hitPointScore = 0; // HP ����
			int dangerousScore = 0; // ������ ���� ����
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			int siegeMinRange = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
			if (target.getType().groundWeapon().maxRange() <= siegeMinRange) {
				List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), siegeMinRange, true, false, null);
				for (Unit unit : nearUnits) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						dangerousScore += 100; // ��� �پ��ִ� ���� �׿��� �Ѵ�.
					} else {
						dangerousScore += 10;
					}
				}
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
	        hitPointScore -= target.getHitPoints() / 10;
			
	        int totalScore = 0;
//	        if (order.getType() == SquadOrderType.WATCH) {
//	        	totalScore = distanceScore;
//	        } else {
	        	totalScore = priorityScore + distanceScore + hitPointScore + dangerousScore;
//	        }
	        
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
}
