

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class SpiderMineManger {
	
	private int mineInMyBaseLevel = 0;

	public int getMineInMyBaseLevel() {
		return mineInMyBaseLevel;
	}

	public void setMineInMyBaseLevel(int mineInMyBaseLevel) {
		this.mineInMyBaseLevel = mineInMyBaseLevel;
	}

	private Map<Integer, MineRemoveReserved> mineRemoveMap;
	private Map<Integer, MineReserved> mineReservedMap;
	private List<Position> goodPositions;
	private List<BaseLocation> myExpansions;

	private static SpiderMineManger instance = new SpiderMineManger();
	
	private boolean initialized = false;
	
	private SpiderMineManger() {}
	
	public static SpiderMineManger Instance() {
		return instance;
	}
	
	// TODO goodPositions 단계적으로 변화. ex) 초반에는 세번째, 네번째 멀티, 그 후에는 점차 증가 
	public void init() {
		if (!MicroSet.Upgrade.hasResearched(TechType.Spider_Mines)) {
			return;
		}

		List<BaseLocation> otherBases = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		Position myReadyToAttackPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
		Chokepoint mySecondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		
		Position enemyReadyToAttackPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
		BaseLocation enemyFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		Chokepoint enemySecondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
//		Position center = new Position(2048, 2048); // 128x128 맵의 센터
//		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer); // region이 좋음
		
		if (!otherBases.isEmpty() && myReadyToAttackPos != null && mySecondChoke != null
				&& enemyReadyToAttackPos != null && enemyFirstExpansion != null && enemySecondChoke != null) {
			mineReservedMap = new HashMap<>();
			mineRemoveMap = new HashMap<>();
			
			goodPositions = new ArrayList<>(); // 마인 심기 좋은 지역
			myExpansions = this.getMyExpansionBaseLocation();
			
			// 3rd 멀티지역
			for (BaseLocation base : otherBases) {
				if (!myExpansions.contains(base)) {
					goodPositions.add(base.getPosition());
				}
			}
			
			// 공격준비지역
			goodPositions.add(myReadyToAttackPos);
			goodPositions.add(mySecondChoke.getCenter());
			
			goodPositions.add(enemyReadyToAttackPos);
			goodPositions.add(enemySecondChoke.getCenter());
			goodPositions.add(enemyFirstExpansion.getPosition());
			
			// 제거해야되는 마인리스트
			initialized = true;
			
			// 테란 스파이더마인 정책 적용

			if (InformationManager.Instance().enemyRace == Race.Terran) {
				CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.MINE_STRATEGY_FOR_TERRAN, 1 * 60 * 24);
			}
		}
	}
	
	public void update() {
		
		// 패스트다크인 경우 본진 마인매설(12000프레임 이하)
//		System.out.println(MyBotModule.Broodwar.getFrameCount());
		if (MyBotModule.Broodwar.getFrameCount() > 12000) {
			if (mineInMyBaseLevel != 0) {
//				MyBotModule.Broodwar.printf("mineInMyBaseLevel ................... 0");
				mineInMyBaseLevel = 0;
			}
			
		} else if (MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Protoss_Dark_Templar) > 0
				|| MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Zerg_Lurker) > 0
				|| MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Protoss_Shuttle) > 0) {
			if (mineInMyBaseLevel != 2) {
//				MyBotModule.Broodwar.printf("mineInMyBaseLevel ................... 2");
				CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.VULTURE_JOIN_SQUAD, 50 * 24);
				mineInMyBaseLevel = 2; // 본진 앞마당 전부
			}
			
		} else if (MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Zerg_Hydralisk) >= 3
				|| MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Protoss_Dragoon) >= 3) {
			if (mineInMyBaseLevel != 1) {
//				MyBotModule.Broodwar.printf("mineInMyBaseLevel ................... 1");
				mineInMyBaseLevel = 1; // 앞마당에 많이
			}
		}
		
		if (!initialized) {
			init();
			return;
		}
		
		// 만료 매설 만료시간 관리 
		List<Integer> expiredList = new ArrayList<>();
		for (Integer unitId : mineReservedMap.keySet()) {
			MineReserved mineReserved = mineReservedMap.get(unitId);
			if (mineReserved.reservedFrame + MicroSet.Vulture.RESV_EXPIRE_FRAME < MyBotModule.Broodwar.getFrameCount()) {
				//System.out.println("expired mine position : " + mineReserved.positionToMine);
				expiredList.add(unitId);
			} else if (!CommandUtil.IsValidUnit(MyBotModule.Broodwar.getUnit(unitId))) {
				expiredList.add(unitId);
			}
//			MyBotModule.Broodwar.drawCircleScreen(mineReserved.positionToMine, 100, Color.White);
		}
		for (Integer unitId : expiredList) {
			mineReservedMap.remove(unitId);
		}
		
		// 만료 제거 만료시간 관리
		List<Integer> expiredRemoveList = new ArrayList<>();
		for (Integer unitId : mineRemoveMap.keySet()) {
			MineRemoveReserved removeReserved = mineRemoveMap.get(unitId);
			if (removeReserved.reservedFrame + MicroSet.Vulture.RESV_EXPIRE_FRAME < MyBotModule.Broodwar.getFrameCount()) {
				expiredRemoveList.add(unitId);
			}
		}
		for (Integer unitId : expiredRemoveList) {
			mineRemoveMap.remove(unitId);
		}
	}
	
	public void addRemoveList(Unit siegeTank) {
		if (siegeTank.getType() != UnitType.Terran_Siege_Tank_Siege_Mode) {
			return;
		}
		
		List<Unit> nearMineList = MapGrid.Instance().getUnitsNear(siegeTank.getPosition(), MicroSet.Vulture.MINE_REMOVE_TANK_DIST, true, false, UnitType.Terran_Vulture_Spider_Mine);
		for (Unit mine : nearMineList) {
			if (mineRemoveMap.get(mine.getID()) == null) {
				mineRemoveMap.put(mine.getID(), new MineRemoveReserved(mine, MyBotModule.Broodwar.getFrameCount()));
			}
		}
	}

	public Position getPositionReserved(Unit vulture) {
		if (!initialized || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		MineReserved mineReserved = mineReservedMap.get(vulture.getID());
		if (mineReserved != null) {
			return mineReserved.positionToMine;
		} else {
			return null;
		}
	}
	
	public void cancelMineReserve(Unit vulture) {
		if (!initialized || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return;
		}
		mineReservedMap.remove(vulture.getID());
	}
	
	public boolean removeMine(Unit vulture) {
		if (!initialized || vulture == null) {
			return false;
		}
		
		for (Integer mineId : mineRemoveMap.keySet()) {
			MineRemoveReserved removeReserved = mineRemoveMap.get(mineId);
			if (CommandUtil.IsValidUnit(removeReserved.mine)
					&& vulture.getDistance(removeReserved.mine.getPosition()) < UnitType.Terran_Vulture.groundWeapon().maxRange()) {
				CommandUtil.attackUnit(vulture, removeReserved.mine);
				return true;
			}
		}
		return false;
	}
	
	public Position enemyPositionToMine(Unit vulture, List<UnitInfo> enemiesInfo) {
		if (!initialized || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);
			if (enemy != null) {
				if (vulture.getDistance(enemy) <= MicroSet.Vulture.MINE_ENEMY_TARGET_DISTANCE) {
					List<Unit> spiderMinesNearEnemy = MapGrid.Instance().getUnitsNear(enemy.getPosition(), MicroSet.Vulture.MINE_ENEMY_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine);
					if (spiderMinesNearEnemy.size() + numOfMineReserved(enemy.getPosition(), MicroSet.Vulture.MINE_ENEMY_RADIUS) < 1) {
						for (int i = 0; i < 3; i++) {
							Position minePosition = MicroUtils.randomPosition(enemy.getPosition(), MicroSet.Vulture.MINE_ENEMY_RADIUS);
							if (noProblemToMine(minePosition)) { // 문제없다면 없다면 매설
								mineReservedMap.put(vulture.getID(), new MineReserved(minePosition, MyBotModule.Broodwar.getFrameCount()));
								return minePosition;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	
	public Position goodPositionToMine(Unit vulture, int mineNumberPerPosition) {
		if (!initialized || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		
		// 마인을 심을 좋은 장소를 찾는다.
		int nearestDistance = 999999;
		Position nearestGoodPosition = null;
		for (Position position : goodPositions) {
			int distance = vulture.getDistance(position);
			if (distance < nearestDistance && distance < MicroSet.Vulture.MINE_SPREAD_RADIUS && MicroUtils.isSafePlace(position)) {
				nearestDistance = distance;
				nearestGoodPosition = position;
			}
		}
		
		// 찾지 못했다..
		if (nearestGoodPosition == null) {
			return null;
		}
		
		boolean exactOneEssential = true;
		List<Unit> unitsOnTile = MyBotModule.Broodwar.getUnitsOnTile(nearestGoodPosition.toTilePosition());
		if (!unitsOnTile.isEmpty()) {
			exactOneEssential = false;
		}
		
		return positionToMine(vulture, nearestGoodPosition, exactOneEssential, mineNumberPerPosition);
	}
	
	public Position positionToMine(Unit vulture, Position position, boolean exactOneEssential, int mineNumberPerPosition) {
		if (!initialized || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		
		// 거의 정확한 position에 마인이 매설되어 있는지 체크한다.
		// 없으면 무조건 매설 (확장 체크 및 방해 용도로 정확한 위치에 하나가 매설되어야 한다.)
		if (exactOneEssential) {
			List<Unit> spiderMinesInExactRadius = MapGrid.Instance().getUnitsNear(position, MicroSet.Vulture.MINE_EXACT_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine);
			if (spiderMinesInExactRadius.size() == 0) {
				for (int i = 0; i < 3; i++) {
					Position minePosition = MicroUtils.randomPosition(position, MicroSet.Vulture.MINE_EXACT_RADIUS);
					if (noProblemToMine(minePosition) && MicroUtils.isSafePlace(minePosition)) { // 문제없다면 없다면 매설
						mineReservedMap.put(vulture.getID(), new MineReserved(minePosition, MyBotModule.Broodwar.getFrameCount()));
						return minePosition;
					}
				}
			}
		}
		
		// 좀 펼쳐진 position에서 마인이 있는지 체크한다. (포지션 별로 설정된 개수만큼 마인을 매설한다. 매설 예정인 마인도 계산한다.)
		List<Unit> spiderMinesInSpreadRadius = MapGrid.Instance().getUnitsNear(position, MicroSet.Vulture.MINE_SPREAD_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine);
		if (spiderMinesInSpreadRadius.size() + numOfMineReserved(position, MicroSet.Vulture.MINE_SPREAD_RADIUS) < mineNumberPerPosition) {
			for (int i = 0; i < 3; i++) {
				Position minePosition = MicroUtils.randomPosition(position, MicroSet.Vulture.MINE_SPREAD_RADIUS);
				if (noProblemToMine(minePosition) && MicroUtils.isSafePlace(minePosition)) { // 문제없다면 없다면 매설
					mineReservedMap.put(vulture.getID(), new MineReserved(minePosition, MyBotModule.Broodwar.getFrameCount()));
					return minePosition;
				}
			}
		}
		return null;
	}
	
	private boolean noProblemToMine(Position position) {
		// 아미 가까운 곳에 마인 매설이예약되었다.
		for (MineReserved mineReserved : mineReservedMap.values()) {
			if (position.getDistance(mineReserved.positionToMine) <= MicroSet.Vulture.MINE_BETWEEN_DIST) {
				return false;
			}
		}
		
		// 마인을 심을 수 있는 장소가 아니다.
		if (!MicroUtils.isValidGroundPosition(position)) {
			return false;
		}
		
		// 해당 지역에 마인이 매설되어 있다.
		int exactPosMineNum = MapGrid.Instance().getUnitsNear(position, MicroSet.Vulture.MINE_EXACT_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine).size();
		int overlapMine = InformationManager.Instance().enemyRace == Race.Terran ? 2 : 1;
		if (exactPosMineNum >= overlapMine) {
			return false;
		}
		
		// 해당 지역에 아군 시즈탱크, 컴셋 스테이션, SCV 등이 있다면 금지 
		List<Unit> units = MapGrid.Instance().getUnitsNear(position, MicroSet.Vulture.MINE_REMOVE_TANK_DIST, true, false, null);
		for (Unit unit : units) {
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| unit.getType() == UnitType.Terran_SCV
					|| unit.getType() == UnitType.Terran_Comsat_Station) {
				return false;
			}
		}
		
		
		// 첫번째 확장지역 마인매설 금지 처리
		BaseLocation myFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
		if (position.getDistance(myFirstExpansion.getPosition()) < MicroSet.Vulture.MINE_REMOVE_TANK_DIST) {
			return false;
		}
		
		return true;
	}
	
	private int numOfMineReserved(Position position, int radius) {
		int reservedMineNum = 0;
		for (MineReserved minReserved : mineReservedMap.values()) {
			if (minReserved.positionToMine.getDistance(position) <= radius) {
				reservedMineNum++;
			}
		}
		return reservedMineNum;
	}
	
	public List<BaseLocation> getMyExpansionBaseLocation() {
		List<BaseLocation> myExpansions = new ArrayList<>();

		double tempDistance;
		double sourceDistance;
		double closestDistance = 100000000;
		
		BaseLocation res = null;
		BaseLocation res2 = null;

		BaseLocation sourceBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
		BaseLocation enemyfirstBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		BaseLocation selfmainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		BaseLocation enemymainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		
		for (BaseLocation targetBaseLocation : BWTA.getStartLocations())
		{
			if (targetBaseLocation.getTilePosition().equals(selfmainBaseLocations.getTilePosition())) continue;
			if (targetBaseLocation.getTilePosition().equals(enemymainBaseLocations.getTilePosition())) continue;
			
			sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
			tempDistance = sourceDistance - enemymainBaseLocations.getGroundDistance(targetBaseLocation);
			
			if (tempDistance < closestDistance && sourceDistance > 0) {
				closestDistance = tempDistance;
				res2 = res;
				res = targetBaseLocation;
			}
		}
		if(res!= null){
			myExpansions.add(res);
		}
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			myExpansions.add(res2);
		}
		
		res=null;
		res2=null;
		BaseLocation res3 = null;
		BaseLocation res4 = null;
		closestDistance = 100000000;
		
		for (BaseLocation targetBaseLocation : BWTA.getBaseLocations()){

			if (targetBaseLocation.isStartLocation()){
				continue;
			}
			if (targetBaseLocation.getTilePosition().equals(sourceBaseLocation.getTilePosition())) continue;
			if (targetBaseLocation.getTilePosition().equals(enemyfirstBaseLocation.getTilePosition())) continue;
			
			TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(targetBaseLocation.getTilePosition());
			if(findGeyser != null){
				if (findGeyser.getDistance(targetBaseLocation.getTilePosition())*32 > 400){
					continue;
				}
			}
			
			sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
			tempDistance = sourceDistance - enemymainBaseLocations.getGroundDistance(targetBaseLocation);
			
			if (tempDistance < closestDistance && sourceDistance > 0) {
				closestDistance = tempDistance;
				res4 = res3;
				res3 = res2;
				res2 = res;
				res = targetBaseLocation;
			}
		}
		
		
		if(res!= null){
			myExpansions.add(res);
		}
		if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.LostTemple){
			if(res2!= null){
				myExpansions.add(res2);
			}
		}
			
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			if(res3!= null){
				myExpansions.add(res3);
			}
			if(res4!= null){
				myExpansions.add(res4);
			}
		}
		
		
		return myExpansions;
	}

}

class MineReserved {
	MineReserved(Position positionToMine, int reservedFrame) {
		this.positionToMine = positionToMine;
		this.reservedFrame = reservedFrame;
	}
	Position positionToMine;
	int reservedFrame;

	@Override
	public String toString() {
		return "MineReserved [positionToMine=" + positionToMine + ", reservedFrame=" + reservedFrame + "]";
	}
}

class MineRemoveReserved {
	MineRemoveReserved(Unit mine, int reservedFrame) {
		this.mine = mine;
		this.reservedFrame = reservedFrame;
	}
	Unit mine;
	int reservedFrame;

	@Override
	public String toString() {
		return "MineRemoveReserved [mine=" + mine + ", reservedFrame=" + reservedFrame + "]";
	}
}
