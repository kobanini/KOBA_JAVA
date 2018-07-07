

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Order;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

class SquadName {

	public static final String IDLE = "Idle";
	public static final String SCOUT_DEFENSE = "ScoutDefense";
	public static final String MAIN_ATTACK = "MainAttack";
	public static final String BASE_DEFENSE_ = "BaseDefense_";
	public static final String CHECKER = "Checker";
	public static final String GUERILLA_ = "Guerilla_";
	public static final String MARINE = "Marine";
	public static final String WRAITH = "Wraith";
	public static final String VESSEL = "Vessel";
	public static final String BUILDING = "Building";
}

class Combat {
	public static final int IDLE_PRIORITY = 0;
	public static final int ATTACK_PRIORITY = 2;
	public static final int WATCHER_PRIORITY = 3;
	public static final int CHECKER_PRIORITY = 4;
	public static final int GUERILLA_PRIORITY = 5;
	public static final int BASE_DEFENSE_PRIORITY = 6;
	public static final int SCOUT_DEFENSE_PRIORITY = 7;
	public static final int EARLY_DEFENSE_PRIORITY = 10;

	public static final int WRAITH_PRIORITY = 100;
	public static final int VESSEL_PRIORITY = 101;
	
	public static final int IDLE_RADIUS = 100;
	public static final int ATTACK_RADIUS = 300;
	public static final int WATCHER_RADIUS = 300;
	public static final int CHECKER_RADIUS = 150;
	public static final int GUERILLA_RADIUS = 300;
	public static final int BASE_DEFENSE_RADIUS = 500; // 32 * 25
	public static final int SCOUT_DEFENSE_RADIUS = 1500;
	public static final int MARINE_RADIUS = 300;
	public static final int WRAITH_RADIUS = 300;
	public static final int VESSEL_RADIUS = 600;
}

enum CombatStrategy {
	DEFENCE_INSIDE, DEFENCE_CHOKEPOINT, ATTACK_ENEMY //, READY_TO_ATTACK
};

enum CombatStrategyDetail {
	VULTURE_JOIN_SQUAD, NO_WAITING_CHOKE, NO_CHECK_NO_GUERILLA, ATTACK_NO_MERCY, MINE_STRATEGY_FOR_TERRAN
};

public class CombatManager {

	private static Chokepoint currTargetChoke = null;
	private static int currTargetChokeExpiredFrame = 0;
	
	private boolean gasRush = false; 
	private boolean photonRush = false;
	
	public boolean pushSiegeLine = false;
	
	private static int photonDefenseWornCnt = 0; 
	private double getWaitingPeriod() {
		Race enemyRace = InformationManager.Instance().enemyRace;
		if (enemyRace == Race.Zerg) {
			return 3.0;
		} else if (enemyRace == Race.Protoss) {
			return 3.3;
		} else if (enemyRace == Race.Terran) {
			return 3.0;
		}
		return 3.0;
	}
	
	private List<Unit> combatUnits = new ArrayList<>();
	public SquadData squadData = new SquadData();

	private boolean initialized = false;
	
	private CombatStrategy combatStrategy;
	private int[] detailStrategyExFrame = new int[10];
	
	public CombatStrategy getCombatStrategy() {
		return combatStrategy;
	}
	public void setCombatStrategy(CombatStrategy combatStrategy) {
		if (this.combatStrategy != combatStrategy) {
//			MyBotModule.Broodwar.sendText("combatStrategy changed : " + combatStrategy);
			this.combatStrategy = combatStrategy;
		}
		
		if (combatStrategy != CombatStrategy.ATTACK_ENEMY) {
			currTargetChoke = null;
		}
	}
	public int getDetailStrategyFrame(CombatStrategyDetail detailStrategy) {
		return detailStrategyExFrame[detailStrategy.ordinal()];
	}
	/// 특수상황 처리 함수 ex) 벌처 본진 귀환 setDetailStrategy(CombatStrategyDetail.VULTURE_JOIN_SQUAD);
	public void setDetailStrategy(CombatStrategyDetail detailStrategy, int frameDuration) {
//		MyBotModule.Broodwar.sendText("detailStrategy enabled : " + detailStrategy.toString());
		detailStrategyExFrame[detailStrategy.ordinal()] = MyBotModule.Broodwar.getFrameCount() + frameDuration;
	}
	public void updateDetailStrategy() {
		for (int i = 0; i < detailStrategyExFrame.length; i++) {
			int expireFrame = detailStrategyExFrame[i];
			if (expireFrame == 0)
				continue;
			
			if((MyBotModule.Broodwar.getFrameCount() > expireFrame)) {
//				MyBotModule.Broodwar.sendText("detailStrategy disabled : " + CombatStrategyDetail.values()[i].toString());
				detailStrategyExFrame[i] = 0;
			}
		}
	}

	private boolean ScoutDefenseNeeded;
	int FastZerglingsInOurBase;
	int FastZealotInOurBase;
	int ScoutDefenseNeededMin;
	
	private static CombatManager instance = new CombatManager();
	
	private CombatManager() {
		ScoutDefenseNeeded = true;
		FastZerglingsInOurBase = 0;
		FastZealotInOurBase = 0;
		ScoutDefenseNeededMin =0;
	}
	
	/// static singleton 객체를 리턴합니다
	public static CombatManager Instance() {
		return instance;
	}

	public void initSquads() {
		
		combatStrategy = CombatStrategy.DEFENCE_INSIDE;
		
		SquadOrder idleOrder = new SquadOrder(SquadOrderType.IDLE, MyBotModule.Broodwar.self().getStartLocation().toPosition(), Combat.IDLE_RADIUS, "Chill out");
		squadData.putSquad(new Squad(SquadName.IDLE, idleOrder, Combat.IDLE_PRIORITY));
		
		SquadOrder enemyScoutDefense = new SquadOrder(SquadOrderType.DEFEND, MyBotModule.Broodwar.self().getStartLocation().toPosition(), Combat.SCOUT_DEFENSE_RADIUS, "Get the scout");
		squadData.putSquad(new Squad(SquadName.SCOUT_DEFENSE, enemyScoutDefense, Combat.SCOUT_DEFENSE_PRIORITY));
		
		SquadOrder EarlyDefense = new SquadOrder(SquadOrderType.DEFEND, MyBotModule.Broodwar.self().getStartLocation().toPosition(), Combat.SCOUT_DEFENSE_RADIUS, "Get enemy");
		squadData.putSquad(new Squad("EarlyDefense", EarlyDefense, Combat.EARLY_DEFENSE_PRIORITY));
		
		SquadOrder attackOrder = new SquadOrder(SquadOrderType.ATTACK, getMainAttackLocation(null), Combat.ATTACK_RADIUS, "Attack enemy base");
		squadData.putSquad(new Squad(SquadName.MAIN_ATTACK, attackOrder, Combat.ATTACK_PRIORITY));

//		SquadOrder watcherOrder = new SquadOrder(SquadOrderType.WATCH, getAttackPosition(null), Combat.WATCHER_RADIUS, "Over Watcher");
//		squadData.putSquad(new Squad("Watcher", watcherOrder, Combat.WATCHER_PRIORITY));

		SquadOrder checkerOrder = new SquadOrder(SquadOrderType.CHECK, getAttackPosition(null), Combat.CHECKER_RADIUS, "Check it out");
		squadData.putSquad(new Squad(SquadName.CHECKER, checkerOrder, Combat.CHECKER_PRIORITY));
		
//		SquadOrder marineOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(null), Combat.MARINE_RADIUS, "Marine");
//		squadData.putSquad(new Squad(SquadName.MARINE, marineOrder, Combat.MARINE_PRIORITY));
		
		SquadOrder wraithOrder = new SquadOrder(SquadOrderType.ATTACK, getAttackPosition(null), Combat.WRAITH_RADIUS, "Wraith");
		squadData.putSquad(new Squad(SquadName.WRAITH, wraithOrder, Combat.WRAITH_PRIORITY));
		
		SquadOrder vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(null), Combat.VESSEL_RADIUS, "Vessel");
		squadData.putSquad(new Squad(SquadName.VESSEL, vesselOrder, Combat.VESSEL_PRIORITY));
		
