import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 봇 프로그램 설정
public class RespondToStrategy {
	
	//클로킹 유닛에 대한 대처
	public boolean enemy_dark_templar;
	public boolean enemy_lurker;
	public boolean enemy_wraith;
	public boolean enemy_wraithcloak;
	
	public boolean enemy_guardian;
	
	public boolean enemy_shuttle;
	public boolean enemy_arbiter;
	public boolean enemy_mutal;
	public boolean enemy_scout;
	public boolean enemy_hive;
	
	public boolean need_vessel;
	public boolean need_valkyrie;
	public boolean need_wraith;
	public boolean need_battlecruiser;
	
	public boolean mainBaseTurret;
	public boolean firstChokeTurret;
	
	public boolean prepareDark;
	

	public int max_turret_to_mutal;
	public int max_vessel;
	public int max_valkyrie;
	public int max_wraith;
	public int max_battlecruiser;

	
	public int need_vessel_time;
	
	//초반 터렛 건설에 대한 체크
	private int chk_turret;
	
	//유닛체크values()
	public boolean chk_scv;
	public boolean chk_marine;
	public boolean chk_goliath;
	public boolean chk_vulture;
	public boolean chk_siege_tank;
	public boolean chk_vessel;
	public boolean chk_wraith;
	public boolean chk_valkyrie;
	
	//건물체크
	public boolean chk_refinery;
	public boolean chk_barrack;
	public boolean chk_engineering_bay;
	public boolean chk_missile_turret;
	public boolean chk_academy;
	public boolean chk_factory;
	public boolean chk_machine_shop;
	public boolean chk_armory;
	public boolean chk_starport;
	public boolean chk_control_tower;
	public boolean chk_comsat_station;
	public boolean chk_science_facility;
	
	
	public boolean center_gateway = false;
	
	public RespondToStrategy() {
		//클로킹 유닛에 대한 대처
		enemy_dark_templar = false;
		enemy_lurker = false;
		enemy_wraith = false;
		enemy_wraithcloak = false;
		
		enemy_guardian = false;
		
		enemy_shuttle = false;
		enemy_arbiter = false;
		enemy_mutal = false;
		enemy_scout = false;
		enemy_hive = false;
		
		need_vessel = false;
		need_valkyrie = false;
		need_wraith = false;
		need_battlecruiser = false;
		
		mainBaseTurret = false;
		firstChokeTurret = false;
		
		prepareDark = false;
		
		max_vessel = 0;
		max_valkyrie = 0;
		max_wraith = 0;
		max_battlecruiser = 0;
		max_turret_to_mutal = 0;
		
		need_vessel_time = 0;
		
		//초반 터렛 건설에 대한 체크
		chk_turret = 0;
		
		//유닛체크values()
		chk_scv = false;
		chk_marine = false;
		chk_goliath = false;
		chk_vulture = false;
		chk_siege_tank = false;
		chk_vessel = false;
		chk_wraith = false;
		chk_valkyrie = false;
		
		//건물체크
		chk_refinery = false;
		chk_barrack = false;
		chk_engineering_bay = false;
		chk_missile_turret = false;
		chk_academy = false;
		chk_factory = false;
		chk_machine_shop = false;
		chk_armory = false;
		chk_starport = false;
		chk_control_tower = false;
		chk_comsat_station = false;
		chk_science_facility = false;
	}
	
	private static RespondToStrategy instance = new RespondToStrategy();
	
	public static RespondToStrategy Instance() {
		return instance;
	}
	
	public boolean needOfEngineeringBay() {
		
		if(enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_shuttle){
			return true;
		}
		return false;
	}
	
	public boolean needOfVessel() {
		
		if(enemy_arbiter || enemy_hive){
			return true;
		}
		return false;
	}
	
