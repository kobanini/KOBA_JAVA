

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

enum Result {
	Loss, Win, BigWin
}

public class CombatExpectation {
	
	public static Result expect(List<Unit> vultures, List<Unit> targets, boolean skipBuilding) {
		int vulturePower = getVulturePower(vultures);
		int enemyPower = enemyPower(targets, skipBuilding);
		Result winExpected = Result.Loss; // lose
		if (vulturePower > enemyPower) {
			if (vulturePower > enemyPower * 2) {
				winExpected = Result.BigWin;
			}
			winExpected = Result.Win;
		}
		return winExpected;
	}
	
	
	public static Result expectByUnitInfo(List<Unit> vultures, List<UnitInfo> targets, boolean skipBuilding) {
		int vulturePower = getVulturePower(vultures);
		int enemyPower = enemyPowerByUnitInfo(targets, skipBuilding);
		Result winExpected = Result.Loss; // lose
		if (vulturePower > enemyPower) {
			if (vulturePower > enemyPower * 2) {
				winExpected = Result.BigWin;
			}
			winExpected = Result.Win;
		}
		return winExpected;
	}

	// 벌처 한기 최대 점수 : 70점 (업그레이드시 100점)
	public static int getVulturePower(List<Unit> vultures) {
		int vulturePower = 0;
		for (Unit vulture : vultures) {
			vulturePower += VULTURE_POWER;
			if (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0) {
				vulturePower += BONUS_ION_THRUSTERS;
			}
			vulturePower += vulture.getHitPoints() * 40.0  / 75.0; // 50
		}
		return vulturePower;
	}
	