		SquadOrder buildingOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(null), Combat.VESSEL_RADIUS, "Building");
		squadData.putSquad(new Squad(SquadName.BUILDING, buildingOrder, Combat.VESSEL_PRIORITY));
		
		initialized = true;
	}
	
	public void update() {
		
//		LagTest test = LagTest.startTest(true);
//		test.setDuration(0);
//		
//		int scvcount1 = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_SCV);
//		test.estimate();
//		int scvcount2 = InformationManager.Instance().getNumUnits(UnitType.Terran_SCV, InformationManager.Instance().selfPlayer);
//		test.estimate();
//		System.out.println(scvcount1 + ", " + scvcount2);
		
		
		if(ScoutDefenseNeeded){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if((unit.getType() == UnitType.Terran_Marine || unit.getType() == UnitType.Terran_Vulture) && unit.isCompleted()){
					ScoutDefenseNeeded = false;
					Squad scoutDefenseSquad = squadData.getSquad(SquadName.SCOUT_DEFENSE);
					if (!scoutDefenseSquad.isEmpty()) {
					   	scoutDefenseSquad.clear();
					}
				}
			}
		}
		
		if (!initialized) {
			initSquads();
		}
		
		combatUnits = this.getCombatUnits();
		if (combatUnits.isEmpty()) {
			return;
		}

		if (CommonUtils.executeRotation(0, 7)) {
			updateIdleSquad();
			if(ScoutDefenseNeeded){
				updateScoutDefenseSquad();
			}
			if(ScoutDefenseNeeded == false){
				
				updateBaseDefenseSquads();
			}
			gasRush = InformationManager.Instance().isGasRushed();
			//포톤러쉬 체크
			photonRush = InformationManager.Instance().isPhotonRushed();
			if(FastZerglingsInOurBase > 0 || gasRush || photonRush){
				updateEarlyDefenseSquad();
			}
			
			if(MyBotModule.Broodwar.getFrameCount() < 11000){
				updateBunker();
			}
			
			updateAttackSquads();
			
			updateWraithSquad();
			updateVesselSquad(); //AttackSquads 뒤에
			updateBuildingSquad();
			updateCheckerSquad();
			updateGuerillaSquad();
			
			SpiderMineManger.Instance().update();
			VultureTravelManager.Instance().update();
			updateDetailStrategy();
		}
		
		if (CommonUtils.executeRotation(0, 47)) {
			doComsatScan();
		}
		
		squadData.update();
	}
	
	private void updateBunker() {
		
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		if (myRegion == null || !myRegion.getCenter().isValid()) {
		    return;
		}
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
		if(enemyUnitsInRegion.size() == 0){
			return;
		}
		
		List<Unit> myUnits = MyBotModule.Broodwar.self().getUnits();
		List<Unit> bunkers = new ArrayList<Unit>();
		List<Unit> myMarines = new ArrayList<Unit>();
		for(Unit unit : myUnits){
			if(unit.getType() == UnitType.Terran_Marine && unit.isCompleted()){
				myMarines.add(unit);
			}
			if(unit.getType() == UnitType.Terran_Bunker && unit.isCompleted()){
				bunkers.add(unit);
			}
		}
		
		if(bunkers.size() == 0){
			return;
		}
		
		
		for (Unit bunker : bunkers) {
			boolean bunkerOut = true;
			if (enemyUnitsInRegion != null) {
				Unit target = closestTarget(bunker, enemyUnitsInRegion);
				if(bunker.getDistance(target) <= 160){
					bunkerOut = false;
				}
				if(bunkerOut){
					bunker.unloadAll();
				}
			}
		}
	}
		
	private Unit closestTarget(Unit bunker, List<Unit> bunkertargets) {
		int closestdistance = 999999;
		Unit closesttarget = null;

		for (Unit target : bunkertargets) {
			int temp = bunker.getDistance(target);
			if(temp < closestdistance){
				closestdistance = temp;
				closesttarget = target;
			}
		}
		return closesttarget;
	}

	private List<Unit> getCombatUnits() {
		List<Unit> combatUnits = new ArrayList<>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (CommandUtil.IsValidUnit(unit)
					&& unit.getType() != UnitType.Terran_Vulture_Spider_Mine
					&& !MicroUtils.isUnitContainedInUnitSet(unit, combatUnits)
					&& (CommandUtil.IsCombatUnit(unit) || unit.getType().isWorker())) {
				MicroUtils.addUnitToUnitSet(unit, combatUnits);
			}
		}
		return combatUnits;
	}
	
	private void doComsatScan() {
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) == 0) {
			return;
		}
		
		// 상대 클록 유닛
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid() && unit.isFlying() ==false) {
				if(InformationManager.Instance().enemyRace == Race.Protoss){
					if(unit.isFlying() && RespondToStrategy.Instance().enemy_arbiter == false){
						continue;
					}
				}
				//주위에 베슬이 있는지 확인하고 베슬이 여기로 오는 로직인지도 확인한 후에 오게 되면 패스 아니면 스캔으로 넘어간다
				List<Unit> nearvessel = MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Science_Vessel.sightRange()*2, true, false, UnitType.Terran_Science_Vessel);
				if(nearvessel!= null){
					Unit neareasetvessel = null;
					int closestDistToVessel = 100000;
					for (Unit vessel : nearvessel) {
						if(vessel.isStasised() || vessel.isLockedDown()){
							continue;
						}
						int tempdist = unit.getDistance(vessel);
						if(tempdist < closestDistToVessel){
							neareasetvessel = vessel;
							closestDistToVessel = tempdist;
						}
					}
					if(neareasetvessel !=null){
						List<Unit> nearallies = MapGrid.Instance().getUnitsNear(neareasetvessel.getPosition(), UnitType.Terran_Science_Vessel.sightRange(), true, false, null);
						if(nearallies !=null && nearallies.size() > 2){
							break;//베슬이 올것으로 예상됨
						}
					}
				}
				
				if(InformationManager.Instance().enemyRace == Race.Protoss){
					List<Unit> myUnits = MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Vulture.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Vulture);
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Goliath.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Goliath));
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Siege_Tank_Siege_Mode));
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Siege_Tank_Tank_Mode));
					int faccnt = 0;
					for(Unit facunit : myUnits){
						if(facunit.getType() == UnitType.Terran_Vulture||facunit.getType() == UnitType.Terran_Goliath||facunit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || facunit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
							faccnt++;
						}
					}
					if(faccnt > 2){
						MicroUtils.smartScan(unit.getPosition());
						return;
					}
				}else if(InformationManager.Instance().enemyRace == Race.Terran){
					List<Unit> myUnits = MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Wraith.sightRange(), true, false, UnitType.Terran_Goliath);
					if(myUnits.size() > 1){
						MicroUtils.smartScan(unit.getPosition());
						return;
					}
				}else{
					List<Unit> myUnits = MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Vulture.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Vulture);
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Goliath.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Goliath));
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Siege_Tank_Siege_Mode));
					myUnits.addAll(MapGrid.Instance().getUnitsNear(unit.getPosition(), UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange()+2, true, false, UnitType.Terran_Siege_Tank_Tank_Mode));
					
					int faccnt = 0;
					for(Unit facunit : myUnits){
						if(facunit.getType() == UnitType.Terran_Vulture||facunit.getType() == UnitType.Terran_Goliath){
							faccnt++;
						}
						if(facunit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || facunit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
							faccnt = 10;
						}
					}
					if(faccnt > 4){
						MicroUtils.smartScan(unit.getPosition());
						return;
					}
				}
			}
		}
		
		Unit comsat = null;
		
