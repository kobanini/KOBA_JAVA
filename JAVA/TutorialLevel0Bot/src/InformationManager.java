

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// 게임 상황정보 중 일부를 자체 자료구조 및 변수들에 저장하고 업데이트하는 class<br>
/// 현재 게임 상황정보는 BWAPI::Broodwar 를 조회하여 파악할 수 있지만, 과거 게임 상황정보는 BWAPI::Broodwar 를 통해 조회가 불가능하기 때문에 InformationManager에서 별도 관리하도록 합니다<br>
/// 또한, BWAPI::Broodwar 나 BWTA 등을 통해 조회할 수 있는 정보이지만 전처리 / 별도 관리하는 것이 유용한 것도 InformationManager에서 별도 관리하도록 합니다
public class InformationManager {
	private static InformationManager instance = new InformationManager();

	public Player selfPlayer;		///< 아군 Player		
	public Player enemyPlayer;		///< 적군 Player		
	public Race selfRace;			///< 아군 Player의 종족
	public Race enemyRace;			///< 적군 Player의 종족  

	private boolean ReceivingEveryMultiInfo;

	//private boolean EarlyDefenseNeeded;
	//private boolean ScoutDefenseNeeded;
	
	private boolean FirstScoutAlive;
	private boolean FirstVultureAlive;
	private boolean ScoutStart;
	private boolean VultureStart;
	
	private Unit myfirstGas;
	private Unit gasRushEnemyRefi;
	private boolean gasRushed;
	private boolean checkGasRush;
	private boolean photonRushed;
//	private int MainBaseSuppleLimit;
	private Unit FirstVulture;
	private Unit FirstCC;
	private Position firstenemyunit;
	
	/// 해당 Player의 주요 건물들이 있는 BaseLocation. <br>
	/// 처음에는 StartLocation 으로 지정. mainBaseLocation 내 모든 건물이 파괴될 경우 재지정<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();

	/// 해당 Player의 mainBaseLocation 이 변경되었는가 (firstChokePoint, secondChokePoint, firstExpansionLocation 를 재지정 했는가)
	private Map<Player, Boolean> mainBaseLocationChanged = new HashMap<Player, Boolean>();

	/// 해당 Player가 점령하고 있는 Region 이 있는 BaseLocation<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();

	/// 해당 Player가 점령하고 있는 Region<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	/// 해당 Player의 mainBaseLocation 에서 가장 가까운 ChokePoint
	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	/// 해당 Player의 mainBaseLocation 에서 가장 가까운 BaseLocation
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	/// 해당 Player의 mainBaseLocation 에서 두번째로 가까운 (firstChokePoint가 아닌) ChokePoint<br>
	/// 게임 맵에 따라서, secondChokePoint 는 일반 상식과 다른 지점이 될 수도 있습니다
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();
	private Map<Player, Chokepoint> thirdChokePointDonotUse = new HashMap<Player, Chokepoint>();
	public Position tighteningPoint = null;
	
	// 나머지 멀티 location (가까운 순으로 sorting)
	private Map<Player, List<BaseLocation>> otherExpansionLocations = new HashMap<Player, List<BaseLocation>>();
	
	/// 센터 진출로
	private Map<Player, Position> readyToAttackPosition = new HashMap<Player, Position>();
	
	private MapSpecificInformation mapSpecificInformation = null;

	/// Player - UnitData(각 Unit 과 그 Unit의 UnitInfo 를 Map 형태로 저장하는 자료구조) 를 저장하는 자료구조 객체
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	private List<BaseLocation> islandBaseLocations = new ArrayList<BaseLocation>();

	/// static singleton 객체를 리턴합니다
	public static InformationManager Instance() {
		return instance;
	}

	public InformationManager() {
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		ReceivingEveryMultiInfo = false;
//		EarlyDefenseNeeded = true;
//		ScoutDefenseNeeded = true;
		FirstScoutAlive = true;
		FirstVultureAlive = true;
		ScoutStart = false;
		VultureStart = false;
		myfirstGas = null;
		gasRushEnemyRefi = null;
		gasRushed = false;
		checkGasRush = true;
		photonRushed = false;
//		MainBaseSuppleLimit =0;
		FirstVulture = null;
		
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
			if(unit.getType() == UnitType.Terran_Command_Center && FirstCC==null){
				FirstCC = unit;
			}
		}
		
		unitData.put(selfPlayer, new UnitData());
		unitData.put(enemyPlayer, new UnitData());
		
		occupiedBaseLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		occupiedRegions.put(selfPlayer, new HashSet());
		occupiedRegions.put(enemyPlayer, new HashSet());

		mainBaseLocations.put(selfPlayer, BWTA.getStartLocation(MyBotModule.Broodwar.self()));
		mainBaseLocationChanged.put(selfPlayer, new Boolean(true));

		occupiedBaseLocations.get(selfPlayer).add(mainBaseLocations.get(selfPlayer));
		if (mainBaseLocations.get(selfPlayer) != null) {
			updateOccupiedRegions(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()),
				MyBotModule.Broodwar.self());
		}
		
//		BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
		for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
		{
//			if (!BWTA.isConnected(targetBaseLocation.getTilePosition(), sourceBaseLocation.getTilePosition())){
			if(targetBaseLocation.isIsland()){
				islandBaseLocations.add(targetBaseLocation);
			}
		}

		mainBaseLocations.put(enemyPlayer, null);
		mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		
		firstChokePoint.put(selfPlayer, null);
		firstChokePoint.put(enemyPlayer, null);
		firstExpansionLocation.put(selfPlayer, null);
		firstExpansionLocation.put(enemyPlayer, null);
		secondChokePoint.put(selfPlayer, null);
		secondChokePoint.put(enemyPlayer, null);
		thirdChokePointDonotUse.put(selfPlayer, null);
		thirdChokePointDonotUse.put(enemyPlayer, null);
		tighteningPoint = null;

		otherExpansionLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		otherExpansionLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		
		readyToAttackPosition.put(selfPlayer, null);
		readyToAttackPosition.put(enemyPlayer, null);

		updateFirstGasInformation();
		updateMapSpecificInformation();
		updateChokePointAndExpansionLocation();
//		checkTileForSupply();
	}

