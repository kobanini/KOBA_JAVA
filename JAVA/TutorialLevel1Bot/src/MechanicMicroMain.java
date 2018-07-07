
@Deprecated
public class MechanicMicroMain {
	
//	private enum ForceType { CHECKER, WATCHER }
//	private Map<Integer, ForceType> forceTypeMap = new HashMap<>();
//	
//	// 전체 메카닉 유닛별 리스트
//	private List<Unit> vultureList = new ArrayList<>();
//	private List<Unit> tankList = new ArrayList<>();
//	private List<Unit> goliathList = new ArrayList<>();
//	private List<Unit> totalUnitList = new ArrayList<>();
//	
//	// 특수임무를 가진 유닛 리스트
//	private static List<Unit> checkerList = new ArrayList<>();
//	private static List<Unit> watcherList = new ArrayList<>();
//	
//	private List<UnitInfo> attackerEnemies = new ArrayList<>();
//	private List<UnitInfo> checkerEnemies = new ArrayList<>();
//	private List<UnitInfo> watcherEnemies = new ArrayList<>();
//	
//	private MechanicMicroVulture mechanicVulture = new MechanicMicroVulture();
//	private MechanicMicroTank mechanicTank = new MechanicMicroTank();
//	private MechanicMicroGoliath mechanicGoliath = new MechanicMicroGoliath();
//	
//	private MechanicMicroVulture mechanicChecker = new MechanicMicroVulture();
//	private MechanicMicroVulture mechanicWatcher = new MechanicMicroVulture();
//	
//	// executeMechanicTerran 실행전 유효한(CommandUtil.IsValidUnit) 유닛들로 세팅된다.
//	public void setUnits(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList) {
//		this.vultureList = vultureList;
//		this.tankList = tankList;
//		this.goliathList = goliathList;
//		
//		this.totalUnitList.clear();
//		this.totalUnitList.addAll(vultureList);
//		this.totalUnitList.addAll(tankList);
//		this.totalUnitList.addAll(goliathList);
//	}
//	
//	private SquadOrder mainAttackOrder;
//	private SquadOrder checkOrder;
//	private SquadOrder watchOrder;
//	
//	// *** 메카닉 테란 컨트롤 실행 ***
//	public void executeMechanicTerran(SquadOrder order) {
//		prepareMechanic(order);
//		nearByEnemies();
//		
////		executeVultureControl();
////		executeTankControl();
////		executeGoliathControl();
//		executeControl();
//	}
//	
//	private void prepareMechanic(SquadOrder order) {
//		// 각각의 squad order 지정
//		SquadOrder checkOrder = new SquadOrder(SquadOrderType.CHECK, null, MicroSet.Combat.CHECKER_RADIUS, "Check it out");
//		SquadOrder watchOrder = null;
//		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//		if (enemyBase != null) {
//			watchOrder = new SquadOrder(SquadOrderType.WATCH, enemyBase.getPosition(), MicroSet.Combat.WATCHER_RADIUS, "Watch over");
//		}
//		this.mainAttackOrder = order;
//		this.checkOrder = checkOrder;
//		this.watchOrder = watchOrder;
//		
//		// checker 지정
//		forceTypeMap.clear(); // forceTypeMap을 세팅하기 전 clear한다.
//		checkerList = filterInvalidUnit(checkerList, ForceType.CHECKER);
//		for (Unit vulture : vultureList) {
//			if (checkerList.size() >= MicroSet.Vulture.maxNumChecker) {
//				break;
//			}
//			if (forceTypeMap.get(vulture.getID()) != null) {
//				continue;
//			}
//			if (!checkerList.contains(vulture)) {
//				checkerList.add(vulture);
//			}
//			forceTypeMap.put(vulture.getID(), ForceType.CHECKER);
//		}
//
//		// watcher unit 지정
//		if (watchOrder != null) {
//			watcherList = filterInvalidUnit(watcherList, ForceType.WATCHER);
//			Unit leaderOfWatcher = MicroUtils.leaderOfUnit(watcherList, watchOrder.getPosition());
//			for (Unit vulture : vultureList) {
//				if (leaderOfWatcher == null || vulture.getDistance(leaderOfWatcher.getPosition()) < watchOrder.getRadius()) {
//					if (forceTypeMap.get(vulture.getID()) != null) {
//						continue;
//					}
//					if (!watcherList.contains(vulture)) {
//						watcherList.add(vulture);
//					}
//					forceTypeMap.put(vulture.getID(), ForceType.WATCHER);
//				}
//			}
//		}
//		if (CommonUtils.executeRotation(0, 48)) {
//			System.out.println("vultureList.size() : " + vultureList.size());
//			System.out.println("tankList.size() : " + tankList.size());
//			System.out.println("goliathList.size() : " + goliathList.size());
//			System.out.println("checkerList.size() : " + checkerList.size());
//			System.out.println("watcherList.size() : " + watcherList.size());
//		}
//	}
//	
//	private void addNearByEnemis(List<UnitInfo> enemies, Position position, int radius) {
//		InformationManager.Instance().getNearbyForce(enemies, position, InformationManager.Instance().enemyPlayer, radius);
//	}
//	
//	private void nearByEnemies() {
//		// mainAttacker의 적
//		attackerEnemies.clear();
//		checkerEnemies.clear();
//		watcherEnemies.clear();
//		
//		for (Unit unit : totalUnitList) {
//			ForceType forceType = forceTypeMap.get(unit);
//			if (forceType != null) {
//				switch (forceType) {
//				case CHECKER:
//					addNearByEnemis(checkerEnemies, unit.getPosition(), checkOrder.getRadius());
//					break;
//					
//				case WATCHER:
//					addNearByEnemis(watcherEnemies, unit.getPosition(), watchOrder.getRadius());
//					break;
//				}
//			}
//			addNearByEnemis(attackerEnemies, unit.getPosition(), mainAttackOrder.getRadius());
//		}
//		if (CommonUtils.executeRotation(0, 10)) {
//			System.out.println("attackerEnemies.size() : " + attackerEnemies.size());
//			System.out.println("checkerEnemies.size() : " + checkerEnemies.size());
//			System.out.println("watcherEnemies.size() : " + watcherEnemies.size());
//		}
//	}
//	
//	private List<Unit> filterInvalidUnit(List<Unit> unitList, ForceType forceType) {
//		List<Unit> newUnitList = new ArrayList<>();
//		for (Unit unit : unitList) {
//			if (CommandUtil.IsValidUnit(unit)) {
//				newUnitList.add(unit);
//				forceTypeMap.put(unit.getID(), forceType);
//			}
//		}
//		return newUnitList;
//	}
//	
//	private void executeControl() {
//		mechanicTank.prepareMechanic(mainAttackOrder, attackerEnemies);
//		mechanicTank.prepareMechanicAdditional(tankList, goliathList, 0);
//		
//		mechanicGoliath.prepareMechanic(mainAttackOrder, attackerEnemies);
//		
//		mechanicVulture.prepareMechanic(mainAttackOrder, attackerEnemies);
//		mechanicVulture.prepareMechanicAdditional(tankList, goliathList, false);
//		
//		mechanicChecker.prepareMechanic(checkOrder, checkerEnemies);
//		mechanicChecker.prepareMechanicAdditional(tankList, goliathList, false);
//		
//		mechanicWatcher.prepareMechanic(watchOrder, watcherEnemies);
//		mechanicWatcher.prepareMechanicAdditional(tankList, goliathList, false);
//		
//		for (Unit unit : totalUnitList) {
//			ForceType forceType = forceTypeMap.get(unit);
//			if (forceType != null) {
//				if (forceType == ForceType.CHECKER) {
//					mechanicVulture.executeMechanicMicro(unit);
//				} else if (forceType == ForceType.WATCHER) {
//					mechanicVulture.executeMechanicMicro(unit);
//				} else {
//					MyBotModule.Broodwar.printf("unknown forceType : " + forceType);
//				}
//			} else if (unit.getType() == UnitType.Terran_Vulture) {
//				mechanicVulture.executeMechanicMicro(unit);
//			} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
//					|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
//				mechanicTank.executeMechanicMicro(unit);
//			} else if (unit.getType() == UnitType.Terran_Vulture) {
//				mechanicGoliath.executeMechanicMicro(unit);
//			}
//		}
//		
//	}
}