//		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Comsat_Station &&	unit.getEnergy() == 200) {
//				comsat = unit;
//			}
//		}
//		if (comsat != null) {
//			
//			Player enemyPlayer = MyBotModule.Broodwar.enemy();
//			Player selfPlayer = MyBotModule.Broodwar.enemy();
//			//find place
//			List<TilePosition> scanArea = new ArrayList<TilePosition>();
//			
////			if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
////				scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
////			}
////			if(InformationManager.Instance().getFirstChokePoint(enemyPlayer)!=null){
////				scanArea.add(InformationManager.Instance().getFirstChokePoint(enemyPlayer).getCenter().toTilePosition());
////			}
////			if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
////				scanArea.add(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
////			}
////			if(InformationManager.Instance().getSecondChokePoint(enemyPlayer)!=null){
////				scanArea.add(InformationManager.Instance().getSecondChokePoint(enemyPlayer).getCenter().toTilePosition());
////			}
////			if(InformationManager.Instance().getSecondChokePoint(selfPlayer)!=null){
////				scanArea.add(InformationManager.Instance().getSecondChokePoint(selfPlayer).getCenter().toTilePosition());
////			}
//			
//			
//			TilePosition target = null;
//			int scantime = 1000000;
//			if(scanArea!= null){
//				for (TilePosition scans : scanArea) {
//					if(MyBotModule.Broodwar.isVisible(scans)){
//						continue;
//					}
//					int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
//					if(scantime > tempscantime){
//						target = scans;
//						scantime = tempscantime;
//					}
//				}
//			}		
//			
//			if(target!= null){
//				MapGrid.Instance().scanAtPosition(target.toPosition());
//				comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
//			}
//		}
		
		
		//저그전 특이사항
		if(InformationManager.Instance().enemyRace == Race.Zerg){
			
			int energy = 50;
			if(RespondToStrategy.Instance().enemy_lurker == true){
				energy = 150;
			}
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Comsat_Station &&	unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {
			
				Player enemyPlayer = MyBotModule.Broodwar.enemy();
				//find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();
				
				if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}
				
				TilePosition target = null;
				int scantime = 1000000;
				if(scanArea.size() > 0){
					for (TilePosition scans : scanArea) {
						if(MyBotModule.Broodwar.isVisible(scans)){
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if(scantime > tempscantime){
							target = scans;
							scantime = tempscantime;
						}
					}
				}		
				if(target!= null){
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}
				
			}
		}
		
		//폴토전 특이사항
		if(InformationManager.Instance().enemyRace == Race.Protoss){
			
			int energy = 50;
			if(RespondToStrategy.Instance().enemy_dark_templar == true || RespondToStrategy.Instance().enemy_arbiter == true ){
				energy = 150;
			}
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Comsat_Station &&	unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {
			
				Player enemyPlayer = MyBotModule.Broodwar.enemy();
				//find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();
				
				if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}
				if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getFirstChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getSecondChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if(MyBotModule.Broodwar.getFrameCount() > 20000){
					if(InformationManager.Instance().getIslandBaseLocations() !=null ){
						for(BaseLocation islands : InformationManager.Instance().getIslandBaseLocations()){
							scanArea.add(islands.getTilePosition());
						}
					}
				}
				TilePosition target = null;
				int scantime = 1000000;
				if(scanArea.size() > 0){
					for (TilePosition scans : scanArea) {
						if(MyBotModule.Broodwar.isVisible(scans)){
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if(scantime > tempscantime){
							target = scans;
							scantime = tempscantime;
						}
					}
				}		
				if(target!= null){
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}
				
			}
		}
				
		//테란 특이사항
		if(InformationManager.Instance().enemyRace == Race.Terran){
			
			int energy = 50;
			if(RespondToStrategy.Instance().enemy_wraith == true){
				energy = 150;
			}
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Comsat_Station &&	unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {
			
				Player enemyPlayer = MyBotModule.Broodwar.enemy();
				//find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();
				
				if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}
				if(InformationManager.Instance().getMainBaseLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getFirstChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if(InformationManager.Instance().getFirstExpansionLocation(enemyPlayer)!=null){
					scanArea.add(InformationManager.Instance().getSecondChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if(MyBotModule.Broodwar.getFrameCount() > 20000){
					if(InformationManager.Instance().getIslandBaseLocations() !=null ){
						for(BaseLocation islands : InformationManager.Instance().getIslandBaseLocations()){
							scanArea.add(islands.getTilePosition());
						}
					}
				}
				TilePosition target = null;
				int scantime = 1000000;
				if(scanArea.size() > 0){
					for (TilePosition scans : scanArea) {
						if(MyBotModule.Broodwar.isVisible(scans)){
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if(scantime > tempscantime){
							target = scans;
							scantime = tempscantime;
						}
					}
				}		
				if(target!= null){
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}
			}
		}
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
				
	}
	
	public Position getMainAttackLocation(Squad squad) {
		
//		combatStrategy = CombatStrategy.DEFENCE_INSIDE;
//		if (combatStrategy == CombatStrategy.DEFENCE_INSIDE) {
//			BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//			BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//			List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
//			for (BaseLocation occupied : occupiedBases) {
//				if (occupied == firstExpansion) {
//					base = firstExpansion;
//					break;
//				}
//			}
//			mainAttackLocation = base.getPosition();

		Position mainAttackLocation = null;
		if (combatStrategy == CombatStrategy.DEFENCE_INSIDE
				|| combatStrategy == CombatStrategy.DEFENCE_CHOKEPOINT) {
//			Position pos1 = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
//			Position pos2 = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
//			System.out.println("pos1 : " + pos1);
//			System.out.println("pos2 : " + pos2);
			// 조이기 전략 -> tighteningPoint 위치가 잘 안잡히는 현상으로 readyToAttackPostion으로 임시 사용한다.
			int tankCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode);
			
			int goliathCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath);
			if (MicroSet.Common.versusMechanicSet() && tankCount + goliathCount >= MicroSet.Common.DEFENSE_READY_TO_ATTACK_SIZE_TERRAN) {
				mainAttackLocation = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
				
			} else if (tankCount + goliathCount >= MicroSet.Common.DEFENSE_READY_TO_ATTACK_SIZE) { // 병력이 모였으면 좀더 전진된 위치(정찰, 확장, 공격 용이)
				mainAttackLocation = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
				
			} else if (tankCount + goliathCount >= MicroSet.Common.DEFENSE_SECONDCHOKE_SIZE || InformationManager.Instance().enemyRace == Race.Terran) {
				Chokepoint secondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
				mainAttackLocation = secondChoke.getCenter(); // 병력이 좀 모였거나, 상대가 테란이면 secondChoke
				
			} else {
				mainAttackLocation = getDefensePosition(squad);
			}
			
//		} else if (combatStrategy == CombatStrategy.READY_TO_ATTACK) { // 헌터에서 사용하면 위치에 따라 꼬일 수 있을듯(수정햇다)
//			mainAttackLocation = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
//			
		} else { //if (combatStrategy == CombatStrategy.ATTACK_ENEMY) {
			mainAttackLocation = getAttackPosition(squad);
		}
		
		return mainAttackLocation;
	}
	
	
	private Position getDefensePosition(Squad squad) {
		Position defensePosition = null;
		Chokepoint firstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
		List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
		for (BaseLocation occupied : occupiedBases) {
			if (occupied == firstExpansion) {
				Chokepoint secondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
				if (InformationManager.Instance().enemyRace == Race.Terran) {
					defensePosition = secondChoke.getCenter();
				} else {
					double distanceFromFirstChoke = firstChoke.getDistance(secondChoke);
					double distanceFromExpansion = firstExpansion.getDistance(secondChoke);
					if (distanceFromFirstChoke < distanceFromExpansion) {
						defensePosition = firstChoke.getCenter();
					} else {
						int x = (firstChoke.getX() + firstExpansion.getX()) / 2;
						int y = (firstChoke.getY() + firstExpansion.getY()) / 2;
						defensePosition = new Position(x, y);
					}
				}
				break;
			}
		}
		if (defensePosition == null) {
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			
			Pair<Position, Position> pairPosition = firstChoke.getSides();
			Position p1 = new Position(pairPosition.first.getX(), pairPosition.second.getY());
			Position p2 = new Position(pairPosition.second.getX(), pairPosition.first.getY());
			
			if (p1.isValid() && BWTA.getRegion(p1) == myBase.getRegion()) {
				return p1;
			} else if (p2.isValid() && BWTA.getRegion(p2) == myBase.getRegion()) {
				return p2;
			} else {
				return firstChoke.getCenter();
			}
		}
		return defensePosition;
	}
	
	private Position getAttackPosition(Squad squad) {
		// 공격포지션
		if (squad != null) {
			boolean enemyBaseDestoryed = false;
			BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
			if (MyBotModule.Broodwar.isVisible(enemyBaseLocation.getTilePosition())) {
				List<Unit> commands = null;
				if (InformationManager.Instance().enemyRace != Race.Zerg) {
					int commandCount = 0;
					commands = MapGrid.Instance().getUnitsNear(enemyBaseLocation.getPosition(), 300, false, true, null);
					for (Unit command : commands) {
						if (command.getType() == UnitType.Zerg_Hatchery
								|| command.getType() == UnitType.Zerg_Lair
								|| command.getType() == UnitType.Zerg_Hive) {
							commandCount++;
							break;
						}
					}
					if (commandCount == 0) {
						enemyBaseDestoryed = true;
					}
					
				} else {
					commands = MapGrid.Instance().getUnitsNear(enemyBaseLocation.getPosition(), 300, false, true
							, InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));
					if (commands.size() == 0) {
						enemyBaseDestoryed = true;
					}
				}
			}
			
			if (!enemyBaseDestoryed) {
				if (getDetailStrategyFrame(CombatStrategyDetail.NO_WAITING_CHOKE) > 0) {
					return enemyBaseLocation.getPosition();
				}
				
				Position nextChokePosition = getNextChokePosition(squad);
				if (nextChokePosition != null) {
					return nextChokePosition;
				}
			}
		}
		
		// 적 건물
		Map<Integer, UnitInfo> unitAndUnitInfoMap = InformationManager.Instance().getUnitAndUnitInfoMap(MyBotModule.Broodwar.enemy());
		for (Integer unitId : unitAndUnitInfoMap.keySet()) {
			UnitInfo ui = unitAndUnitInfoMap.get(unitId);

			if (ui.getType().isBuilding() && ui.getLastPosition() != Position.None) {
				return ui.getLastPosition();
			}
		}

		// 적 유닛
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.getType() == UnitType.Zerg_Larva || !CommandUtil.IsValidUnit(unit, false, true) || !unit.isVisible()) {
				continue;
			}
			return unit.getPosition();
		}

		// 쥐
		return letsFindRat();
	}
	
	// 쥐새끼를 찾자
	public Position letsFindRat() {
		// starting location 중에서 탐험되지 않은 지역
		List<BaseLocation> bases = InformationManager.Instance().getMapSpecificInformation().getStartingBaseLocation();
		for (BaseLocation base : bases) {
			if (!MyBotModule.Broodwar.isExplored(base.getTilePosition())) {
				return base.getPosition();
			}
		}

		// 앞마당 지역
		BaseLocation enemyExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		if (enemyExpansion != null && !MyBotModule.Broodwar.isExplored(enemyExpansion.getTilePosition())) {
			return enemyExpansion.getPosition();
		}
		
		// 제 3멀티 중에서 탐험되지 않은 지역
		List<BaseLocation> otherExpansion = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		if (otherExpansion != null && !otherExpansion.isEmpty()) {
			for (BaseLocation base : otherExpansion) {
				if (!MyBotModule.Broodwar.isExplored(base.getTilePosition())) {
					return base.getPosition();
				}
			}
		}
		
		return new Position(2222, 2222);
	}
	
	private Position getNextChokePosition(Squad squad) {
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Chokepoint enemyFirstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
	    
		if (squad.getName().equals(SquadName.MAIN_ATTACK) && enemyBaseLocation != null && enemyFirstChoke != null) {
			if (squad.getUnitSet().isEmpty()) {
				currTargetChoke = null;
				currTargetChokeExpiredFrame = 0;
				return null;
			}

		    Position squadPosition = MicroUtils.centerOfUnits(squad.getUnitSet());
		    Position enemyFirstChokePosition = enemyFirstChoke.getCenter();
		    if (currTargetChokeExpiredFrame != 0
		    		&& currTargetChokeExpiredFrame < MyBotModule.Broodwar.getFrameCount()) { // 적의 first chokePoint 도착
		    	return enemyBaseLocation.getPosition(); // TODO 언덕 위로 올라가는 것에 대한 판단. 상대의 주력병력을 소모시키전에 언덕위로 진입하는 것은 위험할 수 있다.
		    }

			// 현재 주력병력이 어느 chokepoint에 위치해있는가?(가까운 chokepoint)
		    if (currTargetChoke == null) {
		    	Chokepoint closestChoke = null;
				double closestDist = 999999;
			    for (Chokepoint choke : BWTA.getChokepoints()) {
			    	double distToChoke = squadPosition.getDistance(choke.getCenter());
			    	if (distToChoke < closestDist) {
			    		closestChoke = choke;
			    		closestDist = distToChoke;
			    	}
			    }
			    currTargetChoke = closestChoke;
		    }

		    // 현재의 chokepoint에 도착했다고 판단되면 next chokepoint를 찾는다.
		    // next chokepoint는 최단거리 상에 있는 가장 가까운 chokepoint이다.
		    boolean nextChokeFind = false;
		    if (currTargetChokeExpiredFrame < MyBotModule.Broodwar.getFrameCount()) {
		    	nextChokeFind = true;
		    }
		    
		    if (nextChokeFind) {
	    		if (squadPosition.equals(currTargetChoke)) {
			    	return null;
			    }
		    	
		    	int chokeToEnemyChoke = MapTools.Instance().getGroundDistance(currTargetChoke.getCenter(), enemyFirstChokePosition); // 현재chokepoint ~ 목적지chokepoint
		    	
		    	Chokepoint nextChoke = null;
				int closestChokeToNextChoke = 999999;
		    	for (Chokepoint choke : BWTA.getChokepoints()) {
		    		if (choke.equals(currTargetChoke)) {
		    			continue;
		    		}
		    		int chokeToNextChoke = MapTools.Instance().getGroundDistance(currTargetChoke.getCenter(), choke.getCenter()); // 현재chokepoint ~ 다음chokepoint
		    		int nextChokeToEnemyChoke = MapTools.Instance().getGroundDistance(choke.getCenter(), enemyFirstChokePosition); // 다음chokepoint ~ 목적지chokepoint
		    		if (chokeToNextChoke + nextChokeToEnemyChoke < chokeToEnemyChoke + 10 && chokeToNextChoke > 10 && chokeToNextChoke < closestChokeToNextChoke) { // 최단거리 오차범위 10 * 32
		    			nextChoke = choke;
		    			closestChokeToNextChoke = chokeToNextChoke;
		    		}
			    }
		    	if (nextChoke != null) {
		    		currTargetChoke = nextChoke;
		    		currTargetChokeExpiredFrame = MyBotModule.Broodwar.getFrameCount() + (int) (closestChokeToNextChoke * 24.0 * getWaitingPeriod());
		    	}
		    }
		}
		if (currTargetChoke != null) {
			return currTargetChoke.getCenter();
		} else {
			return null;
		}
	}

	private void updateIdleSquad() {
		Squad squad = squadData.getSquad(SquadName.IDLE);
		
		for (Unit unit : combatUnits) {
			if (squadData.canAssignUnitToSquad(unit, squad)) {
				squadData.assignUnitToSquad(unit, squad);
			}
		}
	}


	
	public Unit getClosestMineral(Unit depot)
	{
		double bestDist = 99999;
		Unit bestMineral = null;
		
		for (Unit mineral : MyBotModule.Broodwar.getAllUnits()){
			if ((mineral.getType() == UnitType.Resource_Mineral_Field) && mineral.getDistance(depot) < 320){
				double dist = mineral.getDistance(depot);
				if (dist < bestDist){
                    bestMineral = mineral;
                    bestDist = dist;
                }
			}
		}
	    return bestMineral;
	}
	
	public Position getBestPosition(Unit depot)
	{
		if(depot == null){
			return new Position(64,64);
		}
		int x =0;
		int y =0;
		int finalx =0;
		int finaly =0;
		int minCnt = 0;
		for (Unit mineral : MyBotModule.Broodwar.getAllUnits()){
			if ((mineral.getType() == UnitType.Resource_Mineral_Field) && mineral.getDistance(depot) < 320){
				x += mineral.getPosition().getX();
				y += mineral.getPosition().getY();
				minCnt++;
			}
		}
		if(minCnt == 0){
			return depot.getPosition();
		}
		finalx = x/minCnt;
		finaly = y/minCnt;
		finalx = (finalx + depot.getPosition().getX())/2;
		finaly = (finaly + depot.getPosition().getY())/2;
		
		Position res = new Position(finalx, finaly);
	    return res;
	}	
	private void updateScoutDefenseSquad() {
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		if (myRegion == null || !myRegion.getCenter().isValid()) {
	        return;
	    }
		
		Squad scoutDefenseSquad = squadData.getSquad(SquadName.SCOUT_DEFENSE);
		
		int assignScoutDefender = 0;
		
		List<Unit> enemyUnitsInRegion = new ArrayList<>();
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
            if (unit.getType() == InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace)
            		&& BWTA.getRegion(unit.getPosition()) == myRegion) {
                enemyUnitsInRegion.add(unit);
            }
        }
		//System.out.println("enemyUnitsInRegion.size(): " + enemyUnitsInRegion.size());
		
		if(enemyUnitsInRegion.size() == 0){
			if (!scoutDefenseSquad.isEmpty()) {
			   	scoutDefenseSquad.clear();
			   	ScoutDefenseNeededMin =0;
			}
			return;
		}
			
		//3마리 이상이면 능봉으로 넘아감
		if(enemyUnitsInRegion.size() > 0){
			int enemyscvcnt = 0;
			for(Unit enemyscvs : enemyUnitsInRegion){
				if(enemyscvs.getType().isWorker()){
					enemyscvcnt++;
				}else{
					ScoutDefenseNeeded = false;
					return;
				}
				if(enemyscvs.getType().isWorker() && enemyscvs.isAttacking()){
					assignScoutDefender++;
				} 
			}
//			if(enemyscvcnt >= 3){
//				ScoutDefenseNeeded = false;
//				return;
//			}
			// 일꾼이
		}
		if(ScoutDefenseNeededMin <= assignScoutDefender){
			ScoutDefenseNeededMin = assignScoutDefender;
		}
		if(assignScoutDefender < ScoutDefenseNeededMin){
			assignScoutDefender = ScoutDefenseNeededMin;
		}
		