	// 저글링 한기 최대 점수 : 15점(업그레이드시 20점)
	// 히드라 한기 최대 점수 : 150점(업그레이드시 200점)
	// 질럿 한기 최대 점수 : 20점(업그레이드시 30점)
	// 드라군 한기 최대 점수 : 300점(업그레이드시 330점)
	public static int enemyPower(List<Unit> targets, boolean skipBuilding) {
		int enemyPower = 0;
		for (Unit target : targets) {
			if (!target.isCompleted() || VULTURE_TARGET.get(target.getType()) == null) {
				continue;
			}
			if (target.getType().isBuilding() && skipBuilding) {
				continue;
			}
			enemyPower += VULTURE_TARGET.get(target.getType());
			
			if (target.getType() == UnitType.Zerg_Zergling && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Anabolic_Synthesis) > 0) {
				enemyPower += BONUS_ANABOLIC_SYNTHESIS;
			} else if (target.getType() == UnitType.Zerg_Hydralisk) {
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Muscular_Augments) > 0) {
					enemyPower += BONUS_MUSCULAR_AUGMENTS;
				}
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
					enemyPower += BONUS_GROOVED_SPINES;
				}
				enemyPower += target.getHitPoints(); // 80
			} else if (target.getType() == UnitType.Zerg_Lurker && target.isBurrowed()) {
				enemyPower += BONUS_LURKER_BURROWED;
			} else if (target.getType() == UnitType.Protoss_Zealot && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0) {
				enemyPower += BONUS_LEG_ENHANCEMENTS;
			} else if (target.getType() == UnitType.Protoss_Dragoon) {
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
					enemyPower += BONUS_SINGULARITY_CHARGE;
				}
				enemyPower += target.getShields() + target.getHitPoints(); // 80 + 100
			}
		}
		return enemyPower;
	}
	
	public static int enemyPowerByUnitInfo(List<UnitInfo> targets, boolean skipBuilding) {
		int enemyPower = 0;
		
		for (UnitInfo targetInfo : targets) {
			Unit target = MicroUtils.getUnitIfVisible(targetInfo);
			
			if ((target != null && !target.isCompleted()) || VULTURE_TARGET.get(targetInfo.getType()) == null) {
				continue;
			}
			if (targetInfo.getType().isBuilding() && skipBuilding) {
				continue;
			}
			
			enemyPower += VULTURE_TARGET.get(targetInfo.getType());
			
			if (InformationManager.Instance().enemyRace == Race.Zerg) {
				if (targetInfo.getType().isWorker()) { // 게릴라하러 가는데 솔직히 일꾼은 계산에서 빼자
					enemyPower -= VULTURE_TARGET.get(targetInfo.getType());
				}
				
				if (targetInfo.getType() == UnitType.Zerg_Zergling && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Anabolic_Synthesis) > 0) {
					enemyPower += BONUS_ANABOLIC_SYNTHESIS;
				} else if (targetInfo.getType() == UnitType.Zerg_Hydralisk) {
					if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Muscular_Augments) > 0) {
						enemyPower += BONUS_MUSCULAR_AUGMENTS;
					}
					if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
						enemyPower += BONUS_GROOVED_SPINES;
					}
					enemyPower += 80; // 80
				} else if (targetInfo.getType() == UnitType.Zerg_Lurker) {
					enemyPower += BONUS_LURKER_BURROWED;
				}
			}

			if (InformationManager.Instance().enemyRace == Race.Protoss) {
				if (targetInfo.getType() == UnitType.Protoss_Zealot && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0) {
					enemyPower += BONUS_LEG_ENHANCEMENTS;
				} else if (targetInfo.getType() == UnitType.Protoss_Dragoon) {
					if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
						enemyPower += BONUS_SINGULARITY_CHARGE;
					}
					enemyPower += 180; // 80 + 100
				}
			}
			
			if (InformationManager.Instance().enemyRace == Race.Terran) {
				//
			}
			
			
			
			
		}
		return enemyPower;
	}
	
	public static int guerillaScore(List<Unit> enemis) {
		int score = 0;
		for (Unit unit : enemis) {
			if (unit.getType().isResourceDepot()) {
				score += 100;
			} else if (unit.getType().isWorker()) {
				score += 100;
			} else if (unit.getType().isBuilding() && !unit.getType().canAttack()) {
				score += 20;
			} else if (unit.getType().groundWeapon().maxRange() < MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
				score += 10;
			} else if (!unit.getType().isFlyer()){
				score += 5;
			} else {
				score -= 100;
			}
		}
		return score;
	}
	
	public static int guerillaScoreByUnitInfo(List<UnitInfo> enemiesInfo) {
		int score = 0;
		for (UnitInfo eui : enemiesInfo) {
			if (eui.getType().isResourceDepot()) { // 자원채취한다 100점
				score += 100;
			} else if (eui.getType() == UnitType.Terran_Supply_Depot || eui.getType() == UnitType.Protoss_Pylon) {
				score += 100;
			} else if (eui.getType().isWorker()) { // 일꾼있다 100점
				score += 100;
			} else if (eui.getType().isBuilding() && !eui.getType().canAttack()) { // 공격할 수 없는 건물
				score += 20;
			} else if (eui.getType().groundWeapon().maxRange() < MicroSet.Tank.SIEGE_MODE_MIN_RANGE) { // melee 유닛 10점
				score += 10;
			} else if (!eui.getType().isFlyer()){ // 걷는애 5점
				score += 5;
			}
		}
		if (score < 50) { // 채소50점이상
			return 0;
		} else {
			return score;
		}
	}
	
	private static final int VULTURE_POWER = 30;
	private static final int BONUS_ION_THRUSTERS = 30;
	
	private static final int BONUS_ANABOLIC_SYNTHESIS = 5;
	private static final int BONUS_MUSCULAR_AUGMENTS = 25;
	private static final int BONUS_GROOVED_SPINES = 25;
	private static final int BONUS_LURKER_BURROWED = 300;
	
	private static final int BONUS_LEG_ENHANCEMENTS = 10;
	private static final int BONUS_SINGULARITY_CHARGE = 30;
	
	private static final Map<UnitType, Integer> VULTURE_TARGET = new HashMap<>();
	
	static {
		VULTURE_TARGET.put(UnitType.Zerg_Larva, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Drone, 1);
		VULTURE_TARGET.put(UnitType.Zerg_Broodling, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Zergling, 15);
		VULTURE_TARGET.put(UnitType.Zerg_Infested_Terran, 15);
		VULTURE_TARGET.put(UnitType.Zerg_Hydralisk, 70); // hitpoint 점수 사용 *
		VULTURE_TARGET.put(UnitType.Zerg_Ultralisk, 100);
		VULTURE_TARGET.put(UnitType.Zerg_Defiler, 100);
		VULTURE_TARGET.put(UnitType.Zerg_Mutalisk, 300);
		VULTURE_TARGET.put(UnitType.Zerg_Guardian, 400);
		VULTURE_TARGET.put(UnitType.Zerg_Sunken_Colony, 800);
		
		VULTURE_TARGET.put(UnitType.Protoss_High_Templar, 0); // 하템 암살해야됨
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Archon, 0);
		VULTURE_TARGET.put(UnitType.Protoss_Probe, 1);
		VULTURE_TARGET.put(UnitType.Protoss_Zealot, 20);
		VULTURE_TARGET.put(UnitType.Protoss_Archon, 50);
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Templar, 100);
		VULTURE_TARGET.put(UnitType.Protoss_Dragoon, 120); // hitpoint 점수 사용 *
		VULTURE_TARGET.put(UnitType.Protoss_Scout, 300);
		VULTURE_TARGET.put(UnitType.Protoss_Carrier, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Reaver, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Photon_Cannon, 600);
		
		
		VULTURE_TARGET.put(UnitType.Terran_Marine, 30); // 하템 암살해야됨
		VULTURE_TARGET.put(UnitType.Terran_Medic, 30);
		VULTURE_TARGET.put(UnitType.Terran_Firebat, 30);
		VULTURE_TARGET.put(UnitType.Terran_Siege_Tank_Tank_Mode, 400);
		VULTURE_TARGET.put(UnitType.Terran_Siege_Tank_Siege_Mode, 400);
		VULTURE_TARGET.put(UnitType.Terran_Vulture, 100);
		VULTURE_TARGET.put(UnitType.Terran_Goliath, 400);
		VULTURE_TARGET.put(UnitType.Terran_Bunker, 400);
	}
}