//	private void checkTileForSupply() {
//
//		int MainBaseSpaceForSup =0;
//		Polygon temp= getMainBaseLocation(selfPlayer).getRegion().getPolygon();
//		for(int y=0; y<128 ; y++){
//			for(int x=0; x<128 ; x++){
//				Position test2 = new Position(x*32+16,y*32+16);
//				if(temp.isInside(test2)){
//					MainBaseSpaceForSup++;
//				}
//			}
//		}
//		MainBaseSuppleLimit =  (int)((MainBaseSpaceForSup - 106)/30)+5;
//	}

	/// Unit 및 BaseLocation, ChokePoint 등에 대한 정보를 업데이트합니다
	public void update() {
		
		
		if(MyBotModule.Broodwar.getFrameCount() % 8 == 0) {
			updateUnitsInfo();
			updateCurrentStatusInfo();
		}
		// occupiedBaseLocation 이나 occupiedRegion 은 거의 안바뀌므로 자주 안해도 된다
		if (MyBotModule.Broodwar.getFrameCount() % 31 == 0) {
			updateBaseLocationInfo();
//			setEveryMultiInfo();
		}
		
		
	}

	private void updateCurrentStatusInfo() {
//		if(EarlyDefenseNeeded){
//			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//				if((unit.getType() == UnitType.Terran_Bunker || unit.getType() == UnitType.Terran_Vulture) && unit.isCompleted()){
//					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) >= 4){
//						EarlyDefenseNeeded = false;
//					}
//				}
//			}
//		}
//		private boolean ScoutDefenseNeeded;
//		if(ScoutDefenseNeeded){
//			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//				if((unit.getType() == UnitType.Terran_Marine || unit.getType() == UnitType.Terran_Bunker || unit.getType() == UnitType.Terran_Vulture) && unit.isCompleted()){
//					ScoutDefenseNeeded = false;
//				}
//			}
//			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
//				if(unit.getType().isBuilding() ==false && unit.getType().isWorker() == false){
//					ScoutDefenseNeeded = false;
//				}
//			}
//		}
			
			
		if(VultureStart == false && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) > 0){
			VultureStart = true;
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
				if(unit.getType() == UnitType.Terran_Vulture){
					FirstVulture = unit; 
					FirstVultureAlive = true;
				}
			}
		}
		if(VultureStart == true && FirstVultureAlive == true  && FirstVulture.exists() == false){
			FirstVultureAlive = false;
		}
		
		
		if(ScoutStart == false && WorkerManager.Instance().getScoutWorker() != null){
			ScoutStart = true;
		}
		if(ScoutStart == true && ((WorkerManager.Instance().getScoutWorker() != null 
				&& WorkerManager.Instance().getScoutWorker().getHitPoints() <= 0)
				|| WorkerManager.Instance().getScoutWorker() == null )){
			if(InformationManager.Instance().getEnemyUnits().size() > 0){
				firstenemyunit = InformationManager.Instance().getEnemyUnits().get(0).getLastPosition();
			}
			FirstScoutAlive = false;
		}
		
		if(checkGasRush == true){
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
				if(unit.getType() == UnitType.Terran_Refinery && unit.isCompleted() && myfirstGas !=null){
					if(myfirstGas.getPosition().equals(unit.getPosition())){
						checkGasRush = false;//가스 러쉬 위험 끝
					}
				}
			}
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == getRefineryBuildingType(enemyRace) && myfirstGas !=null){
					if(myfirstGas.getPosition().equals(unit.getPosition())){
						gasRushed = true;//가스 러쉬 당함
						gasRushEnemyRefi = unit;
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) > 0){

							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
							BuildOrderItem currentItem = null; 
							
							if (!tempbuildQueue.isEmpty()) {
								currentItem= tempbuildQueue.getHighestPriorityItem();
								while(true){
									if(currentItem.metaType.isUnit() == true && currentItem.metaType.isRefinery()){
										tempbuildQueue.removeCurrentItem();
										break;
									}else if(tempbuildQueue.canGetNextItem() == true){
										tempbuildQueue.PointToNextItem();
										currentItem = tempbuildQueue.getItem();
									}else{
										break;
									}
								}
							}
						}
					}
				}
			}
			
			if(gasRushed == true && gasRushEnemyRefi != null){
				if(gasRushEnemyRefi == null || gasRushEnemyRefi.getHitPoints() <= 0 || gasRushEnemyRefi.isTargetable() == false){
					gasRushed = false;//가스 러쉬 위험 끝
//					System.out.println("gas rush finished");
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) < 1){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
				}
			}
		}
		//10000프레임 이전까지만 포톤러쉬 확인.
		if (MyBotModule.Broodwar.getFrameCount() < 10000) {
			BaseLocation base = null;
			// 1. 본진에 적 포톤캐논이 있는지 본다.
			base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			Region myRegion = base.getRegion();
			List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
			InformationManager.Instance().enemyPlayer);
			if (enemyUnitsInRegion.size() >= 1) {
				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
					if (enemyUnitsInRegion.get(enemy).getType() == getAdvancedRushBuildingType(enemyRace)) {
						photonRushed = true;
					}
				}
			}
		}
