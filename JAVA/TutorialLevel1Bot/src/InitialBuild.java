

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// 봇 프로그램 설정
public class InitialBuild {

	private static InitialBuild instance = new InitialBuild();
	
	public static InitialBuild Instance() {
		return instance;
	}
	
	
	public void setInitialBuildOrder() {

		//@@@@@@ 맵과 상대 종족에 따른 initial build 를 따져봐야된다.
		BlockingEntrance.Instance().SetBlockingPosition();
		
		TilePosition firstSupplyPos = new TilePosition(BlockingEntrance.Instance().first_suppleX,BlockingEntrance.Instance().first_suppleY);
		TilePosition secondSupplyPos = new TilePosition(BlockingEntrance.Instance().second_suppleX,BlockingEntrance.Instance().second_suppleY);
		TilePosition barrackPos = new TilePosition(BlockingEntrance.Instance().barrackX,BlockingEntrance.Instance().barrackY);
		TilePosition factoryPos = new TilePosition(BlockingEntrance.Instance().factoryX,BlockingEntrance.Instance().factoryY);
		TilePosition bunkerPos = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
		TilePosition turret1Pos = new TilePosition(BlockingEntrance.Instance().turret1X,BlockingEntrance.Instance().turret1Y);		
		
System.out.println("turret1X="+BlockingEntrance.Instance().turret1X);
System.out.println("turret1Y="+BlockingEntrance.Instance().turret1Y);

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			System.out.println("InittialBuildOrder:Terran");
			
//			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				//기타맵 테란전

				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true,true);
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true,true);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Machine_Shop);
				queueBuildSeed(true, UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Siege_Tank_Tank_Mode);
				
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				queueBuildSeed(true, UnitType.Terran_Starport, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//				queueBuildSeed(true, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.FirstChokePoint);
//				queueBuildSeed(true, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuildSeed(true, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.SecondChokePoint);
//				queueBuildSeed(true, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard);
//			}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
//				//헌터 테란전
//				
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
//				queueBuild(true, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Refinery);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Marine);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_Machine_Shop);
//				queueBuildSeed(true, UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//				queueBuild(false, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_Vulture);
//				queueBuildSeed(true, UnitType.Terran_Starport, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
//				queueBuild(true, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Refinery);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Marine);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Factory);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(TechType.Spider_Mines);
//				queueBuild(true, UnitType.Terran_Vulture, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuild(TechType.Tank_Siege_Mode);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuild(true, UnitType.Terran_Factory);
//				queueBuild(true, UnitType.Terran_Armory, UnitType.Terran_Academy);
//			}
			
		}else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			System.out.println("InittialBuildOrder:Protoss");
			//if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true,true);
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				queueBuildSeed(false, UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Machine_Shop);
				//queueBuild(false, UnitType.Terran_Vulture);
				//queueBuild(false, UnitType.Terran_Siege_Tank_Tank_Mode);
				
				queueBuild(false, UnitType.Terran_SCV);
				
				//queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				
//			}
//			else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
//				queueBuild(true, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Refinery);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_Marine);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
//				queueBuild(false, UnitType.Terran_Siege_Tank_Tank_Mode);
//				//queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//			}

		}else{//저그전
			System.out.println("InittialBuildOrder:Zerg");
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	            BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
	            BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
	            queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다. 
	            queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
	            ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
	            BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true);
	            queueBuild(false, UnitType.Terran_SCV);
	            queueBuild(true, UnitType.Terran_Marine);
	            queueBuild(true, UnitType.Terran_Refinery);
	            queueBuild(false, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,false);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Machine_Shop);
				//queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Vulture);
				//queueBuild(true, UnitType.Terran_Factory);
				queueBuild(true, UnitType.Terran_Armory);
		}
	}
	
	public void queueBuild(boolean blocking, UnitType... types) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		BuildOrderItem.SeedPositionStrategy defaultSeedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
		for (UnitType type : types) {
			bq.queueAsLowestPriority(type, defaultSeedPosition, blocking);
		}
	}
	
	public void queueBuildSeed(boolean blocking, UnitType type, BuildOrderItem.SeedPositionStrategy seedPosition) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type, seedPosition, blocking);
	}
	
	public void queueBuild(TechType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
	
	public void queueBuild(UpgradeType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
}