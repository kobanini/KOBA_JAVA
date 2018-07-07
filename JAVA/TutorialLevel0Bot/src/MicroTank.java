

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Chokepoint;

public class MicroTank extends MicroManager {
	
	private int initFrame = 0;
	private Unit initTarget = null;

	@Override
	protected void executeMicro(List<Unit> targets) {
		List<Unit> tanks = getUnits();
		List<Unit> tankTargets = MicroUtils.filterTargets(targets, false);
		
		if (tankTargets.isEmpty()) {
			if (initFrame > 0) {
				initFrame = 0;
				initTarget = null;
			}
			// ���� ���� ���� ���, orderPosition���� �̵�
			for (Unit tank : tanks) {
				// ���������Ÿ� : ���ֽþ� + ����Ÿ�Ժ� ��ü���� ����ϴ� �αװ� (ex: 320pixel(����þ�) + 25 * log12)
				// �ش� ���� MicroSet.Tank.SIEGE_ARRANGE_DISTANCE �̻��̾�� �Ѵ� ������ sight�� �ص� 320�� ���� ���� ����.
				int tankAreaDist = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + (int) (Math.log(tanks.size()) * 10);
				moveIt(tank, tankAreaDist);
			}
			return;
		}
		
		// �̴Ͻÿ������Ѵ�.
		if (initFrame == 0) {
//			MyBotModule.Broodwar.sendText("initiate!");
			initFrame = MyBotModule.Broodwar.getFrameCount();
			initTarget = tankTargets.get(0);
		}

		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setGoalPosition(order.getPosition());
		
		for (Unit tank : tanks) {
//	        boolean tankNearChokepoint = false; 
//	        for (Chokepoint choke : BWTA.getChokepoints()) {
//	            if (choke.getCenter().getDistance(tank.getPosition()) < 64) {
//	                tankNearChokepoint = true;
//	                break;
//	            }
//	        }
			
			
			// [����/����� ��]
			// ������� �Ǿ� ���� ���
			//   1. ��ȿ�������� ���� ������ ���÷��� �������� �� ũ�ٰ� �Ǵ� -> ����� TODO ���� ��ٷ�����
			//   2. ��ȿ�������� ���� ����, ����, ���۸� ������ �پ��ִ� -> �����
			//   3. ��ȿ�������� ���� ����, ���� �ִ�������� �ָ� �ִ�
			//      1) ��ǥ������ �ڸ��� ��Ҵ� -> ��������
			//      1) ������ �� �ִ� ����� ��� �ִ� -> ��������
			//      2) ������ �� �ִ� ����� ��� ���� -> ���ݴ��� �츮���� ���� -> ��������
			//      3) ������ �� �ִ� ����� ��� ���� -> ���ݴ��� �츮���� ���� -> �����
			//   4. ��ȿ�������� ���� �ִ� -> ����
			// ��ũ���� �Ǿ� �ִ� ���
			//   1. �̴Ͻÿ���Ʈ -> ������
			//   2. ��ũ���� ���� ���� �ִ� -> ī���� TODO �׷��� �����带 �ؾߵ� ���� �ִ�.
			//   3. ��ũ���δ� ���� ���� ����, �����������  ���� �ִ�
			//      1) �и������̴� -> ī����
			//      2) �����������̴� -> ������
			//   4. ������ �������� ���� ���� ���� ����.
			//      1) ������ �� �ִ� ����� ��� �ִ� -> ������
			//      2) ������ �� �ִ� ����� ��� ���� -> ī���� 
			
			// ***** ������ *****
			if (tank.isSieged()) {
				Unit target = getTarget(tank, tankTargets, true);
				
				if (target == null) {
					// 1. ��ȿ�������� ���� ������ ���÷��� �������� �� ũ�ٰ� �Ǵ�
					tank.stop();
					
				} else if (tank.getDistance(target) < MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
					// 2. ��ȿ�������� ���� ����, ����, ���۸� ������ �پ��ִ�
					tank.unsiege();
					
				} else if (tank.getDistance(target) > MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
					// 3. ��ȿ�������� ���� ����, ���� �ִ�������� �ָ� �ִ�
					// (���� ���� ���� ���ؼ� ��� Ǫ�� ���� �����ؾ� �Ѵ�.)
					
					if (tank.getDistance(order.getPosition()) <= order.getRadius()) {
						continue; // order position������ ��� Ǯ�� �ʴ´�.
					}
					
					boolean targetIsFree = true; // target�� �������ݿ��� �����ο
					Unit closestTank = tank;
					int closestTargetDistance = tank.getDistance(target); // �ش� ������ ������ ���� �ʴ´�. �׷��� üũ�ϴ°Ŵ�.
					while (closestTank != null && targetIsFree) { // ����� ���� �� Ÿ�ٿ� ��� ��� �ִ��� üũ�Ѵ�.
						List<Unit> linkedTanks = MapGrid.Instance().getUnitsNear(closestTank.getPosition(), MicroSet.Tank.SIEGE_LINK_DISTANCE, true, false, UnitType.Terran_Siege_Tank_Siege_Mode);
						closestTank = null;
						for (Unit linkedTank : linkedTanks) {
							int targetDistanceWithLinked = linkedTank.getDistance(target);
							if (targetDistanceWithLinked <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE) { // ����� ��ũ�� ������ �� �ִٸ� target�� ���°� ��� Ǯ �ʿ����.
								targetIsFree = false;
								break;
							} else if (targetDistanceWithLinked < closestTargetDistance) { // ����� ��ũ�� ������ �� ������ �� �����ٸ� �ٽ� �� ����� ��ũ�� ã�´�.
								closestTank = linkedTank;
								closestTargetDistance = targetDistanceWithLinked;
								break;
							}
						}
					}
					
					if (targetIsFree && MyBotModule.Broodwar.getFrameCount() - initFrame > 8) {
						tank.unsiege();
					}
//					if (targetIsFree) {
//						List<Unit> ourUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), target.getType().groundWeapon().maxRange(), true, false, null);
//						if (!ourUnits.isEmpty()) {
//							tank.unsiege();
//						}
//					}
					
				} else {
					// 4. ��ȿ�������� ���� �ִ�
					CommandUtil.attackUnit(tank, target);
				}
				
			} else { // ***** ��ũ��� *****
				List<Unit> targetsInTankRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), MicroSet.Tank.TANK_MODE_RANGE, false, true, null);
				if (!MicroSet.Upgrade.hasResearched(TechType.Tank_Siege_Mode)) {
					// 0. ���� ������ �ȵ����� ����
					Unit target = null;
					if (targetsInTankRange.isEmpty()) {
						target = getTarget(tank, tankTargets, false);
					} else {
						target = getTarget(tank, targetsInTankRange, false);
					}
					MicroUtils.preciseKiting(tank, target, kitingOption);
					
				} else if (initFrame == MyBotModule.Broodwar.getFrameCount() && tank.getDistance(initTarget) <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 150) {
					// 1. �̴Ͻÿ���Ʈ
					tank.siege();
					
				} else if (!targetsInTankRange.isEmpty()) {
					// 2. ��ũ���� ���� ���� �ִ�
					Unit target = getTarget(tank, targetsInTankRange, false);
					MicroUtils.preciseKiting(tank, target, kitingOption);
					
				} else {
					List<Unit> targetsInSiegeRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), MicroSet.Tank.SIEGE_MODE_MAX_RANGE, false, true, null);