//		int assignScoutDefender = 0;
//		for (Unit workers : enemyUnitsInRegion) {
//			if(workers.getType().isWorker() && workers.isAttacking()){
//				assignScoutDefender++;
//			} // 일꾼이 내 지역에서 돌아다닌다.
//		}
//		boolean assignScoutDefender = enemyUnitsInRegion.size() == 1 && enemyUnitsInRegion.get(0).getType().isWorker() && enemyUnitsInRegion.get(0).isAttacking(); // 일꾼 한마리가 내 지역에서 돌아다닌다.

//		System.out.println("assignScoutDefender: " + assignScoutDefender);
//		System.out.println("assigned: " + scoutDefenseSquad.getUnitSet().size());
//		Unit defenderToAdd = findClosestDefender(scoutDefenseSquad, scoutDefenseSquad.getOrder().getPosition(), true, false);
    	
    	
    	for(Unit enemy : enemyUnitsInRegion){
    		Unit workerDefender = findClosestWorkerToTarget(enemy, false);
	        if(workerDefender == null)
	        	return;
	        if (scoutDefenseSquad.getUnitSet().size() < assignScoutDefender  && workerDefender != null) {
	        	if (squadData.canAssignUnitToSquad(workerDefender, scoutDefenseSquad)) {
	        		WorkerManager.Instance().setCombatWorker(workerDefender);
	        		squadData.assignUnitToSquad(workerDefender, scoutDefenseSquad);
	        	}
	        }
    	}
