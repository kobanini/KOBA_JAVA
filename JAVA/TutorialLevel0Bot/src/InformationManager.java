

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

/// ���� ��Ȳ���� �� �Ϻθ� ��ü �ڷᱸ�� �� �����鿡 �����ϰ� ������Ʈ�ϴ� class<br>
/// ���� ���� ��Ȳ������ BWAPI::Broodwar �� ��ȸ�Ͽ� �ľ��� �� ������, ���� ���� ��Ȳ������ BWAPI::Broodwar �� ���� ��ȸ�� �Ұ����ϱ� ������ InformationManager���� ���� �����ϵ��� �մϴ�<br>
/// ����, BWAPI::Broodwar �� BWTA ���� ���� ��ȸ�� �� �ִ� ���������� ��ó�� / ���� �����ϴ� ���� ������ �͵� InformationManager���� ���� �����ϵ��� �մϴ�
public class InformationManager {
	private static InformationManager instance = new InformationManager();

	public Player selfPlayer;		///< �Ʊ� Player		
	public Player enemyPlayer;		///< ���� Player		
	public Race selfRace;			///< �Ʊ� Player�� ����
	public Race enemyRace;			///< ���� Player�� ����  

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
	
	/// �ش� Player�� �ֿ� �ǹ����� �ִ� BaseLocation. <br>
	/// ó������ StartLocation ���� ����. mainBaseLocation �� ��� �ǹ��� �ı��� ��� ������<br>
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();

	/// �ش� Player�� mainBaseLocation �� ����Ǿ��°� (firstChokePoint, secondChokePoint, firstExpansionLocation �� ������ �ߴ°�)
	private Map<Player, Boolean> mainBaseLocationChanged = new HashMap<Player, Boolean>();

	/// �ش� Player�� �����ϰ� �ִ� Region �� �ִ� BaseLocation<br>
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();

	/// �ش� Player�� �����ϰ� �ִ� Region<br>
	/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	/// �ش� Player�� mainBaseLocation ���� ���� ����� ChokePoint
	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	/// �ش� Player�� mainBaseLocation ���� ���� ����� BaseLocation
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	/// �ش� Player�� mainBaseLocation ���� �ι�°�� ����� (firstChokePoint�� �ƴ�) ChokePoint<br>
	/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();
	private Map<Player, Chokepoint> thirdChokePointDonotUse = new HashMap<Player, Chokepoint>();
	public Position tighteningPoint = null;
	
	// ������ ��Ƽ location (����� ������ sorting)
	private Map<Player, List<BaseLocation>> otherExpansionLocations = new HashMap<Player, List<BaseLocation>>();
	
	/// ���� �����
	private Map<Player, Position> readyToAttackPosition = new HashMap<Player, Position>();
	
	private MapSpecificInformation mapSpecificInformation = null;

	/// Player - UnitData(�� Unit �� �� Unit�� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ��) �� �����ϴ� �ڷᱸ�� ��ü
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	private List<BaseLocation> islandBaseLocations = new ArrayList<BaseLocation>();

	/// static singleton ��ü�� �����մϴ�
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

