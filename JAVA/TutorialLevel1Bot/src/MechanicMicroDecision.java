

import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

public class MechanicMicroDecision {

	public static MechanicMicroDecision makeDecisionToDoNothing() {
		return new MechanicMicroDecision(-1);
	}
	public static MechanicMicroDecision makeDecisionToStop() {
		return new MechanicMicroDecision(0);
	}
	public static MechanicMicroDecision makeDecisionToFlee(Position enemyPosition) {
		return new MechanicMicroDecision(0, enemyPosition);
	}
	public static MechanicMicroDecision makeDecisionToKiting(UnitInfo targetInfo) {
		return new MechanicMicroDecision(1, targetInfo);
	}
	public static MechanicMicroDecision makeDecisionToGo() {
		return new MechanicMicroDecision(2);
	}
	public static MechanicMicroDecision makeDecisionToChange() {
		return new MechanicMicroDecision(3);
	}
	
	private MechanicMicroDecision(int decision) {
		this.decision = decision;
	}
	private MechanicMicroDecision(int decision, Position enemyPosition) {
		this.decision = decision;
		this.enemyPosition = enemyPosition;
	}
	private MechanicMicroDecision(int decision, UnitInfo targetInfo) {
		this.decision = decision;
		this.targetInfo = targetInfo;
	}
	public int getDecision() {
		return decision;
	}
	
	// ��ó, �񸮾� -> 0: flee, 1: kiting, 2: go
	// ��ũ��� -> 0: flee, 1: kiting, 2: go, 3: change
	// ������ -> 0: stop, 1: kiting(attack unit), 2: go, 3:change
	private int decision; 
	private UnitInfo targetInfo;
	private Position enemyPosition;
	
	public UnitInfo getTargetInfo() {
		return targetInfo;
	}
	public Position getEnemyPosition() {
		return enemyPosition;
	}
	
	public static MechanicMicroDecision makeDecisionForSiegeMode(Unit mechanicUnit, List<UnitInfo> enemiesInfo, List<Unit> tanks, SquadOrder order, int saveUnitLevel) {

		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
		
		// siegeTank Ư���ɼ�
		boolean existSplashLossTarget = false;
		boolean existTooCloseTarget = false;
		boolean existTooFarTarget = false;
		boolean existCloakTarget = false;
		Unit cloakTargetUnit = null;
		boolean targetOutOfSight = false;
		
		Unit closestTooFarTarget = null;
		int closestTooFarTargetDistance = 0;
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);
			if (enemy == null) {

//				int noUnitFrame = MicroSet.Common.NO_UNIT_FRAME;
//				if (enemyInfo.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || enemyInfo.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
//					noUnitFrame = MicroSet.Common.NO_SIEGE_FRAME;
//				}
				
				if (bestTargetInfo == null && !MyBotModule.Broodwar.isVisible(enemyInfo.getLastPosition().toTilePosition())) {
					
					int distanceToTarget = mechanicUnit.getDistance(enemyInfo.getLastPosition());
					if (saveUnitLevel == 0 && distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 5) {
						bestTargetInfo = enemyInfo;
						targetOutOfSight = true;
					} else if (saveUnitLevel >= 1 && distanceToTarget <= (MicroSet.Tank.SIEGE_MODE_MAX_RANGE + (int) MicroSet.Common.BACKOFF_DIST_SIEGE_TANK)) {
						bestTargetInfo = enemyInfo;
						targetOutOfSight = true;
					} else {
//						System.out.println(distanceToTarget + ", " + (MicroSet.Tank.SIEGE_MODE_MAX_RANGE + (int) MicroSet.Common.BACKOFF_DIST_SIEGE_TANK));
					}
				}
				
				continue;
			}

			UnitType enemyUnitType = enemy.getType();
			
			// �켱���� ���� : ���� �켱���� ��
			int priorityScore = TargetPriority.getPriority(mechanicUnit.getType(), enemyUnitType);
			int splashScore = 0; // ���÷��� ���� : ������ ��ũ�� �ش� 
			int distanceScore = 0; // �Ÿ� ���� : �ְ����� 100��. �ּ��� ���� ����.
			int hitPointScore = 0; // HP ���� : �ְ����� 50��. HP�� ���� ���� ���� ����.
			int specialScore = 0; // Ư�� ���� : ��ũ�տ� �پ��ִ� �и����� +100��
			