//    	while(workerDefendersNeeded < assignScoutDefender){
//	    	Unit enemyWorker = enemyUnitsInRegion.get(workerDefendersNeeded);
//	        Unit workerDefender = findClosestWorkerToTarget(enemyWorker, false);
//	        if(workerDefender == null)
//	        	return;
//	        if (scoutDefenseSquad.getUnitSet().size() < assignScoutDefender  && workerDefender != null) {
//	        	if (squadData.canAssignUnitToSquad(workerDefender, scoutDefenseSquad)) {
//	        		WorkerManager.Instance().setCombatWorker(workerDefender);
//	        		squadData.assignUnitToSquad(workerDefender, scoutDefenseSquad);
//	        	}
//	        }
//            workerDefendersNeeded++;
//    	}
    	
    	
    	if (!scoutDefenseSquad.isEmpty() && assignScoutDefender == 0) {
		   	scoutDefenseSquad.clear();
		}
	}
	private void updateEarlyDefenseSquad() {
		int photonCnt = 0;
		
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		if (myRegion == null || !myRegion.getCenter().isValid()) {
	        return;
	    }
		
		Squad earlyDefenseSquad = squadData.getSquad("EarlyDefense");
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
		if(enemyUnitsInRegion.size() == 0){
			if (!earlyDefenseSquad.isEmpty()) {
				earlyDefenseSquad.clear();
			}
			return;
		}
		
		Unit CC = null;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
		    if(unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()){
				CC = unit;
			}
		}
		
		double dist = 0;
		if(CC==null){return;}
		Position mineral = getBestPosition(CC);	
		double bestDist = 99999;
		Unit bestTarget = null;

		//포톤수 체크
		//완성된 포톤이 있으면 공격안하는걸로 
		for (Unit target : enemyUnitsInRegion){
			dist = mineral.getDistance(target);
			if (dist < bestDist){
                bestTarget = target;
                bestDist = dist;
            }
			if(photonRush){
				if (target.getType() == InformationManager.Instance().getAdvancedRushBuildingType(InformationManager.Instance().enemyRace)) {
					if(target.isCompleted()){
						photonCnt = 0;
						break;
					}else{
						photonCnt++;
					}
				}
			}
		}
		if(mineral==null){return;}
		