	public void update() {
		max_turret_to_mutal = 0;
//		if(need_vessel==false && need_vessel_time!=0 && MyBotModule.Broodwar.getFrameCount() - need_vessel_time > 5000){
//			need_vessel = true;
//		}
		
		//System.out.println("Respond Strategy Manager On Update!!!!!!!!!!!!!!! ");
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {

			if (unit.getType() == UnitType.Terran_SCV ) {
				chk_scv = true;
			}
			if (unit.getType() == UnitType.Terran_Marine) {
				chk_marine = true;
			}
			if (unit.getType() == UnitType.Terran_Goliath) {
				chk_goliath = true;
			}
			if (unit.getType() == UnitType.Terran_Vulture) {
				chk_vulture = true;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
				chk_siege_tank = true;
			}
			if (unit.getType() == UnitType.Terran_Wraith) {
				chk_wraith = true;
			}
			if (unit.getType() == UnitType.Terran_Valkyrie) {
				chk_valkyrie = true;
			}
			if (unit.getType() == UnitType.Terran_Science_Vessel) {
				chk_vessel = true;
			}
			if (unit.getType() == UnitType.Terran_Refinery) {
				chk_refinery = true;
			}
			if (unit.getType() == UnitType.Terran_Barracks) {
				chk_barrack = true;
			}
			if (unit.getType() == UnitType.Terran_Engineering_Bay) {
				chk_engineering_bay = true;
			}
			if (unit.getType() == UnitType.Terran_Missile_Turret) {
				chk_missile_turret = true;
			}
			if (unit.getType() == UnitType.Terran_Academy) {
				chk_academy = true;
			}
			if (unit.getType() == UnitType.Terran_Comsat_Station) {
				chk_comsat_station = true;
			}
			if (unit.getType() == UnitType.Terran_Factory) {
				chk_factory = true;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop) {
				chk_machine_shop = true;
			}
			if (unit.getType() == UnitType.Terran_Armory) {
				chk_armory = true;
			}
			if (unit.getType() == UnitType.Terran_Starport) {
				chk_starport = true;
			}
			if (unit.getType() == UnitType.Terran_Control_Tower) {
				chk_control_tower = true;
			}
			if (unit.getType() == UnitType.Terran_Science_Facility) {
				chk_science_facility = true;
			}
		}
		
		//최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행 
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			RespondVsProtoss();
		}else if (InformationManager.Instance().enemyRace == Race.Terran) {
			RespondVsTerran();
		}else{
			RespondVsZerg();
		}
		
		RespondExecute();
	}
		
	boolean once = true;
	public void RespondVsProtoss() {
		boolean blocked = true;
		//2gate zealot
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.protossBasic_DoublePhoto){
			
			if(MyBotModule.Broodwar.getFrameCount() < 10000){
				BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
				BuildOrderItem checkItem = null; 
	
				if (!tempbuildQueue.isEmpty()) {
					checkItem= tempbuildQueue.getHighestPriorityItem();
					while(true){
						if(tempbuildQueue.canGetNextItem() == true){
							tempbuildQueue.canGetNextItem();
						}else{
							break;
						}
						tempbuildQueue.PointToNextItem();
						checkItem = tempbuildQueue.getItem();
						
						if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Command_Center){
							tempbuildQueue.removeCurrentItem();
						}
					}
				}
			}
			if(once){
							
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,false);
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) 
						+BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory) 
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) <= 2) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,true);
				}
				once = false;
			}
			//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,true);
			
			
			
//			if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) >= 1){
//				
//				
//				if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
//						
//						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//						BuildOrderItem checkItem = null; 
//			
//						if (!tempbuildQueue.isEmpty()) {
//							checkItem= tempbuildQueue.getHighestPriorityItem();
//							while(true){
//								if(tempbuildQueue.canGetNextItem() == true){
//									tempbuildQueue.canGetNextItem();
//								}else{
//									break;
//								}
//								tempbuildQueue.PointToNextItem();
//								checkItem = tempbuildQueue.getItem();
//								
//								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Factory){
//									tempbuildQueue.removeCurrentItem();
//								}
//								
//								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop){
//									tempbuildQueue.removeCurrentItem();
//								}
//								if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Vulture){
//									tempbuildQueue.removeCurrentItem();
//								}
//								
//							}
//						}
//						
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop,true);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_SCV,false);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_SCV,false);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_SCV,false);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_SCV,false);
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_SCV,false);
//					}
//				}
//				
		}
		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush) {
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1
					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1) {
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3) {
					if (StrategyManager.Instance().LiftChecker == false
							&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1) {
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
					if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
							&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker,
									null) == 0) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}
		}
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush){
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1){
				if(center_gateway){
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
						if(StrategyManager.Instance().LiftChecker == false && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							}
						}
						if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
								&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){

							TilePosition bunkerPos = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
							ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker, bunkerPos,true);

						}
					}
				}else{
					if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) >= 1) {
						if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
							if(StrategyManager.Instance().LiftChecker == false && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
								if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
								}
							}
							if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
									&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DragoonPush
				||StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_PhotonRush){
			
		}
	
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DoubleNexus
				&& StrategyManager.Instance().getCurrentStrategyBasic() != StrategyManager.Strategys.protossBasic_DoublePhoto){
			
			if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}
		}else{
			if(MyBotModule.Broodwar.enemy().allUnitCount(UnitType.Protoss_Nexus) ==2){
				if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					}
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Shuttle){
			enemy_shuttle = true;
		}
		
		//protossException_Dark start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Dark){
			enemy_dark_templar = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 1;
			//}
		}
		//protossException_Dark end
		
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Arbiter){
			enemy_arbiter = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 2;
			//}
		}
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Scout){
			enemy_scout = true;
		}
	}
	
	public void RespondVsTerran() {
		
		max_wraith = 5;
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_Bionic){
			max_wraith = 0;
			
			//스타포트 지우기
			BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
			BuildOrderItem checkItem = null; 

			if (!tempbuildQueue.isEmpty()) {
				checkItem= tempbuildQueue.getHighestPriorityItem();
				while(true){
					if(tempbuildQueue.canGetNextItem() == true){
						tempbuildQueue.canGetNextItem();
					}else{
						break;
					}
					tempbuildQueue.PointToNextItem();
					checkItem = tempbuildQueue.getItem();
					
					if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Starport){
						tempbuildQueue.removeCurrentItem();
					}
				}
			}
			
		}
		
		
		//terranException_Wraith start
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_MechanicWithWraith){
			enemy_wraith = true;
		}
		//terranException_Wraith start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.terranException_WraithCloak){
			enemy_wraithcloak = true;
			need_vessel = true;
			max_vessel = 2;
		}
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_MechanicWithWraith){
			enemy_wraith = true;
		}
	}
		
	boolean expanchcker = false;
	public void RespondVsZerg() {	
		
		if(StrategyManager.Instance().LiftChecker == false && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) > 1){
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_PrepareLurker
			||StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_FastLurker){
			enemy_lurker = true;
			need_vessel = true;
			max_vessel = 1;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_HighTech){
			need_vessel = true;
			max_vessel = 4;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_MutalMany){
			
			if(MyBotModule.Broodwar.self().hasResearched(TechType.Irradiate) ==false &&MyBotModule.Broodwar.self().isResearching(TechType.Irradiate) ==false){
				if(BuildManager.Instance().buildQueue.getItemCount(TechType.Irradiate) < 1){
					BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate);
				}
			}
			need_vessel = true;
			max_vessel = 4;
		}