//		private boolean GasRushed;
//		private boolean CheckGasRush;
	}

	/// 전체 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitsInfo() {
		// update our units info
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			updateUnitInfo(unit);
		}
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			updateUnitInfo(unit);
		}

		// remove bad enemy units
		if (unitData.get(enemyPlayer) != null) {
			unitData.get(enemyPlayer).removeBadUnits();
		}
		if (unitData.get(selfPlayer) != null) {
			unitData.get(selfPlayer).removeBadUnits();
		}
	}

	/// 해당 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitInfo(Unit unit) {
		try {
			if (!(unit.getPlayer() == selfPlayer || unit.getPlayer() == enemyPlayer)) {
				return;
			}

			if (enemyRace == Race.Unknown && unit.getPlayer() == enemyPlayer) {
				enemyRace = unit.getType().getRace();
			}
			
			if(unit.getPlayer() == selfPlayer && unit.getType() == UnitType.Terran_Vulture_Spider_Mine){
				return;
			}
			
			unitData.get(unit.getPlayer()).updateUnitInfo(unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitShow(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitHide(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitCreate(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitComplete(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitMorph(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitRenegade(Unit unit) { 
		updateUnitInfo(unit); 
	}
	
	/// Unit 에 대한 정보를 업데이트합니다 <br>
	/// 유닛이 파괴/사망한 경우, 해당 유닛 정보를 삭제합니다
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}

		unitData.get(unit.getPlayer()).removeUnit(unit);
	}

	
	/// 해당 Player (아군 or 적군) 의 position 주위의 유닛 목록을 unitInfo 에 저장합니다		 
	public List<UnitInfo> getNearbyForce(Position p, Player player, int radius) {
		List<UnitInfo> unitInfo = new ArrayList<>();
		getNearbyForce(unitInfo, p, player, radius, false);
		return unitInfo;
	}
		
	public List<UnitInfo> getNearbyForce(Position p, Player player, int radius, boolean allUnits) {
		List<UnitInfo> unitInfo = new ArrayList<>();
		getNearbyForce(unitInfo, p, player, radius, allUnits);
		return unitInfo;
	}
	
	public void getNearbyForce(List<UnitInfo> unitInfo, Position p, Player player, int radius) {
		getNearbyForce(unitInfo, p, player, radius, false);
	}
	
	public void getNearbyForce(List<UnitInfo> unitInfo, Position p, Player player, int radius, boolean allUnits) {
		Iterator<Integer> it = getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		
		int currFrame = MyBotModule.Broodwar.getFrameCount();
		while (it.hasNext()) {
			final UnitInfo ui = getUnitData(player).getUnitAndUnitInfoMap().get(it.next());
			if (unitInfo.contains(ui)) {
				continue;
			}

			// if it's a combat unit we care about
			// and it's finished!
			if (allUnits || ui.getType() == UnitType.Terran_Barracks
					|| ui.getType() == UnitType.Terran_Engineering_Bay
					|| (isCombatUnitType(ui.getType()) && ui.isCompleted())) {
				if (!ui.getType().isBuilding() && (currFrame - ui.getUpdateFrame()) > MicroSet.Common.NO_UNIT_FRAME(ui.getType())) {
					continue;
				}
				
				// determine its attack range
				int range = 0;
				if (ui.getType().groundWeapon() != WeaponType.None) {
					range = ui.getType().groundWeapon().maxRange() + 40;
				}

				// if it can attack into the radius we care about
				if (ui.getLastPosition().getDistance(p) <= (radius + range)) {
					// add it to the vector
					// C++ : unitInfo.push_back(ui);
					unitInfo.add(ui);
				}
			} else if (ui.getType().isDetector() && ui.getLastPosition().getDistance(p) <= (radius + 250)) {
				if (unitInfo.contains(ui)) {
					continue;
				}
				// add it to the vector
				// C++ : unitInfo.push_back(ui);
				unitInfo.add(ui);
			}
		}
	}

	/// 해당 Player (아군 or 적군) 의 해당 UnitType 유닛 숫자를 리턴합니다 (훈련/건설 중인 유닛 숫자까지 포함)
	public int getNumUnits(UnitType t, Player player) {
		return getUnitData(player).getNumUnits(t.toString());
	}

	/// 해당 Player (아군 or 적군) 의 모든 유닛 통계 UnitData 을 리턴합니다		 
	public final UnitData getUnitData(Player player) {
		return unitData.get(player);
	}

	public void updateBaseLocationInfo() {
		if (occupiedRegions.get(selfPlayer) != null) {
			occupiedRegions.get(selfPlayer).clear();
		}
		if (occupiedRegions.get(enemyPlayer) != null) {
			occupiedRegions.get(enemyPlayer).clear();
		}
		if (occupiedBaseLocations.get(selfPlayer) != null) {
			occupiedBaseLocations.get(selfPlayer).clear();
		}
		if (occupiedBaseLocations.get(enemyPlayer) != null) {
			occupiedBaseLocations.get(enemyPlayer).clear();
		}

		// enemy 의 startLocation을 아직 모르는 경우
		if (mainBaseLocations.get(enemyPlayer) == null) {
			// how many start locations have we explored
			int exploredStartLocations = 0;
			boolean enemyStartLocationFound = false;

			// an unexplored base location holder
			BaseLocation unexplored = null;

			for (BaseLocation startLocation : BWTA.getStartLocations()) {
				if (existsPlayerBuildingInRegion(BWTA.getRegion(startLocation.getTilePosition()), enemyPlayer)) {
					if (enemyStartLocationFound == false) {
						enemyStartLocationFound = true;
						mainBaseLocations.put(enemyPlayer, startLocation);
						mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));
					}
				}

				if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {
					// if it's explored, increment
					exploredStartLocations++;
				} else {
					// otherwise set it as unexplored base
					unexplored = startLocation;
				}
			}

			// if we've explored every start location except one, it's the enemy
			if (!enemyStartLocationFound && exploredStartLocations == ((int) BWTA.getStartLocations().size() - 1)) {
				enemyStartLocationFound = true;
				mainBaseLocations.put(enemyPlayer, unexplored);
				mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
				// C++ : _occupiedBaseLocations[_enemy].push_back(unexplored);
				if(occupiedBaseLocations.get(enemyPlayer) == null)
				{
					occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>()); 
				}
				occupiedBaseLocations.get(enemyPlayer).add(unexplored);
			}
		}
		
		
		
		//끝까지 상대 location 못 찾았을때
		if (mainBaseLocations.get(enemyPlayer) == null && FirstScoutAlive == false && (FirstVultureAlive ==false || MyBotModule.Broodwar.getFrameCount() >= 8500 )) {
			
			List<UnitInfo> enemyUnits = null;
			enemyUnits = InformationManager.Instance().getEnemyUnits();
			
			if(enemyUnits.size() > 0){
				for(UnitInfo fogUnit : enemyUnits){
					
					BaseLocation closestBase = null;
					if(fogUnit.getType().isBuilding() == true){
						int minimumDistance = 999999;
						for (BaseLocation startLocation : BWTA.getStartLocations()) {
							if (startLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
							if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {continue;}
							
							int dist = MapTools.Instance().getGroundDistance(fogUnit.getLastPosition(), startLocation.getPosition());
							if (dist < minimumDistance) {
								closestBase = startLocation;
								minimumDistance = dist;
							}
						}
					}
					
					if(closestBase ==null){
						int minimumDistance = 999999;
						for (BaseLocation startLocation : BWTA.getStartLocations()) {
							if (startLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
							if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {continue;}
							
							int dist = MapTools.Instance().getGroundDistance(firstenemyunit, startLocation.getPosition());
							if (dist < minimumDistance) {
								closestBase = startLocation;
								minimumDistance = dist;
							}
						}
					}
					
					mainBaseLocations.put(enemyPlayer, closestBase);
					mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));
					break;
				}
			}
		}

		// update occupied base location
		// 어떤 Base Location 에는 아군 건물, 적군 건물 모두 혼재해있어서 동시에 여러 Player 가 Occupy 하고
		// 있는 것으로 판정될 수 있다
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			if (hasBuildingAroundBaseLocation(baseLocation, enemyPlayer)) {
				// C++ : _occupiedBaseLocations[_enemy].push_back(baseLocation);
				occupiedBaseLocations.get(enemyPlayer).add(baseLocation);
			}

			if (hasBuildingAroundBaseLocation(baseLocation, selfPlayer)) {
				// C++ : _occupiedBaseLocations[_self].push_back(baseLocation);
				occupiedBaseLocations.get(selfPlayer).add(baseLocation);
			}
		}

		if (mainBaseLocations.get(enemyPlayer) != null) {
			
			// 적 MainBaseLocation 업데이트 로직 버그 수정
			// 적군의 빠른 앞마당 건물 건설 + 아군의 가장 마지막 정찰 방문의 경우, 
			// enemy의 mainBaseLocations를 방문안한 상태에서는 건물이 하나도 없다고 판단하여 mainBaseLocation 을 변경하는 현상이 발생해서
			// enemy의 mainBaseLocations을 실제 방문했었던 적이 한번은 있어야 한다라는 조건 추가.  
			if (MyBotModule.Broodwar.isExplored(mainBaseLocations.get(enemyPlayer).getTilePosition())) {
		
				if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(enemyPlayer).getTilePosition()), enemyPlayer) == false) {
					for (BaseLocation loaction : occupiedBaseLocations.get(enemyPlayer)) {
						if (existsPlayerBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()),enemyPlayer)) {
							mainBaseLocations.put(enemyPlayer, loaction);
							mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
							break;
						}
					}
				}
			}
		}

		// self의 mainBaseLocations에 대해, 그곳에 있는 건물이 모두 파괴된 경우
		// _occupiedBaseLocations 중에서 _mainBaseLocations 를 선정한다
		if (mainBaseLocations.get(selfPlayer) != null) {
			if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()), selfPlayer) == false) {
				for (BaseLocation location : occupiedBaseLocations.get(selfPlayer)) {
					if (existsPlayerBuildingInRegion(BWTA.getRegion(location.getTilePosition()), selfPlayer)) {
						mainBaseLocations.put(selfPlayer, location);
						mainBaseLocationChanged.put(selfPlayer, new Boolean(true));				
						break;
					}
				}
			}
		}

		Iterator<Integer> it = null;
		if (unitData.get(enemyPlayer) != null) {
			it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each enemy building unit we know about
			// for (const auto & kv : unitData.get(enemy).getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.enemy());
				}
			}
		}

		if (unitData.get(selfPlayer) != null) {
			it = unitData.get(selfPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each of our building units
			// for (const auto & kv : _unitData[_self].getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(selfPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					if (CommandUtil.IsValidUnit(ui.getUnit())
							&& ui.getUnit().isLifted()
							&& (ui.getType() == UnitType.Terran_Barracks || ui.getType() == UnitType.Terran_Engineering_Bay)) {
						continue;
					}
					
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.self());
				}
			}
		}

		updateChokePointAndExpansionLocation();
	}
	
	public List<UnitInfo> getEnemyUnits(){
		return getEnemyUnits(null);
	}
	public List<UnitInfo> getEnemyUnits(UnitType type)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				if(type == null){
					units.add(ui);
				}else if(type == ui.getType()){
					units.add(ui);
				}
			}
		}
		
		return units;
	}
	
	public List<UnitInfo> getEnemyUnitsNear(Unit myunit, int radius, boolean ground, boolean air)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				if(myunit.getDistance(ui.getLastPosition()) > radius){
					continue;
				}
				if(ui.getType().isBuilding()){
					if(ground){
						if(ui.getType().groundWeapon() != WeaponType.None){
							units.add(ui);
						}
					}
					if(air){
						if(ui.getType().airWeapon() != WeaponType.None){
							units.add(ui);
						}
					}
				}
			}
		}
		
		return units;
	}
	
	public List<UnitInfo> getEnemyBuildingUnitsNear(Unit myunit, int radius, boolean canAttack, boolean ground, boolean air)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				
				if(ui.getLastPosition() == Position.None){
					continue;
				}
				if(myunit.getDistance(ui.getLastPosition()) > radius){
					continue;
				}
				if(ui.getType().isBuilding()){
					if (canAttack != true){
						units.add(ui);
					}else{
						if(ground){
							if(ui.getType().groundWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
						if(air){
							if(ui.getType().airWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
					}
				}
			}
		}
		
		return units;
	}
	
	public List<UnitInfo> getEnemyBuildingUnitsNear(Position myunit, int radius, boolean canAttack, boolean ground, boolean air)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				
				if(ui.getLastPosition() == Position.None){
					continue;
				}
				if(myunit.getDistance(ui.getLastPosition()) > radius){
					continue;
				}
				if(ui.getType().isBuilding()){
					if (canAttack != true){
						units.add(ui);
					}else{
						if(ground){
							if(ui.getType().groundWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
						if(air){
							if(ui.getType().airWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
					}
				}
			}
		}
		
		return units;
	}
	
	

//	public void setEveryMultiInfo() {
//		
//		if (mainBaseLocations.get(selfPlayer) != null && mainBaseLocations.get(enemyPlayer) != null) {
//			BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
//			for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
//			{
//				if (!BWTA.isConnected(targetBaseLocation.getTilePosition(), sourceBaseLocation.getTilePosition())) continue;
//				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
//				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
//				//적군 베이스도 아닐때
//				if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,10) == true) continue;
//				
////				occupiedBaseLocations.
//				
////				System.out.print("targetBaseLocationX : " + targetBaseLocation.getTilePosition().getX());
////				System.out.println(", targetBaseLocationY : " + targetBaseLocation.getTilePosition().getY());
////				
////				System.out.println("getduration: "+ MapGrid.Instance().getCellLastVisitDuration(targetBaseLocation.getPosition()));
//				if (MapGrid.Instance().getCellLastVisitDuration(targetBaseLocation.getPosition()) > 8000)
//				{
//					ReceivingEveryMultiInfo = false;
////					System.out.println("ReceivingEveryMultiInfo1: " + ReceivingEveryMultiInfo);
//					return;
//				}
//			}
//			ReceivingEveryMultiInfo = true;
////			System.out.println("ReceivingEveryMultiInfo2: " + ReceivingEveryMultiInfo);
//		}else{
//			ReceivingEveryMultiInfo = false;
////			System.out.println("ReceivingEveryMultiInfo3: " + ReceivingEveryMultiInfo);
//		}
//	}
	public BaseLocation getNextExpansionLocation() {
		
		BaseLocation res = null;
	
		if (mainBaseLocations.get(selfPlayer) != null && firstExpansionLocation.get(selfPlayer) != null && mainBaseLocations.get(enemyPlayer) != null) {
			BaseLocation sourceBaseLocation = firstExpansionLocation.get(selfPlayer);
			BaseLocation enemyBaseLocation = mainBaseLocations.get(enemyPlayer);
			
			double tempDistance;
			double sourceDistance;
			double closestDistance = 1000000000;
			
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center)%2 == 0){
			
				for (BaseLocation targetBaseLocation : BWTA.getStartLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,selfPlayer,6) == true) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,6) == true) continue;
					