//		System.out.println("bestTarget: " + bestTarget.getID());
//		System.out.println("bestDist: " + bestDist);
		if(bestTarget!=null){
			if(photonRush){
				int k=0;
				//while(k< (int)(4+enemyUnitsInRegion.size()*2) ){
				for (Unit workerDefender : MyBotModule.Broodwar.self().getUnits()){
					if (workerDefender.getType() == UnitType.Terran_SCV) {
					    WorkerManager.Instance().setCombatWorker(workerDefender);
					    squadData.assignWorkerToSquad(workerDefender, earlyDefenseSquad);
					    k++;
		            }
					if(k > photonCnt*4){
						break;
					}
					
				}
				
			
			}else{ 
				if(bestDist < 250){
					int k=0;
					//while(k< (int)(4+enemyUnitsInRegion.size()*2) ){
					for (Unit workerDefender : MyBotModule.Broodwar.self().getUnits()){
						if (workerDefender.getType() == UnitType.Terran_SCV) {
						    WorkerManager.Instance().setCombatWorker(workerDefender);
						    squadData.assignWorkerToSquad(workerDefender, earlyDefenseSquad);
						    k++;
			            }
						if(gasRush){
							if(k > 3){
								break;
							}
						}else{
							int marine = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine);
							int bunker = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Bunker);
							if(marine>1 && bunker>0){
								marine+=1;
							}
							if(marine==0){
								marine = -5;
							}
							if(k > (int)((enemyUnitsInRegion.size()-marine)*2)){
								break;
							}
						}
					}
				}else if (!earlyDefenseSquad.isEmpty() && bestDist >= 240){//180) {
					earlyDefenseSquad.clear();
			    }
			}
		}
		
		if(!earlyDefenseSquad.isEmpty() && bestTarget == null){//180) {
			earlyDefenseSquad.clear();
	    }
	}
	
	private void updateBaseDefenseSquads() {
		
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Region enemyRegion = null;
		if (enemyBaseLocation != null) {
			enemyRegion = BWTA.getRegion(enemyBaseLocation.getPosition());
		}
		
		for (Region myRegion : InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer)) {
			if (myRegion == enemyRegion) { // 가스러시 예외처리
//				System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//						+ myRegion.getCenter().toTilePosition().getY() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
				continue;
			}
			
			Position regionCenter = myRegion.getCenter();
			if (!regionCenter.isValid()) {
//				System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//						+ myRegion.getCenter().toTilePosition().getY() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
				continue;
			}

			boolean tooFarToDefense = false;
			boolean doNotAssignMechanicUnit = false;
			boolean doNotAssignTankNGoliath = false;
			
			BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			if (!regionCenter.equals(BWTA.getRegion(mainBase.getPosition()).getCenter()) && regionCenter.getDistance(firstExpansion.getPosition()) >= 1200/*800*/) {
				if (centerIsOccupied(InformationManager.Instance().enemyPlayer)) {
//					System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//							+ myRegion.getCenter().toTilePosition().getY() + " "
//							+ new Exception().getStackTrace()[0].getLineNumber());
					continue;
				} else {
					tooFarToDefense = true;
//					doNotAssignTankNGoliath = true;
				}
			}
			
			// 초반 방어모드에서 앞마당 방어스쿼드를 위해 메카닉 유닛을 할당하지 않는다.
			// (왜냐하면 메인공격스쿼드의 위치가 곧 방어스쿼드의 위치이며, 메인공격스쿼드의 특성을 활용하기 위해서이다.)
			if (combatStrategy == CombatStrategy.DEFENCE_INSIDE || combatStrategy == CombatStrategy.DEFENCE_CHOKEPOINT) {
				BaseLocation expansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
				
				int tankCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
						+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode);
				
				int goliathCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath);
				
				if (tankCount + goliathCount < MicroSet.Common.DEFENSE_READY_TO_ATTACK_SIZE && expansion != null && regionCenter.equals(expansion.getRegion().getCenter())) {
//					System.out.println("do not assign mechanic units for first expansion defense");
					doNotAssignMechanicUnit = true;
				}
			}
			
			List<Unit> enemyUnitsInRegion = new ArrayList<>();
	        for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
	            if (unit.getType() == UnitType.Zerg_Overlord || //TODO 오버로드 오면 방어 안하는거 맞아?ㅡㅡ;;; 드롭도 그렇고.... 오버가 왔는데.. 무시될텐데..
					unit.getType() == UnitType.Protoss_Observer ||
					unit.isLifted()) { // floating terran building
//					System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//							+ myRegion.getCenter().toTilePosition().getY() + " "
//							+ new Exception().getStackTrace()[0].getLineNumber());
	            	continue;
	            }
	            
	            if (BWTA.getRegion(unit.getTilePosition()) == myRegion) {
	                enemyUnitsInRegion.add(unit);
	            }
	        }
	        if (tooFarToDefense && enemyUnitsInRegion.size() >= 5) {
//				System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//						+ myRegion.getCenter().toTilePosition().getY() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
	        	continue;
	        }


	    	FastZerglingsInOurBase = 0;
	    	FastZealotInOurBase = 0;
			
//	        if(InformationManager.Instance().isEarlyDefenseNeeded()){
//	        if(FastZerglingsInOurBase > 0){
//	        	for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//					if (unit.getType() == UnitType.Terran_Command_Center &&	unit.isCompleted()) {
//						regionCenter = unit.getPosition();
//					}
//				}
//	        	baseDFradius = 400;
//	        }
//	        }
	        
	        String squadName = SquadName.BASE_DEFENSE_ + regionCenter.getX() + " " + regionCenter.getY();
        	Squad squad = squadData.getSquad(squadName);
	        if (enemyUnitsInRegion.isEmpty()) {
				if (squad != null) {
	        		squad.clear();
	        	}
//				System.out.println("myRegion " + " " + myRegion.getCenter().toTilePosition().getX() + " "
//						+ myRegion.getCenter().toTilePosition().getY() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
				continue;
	        } else {
	        	if (squad == null) {
	        		SquadOrder squadOrder = new SquadOrder(SquadOrderType.DEFEND, regionCenter, Combat.BASE_DEFENSE_RADIUS, "Defend region");
	        		squadData.putSquad(new Squad(squadName, squadOrder, Combat.BASE_DEFENSE_PRIORITY));
	        	}
	        }
			int numDefendersPerEnemyUnit = 2;

			int numEnemyFlyingInRegion = 0;
			int numEnemyGroundInRegion = 0;
//			int numZerglingsInOurBase = 0;
			for (Unit unit : enemyUnitsInRegion) {
				if(unit.isFlying()) {
					numEnemyFlyingInRegion++;
				} else {
					numEnemyGroundInRegion++;
				}
				if((MyBotModule.Broodwar.getFrameCount() < 10000 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) <= 2)){
					if(unit.getType() == UnitType.Zerg_Zergling){
						FastZerglingsInOurBase++;
					}
					if(unit.getType() == UnitType.Protoss_Zealot){
						FastZealotInOurBase++;
					}
				}
			}
			
			boolean pullWorkers = false;
			
			Squad defenseSquad = squadData.getSquad(squadName);
		    int flyingDefendersNeeded = numDefendersPerEnemyUnit * numEnemyFlyingInRegion;
		    int groundDefendersNeeded = numDefendersPerEnemyUnit * numEnemyGroundInRegion;
		    
		    // Count static defense as air defenders.
//			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//				if (unit.getType() == UnitType.Terran_Missile_Turret &&
//					unit.isCompleted() && unit.isPowered() && BWTA.getRegion(unit.getTilePosition()) == myRegion) {
//					flyingDefendersNeeded -= 2;
//				}
//			}
			
			
//			System.out.println("FastZerglingsInOurBase: " + FastZerglingsInOurBase);
//			System.out.println("groundDefendersNeeded: " + groundDefendersNeeded);
			
//			if(InformationManager.Instance().isEarlyDefenseNeeded() == true){
//				updateDefenseSquadUnits(defenseSquad, flyingDefendersNeeded, groundDefendersNeeded, false);
//			}else{
			updateDefenseSquadUnits(defenseSquad, flyingDefendersNeeded, groundDefendersNeeded, pullWorkers, doNotAssignMechanicUnit, doNotAssignTankNGoliath);
