

import java.util.List;

import bwapi.Position;
import bwapi.Unit;

public class MicroGoliath extends MicroManager {

//	private int attackFrame = 0;

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> goliaths = getUnits();
		List<Unit> goliathTargets = MicroUtils.filterTargets(targets, true);
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setGoalPosition(order.getPosition());
		
		for (Unit goliath : goliaths) {
//			if (order.getType() != SquadOrderType.BATTLE && awayFromChokePoint(goliath)) {
//				continue;
//			}
//			if (order.getType() == SquadOrderType.ATTACK && inUnityThereIsStrength(goliath)) {
//				continue;
//			}
			
			Unit target = getTarget(goliath, goliathTargets);
			if (target != null) {
				MicroUtils.preciseKiting(goliath, target, kitingOption);
			} else {
				// if we're not near the order position, go there
				if (goliath.getDistance(order.getPosition()) > order.getRadius()) {
					CommandUtil.attackMove(goliath, order.getPosition());
				} else {
					if (goliath.isIdle()) {
						Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), order.getRadius());
						CommandUtil.attackMove(goliath, randomPosition);
					}
				}
			}
		}
		
//		if (!goliathTargets.isEmpty()) {
//
//			for (Unit goliath : goliaths) {
//				Unit target = getTarget(goliath, goliathTargets);
//				MicroUtils.smartKiteTarget(goliath, target, order.getPosition(), true, true);
//			}
//			
//			// 그룹을 나누어 카이팅
//			List<List<Unit>> goliathGroups = divedToGroup(goliaths, 5);
//			MyBotModule.Broodwar.sendText("goliathGroups.size() : " + goliathGroups.size());
//			
//			for (List<Unit> goliathGroup : goliathGroups) {
//				int kitingType = kitingType(goliathGroup);
//				
//				switch (kitingType) {
//				case 1:
//					List<Unit> goliathGroupTargets = new ArrayList<>();
//					for (Unit goliath : goliathGroup) {
//						MapGrid.Instance().getUnitsNear(goliathGroupTargets, goliath.getPosition(), 100, false, true);
//					}
//					
//					if (!goliathGroupTargets.isEmpty()) {
//						attackFrame = MicroUtils.groupKite(goliathGroup, goliathGroupTargets, attackFrame);
//						if (attackFrame > 0) {
//							for (Unit goliath : goliathGroup) {
//								Unit target = getTarget(goliath, goliathTargets);
//								CommandUtil.attackUnit(goliath, target);
//							}
//						}
//					} else {
//						for (Unit goliath : goliathGroup) {
//							Unit target = getTarget(goliath, goliathTargets);
//							MicroUtils.smartKiteTarget(goliath, target, order.getPosition(), true, true);
//						}
//					}
//					
//					break;
//					
//				case 2:
//					for (Unit goliath : goliathGroup) {
//						Unit target = getTarget(goliath, goliathTargets);
//						MicroUtils.smartKiteTarget(goliath, target, order.getPosition(), true, false);
//					}
//					break;
//					
//				case 3:
//					for (Unit goliath : goliathGroup) {
//						Unit target = getTarget(goliath, goliathTargets);
//						CommandUtil.attackUnit(goliath, target);
//					}
//					break;
//				}
//			}
//		} else {
//			for (Unit goliath : goliaths) {
//				if (goliath.getDistance(order.getPosition()) > 100) {
//					CommandUtil.attackMove(goliath, order.getPosition());
//				}
//			}
//		}
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			if (!target.isDetected()) continue;
			
			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
	        hitPointScore -= target.getHitPoints() / 10;
			
			int totalScore = priorityScore + distanceScore + hitPointScore;
			if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	
//	private List<List<Unit>> divedToGroup(List<Unit> goliaths, int groupMax) {
//		List<List<Unit>> goliathGroups = new ArrayList<>();
//
//		List<Integer> assignedUnitId = new ArrayList<>();
//		for (Unit goliath : goliaths) {
//			if (assignedUnitId.contains(goliath.getID())) {
//				continue;
//			}
//
//			List<Unit> goliathGroup = new ArrayList<>();
//			goliathGroup.add(goliath);
//			assignedUnitId.add(goliath.getID());
//			
//			List<Unit> checkGoliaths = goliathGroup;
//			
//			boolean isFull = false;
//			while (!isFull && !checkGoliaths.isEmpty()) {
//				List<Unit> nearUnits = new ArrayList<>();
//				for (Unit checkGoliath : checkGoliaths) {
//					MapGrid.Instance().getUnitsNear(nearUnits, checkGoliath.getPosition(), 50, true, false);
//				}
//
//				List<Unit> toAdd = new ArrayList<>();
//				for (Unit nearUnit : nearUnits) {
//					if (assignedUnitId.contains(nearUnit.getID()) || nearUnit.getType() != UnitType.Terran_Goliath) {
//						continue;
//					}
//					toAdd.add(nearUnit);
//					assignedUnitId.add(nearUnit.getID());
//					
//					if (goliathGroup.size() + toAdd.size() > groupMax) {
//						isFull = true;
//						break;
//					}
//				}
//				goliathGroup.addAll(toAdd);
//				checkGoliaths = toAdd;
//			}
//			
//			goliathGroups.add(goliathGroup);
//		}
//		return goliathGroups;
//	}
//	
//	private int kitingType(List<Unit> rangedUnit) {
//
//		int flying = 0;
//		int ground = 0;
//		List<Unit> nearEnemies = new ArrayList<>();
//		for (Unit unit : rangedUnit) {
//			MapGrid.Instance().getUnitsNear(nearEnemies, unit.getPosition(), 200, false, true);
//		}
//		for (Unit enemy : nearEnemies) {
//			if (enemy.getType().isBuilding()) {
//				continue;
//			}
//
//			if (enemy.isFlying()) {
//				flying++;
//			} else {
//				ground++;
//			}
//		}
//		
//		return 1;
//	}

}