					if (!targetsInSiegeRange.isEmpty()) {
						// 3. ��ũ���δ� ���� ���� ����, �����������  ���� �ִ�
						Unit target = getTarget(tank, targetsInSiegeRange, true);
						// �и������̸� �� �������� ������(������� Ÿ���� ���Ͽ� splash������ target�� null�� �� �� �ִ�. �̶��� ������)
						// ������ �� �ִ� �Ÿ��̸� ������� ����.
						if (target == null || target.getType().groundWeapon().maxRange() <= MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
							target = getTarget(tank, targetsInSiegeRange, false);
							MicroUtils.preciseKiting(tank, target, kitingOption);
						} else {
							tank.siege();
						}
						
					} else {
						// 4. ������ �������� ���� ���� ���� ������
						// ����� �Ÿ��� ��� Ÿ���� ���� ������ �������Ѵ�.
						boolean shouldSiege = false;
						List<Unit> linkedTanks = MapGrid.Instance().getUnitsNear(tank.getPosition(), MicroSet.Tank.SIEGE_LINK_DISTANCE, true, false, UnitType.Terran_Siege_Tank_Siege_Mode);
						for (Unit linkedTank : linkedTanks) {
							List<Unit> targetsInLinkedSiegeRange = MapGrid.Instance().getUnitsNear(linkedTank.getPosition(), MicroSet.Tank.SIEGE_MODE_MAX_RANGE, false, true, null);
							if (!targetsInLinkedSiegeRange.isEmpty()) {
								shouldSiege = true;
								break;
							}
						}
						
						if (shouldSiege) {
							tank.siege();
						} else {
							Unit target = getTarget(tank, tankTargets, false);
							MicroUtils.preciseKiting(tank, target, kitingOption);
						}
					}
				}
			}
		}
	}
	
	
	private void moveIt(Unit tank, int siegeAreaDist) {
		// �����ߴٸ� ������
		if (tank.getDistance(order.getPosition()) <= siegeAreaDist && tank.canSiege()) {
			Position positionToSiege = findPositionToSiege(siegeAreaDist);
			if (positionToSiege != null) {
				if (tank.getDistance(positionToSiege) <= 50) {
					tank.siege();
				} else {
					CommandUtil.attackMove(tank, positionToSiege);
				}
			}
			
		} else if (tank.getDistance(order.getPosition()) <= order.getRadius()) {
			if (tank.isIdle()) {
				Position randomPosition = MicroUtils.randomPosition(tank.getPosition(), order.getRadius());
				CommandUtil.attackMove(tank, randomPosition);
			}
			
		} else {
			// ���� �������� ��������
            if (tank.canUnsiege()) {
                tank.unsiege();
            } else {
            	CommandUtil.attackMove(tank, order.getPosition());
            }
		}
	}
	
	private Position findPositionToSiege(int siegeAreaDist) {
		Chokepoint choke = BWTA.getNearestChokepoint(order.getPosition());
		
		int seigeNumLimit = 1;
		int distanceFromOrderPosition = MicroSet.Tank.getSiegeArrangeDistance();
		
		while (seigeNumLimit < 10) {
			while (distanceFromOrderPosition < siegeAreaDist) {
				for (Integer angle : MicroSet.FleeAngle.EIGHT_360_ANGLE) {
					double radianAdjust = MicroUtils.rotate(0.0, angle);
				    Position fleeVector = new Position((int)(distanceFromOrderPosition * Math.cos(radianAdjust)), (int)(distanceFromOrderPosition * Math.sin(radianAdjust)));
				    int x = order.getPosition().getX() + fleeVector.getX();
				    int y = order.getPosition().getY() + fleeVector.getY();
				    
				    Position movePosition = new Position(x, y);
				    if (movePosition.isValid() && BWTA.getRegion(movePosition) != null
							&& MyBotModule.Broodwar.isWalkable(movePosition.getX() / 8, movePosition.getY() / 8)) {
				    	
				    	if (choke.getCenter().getDistance(movePosition) >= 150) {
				    		int siegeCount = MapGrid.Instance().getUnitsNear(movePosition, 100, true, false, UnitType.Terran_Siege_Tank_Siege_Mode).size();
							if (siegeCount < seigeNumLimit) {
								return movePosition;
							} 
				    	}
				    }
				}
				
				if (distanceFromOrderPosition <= 0) {
					distanceFromOrderPosition = MicroSet.Tank.getSiegeArrangeDistance() + 50;
				} else if (distanceFromOrderPosition <= MicroSet.Tank.getSiegeArrangeDistance()) {
					distanceFromOrderPosition -= 50;
				} else {
					distanceFromOrderPosition += 50;
				}
			}
			distanceFromOrderPosition = MicroSet.Tank.getSiegeArrangeDistance();
			seigeNumLimit++;
		}
		
//		MyBotModule.Broodwar.sendText("findPositionToSiege is null");
		return null;
	}
	
	
	// ������(isSieged)�� ��� ���ݹ��� �ȿ� ���� ������ ���� ������ ���� ã�´�. splash�� ���� ���ذ� �� ���� ��� null�� ���ϵ� �� �ִ�. 
	//                         ���ݹ��� �ȿ� ���� ������ ��ü ������(targets)���� ���� ã�´�.
	// �����尡 �ƴ� ��� ����, ��ü ��ü ������(targets)���� ���� ã�´�.
	private Unit getTarget(Unit tank, List<Unit> targets, boolean isSieged) {
		
		Unit bestTarget = null;
	    int bestTargetScore = -99999;
		
		List<Unit> newTargets = targets;
		if (isSieged) {
			List<Unit> targetsInSiegeRange = new ArrayList<>();
		    for (Unit target : targets) {
		    	if (!target.isDetected()) continue;
		    	
		    	int targetDistance = target.getDistance(tank);
		        if (targetDistance >= MicroSet.Tank.SIEGE_MODE_MIN_RANGE && targetDistance <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
		            targetsInSiegeRange.add(target);
		        }
		    }

		    if (!targetsInSiegeRange.isEmpty()) {
		    	newTargets = targetsInSiegeRange;
		    }
		}
		    
	    for (Unit target : newTargets) {
	    	if (!target.isDetected()) continue;
	        
	        int priorityScore = TargetPriority.getPriority(tank, target); // ���ֺ� �켱����
			int splashScore = 0; // ���÷��� ����
			int distanceScore = 0; // �Ÿ� ����
			int hitPointScore = 0; // HP ����

	        // ���÷���
			if (isSieged) {
				List<Unit> unitsInSplash = target.getUnitsInRadius(MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD);
		        for (Unit unitInSplash : unitsInSplash) {
	        		int splashUnitDistance = target.getDistance(unitInSplash);
		        	int priorityInSpash = TargetPriority.getPriority(tank, unitInSplash);
			        if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_INNER_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.8);
			        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_MEDIAN_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.4);
			        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.2);
			        } else {
//			        	MyBotModule.Broodwar.sendText("tank splash enemy error");
			        }
			        
			        // �Ʊ��� ��� �켱������ ����. priority���� ���̳ʽ�(-)�� ���� ���� �ִ�. �̶��� Ÿ������ �������� �ʴ´�.
		        	if (unitInSplash.getPlayer() == InformationManager.Instance().enemyPlayer) {
		        		splashScore += priorityInSpash;
		        	} else if (unitInSplash.getPlayer() == InformationManager.Instance().selfPlayer) {
		        		splashScore -= priorityInSpash;
		        	}
		        }
		        
		        if (priorityScore + splashScore < 0) { // splash�� ���� �Ʊ����ذ� ���Ѱ��
		        	continue;
		        }
		        
			} else {
				if (tank.isInWeaponRange(target)) {
					distanceScore += 100;
				}
				
				distanceScore -= tank.getDistance(target) / 5;
			}
			
			// �ѹ濡 �״´ٸ� HP ���� ���� �켱������ ����.
			// �ѹ濡 ���״´ٸ� HP�� ���� ���� �켱������ ����.
			if (MicroUtils.killedByNShot(tank, target, 1)) {
				hitPointScore += target.getHitPoints() / 10;
	        } else {
	        	hitPointScore -= target.getHitPoints() / 10;
	        }
	        
			int totalScore = priorityScore + splashScore + distanceScore + hitPointScore;
			if (totalScore > bestTargetScore) {
				bestTarget = target;
				bestTargetScore = totalScore;
			}
	    }
	    
	    return bestTarget;
	}

}