//		else{
//			need_valkyrie = false;
//			max_valkyrie = 0;
//		}
		
//		if(MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost) == 1){
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_Mutal ||
                StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_MutalMany ||
                        StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_HydraMutal ||
                                StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_LingMutal)
		{
			max_turret_to_mutal = 3;
		}
	
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_OnLyLing){
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer) < 5	){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) < 1){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_Mutal){
			enemy_mutal = true;
		}
		
		
		if(expanchcker==false){
			BaseLocation enemyMainbase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
			
			if(enemyMainbase != null && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1 && StrategyManager.Instance().getFacUnits() >= 25){
				
				
				BaseLocation enemyFEbase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
				List<BaseLocation> enemybases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
				
				for(BaseLocation check : enemybases){
					if(enemyFEbase.equals(check)){
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
							expanchcker = true;
							
						}
					}
				}
			}
			if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==2)
				expanchcker = true;
		}
	}
		
		
	public void RespondExecute() {

		// if(prepareDark == true){
		// if(!chk_engineering_bay){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay)
		// +
		// ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay,
		// null) == 0){
		// BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
		// BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// //}
		// }
		// }
		// }
		
		//marine for fast zergling and zealot start
		if(StrategyManager.Instance().LiftChecker == false && CombatManager.Instance().FastZerglingsInOurBase > 0 || CombatManager.Instance().FastZealotInOurBase > 0){
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
				}
			}
		}
		//marine for fast zergling and zealot end
		// enemy_dark_templar & enemy_lurker & enemy_wraith 클로킹 유닛에 대한 대비
		// if(enemy_dark_templar || enemy_wraith || enemy_lurker ||
		// enemy_arbiter || enemy_mutal || prepareDark){
		if (enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_arbiter || prepareDark) {
			if (need_vessel_time == 0) {
				need_vessel_time = MyBotModule.Broodwar.getFrameCount();
			}

			if (!chk_comsat_station && StrategyManager.Instance().getFacUnits() >= 32) {
				// 컴셋이 없다면
				if (!chk_academy) {
					// 아카데미가 없다면
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy,
									null) == 0) {
						// 지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				} else {
					if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
						// 아카데미가 완성되었고
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1
								&& ConstructionManager.Instance()
										.getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
							// 빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
							if (MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
								BuildManager.Instance().buildQueue
										.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
							}
						}
					}
				}
			}

			if(chk_engineering_bay == false && (InformationManager.Instance().enemyRace != Race.Protoss || MyBotModule.Broodwar.getFrameCount() > 5000)){
				
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}else{
				int turretcnt = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
					BaseLocation tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
					BaseLocation tempExpLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
					Chokepoint tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
					Chokepoint temp2ChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
 
					mainBaseTurret = false;
					firstChokeTurret = false;
					Boolean secondChokeTurret = false;
					Boolean firstChokeMainHalfTurret = false;
					Boolean firstChokeExpHalfTurret = false;
					
//					MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),180, Color.White);
 					if (tempBaseLocation != null) {
 						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempBaseLocation.getPosition(),350+turretcnt*15);
// 						MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),300+turretcnt*15, Color.Red);
 						for(Unit turret : turretInRegion){
 							if (turret.getType() == UnitType.Terran_Missile_Turret) {
 								mainBaseTurret = true;
 							}
 						}
						if (!mainBaseTurret) {
							//if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getRegion().getCenter().toTilePosition(), 300)
							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300)
							
								+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), true);
							}
						}
 					}
 
 					if (tempBaseLocation != null) { 
   						//Position firstChokeMainHalf = new Position((tempBaseLocation.getRegion().getCenter().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getRegion().getCenter().getY() + tempChokePoint.getY()*2)/3 - 60);
 						Position firstChokeMainHalf = new Position((tempBaseLocation.getPosition().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getPosition().getY() + tempChokePoint.getY()*2)/3 - 60);
   						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(firstChokeMainHalf,180+turretcnt*15);
//   						MyBotModule.Broodwar.drawCircleMap(firstChokeMainHalf,180+turretcnt*15, Color.Orange);	

   						for(Unit turret : turretInRegion){
   							if (turret.getType() == UnitType.Terran_Missile_Turret) {
   								firstChokeMainHalfTurret = true;
   							}
   						}
   						if (!firstChokeMainHalfTurret) {
   							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeMainHalf.toTilePosition(), 180) 
   									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), 180) == 0){
   								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), true);
   							}
   						}
					}
 					
 					if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
						if (tempChokePoint != null) {
							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(),150+turretcnt*15);