//					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(targetBaseLocation.getTilePosition());
//					if(findGeyser != null){
//						if (findGeyser.getDistance(targetBaseLocation.getTilePosition())*32 > 300){
//							continue;
//						}
//					}
//					
					sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
					tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
					
					if (tempDistance < closestDistance && sourceDistance > 0) {
						closestDistance = tempDistance;
						res = targetBaseLocation;
					}
				}
			}else{
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
					if (firstExpansionLocation.get(enemyPlayer) != null){
						if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(enemyPlayer).getTilePosition())) continue;
					}
					if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(selfPlayer).getTilePosition())) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,selfPlayer,6) == true) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,6) == true) continue;
					
					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(targetBaseLocation.getTilePosition());
					if(findGeyser != null){
						if (findGeyser.getDistance(targetBaseLocation.getTilePosition())*32 > 300){
							continue;
						}
					}
					
					sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
					tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
					
					if (tempDistance < closestDistance && sourceDistance > 0) {
						closestDistance = tempDistance;
						res = targetBaseLocation;
					}
				}
			}
			if(res ==null){
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
					if (firstExpansionLocation.get(enemyPlayer) != null){
						if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(enemyPlayer).getTilePosition())) continue;
					}
					if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(selfPlayer).getTilePosition())) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,selfPlayer,6) == true) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,6) == true) continue;
					
					sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
					tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
					
					if (tempDistance < closestDistance && sourceDistance > 0) {
						closestDistance = tempDistance;
						res = targetBaseLocation;
					}
				}
			}
			
		}
		if(res ==null){
			if (mainBaseLocations.get(selfPlayer) != null && firstExpansionLocation.get(selfPlayer) != null) {
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
					//if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
					if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(selfPlayer).getTilePosition())) continue;
					if (hasBuildingAroundBaseLocation(targetBaseLocation,selfPlayer,6) == true) continue;
					
					res = targetBaseLocation;
				}
			}
			
		}
		return res;
	}
	

	public TilePosition getLastBuilingLocation() {
		
		TilePosition res = null;
		
		BaseLocation mainBaseLocation = mainBaseLocations.get(selfPlayer);
		BaseLocation sourceBaseLocation = firstExpansionLocation.get(selfPlayer);
		List<BaseLocation> occupiedBaseLocation = occupiedBaseLocations.get(selfPlayer);
		BaseLocation enemyBaseLocation = mainBaseLocations.get(enemyPlayer);
		
		double tempDistance =0;
		double sourceDistance =0 ;
		double closestDistance = 1000000000;
		
		for (BaseLocation targetBaseLocation : occupiedBaseLocation)
		{
			if(targetBaseLocation.isStartLocation() == false) continue;
			if (targetBaseLocation.getTilePosition().equals(mainBaseLocation.getTilePosition())) continue;
			if (targetBaseLocation.getTilePosition().equals(enemyBaseLocation.getTilePosition())) continue;
				
			sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
			tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
			
			if (tempDistance < closestDistance && sourceDistance > 0) {
				closestDistance = tempDistance;
				res = targetBaseLocation.getTilePosition();
			}
		}
		
		return res;
	}
	
	public TilePosition getLastBuilingFinalLocation() {
		
		TilePosition res = null;
		
		BaseLocation mainBaseLocation = mainBaseLocations.get(selfPlayer);
		BaseLocation sourceBaseLocation = firstExpansionLocation.get(selfPlayer);
		BaseLocation enemyBaseLocation = mainBaseLocations.get(enemyPlayer);
		
		double tempDistance =0;
		double sourceDistance =0 ;
		double closestDistance = 1000000000;
		
		for (BaseLocation targetBaseLocation : BWTA.getStartLocations())
		{
			if (targetBaseLocation.getTilePosition().equals(mainBaseLocation.getTilePosition())) continue;
			if (targetBaseLocation.getTilePosition().equals(enemyBaseLocation.getTilePosition())) continue;
			
			sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
			tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
			
			if (tempDistance < closestDistance && sourceDistance > 0) {
				closestDistance = tempDistance;
				res = targetBaseLocation.getRegion().getCenter().toTilePosition();
			}
			
			res = targetBaseLocation.getRegion().getCenter().toTilePosition();
		}
		
		return res;
	}



	public void updateChokePointAndExpansionLocation() {
		
		Position Center = new Position(2048,2048);
		if (mainBaseLocationChanged.get(selfPlayer).booleanValue() == true) {
		
			if (mainBaseLocations.get(selfPlayer) != null) {
				BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
	
				firstChokePoint.put(selfPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));
							
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
	
					tempDistance = MapTools.Instance().getGroundDistance(sourceBaseLocation.getPosition(), targetBaseLocation.getPosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(selfPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(selfPlayer).getCenter())) continue;
	
					tempDistance = MapTools.Instance().getGroundDistance(sourceBaseLocation.getPosition(), chokepoint.getPoint())*1.1;
					tempDistance += MapTools.Instance().getGroundDistance(Center, chokepoint.getPoint());
//					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition()); //욱스가 주석 남기라고 함
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						thirdChokePointDonotUse.put(selfPlayer, secondChokePoint.get(selfPlayer));
						secondChokePoint.put(selfPlayer, chokepoint);
					}
				}
				//헌트 특이사항
				if(mapSpecificInformation.getMap() == MAP.TheHunters){
					if(FirstCC.getTilePosition().getX() == 114 && FirstCC.getTilePosition().getY() == 80){	
//							startingX == 114 && startingY == 80
//							BlockingEntrance.Instance().getStartingInt() == 3){		
						firstChokePoint.put(selfPlayer,  secondChokePoint.get(selfPlayer));
						secondChokePoint.put(selfPlayer,  thirdChokePointDonotUse.get(selfPlayer));
					}
				}
				this.updateOtherExpansionLocation(sourceBaseLocation);
			}
			mainBaseLocationChanged.put(selfPlayer, new Boolean(false));
		}
		
		if (mainBaseLocationChanged.get(enemyPlayer).booleanValue() == true) {
	
			if (mainBaseLocations.get(enemyPlayer) != null && mainBaseLocations.get(selfPlayer) != null) {
				BaseLocation enemySourceBaseLocation = mainBaseLocations.get(enemyPlayer);
				BaseLocation mySourceBaseLocation = mainBaseLocations.get(selfPlayer);
				
				firstChokePoint.put(enemyPlayer, BWTA.getNearestChokepoint(enemySourceBaseLocation.getTilePosition()));
				
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
	
					tempDistance = MapTools.Instance().getGroundDistance(enemySourceBaseLocation.getPosition(), targetBaseLocation.getPosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(enemyPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(enemyPlayer).getCenter())) continue;
	
					tempDistance = MapTools.Instance().getGroundDistance(enemySourceBaseLocation.getPosition(), chokepoint.getPoint())*1.1;
					tempDistance += MapTools.Instance().getGroundDistance(Center, chokepoint.getPoint());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						thirdChokePointDonotUse.put(enemyPlayer, secondChokePoint.get(enemyPlayer));
						secondChokePoint.put(enemyPlayer, chokepoint);
					}
				}
				//헌트 특이사항
				if(mapSpecificInformation.getMap() == MAP.TheHunters){
					
					if(FirstCC.getTilePosition().getX() == 114 && FirstCC.getTilePosition().getY() == 80){	
						firstChokePoint.put(enemyPlayer,  secondChokePoint.get(enemyPlayer));
						secondChokePoint.put(enemyPlayer,  thirdChokePointDonotUse.get(enemyPlayer));
					}
				}
					
				double tempDistanceFromSelf;
				double tempDistanceFromEnemy;
				double tempDistanceForHunter =0;
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					tempDistanceFromSelf = MapTools.Instance().getGroundDistance(mySourceBaseLocation.getPosition(), chokepoint.getPoint());
					tempDistanceFromEnemy = MapTools.Instance().getGroundDistance(enemySourceBaseLocation.getPosition(), chokepoint.getPoint());