	/// Unit �� BaseLocation, ChokePoint � ���� ������ ������Ʈ�մϴ�
	public void update() {
		
		
		if(MyBotModule.Broodwar.getFrameCount() % 8 == 0) {
			updateUnitsInfo();
			updateCurrentStatusInfo();
		}
		// occupiedBaseLocation �̳� occupiedRegion �� ���� �ȹٲ�Ƿ� ���� ���ص� �ȴ�
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
						checkGasRush = false;//���� ���� ���� ��
					}
				}
			}
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == getRefineryBuildingType(enemyRace) && myfirstGas !=null){
					if(myfirstGas.getPosition().equals(unit.getPosition())){
						gasRushed = true;//���� ���� ����
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
					gasRushed = false;//���� ���� ���� ��
//					System.out.println("gas rush finished");
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) < 1){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
				}
			}
		}
		//10000������ ���������� ���淯�� Ȯ��.
		if (MyBotModule.Broodwar.getFrameCount() < 10000) {
			BaseLocation base = null;
			// 1. ������ �� ����ĳ���� �ִ��� ����.
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

	/// ��ü unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
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

	/// �ش� unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
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

	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitShow(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitHide(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitCreate(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitComplete(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitMorph(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit �� ���� ������ ������Ʈ�մϴ�
	public void onUnitRenegade(Unit unit) { 
		updateUnitInfo(unit); 
	}
	
	/// Unit �� ���� ������ ������Ʈ�մϴ� <br>
	/// ������ �ı�/����� ���, �ش� ���� ������ �����մϴ�
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}

		unitData.get(unit.getPlayer()).removeUnit(unit);
	}

	
	/// �ش� Player (�Ʊ� or ����) �� position ������ ���� ����� unitInfo �� �����մϴ�		 
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

	/// �ش� Player (�Ʊ� or ����) �� �ش� UnitType ���� ���ڸ� �����մϴ� (�Ʒ�/�Ǽ� ���� ���� ���ڱ��� ����)
	public int getNumUnits(UnitType t, Player player) {
		return getUnitData(player).getNumUnits(t.toString());
	}

	/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� UnitData �� �����մϴ�		 
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

		// enemy �� startLocation�� ���� �𸣴� ���
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
		
		
		
		//������ ��� location �� ã������
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
		// � Base Location ���� �Ʊ� �ǹ�, ���� �ǹ� ��� ȥ�����־ ���ÿ� ���� Player �� Occupy �ϰ�
		// �ִ� ������ ������ �� �ִ�
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
			
			// �� MainBaseLocation ������Ʈ ���� ���� ����
			// ������ ���� �ո��� �ǹ� �Ǽ� + �Ʊ��� ���� ������ ���� �湮�� ���, 
			// enemy�� mainBaseLocations�� �湮���� ���¿����� �ǹ��� �ϳ��� ���ٰ� �Ǵ��Ͽ� mainBaseLocation �� �����ϴ� ������ �߻��ؼ�
			// enemy�� mainBaseLocations�� ���� �湮�߾��� ���� �ѹ��� �־�� �Ѵٶ�� ���� �߰�.  
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

		// self�� mainBaseLocations�� ����, �װ��� �ִ� �ǹ��� ��� �ı��� ���
		// _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
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
//				//���� ���̽��� �ƴҶ�
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
//					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition()); //���� �ּ� ������ ��
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						thirdChokePointDonotUse.put(selfPlayer, secondChokePoint.get(selfPlayer));
						secondChokePoint.put(selfPlayer, chokepoint);
					}
				}
				//��Ʈ Ư�̻���
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
				//��Ʈ Ư�̻���
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
//						tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition()); //���� �ּ� ������ ��
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
			// BaseLocation�� equal�� ���ϸ� ������ ���� �� �ִ�.
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

    	int chokeToEnemyChoke = MapTools.Instance().getGroundDistance(currChoke.getCenter(), enemyFirstChoke.getCenter()); // ����chokepoint ~ ������chokepoint
    	
    	Chokepoint nextChoke = null;
		int closestChokeToNextChoke = 999999;
    	for (Chokepoint choke : BWTA.getChokepoints()) {
    		if (choke.equals(currChoke)) {
    			continue;
    		}
    		int chokeToNextChoke = MapTools.Instance().getGroundDistance(currChoke.getCenter(), choke.getCenter()); // ����chokepoint ~ ����chokepoint
    		int nextChokeToEnemyChoke = MapTools.Instance().getGroundDistance(choke.getCenter(), enemyFirstChoke.getCenter()); // ����chokepoint ~ ������chokepoint
    		if (chokeToNextChoke + nextChokeToEnemyChoke < chokeToEnemyChoke + 10 // �ִܰŸ� �������� 10 * 32
    				&& chokeToNextChoke > 10 // �ʹ� ������ �ʾƾ� �Ѵ�.
    				&& chokeToNextChoke < closestChokeToNextChoke) { // ���� ����� ��ũ����Ʈ�� ����
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

	/// �ش� BaseLocation �� player�� �ǹ��� �����ϴ��� �����մϴ�
	/// @param baseLocation ��� BaseLocation
	/// @param player �Ʊ� / ����
	/// @param radius TilePosition ����
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player, int radius) {

		// invalid regions aren't considered the same, but they will both be null
		if (baseLocation == null) {
			return false;
		}
		// ������ 10 (TilePosition ����) �̸� ���� ȭ�� �����̴�
		if(radius > 10){
			radius = 10;
		}
		
		if (unitData.get(player) != null) {
			Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					
					// ������ִ� �跰, ������ ������ �������� �Ⱦ���. �ֳĸ� �츮�� �̰͵��� �þ�Ȯ�������� �� ���̱� �����̴�.
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
	
	/// �ش� BaseLocation ���� 10Ÿ�� �ݰ� ���� player�� �ǹ��� �����ϴ��� �����մϴ�
	/// @param baseLocation ��� BaseLocation
	/// @param player �Ʊ� / ����
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player) {
		return hasBuildingAroundBaseLocation(baseLocation, player, 10);
	}

	/// �ش� Region �� �ش� Player�� �ǹ��� �����ϴ��� �����մϴ�
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
				
				// Terran ������ Lifted �ǹ��� ���, BWTA.getRegion ����� null �̴�
				if (BWTA.getRegion(ui.getLastPosition()) == null) continue;

				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}
		return false;
	}

	//������ ������
	public List<BaseLocation> getIslandBaseLocations() {
		return islandBaseLocations;
	}
	/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� (���� �ֱٰ�) UnitAndUnitInfoMap �� �����մϴ�<br>	 
	/// �ľǵ� �������� �����ϱ� ������ ������ ������ Ʋ�� ���� �� �ֽ��ϴ�
	public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap(Player player) {
		return getUnitData(player).getUnitAndUnitInfoMap();
	}

	/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ Region ����� �����մϴ�
	public Set<Region> getOccupiedRegions(Player player) {
		return occupiedRegions.get(player);
	}
	public Unit getFirstCC() {
		return FirstCC;
	}

	/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ BaseLocation ����� �����մϴ�		 
	public List<BaseLocation> getOccupiedBaseLocations(Player player) {
		return occupiedBaseLocations.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation �� �����մϴ�		 
	public BaseLocation getMainBaseLocation(Player player) {
		return mainBaseLocations.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� ChokePoint �� �����մϴ�		 
	public Chokepoint getFirstChokePoint(Player player) {
		return firstChokePoint.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� Expansion BaseLocation �� �����մϴ�		 
	public BaseLocation getFirstExpansionLocation(Player player) {
		return firstExpansionLocation.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� �ι�°�� ����� ChokePoint �� �����մϴ�<br>		 
	/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation�� First Expansion�� ������ BaseLocation�� ����� ������ �����Ͽ� �����մϴ�		 
	public List<BaseLocation> getOtherExpansionLocations(Player player) {
		return otherExpansionLocations.get(player);
	}

	/// ���� ����� �������� �����Ѵ�. ���Ϳ��� ��ٰ� ����� å�Ӹ�����. insaneojw
	public Position getReadyToAttackPosition(Player player) {
		return readyToAttackPosition.get(player);
	}

	//��� ��Ƽ�� Ȯ�ε� �������� Ȯ��
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
	
	//������ ���̽� ���� Ȯ��
	public int getOccupiedBaseLocationsCnt(Player player) {
		return occupiedBaseLocations.get(player).size();
	}
	
	
	/// �ش� UnitType �� ���� �������� �����մϴ�
	public final boolean isCombatUnitType(UnitType type) {
		if (type == UnitType.Zerg_Lurker /* || type == UnitType.Protoss_Dark_Templar*/) {
			// return false; �� false�� �Ǿ� �ֳ�?
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
	
	// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatUnitType() {
		return getBasicCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedCombatUnitType() {
		return getAdvancedCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
	public UnitType getBasicCombatBuildingType() {
		return getBasicCombatBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getObserverUnitType() {
		return getObserverUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicResourceDepotBuildingType() {
		return getBasicResourceDepotBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getRefineryBuildingType() {
		return getRefineryBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
	public UnitType getWorkerType() {
		return getWorkerType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicSupplyProviderUnitType() {
		return getBasicSupplyProviderUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getBasicDefenseBuildingType() {
		return getBasicDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
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

	// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedDefenseBuildingType() {
		return getAdvancedDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
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
	
	// �ش� ������ UnitType �� ������� ���� ����� �ϴ� UnitType�� �����մϴ�
	public UnitType getAdvancedRushBuildingType() {
		return getAdvancedRushBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
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
		
		// position���� map �Ǵ�
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
		
		// name���� map �Ǵ�
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

		// ���� ����
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