//							MyBotModule.Broodwar.drawCircleMap(tempChokePoint.getCenter(),150+turretcnt*15, Color.Blue);
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									firstChokeTurret = true;
								}
							}
							if (!firstChokeTurret) {
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) 
									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) == 0){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), true);
								}
							}
						}  
					}else{
						if (tempBaseLocation != null) { 
	   						Position firstChokeExpHalf = new Position((tempExpLocation.getPosition().getX()*2 + tempChokePoint.getX())/3, (tempExpLocation.getPosition().getY()*2 + tempChokePoint.getY())/3);
	   						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(firstChokeExpHalf.getPoint(),210+turretcnt*15);
//	   						MyBotModule.Broodwar.drawCircleMap(firstChokeExpHalf,150+turretcnt*15, Color.Blue);
	   						for(Unit turret : turretInRegion){
	   							if (turret.getType() == UnitType.Terran_Missile_Turret) {
	   								firstChokeExpHalfTurret = true;
	   							}
	   						}
	   						if (!firstChokeExpHalfTurret) {
	   							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeExpHalf.toTilePosition(), 150) 
	   									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), 150) == 0){
	   								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), true);
	   							}
	   						}
						}
						
					}
 					
 					if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1){
 						if (temp2ChokePoint != null) {
 							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(temp2ChokePoint.getCenter(),100+turretcnt*15);

 							for(Unit turret : turretInRegion){
 								if (turret.getType() == UnitType.Terran_Missile_Turret) {
 									secondChokeTurret = true;
 								}
 							}
 							if (!secondChokeTurret) {
 								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) 
 										+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) == 0){
 									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,  temp2ChokePoint.getCenter().toTilePosition(), true);
 								}
 							}
 						}
 					}
				}
			}
		}
		
		if(enemy_scout || enemy_shuttle || enemy_wraith){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}else{
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
		
		if(enemy_arbiter){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}else{
				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
		
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_BattleCruiser){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.AttackIsland){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else{
			need_battlecruiser = false;
			max_battlecruiser = 0;
		}

		if (max_turret_to_mutal != 0) {

			if (!chk_engineering_bay) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) 
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay,	null) == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			} else {
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
					int build_turret_cnt = 0;
					int turretcnt =  MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
					//지역 멀티
					
					BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					BaseLocation expBase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
					if (mainBase != null) {
						
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(mainBase.getPosition(), 550+turretcnt*15);
						build_turret_cnt = 0;
						for(Unit unit: turretInRegion){
							if (unit.getType() == UnitType.Terran_Missile_Turret) {
								build_turret_cnt++;
							}
						}

						if (build_turret_cnt < max_turret_to_mutal) {
							if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(), 300) < 1
									&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	mainBase.getPosition().toTilePosition(), 300) == 0) {
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(),true);
							}
						}
					}
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) > 1){
						if (expBase != null) {
							
							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(expBase.getPosition(), 300+turretcnt*15);
							build_turret_cnt = 0;
							for(Unit unit: turretInRegion){
								if (unit.getType() == UnitType.Terran_Missile_Turret) {
									build_turret_cnt++;
								}
							}
	
							if (build_turret_cnt < max_turret_to_mutal) {
								if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(), 300) < 1
										&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	expBase.getPosition().toTilePosition(), 300) == 0) {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(),true);
								}
							}
						}
					}
				}
			}
		}
	}
}