//			System.out.print("squadName: " + squadName+"("+regionCenter.getX()/32+","+regionCenter.getY()/32+")" + ", " + "groundDefendersNeeded" + groundDefendersNeeded+" "+ new Exception().getStackTrace()[0].getLineNumber());
//			}
//
//			for (Unit marine : defenseSquad.getUnitSet()){
//				System.out.print(" " + marine.getType());
//			}
//			System.out.println(" END");
		}
		
	}
	
	private Unit findClosestWorkerToTarget(Unit enemyWorker, boolean early) {
		Unit closestMineralWorker = null;
		int closestDist = 1000 + 128;
		for (Unit unit : combatUnits) {
			if (unit.getType().isWorker() && !WorkerManager.Instance().isCombatWorker(unit) && (early || WorkerManager.Instance().isMineralWorker(unit))) {
				int dist = unit.getDistance(enemyWorker);
				
				if(!early){
					if (unit.isCarryingMinerals()) {
						dist += 96;
					}
				}
				if (dist < closestDist) {
					closestMineralWorker = unit;
					closestDist = dist; 
				}
			}
		}
		
		return closestMineralWorker;
	}
	
	private void updateDefenseSquadUnits(Squad defenseSquad, int flyingDefendersNeeded, int groundDefendersNeeded, boolean pullWorkers, boolean doNotAssignMechanicUnit, boolean doNotAssignTankNGoliath) {
		if (flyingDefendersNeeded <= 0 && groundDefendersNeeded <= 0) {
			defenseSquad.clear();
			return;
		}
		
		List<Unit> squadUnits = defenseSquad.getUnitSet();
		
		int flyingDefendersInSquad = 0;
		int groundDefendersInSquad = 0;
		for (Unit unit : squadUnits) {
			if (CommandUtil.CanAttackAir(unit)) {
				flyingDefendersInSquad++;
			}
			if (CommandUtil.CanAttackGround(unit)) {
				groundDefendersInSquad++;
			}
		}
		// add flying defenders if we still need them
		int flyingDefendersAdded = 0;
		while (flyingDefendersNeeded > flyingDefendersInSquad + flyingDefendersAdded) {
			Unit defenderToAdd = findClosestDefender(defenseSquad, defenseSquad.getOrder().getPosition(), true, false, doNotAssignMechanicUnit, doNotAssignTankNGoliath);

			if (defenderToAdd != null) {
				squadData.assignUnitToSquad(defenderToAdd, defenseSquad);
				flyingDefendersAdded++;
			}
			else {
				break;
			}
		}
		int groundDefendersAdded = 0;
		while (groundDefendersNeeded > groundDefendersInSquad + groundDefendersAdded) {
			Unit defenderToAdd = findClosestDefender(defenseSquad, defenseSquad.getOrder().getPosition(), false, false, doNotAssignMechanicUnit, doNotAssignTankNGoliath);

			if (defenderToAdd != null) {
				squadData.assignUnitToSquad(defenderToAdd, defenseSquad);
				groundDefendersAdded++;
			}
			else {
				break;
			}
		}
	}
	
	private void updateAttackSquads() {
	    Squad mainAttackSquad = squadData.getSquad(SquadName.MAIN_ATTACK);

		for (Unit unit : combatUnits) {
			if (unit.getType() == UnitType.Terran_Vulture || unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
					|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| unit.getType() == UnitType.Terran_Goliath) {
				if (squadData.canAssignUnitToSquad(unit, mainAttackSquad)) {
					squadData.assignUnitToSquad(unit, mainAttackSquad);// 배슬, 드랍십도 포함됨
				}
			}

			// 시즈탱크 근처에 일정양의 미사일터렛을 건설한다.
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0) {
					int build_turret_cnt = 0;
					List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(unit.getPosition(), 8 * 32);

					build_turret_cnt = 0;
					for (Unit unit2 : turretInRegion) {
						if (unit2.getType() == UnitType.Terran_Missile_Turret) {
							build_turret_cnt++;
						}
					}

					if (build_turret_cnt < 2) {
						if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,
								unit.getPosition().toTilePosition(), 20) < 1
								&& ConstructionManager.Instance().getConstructionQueueItemCountNear(
										UnitType.Terran_Missile_Turret, unit.getPosition().toTilePosition(), 20) == 0) {
							System.out.println("updateAttackSquads " + unit.getType() + "(" + build_turret_cnt + ")"
									+ "(" + unit.getTilePosition().getX() + "," + unit.getTilePosition().getY() + ") "
									+ new Exception().getStackTrace()[0].getLineNumber());

							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
									unit.getTilePosition(), true);
						}
					}
				}
			}
		}

//		int bonusRadius = (int) (Math.log(mainAttackSquad.getUnitSet().size()) * 15);
		SquadOrder mainAttackOrder = new SquadOrder(SquadOrderType.ATTACK, getMainAttackLocation(mainAttackSquad), Combat.ATTACK_RADIUS, "Attack enemy base");
	    mainAttackSquad.setOrder(mainAttackOrder);
	}
	
	private Unit findClosestDefender(Squad defenseSquad, Position pos, boolean flyingDefender, boolean pullWorkers, boolean doNotAssignMechanicUnit, boolean doNotAssignTankNGoliath) {
		Unit closestDefender = null;
		int minDistance = 99999;

		for (Unit unit : combatUnits) {
			if (doNotAssignMechanicUnit && MicroUtils.isFactoryUnit(unit.getType())) {
				continue;
			}
			if (doNotAssignTankNGoliath && (
					unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
					|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| unit.getType() == UnitType.Terran_Goliath)) {
				continue;
			}
			if ((flyingDefender && !CommandUtil.CanAttackAir(unit)) ||
				(!flyingDefender && !CommandUtil.CanAttackGround(unit))) {
	            continue;
	        }
	        if (!squadData.canAssignUnitToSquad(unit, defenseSquad)) {
	            continue;
	        }
			int dist = unit.getDistance(pos);

			// Pull workers only if requested, and not from distant bases.
			if (unit.getType().isWorker() && (!pullWorkers || dist > 1000)) {
	            continue;
	        }

			if (dist < minDistance) {
	            closestDefender = unit;
	            minDistance = dist;
	        }
		}

		return closestDefender;
	}
	
	private void updateWraithSquad() {//TODO 현재는 본진 공격 용 레이스만 있음
		Squad wraithSquad = squadData.getSquad(SquadName.WRAITH);
		
		for (Unit unit : combatUnits) {
	        if (unit.getType() == UnitType.Terran_Wraith && squadData.canAssignUnitToSquad(unit, wraithSquad)) {
				squadData.assignUnitToSquad(unit, wraithSquad);// 레이스만
	        }
	    }
		
		if(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer) != null){
			SquadOrder wraithOrder = new SquadOrder(SquadOrderType.ATTACK, InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getPoint()
					, UnitType.Terran_Wraith.sightRange(), SquadName.WRAITH);
			wraithSquad.setOrder(wraithOrder);
		}
	}
	
	private void updateVesselSquad() {//TODO 현재는 본진 공격 용 레이스만 있음
		Squad vesselSquad = squadData.getSquad(SquadName.VESSEL);
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (!CommandUtil.IsValidUnit(unit)) {
				continue;
			}
	        if (unit.getType() == UnitType.Terran_Science_Vessel && squadData.canAssignUnitToSquad(unit, vesselSquad)) {
				squadData.assignUnitToSquad(unit, vesselSquad);// 베슬만
				
				//여기서 각 유닛별 order를 지정한다. by insaneojw
				//setUnitOrder(unitId, order)
	        }
	    }

		// ############ 이 아래의 코드는 필요가 없다.(또는 default order로 사용가능) microVessel실행시 각 유닛의 order로 대체한다. ###############
		SquadOrder vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getMainAttackLocation(vesselSquad), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");

		if (combatStrategy == CombatStrategy.ATTACK_ENEMY) {
			List<Unit> units = squadData.getSquad(SquadName.MAIN_ATTACK).getUnitSet();
			if (units.size()>0) {
				Unit leader = MicroUtils.leaderOfUnit(units, getMainAttackLocation(squadData.getSquad(SquadName.MAIN_ATTACK)));
				if(leader !=null){
					vesselOrder = new SquadOrder(SquadOrderType.ATTACK, leader.getPosition(), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
				}
			}
		}
		vesselSquad.setOrder(vesselOrder);
	}
	
	private void updateBuildingSquad() {
		Squad buildingSquad = squadData.getSquad(SquadName.BUILDING);
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (!CommandUtil.IsValidUnit(unit)) {
				continue;
			}
	        if (MyBotModule.Broodwar.getFrameCount() > 12000 && unit.isLifted() && (unit.getType() == UnitType.Terran_Engineering_Bay || unit.getType() == UnitType.Terran_Barracks) && squadData.canAssignUnitToSquad(unit, buildingSquad)) {
				squadData.assignUnitToSquad(unit, buildingSquad);// 
				
				//여기서 각 유닛별 order를 지정한다. by insaneojw
				//setUnitOrder(unitId, order)
	        }
	    }

		BaseLocation temp= InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Position sight = null;
		if(temp ==null){
			sight = getMainAttackLocation(squadData.getSquad(SquadName.MAIN_ATTACK));
		}else{
			sight = temp.getPosition();
		}
		// ############ 이 아래의 코드는 필요가 없다.(또는 default order로 사용가능) microbuilding실행시 각 유닛의 order로 대체한다. ###############
		SquadOrder buildingOrder = new SquadOrder(SquadOrderType.DEFEND, sight, UnitType.Terran_Science_Vessel.sightRange(), "Building");
		
		buildingSquad.setOrder(buildingOrder);
	}
	