			List<Unit> unitsInSplash = enemy.getUnitsInRadius(MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD);
	        for (Unit unitInSplash : unitsInSplash) {
        		int splashUnitDistance = enemy.getDistance(unitInSplash.getPosition());
	        	int priorityInSpash = TargetPriority.getPriority(mechanicUnit, unitInSplash);
		        if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_INNER_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.8);
		        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_MEDIAN_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.4);
		        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.2);
		        }
		        
		        // �Ʊ��� ��� �켱������ ����. priority���� ���̳ʽ�(-)�� ���� ���� �ִ�. �̶��� Ÿ������ �������� �ʴ´�.
	        	if (unitInSplash.getPlayer() == InformationManager.Instance().enemyPlayer) {
	        		splashScore += priorityInSpash;
	        	} else if (unitInSplash.getPlayer() == InformationManager.Instance().selfPlayer) {
	        		splashScore -= priorityInSpash;
	        	}
	        }
	        if (priorityScore + splashScore < 0) { // splash�� ���� �Ʊ����ذ� �� ���� ��� skip
	        	existSplashLossTarget = true;
	        	continue;
	        }

	    	int distanceToTarget = mechanicUnit.getDistance(enemy.getPosition());
			if (!mechanicUnit.isInWeaponRange(enemy)) { // ���� �����ȿ� Ÿ���� ���� ��� skip
				if (distanceToTarget < MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
					existTooCloseTarget = true;
		        } else if (distanceToTarget > MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
		        	existTooFarTarget = true;
		        	if (closestTooFarTarget == null || distanceToTarget < closestTooFarTargetDistance) {
		        		closestTooFarTarget = enemy;
		        		closestTooFarTargetDistance = distanceToTarget;
		        	}
		        }
	        	continue;
			}
			
			distanceScore = 100 - distanceToTarget / 5;

			// ������ : �ѹ濡 �״´ٸ� HP ���� ���� �켱������ ����.
			if (MicroUtils.killedByNShot(mechanicUnit, enemy, 1)) {
				hitPointScore = 50 + enemy.getHitPoints() / 10;
			} else {
				hitPointScore = 50 - enemy.getHitPoints() / 10;
			}
			
			// Ŭ��ŷ ���� : -1000��
			if (!enemy.isDetected()) {
				existCloakTarget = true;
				cloakTargetUnit = enemy;
				continue;
			}
			
			int totalScore = priorityScore + splashScore + distanceScore + hitPointScore + specialScore;
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTargetInfo = enemyInfo;
				targetOutOfSight = false;
			}
		}
		
		if (bestTargetInfo == null) {
			if (existSplashLossTarget) {
				return MechanicMicroDecision.makeDecisionToStop();
			} else if (existTooCloseTarget) {
				return MechanicMicroDecision.makeDecisionToChange();
			} else if (existTooFarTarget) {
				if (mechanicUnit.getDistance(order.getPosition()) < order.getRadius()) {
					return MechanicMicroDecision.makeDecisionToDoNothing();
				}
				for (Unit tank : tanks) { // target�� ����� ���� �������ݿ��� �������� ���ϴٸ� ��������
					int distanceToTarget = tank.getDistance(closestTooFarTarget);
					if (saveUnitLevel <= 1 && tank.isInWeaponRange(closestTooFarTarget) && tank.getDistance(mechanicUnit.getPosition()) < MicroSet.Tank.SIEGE_LINK_DISTANCE) {
						return MechanicMicroDecision.makeDecisionToDoNothing();
					} else if (saveUnitLevel == 2 && distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + MicroSet.Common.BACKOFF_DIST_SIEGE_TANK) {
						return MechanicMicroDecision.makeDecisionToDoNothing();
					}
				}
				if (saveUnitLevel == 0 || InformationManager.Instance().enemyRace != Race.Terran) {
					return MechanicMicroDecision.makeDecisionToChange();
				}
				return MechanicMicroDecision.makeDecisionToDoNothing();
			} else if (existCloakTarget) {
				if (cloakTargetUnit.getType() == UnitType.Protoss_Dark_Templar) {
					return MechanicMicroDecision.makeDecisionToChange();
				} else if (cloakTargetUnit.getType() == UnitType.Zerg_Lurker) {
					return MechanicMicroDecision.makeDecisionToDoNothing();
				}
			}
			return MechanicMicroDecision.makeDecisionToGo();
		} else {
			if (targetOutOfSight) { // �������� ������ �����Ÿ� �ȿ� ���� �ִٸ� ������ ����(saveUnitLevel�� ���� ����)
//				CommonUtils.consoleOut(1, mechanicUnit.getID(), "11111");
				return MechanicMicroDecision.makeDecisionToDoNothing();				
			} else {
				return MechanicMicroDecision.makeDecisionToKiting(bestTargetInfo);
			}
		}
		
		
	}
	
	public static MechanicMicroDecision makeDecision(Unit mechanicUnit, List<UnitInfo> enemiesInfo, int saveUnitLevel) {
		return makeDecision(mechanicUnit, enemiesInfo, null, saveUnitLevel);
	}

	public static MechanicMicroDecision makeDecision(Unit mechanicUnit, List<UnitInfo> enemiesInfo, List<UnitInfo> flyingEnemiesInfo, int saveUnitLevel) {
		
		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
//		int dangerousSiegeTankCount = 0;
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);

			// �����ϸ� �ȵǴ� ���� �ִ��� �Ǵ� (��ū, ����ĳ��, ������ũ, ��Ŀ)
			boolean enemyIsComplete = enemyInfo.isCompleted();
			Position enemyPosition = enemyInfo.getLastPosition();
			UnitType enemyUnitType = enemyInfo.getType();
			if (enemy != null) {
				if (!CommandUtil.IsValidUnit(enemy)) {
					continue;
				}
				enemyIsComplete = enemy.isCompleted();
				enemyPosition = enemy.getPosition();
				enemyUnitType = enemy.getType();
			}
			
			if (enemyIsComplete && saveUnitLevel >= 1) {
				if (enemyUnitType == UnitType.Zerg_Sunken_Colony
						|| enemyUnitType == UnitType.Protoss_Photon_Cannon
						|| enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode
						|| enemyUnitType == UnitType.Terran_Bunker
						|| (enemyUnitType == UnitType.Zerg_Lurker && enemy != null && enemy.isBurrowed() && !enemy.isDetected())
						|| (saveUnitLevel >= 2 && allRangeUnitType(MyBotModule.Broodwar.enemy(), enemyUnitType))) {
					
					int enemyGroundWeaponRange = enemyUnitType.groundWeapon().maxRange();
					if (enemyUnitType == UnitType.Terran_Bunker) {
						enemyGroundWeaponRange = MyBotModule.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 64; // 32->64(��û �ѵη��¾Ƽ� �ø�)
					}
					
					double distanceToNearEnemy = mechanicUnit.getDistance(enemyPosition);
					double safeDistance = enemyGroundWeaponRange;
					if (enemyUnitType == UnitType.Terran_Siege_Tank_Tank_Mode || enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode) {
						if (saveUnitLevel <= 1 && (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)) {
							if (MicroUtils.exposedByEnemy(mechanicUnit, flyingEnemiesInfo)) { // ������ ����Ǵ� �������̸� �����Ÿ��� ���. 
								safeDistance += (MicroSet.Common.BACKOFF_DIST_SIEGE_TANK * 2/3);
							} else { // ���� �����Ÿ��� ��� ����.
								safeDistance = MicroSet.Tank.SIEGE_MODE_SIGHT + 30; // 320 + 30
							}
							
						} else {
							safeDistance += MicroSet.Common.BACKOFF_DIST_SIEGE_TANK;
						}
						
					} else if (enemyUnitType == UnitType.Zerg_Sunken_Colony || enemyUnitType == UnitType.Protoss_Photon_Cannon || enemyUnitType == UnitType.Terran_Bunker) {
						// ��ũ�� ����� ����ϱ� ������ �����ϸ� �ȵ�
						if (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							safeDistance += (MicroSet.Common.BACKOFF_DIST_DEF_TOWER * 2/3);
						} else {
							safeDistance += MicroSet.Common.BACKOFF_DIST_DEF_TOWER;
						}
					} else {
						if (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							safeDistance += (MicroSet.Common.BACKOFF_DIST_RANGE_ENEMY * 2/3);
						} else {
							safeDistance += MicroSet.Common.BACKOFF_DIST_RANGE_ENEMY;
						}
					}
					
					if (distanceToNearEnemy < safeDistance) {
						return MechanicMicroDecision.makeDecisionToFlee(enemyPosition);
					}
//					else if ((enemyUnitType == UnitType.Terran_Siege_Tank_Tank_Mode || enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode)
//							&& (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
//							&& distanceToNearEnemy <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) {
//						if (++dangerousSiegeTankCount >= 2) { // ����Ÿ���⿡�� �ڽ��� ���� �� �ִ� ��� 2�� �̻��� ������ ������ ġ���� ���ذ� ���� �� �ִ�.
//							return MechanicMicroDecision.makeDecisionToFlee(enemyPosition);
//						}
//					}
				}
			}
			
			// �켱���� ���� : ���� �켱���� ��
			int priorityScore = TargetPriority.getPriority(mechanicUnit.getType(), enemyUnitType);
			int distanceScore = 0; // �Ÿ� ���� : �ְ����� 100��. �ּ��� ���� ����.
			int hitPointScore = 0; // HP ���� : �ְ����� 50��. HP�� ���� ���� ���� ����.
			int specialScore = 0; // Ư�� ���� : ��ũ�տ� �پ��ִ� �и����� +100��
			
			if (enemy != null) {
				if (mechanicUnit.isInWeaponRange(enemy)) {
					distanceScore = 100;
				}
				distanceScore -= mechanicUnit.getDistance(enemy.getPosition()) / 5;
				hitPointScore = 50 - enemy.getHitPoints() / 10;
				
		        // ��� �پ��ִ� �и����� : +200��
				if (enemyUnitType.groundWeapon().maxRange() <= MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
					List<Unit> nearSiege = MapGrid.Instance().getUnitsNear(enemyPosition, MicroSet.Tank.SIEGE_MODE_MIN_RANGE, true, false, UnitType.Terran_Siege_Tank_Siege_Mode);
					if (!nearSiege.isEmpty()) {
						specialScore += 100;
					}
				}
				// Ŭ��ŷ ���� : -1000��
				if (!enemy.isDetected() && enemyUnitType == UnitType.Protoss_Dark_Templar) {
					specialScore -= 1000;
				}
				if (enemyUnitType == UnitType.Protoss_Interceptor) {
					specialScore -= 1000;
				}
		        
			} else {
				distanceScore -= mechanicUnit.getDistance(enemyInfo.getLastPosition()) / 5;
				hitPointScore = 50 - enemyInfo.getLastHealth() / 10;
				specialScore = -100;
				if (enemyInfo.getType().isCloakable()) {
					continue;
				}
			}
			int totalScore = priorityScore + distanceScore + hitPointScore + specialScore;
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTargetInfo = enemyInfo;
			}
		}
		
		if (bestTargetInfo == null) {
			return MechanicMicroDecision.makeDecisionToGo();
		} else {
			return MechanicMicroDecision.makeDecisionToKiting(bestTargetInfo);
		}
	}
	
	public static boolean allRangeUnitType(Player player, UnitType unitType) { // ��ĭ �̻���ʹ� ������ �����̶�� �ұ�
		return player.weaponMaxRange(unitType.groundWeapon()) > UnitType.Protoss_Archon.groundWeapon().maxRange();
	}
	
	@Override
	public String toString() {
		return "MechanicMicroDecision [decision=" + decision + ", targetInfo=" + targetInfo + ", enemyPosition="
				+ enemyPosition + "]";
	}
}