//						tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition()); //욱스가 주석 남기라고 함
					if (tempDistanceForHunter < closestDistance && tempDistanceFromEnemy-tempDistanceFromSelf > 0) {
						closestDistance = tempDistanceForHunter;
						tighteningPoint = chokepoint.getCenter();
					}
				}
				this.updateReadyToAttackPosition();
				this.updateOtherExpansionLocation(enemySourceBaseLocation);
			}
			mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		}
	}
	
	class BaseDistance {
		BaseDistance(BaseLocation base, double distance) {
			this.base = base;
			this.distance = distance;
		}
		BaseLocation base;
		double distance;
	}
	
	public void updateOtherExpansionLocation(BaseLocation baseLocation) {
		
		final BaseLocation myBase = mainBaseLocations.get(selfPlayer);
		final BaseLocation myFirstExpansion = firstExpansionLocation.get(selfPlayer);
		
		final BaseLocation enemyBase = mainBaseLocations.get(enemyPlayer);
		final BaseLocation enemyFirstExpansion = firstExpansionLocation.get(enemyPlayer);
		
		if (myBase == null || myFirstExpansion == null || enemyBase == null || enemyFirstExpansion == null) {
			return;
		}

		otherExpansionLocations.get(selfPlayer).clear();
		otherExpansionLocations.get(enemyPlayer).clear();
		
		int islandCnt = 0;
		int mainBaseCnt = 0;
		for (BaseLocation base : BWTA.getBaseLocations()) {
			if (base.isIsland()) {
				islandCnt++;
				continue;
			}
			// BaseLocation을 equal로 비교하면 오류가 있을 수 있다.
			if (base.getPosition().equals(myBase.getPosition()) || base.getPosition().equals(myFirstExpansion.getPosition())
					|| base.getPosition().equals(enemyBase.getPosition()) || base.getPosition().equals(enemyFirstExpansion.getPosition())) {
				mainBaseCnt++;
				continue;
			}
			if (base.minerals() < 1000) {
				continue;
			}
			otherExpansionLocations.get(selfPlayer).add(base);
			otherExpansionLocations.get(enemyPlayer).add(base);
		}
		//System.out.println("updateOtherExpansionLocation -> " + islandCnt + " / " + mainBaseCnt);
		
		Collections.sort(otherExpansionLocations.get(selfPlayer), new Comparator<BaseLocation>() {
			@Override public int compare(BaseLocation base1, BaseLocation base2) {
				BaseLocation srcBase = myFirstExpansion;
				return (int) (srcBase.getGroundDistance(base1) - srcBase.getGroundDistance(base2));
			}
		});
		Collections.sort(otherExpansionLocations.get(enemyPlayer), new Comparator<BaseLocation>() {
			@Override public int compare(BaseLocation base1, BaseLocation base2) {
				BaseLocation srcBase = enemyFirstExpansion;
				return (int) (srcBase.getGroundDistance(base1) - srcBase.getGroundDistance(base2));
			}
		});
	}
	
	public void updateReadyToAttackPosition() {
		try {
			Chokepoint secChokeSelf = secondChokePoint.get(selfPlayer);
			Chokepoint secChokeEnemy = secondChokePoint.get(enemyPlayer);
			Position selfReadyToPos = getNextChokepoint(secChokeSelf, enemyPlayer).getCenter();
			Position enemyReadyToPos = getNextChokepoint(secChokeEnemy, selfPlayer).getCenter();
			
//			System.out.println("###selfReadyToPos: " + selfReadyToPos);
//			System.out.println("###enemyReadyToPos: " + enemyReadyToPos);
			
			readyToAttackPosition.put(selfPlayer, selfReadyToPos);
			readyToAttackPosition.put(enemyPlayer, enemyReadyToPos);
			
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
	public Chokepoint getNextChokepoint(Chokepoint currChoke, Player toPlayer) {
		Chokepoint enemyFirstChoke = firstChokePoint.get(toPlayer);

    	int chokeToEnemyChoke = MapTools.Instance().getGroundDistance(currChoke.getCenter(), enemyFirstChoke.getCenter()); // 현재chokepoint ~ 목적지chokepoint
    	
    	Chokepoint nextChoke = null;
		int closestChokeToNextChoke = 999999;
    	for (Chokepoint choke : BWTA.getChokepoints()) {
    		if (choke.equals(currChoke)) {
    			continue;
    		}
    		int chokeToNextChoke = MapTools.Instance().getGroundDistance(currChoke.getCenter(), choke.getCenter()); // 현재chokepoint ~ 다음chokepoint
    		int nextChokeToEnemyChoke = MapTools.Instance().getGroundDistance(choke.getCenter(), enemyFirstChoke.getCenter()); // 다음chokepoint ~ 목적지chokepoint
    		if (chokeToNextChoke + nextChokeToEnemyChoke < chokeToEnemyChoke + 10 // 최단거리 오차범위 10 * 32
    				&& chokeToNextChoke > 10 // 너무 가깝지 않아야 한다.
    				&& chokeToNextChoke < closestChokeToNextChoke) { // 가장 가까운 초크포인트를 선정
    			nextChoke = choke;
    			closestChokeToNextChoke = chokeToNextChoke;
    		}
	    }
   		return nextChoke;
	}
	

	public void updateOccupiedRegions(Region region, Player player) {
		// if the region is valid (flying buildings may be in null regions)
		if (region != null) {
			// add it to the list of occupied regions
			if (occupiedRegions.get(player) == null) {
				occupiedRegions.put(player, new HashSet<Region>());
			}
			occupiedRegions.get(player).add(region);
		}
	}

	/// 해당 BaseLocation 에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	/// @param radius TilePosition 단위
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player, int radius) {

		// invalid regions aren't considered the same, but they will both be null
		if (baseLocation == null) {
			return false;
		}
		// 반지름 10 (TilePosition 단위) 이면 거의 화면 가득이다
		if(radius > 10){
			radius = 10;
		}
		
		if (unitData.get(player) != null) {
			Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					
					// 띄워졌있는 배럭, 엔베는 차지한 영역으로 안쓴다. 왜냐면 우리는 이것들을 시야확보용으로 쓸 것이기 때문이다.
					if (player == MyBotModule.Broodwar.self()
							&& CommandUtil.IsValidUnit(ui.getUnit())
							&& ui.getUnit().isLifted()
							&& (ui.getType() == UnitType.Terran_Barracks || ui.getType() == UnitType.Terran_Engineering_Bay)) {
						continue;
					}
					
					TilePosition buildingPosition = ui.getLastPosition().toTilePosition();

					if(BWTA.getRegion(buildingPosition) != BWTA.getRegion(baseLocation.getTilePosition())){ //basicbot 1.2
						continue;
					}
					
//					System.out.print("buildingPositionX : " + buildingPosition.getX());
//					System.out.println(", buildingPositionY : " + buildingPosition.getY());
//					System.out.print("baseLocationX : " + baseLocation.getTilePosition().getX());
//					System.out.println(", baseLocationY : " + baseLocation.getTilePosition().getY());
					
					if (buildingPosition.getX() >= baseLocation.getTilePosition().getX() - radius
							&& buildingPosition.getX() <= baseLocation.getTilePosition().getX() + radius
							&& buildingPosition.getY() >= baseLocation.getTilePosition().getY() - radius
							&& buildingPosition.getY() <= baseLocation.getTilePosition().getY() + radius) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/// 해당 BaseLocation 주위 10타일 반경 내에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player) {
		return hasBuildingAroundBaseLocation(baseLocation, player, 10);
	}

	/// 해당 Region 에 해당 Player의 건물이 존재하는지 리턴합니다
	public boolean existsPlayerBuildingInRegion(Region region, Player player) {
		// invalid regions aren't considered the same, but they will both be null
		if (region == null || player == null) {
			return false;
		}

		Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for (const auto & kv : unitData.get(self).getUnits())
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
			if (ui.getType().isBuilding() ) {
				
				// Terran 종족의 Lifted 건물의 경우, BWTA.getRegion 결과가 null 이다
				if (BWTA.getRegion(ui.getLastPosition()) == null) continue;

				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}
		return false;
	}

	//지도상 섬지역
	public List<BaseLocation> getIslandBaseLocations() {
		return islandBaseLocations;
	}
	/// 해당 Player (아군 or 적군) 의 모든 유닛 목록 (가장 최근값) UnitAndUnitInfoMap 을 리턴합니다<br>	 
	/// 파악된 정보만을 리턴하기 때문에 적군의 정보는 틀린 값일 수 있습니다
	public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap(Player player) {
		return getUnitData(player).getUnitAndUnitInfoMap();
	}

	/// 해당 Player (아군 or 적군) 가 건물을 건설해서 점령한 Region 목록을 리턴합니다
	public Set<Region> getOccupiedRegions(Player player) {
		return occupiedRegions.get(player);
	}
	public Unit getFirstCC() {
		return FirstCC;
	}

	/// 해당 Player (아군 or 적군) 의 건물을 건설해서 점령한 BaseLocation 목록을 리턴합니다		 
	public List<BaseLocation> getOccupiedBaseLocations(Player player) {
		return occupiedBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 을 리턴합니다		 
	public BaseLocation getMainBaseLocation(Player player) {
		return mainBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 ChokePoint 를 리턴합니다		 
	public Chokepoint getFirstChokePoint(Player player) {
		return firstChokePoint.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 Expansion BaseLocation 를 리턴합니다		 
	public BaseLocation getFirstExpansionLocation(Player player) {
		return firstExpansionLocation.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 두번째로 가까운 ChokePoint 를 리턴합니다<br>		 
	/// 게임 맵에 따라서, secondChokePoint 는 일반 상식과 다른 지점이 될 수도 있습니다
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation과 First Expansion을 제외한 BaseLocation을 가까운 순으로 정렬하여 리턴합니다		 
	public List<BaseLocation> getOtherExpansionLocations(Player player) {
		return otherExpansionLocations.get(player);
	}

	/// 센터 진출로 포지션을 리턴한다. 헌터에서 썼다가 결과는 책임못진다. insaneojw
	public Position getReadyToAttackPosition(Player player) {
		return readyToAttackPosition.get(player);
	}

	//모든 멀티가 확인된 상태인지 확인
	public boolean isReceivingEveryMultiInfo() {
		return ReceivingEveryMultiInfo;
	}
//	public boolean isEarlyDefenseNeeded() {
//		return EarlyDefenseNeeded;
//	}
//	public boolean isScoutDefenseNeeded() {
//		return ScoutDefenseNeeded;
//	}
//	public void setScoutDefenseNeeded(boolean b) {
//		ScoutDefenseNeeded = b;
//	}
	public boolean isFirstScoutAlive() {
		return FirstScoutAlive;
	}
	public boolean isGasRushed() {
		return gasRushed;
	}
	public boolean isPhotonRushed() {
		return photonRushed;
	}
	public Unit getMyfirstGas() {
		return myfirstGas;
	}
//	public int getMainBaseSuppleLimit() {
//		return MainBaseSuppleLimit;
//	}
	
	//점령한 베이스 개수 확인
	public int getOccupiedBaseLocationsCnt(Player player) {
		return occupiedBaseLocations.get(player).size();
	}
	
	
	/// 해당 UnitType 이 전투 유닛인지 리턴합니다
	public final boolean isCombatUnitType(UnitType type) {
		if (type == UnitType.Zerg_Lurker /* || type == UnitType.Protoss_Dark_Templar*/) {
			// return false; 왜 false로 되어 있나?
			return true;
		}

		// check for various types of combat units
		if (type.canAttack()
				|| type == UnitType.Terran_Medic
				|| type == UnitType.Protoss_Observer
				|| type == UnitType.Protoss_Carrier
				|| type == UnitType.Terran_Bunker
				|| type == UnitType.Protoss_High_Templar) {
			return true;
		}

		return false;
	}
	
	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType() {
		return getBasicCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Zealot;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Marine;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Zergling;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType() {
		return getAdvancedCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Dragoon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Medic;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Hydralisk;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType() {
		return getBasicCombatBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Gateway;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Barracks;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Hatchery;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType() {
		return getObserverUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Observer;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Science_Vessel;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Overlord;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType() {
		return getBasicResourceDepotBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Nexus;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Command_Center;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType() {
		return getRefineryBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Assimilator;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Refinery;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Extractor;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType() {
		return getWorkerType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Probe;
		} else if (race == Race.Terran) {
			return UnitType.Terran_SCV;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Drone;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType() {
		return getBasicSupplyProviderUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Pylon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Supply_Depot;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Overlord;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType() {
		return getBasicDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Pylon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Bunker;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Creep_Colony;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType() {
		return getAdvancedDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Photon_Cannon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Missile_Turret;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Sunken_Colony;
//		} else {
//			return UnitType.None;
//		}
	}
	
	// 해당 종족의 UnitType 중 방어유닛 러쉬 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedRushBuildingType() {
		return getAdvancedRushBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedRushBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Photon_Cannon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Bunker;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Sunken_Colony;
		} else {
			return UnitType.None;
		}
	}
	
	public void updateFirstGasInformation() {
		if(selfPlayer!= null && getMainBaseLocation(selfPlayer)!= null && getMainBaseLocation(selfPlayer).getGeysers().size() > 0){
			myfirstGas = getMainBaseLocation(selfPlayer).getGeysers().get(0);
		}
	}
	public void updateMapSpecificInformation() {
		List<BaseLocation> startingBase = new ArrayList<>();
		MAP candiMapByPosition = null;
		for (BaseLocation base : BWTA.getStartLocations()) {
			if (base.isStartLocation()) {
				startingBase.add(base);
			}
		}
		
		// position으로 map 판단
		final int posFighting[][] = new int[][]{{288, 3760}, {288, 240}, {3808, 272}, {3808, 3792}};
		final int posLost[][] = new int[][]{{288, 2832}, {928, 3824}, {3808, 912}, {1888, 240}};
		if (startingBase.size() == 8) {
			candiMapByPosition = MAP.TheHunters;
		} else if (startingBase.size() == 4) {
			Position basePos = mainBaseLocations.get(selfPlayer).getPosition();
			for (int[] pos : posFighting) {
				if (basePos.equals(new Position(pos[0], pos[1]))) {
					candiMapByPosition = MAP.FightingSpririts;
					break;
				}
			}
			if (candiMapByPosition == null) {
				for (int[] pos : posLost) {
					if (basePos.equals(new Position(pos[0], pos[1]))) {
						candiMapByPosition = MAP.LostTemple;
						break;
					}
				}
			}
		} else {
			candiMapByPosition = MAP.Unknown;
		}
		
		// name으로 map 판단
		MAP candiMapByName = null;
		String mapName = MyBotModule.Broodwar.mapFileName().toUpperCase();
		if (mapName.matches(".*HUNT.*")) {
			candiMapByName = MAP.TheHunters;
		} else if (mapName.matches(".*LOST.*") || mapName.matches(".*TEMPLE.*")) {
			candiMapByName = MAP.LostTemple;
		} else if (mapName.matches(".*FIGHT.*") || mapName.matches(".*SPIRIT.*")) {
			candiMapByName = MAP.FightingSpririts;
		} else {
			candiMapByName = MAP.Unknown;
		}

		// 최종 결정
		MAP mapDecision = MAP.LostTemple;
		if (candiMapByPosition == candiMapByName) {
			mapDecision = candiMapByPosition;
//			System.out.println("map : " + candiMapByPosition + "(100%)");
		} else {
			if (candiMapByPosition != MAP.Unknown) {
				mapDecision = candiMapByPosition;
//				System.out.println("map : " + mapDecision + "(mapByName is -> " + candiMapByName + ")");
			} else if (candiMapByName != MAP.Unknown) {
				mapDecision = candiMapByName;
//				System.out.println("map : " + mapDecision + "(mapByPosition is -> " + candiMapByPosition + ")");
			}
		}

		MapSpecificInformation tempMapInfo = new MapSpecificInformation();
		tempMapInfo.setMap(mapDecision);
		tempMapInfo.setStartingBaseLocation(startingBase);
		
		mapSpecificInformation = tempMapInfo;
	}
	
	public MapSpecificInformation getMapSpecificInformation() {
		return mapSpecificInformation;
	}
}