//	private void updateWatcherSquad() {
//	    Squad watcherSquad = squadData.getSquad("Watcher");
//
//		for (Unit unit : combatUnits) {
//			if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, watcherSquad)) {
//				squadData.assignUnitToSquad(unit, watcherSquad);
//			}
//		}
//	    
//		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//		if (enemyBase != null) {
//			SquadOrder watchOrder = new SquadOrder(SquadOrderType.WATCH, enemyBase.getPosition(), Combat.WATCHER_RADIUS, "Over Watcher");
//			watcherSquad.setOrder(watchOrder);
//		}
//	}
	
	private void updateCheckerSquad() {
		if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters
				&& InformationManager.Instance().enemyRace == Race.Terran
				&& centerIsOccupied(InformationManager.Instance().enemyPlayer)) {
			return;
		} else if (getDetailStrategyFrame(CombatStrategyDetail.NO_CHECK_NO_GUERILLA) > 0) {
			return;
		}
		
		Squad checkerSquad = squadData.getSquad(SquadName.CHECKER);

		// 모든 정찰벌처가 하나의 스파이더 마인도 가지고 있지 않다면 정찰 스쿼드를 교체한다.
		// 단, 이미 경로에서 벗어나 안정적으로 정찰을 하고 있는 벌처가 돌아오거나 전투중인 벌처를 갑자기 정찰로 빼는건 좋지 않은 영향이 있을 수 있다.
		// 그러므로 VultureTravelManager에서 교체가 가능한 타이밍을 관리한다.(정찰이 마쳐진 상황)
		if (!checkerSquad.getUnitSet().isEmpty() && VultureTravelManager.timeToShiftDuty()) {
			boolean clearSquad = true;
			for (Unit vulture : checkerSquad.getUnitSet()) {
				if (vulture.getSpiderMineCount() > 0) {
					clearSquad = false;
					break;
				}
			}
			if (clearSquad) {
				checkerSquad.clear();
				return;
			}
		}

		// checker 유닛 할당
		List<Unit> spareList = new ArrayList<>();
		for (Unit unit : combatUnits) {
			if (checkerSquad.getUnitSet().size() >= MicroSet.Vulture.maxNumChecker) {
				break;
			}
			if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, checkerSquad)) {
				if (unit.getSpiderMineCount() >= 2) {
					squadData.assignUnitToSquad(unit, checkerSquad);
				} else {
					spareList.add(unit);
				}
			}
		}
		
		for (Unit unit : spareList) {
			if (checkerSquad.getUnitSet().size() >= MicroSet.Vulture.maxNumChecker) {
				break;
			}
			squadData.assignUnitToSquad(unit, checkerSquad);
		}
		
		SquadOrder checkerOrder = new SquadOrder(SquadOrderType.CHECK, getMainAttackLocation(checkerSquad), Combat.CHECKER_RADIUS, "Check it out");
		checkerSquad.setOrder(checkerOrder);
	}
	
	private void updateGuerillaSquad() {
		if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters
				&& InformationManager.Instance().enemyRace == Race.Terran
				&& centerIsOccupied(InformationManager.Instance().enemyPlayer)) {
			return;
		} else if (getDetailStrategyFrame(CombatStrategyDetail.NO_CHECK_NO_GUERILLA) > 0) {
			return;
		}
		
		List<Unit> assignableVultures = new ArrayList<>();
		for (Unit unit : combatUnits) {
			if (unit.getType() == UnitType.Terran_Vulture) {
				Squad unitSquad = squadData.getUnitSquad(unit);
				if (unitSquad == null || Combat.GUERILLA_PRIORITY > unitSquad.getPriority()) {
					assignableVultures.add(unit);

					// 전투중일때는 최대 20%의 벌처만을 게릴라로 보낼 수 있다.
					int vultureCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
					if (combatStrategy == CombatStrategy.ATTACK_ENEMY && assignableVultures.size() > (int) (vultureCount * 0.2)) {
						break;
					}
				}
			}
		}
		int vulturePower = CombatExpectation.getVulturePower(assignableVultures);
		
		// 1. Travel Site에 대한 처리
		BaseLocation bestGuerillaSite = VultureTravelManager.Instance().getBestGuerillaSite(assignableVultures);
		if (bestGuerillaSite != null) {
			// 안개속의 적들을 상대로 계산해서 게릴라 타깃이 가능한지 확인한다.
			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(bestGuerillaSite.getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS);
			int enemyPower = CombatExpectation.enemyPowerByUnitInfo(enemiesInfo, false);
			
			if (vulturePower > enemyPower) {
				String squadName = SquadName.GUERILLA_ + bestGuerillaSite.getPosition().toString();
				Squad guerillaSquad = squadData.getSquad(squadName);
				// 게릴라 스쿼드 생성(포지션 별)
				if (guerillaSquad == null) {
	        		SquadOrder squadOrder = new SquadOrder(SquadOrderType.GUERILLA, bestGuerillaSite.getPosition(), MicroSet.Vulture.GEURILLA_RADIUS, "Let's get it on");
	        		guerillaSquad = new Squad(squadName, squadOrder, Combat.GUERILLA_PRIORITY);
	        		squadData.putSquad(guerillaSquad);
	        	}
        		// 게릴라 유닛이 남아 있다면 더 할당하지 않는다.
				if (!guerillaSquad.getUnitSet().isEmpty()) {
					VultureTravelManager.Instance().guerillaStart(squadName);
        		} else {
        			for (Unit assignableVulture : assignableVultures) {
    					squadData.assignUnitToSquad(assignableVulture, guerillaSquad);
    					int squadPower = CombatExpectation.getVulturePower(guerillaSquad.getUnitSet());
    					if (squadPower > enemyPower + MicroSet.Vulture.GEURILLA_EXTRA_POWER) { 
    						break; // 충분한 파워
    					}
    				}
        			VultureTravelManager.Instance().guerillaStart(squadName);
        		}
			}
		}
		
		// TODO 2. base가 아닌 지역에 대한 처리
		
		// 게릴라 임무 종료 조건
		// 1. 게릴라 지역이 clear 되었다.
		// 2. 현재 병력으로 이길수 없다.
		List<Squad> guerillaSquads = squadData.getSquadList(SquadName.GUERILLA_);
		for (Squad squad : guerillaSquads) {
			if (squad.getUnitSet().isEmpty()) {
				continue;
			}
			
			if (MyBotModule.Broodwar.isVisible(squad.getOrder().getPosition().toTilePosition())) {
				List<Unit> enemies = MapGrid.Instance().getUnitsNear(squad.getOrder().getPosition(), MicroSet.Vulture.GEURILLA_RADIUS, false, true, null);
				if (enemies.isEmpty()) {
					
					squad.clear();
					continue;
				}
			}
			
			List<Unit> workers = MapGrid.Instance().getUnitsNear(squad.getOrder().getPosition(), MicroSet.Vulture.GEURILLA_RADIUS, false, true, InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace));
			if (workers.isEmpty()) {
				List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(squad.getOrder().getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS, true);
				Result result = CombatExpectation.expectByUnitInfo(squad.getUnitSet(), enemiesInfo, false);
				if (result == Result.Loss) {
					
					squad.clear();
					continue;
				}

				int guerillaScore = CombatExpectation.guerillaScoreByUnitInfo(enemiesInfo);
				if (guerillaScore <= 0) {
					
					squad.clear();
					continue;
				}
			}
		}
		
	}
	
	private boolean centerIsOccupied(Player player) {
		List<UnitInfo> enemyUnitInfos = InformationManager.Instance().getNearbyForce(new Position(2048, 2048), player, 300);
		return enemyUnitInfos.size() >= 5;
	}
}

