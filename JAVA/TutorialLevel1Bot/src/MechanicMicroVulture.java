

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class MechanicMicroVulture extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> notVultureUnitList = new ArrayList<>();
	private int saveUnitLevel = 1;
	
	private boolean attackWithMechanics = false;
	private int stickToMechanicRadius = 0;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = MicroUtils.filterTargetInfos(enemiesInfo, false);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, boolean attackWithMechanics) {
		this.notVultureUnitList.clear();
		this.notVultureUnitList.addAll(tankList);
		this.notVultureUnitList.addAll(goliathList);
		this.saveUnitLevel = saveUnitLevel;
		this.attackWithMechanics = attackWithMechanics && notVultureUnitList.size() > 0;
		if (this.attackWithMechanics) {
			this.stickToMechanicRadius = 120 + (int) (Math.log(vultureList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToMechanicRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit vulture) {
		if (!CommonUtils.executeUnitRotation(vulture, LagObserver.groupsize())) {
			return;
		}
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(vulture, enemiesInfo, null, saveUnitLevel); // 0: flee, 1: kiting, 2: attack

		KitingOption kOpt = KitingOption.vultureKitingOption();
		Position retreatPosition = order.getPosition();
		switch (decision.getDecision()) {
		case 0: // flee �ƿ� �ο� ������ ���� ���� : ��ū, ĳ��, ���� �� ���ٱ���, �Ǵ� regroup ��
			if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(vulture, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			if (order.getType() == SquadOrderType.WATCH) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			} else if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					retreatPosition = travelBase.getPosition();
				}
			}

			boolean haveToFight = true;
			Unit closeMechanic = null;
			if (attackWithMechanics) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit mechanicUnit : notVultureUnitList) {
					int dist = vulture.getDistance(mechanicUnit.getPosition());
					if (dist < closeDist) {
						closeMechanic = mechanicUnit;
						closeDist = dist;
						// ����� ���� ��ī�������� ������ �ο��.
						if (closeDist < stickToMechanicRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			} else {
				for (Unit notVultureUnit : notVultureUnitList) {
					if (vulture.getDistance(notVultureUnit) < MicroSet.Common.MAIN_SQUAD_COVERAGE) {
						kOpt = KitingOption.defaultKitingOption();
						retreatPosition = notVultureUnit.getPosition();
						break;
					}
				}
			}

			if (haveToFight) {
				Unit enemy = MicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && vulture.isInWeaponRange(enemy)) {
					vulture.holdPosition();
				} else {
					kOpt.setGoalPosition(retreatPosition);
					MicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
				}
			} else {
				CommandUtil.move(vulture, closeMechanic.getPosition());
			}
			break;
			
		case 2: // attack move
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			
			// checker : ������ ��ǥ����(travelBase)���� �̵�. (order position�� null�̴�.)
			// watcher : ��ǥ����(��base)���� �̵�. �տ� ������ �ʴ� ���� ������ ����base�� ����.
			Position movePosition = order.getPosition();
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					movePosition = travelBase.getPosition();
				}
			}
			
			if (MicroSet.Common.versusMechanicSet()) {
				// �׶����� go
				int distToOrder = vulture.getDistance(movePosition);
				if (distToOrder <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition�� �ѷ��� ������ �����.
					if (vulture.isIdle() || vulture.isBraking()) {
						if (!vulture.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), 100);
							CommandUtil.attackMove(vulture, randomPosition);
						}
					}
				} else {
					CommandUtil.attackMove(vulture, movePosition);
				}
				
			} else {
				// �̵��������� attackMove�� ����.
				if (vulture.getDistance(movePosition) > order.getRadius()) {
//					if (saveUnit) {
						CommandUtil.move(vulture, movePosition);
//					} else {
//						CommandUtil.attackMove(vulture, movePosition);
//					}
					
				} else { // ������ ����
					if (vulture.isIdle() || vulture.isBraking()) {
						Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), 100);
						CommandUtil.attackMove(vulture, randomPosition);
					}
				}
			}
			break;
		}
	}
	
	private boolean useReservedSpiderMine(Unit vulture) {
		// ���θż��� ����� ��ó��� �ż�����
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine != null) {
			CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		return false;
	}
	
	private boolean reserveSpiderMine(Unit vulture) {
		
		Position minePosition = null;
		
		if (CombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.MINE_STRATEGY_FOR_TERRAN) > 0) {
			BaseLocation enemyFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
			if (enemyFirstExpansion != null) {
				int distance = vulture.getDistance(enemyFirstExpansion.getPosition());
				if (distance < MicroSet.Tank.SIEGE_MODE_MAX_RANGE && MicroUtils.isSafePlace(enemyFirstExpansion.getPosition())) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, enemyFirstExpansion.getPosition(), true, MicroSet.Vulture.spiderMineNumPerPosition * 2);
				}
			}
			
			if (minePosition == null) {
				Position enemyReadyPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
				
				int distance = vulture.getDistance(enemyReadyPos);
				if (distance <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition);
				}
			}
			
		} else {
			minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture, MicroSet.Vulture.spiderMineNumPerGoodPosition);
			
			if (minePosition == null && order.getType() == SquadOrderType.WATCH) {
//				// �� ���ֿ��� ���� �����ϱ�
//				if (InformationManager.Instance().enemyRace == Race.Terran && saveUnitLevel == 0) {
//					minePosition = SpiderMineManger.Instance().enemyPositionToMine(vulture, enemiesInfo);
//				}
				int mineCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Vulture_Spider_Mine);
				if (mineCount <= MicroSet.Vulture.MINE_MAX_NUM) {
					// �� �������� ���� �ɱ�
					Region vultureRegion = BWTA.getRegion(vulture.getPosition());
					BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);

					int minePrepareLevel = SpiderMineManger.Instance().getMineInMyBaseLevel(); // 0: �����ż�X, ������������, 1: �����ż�X, ������������, 2: ��������, ������������
					boolean vultureInMyBaseRegion = vultureRegion == BWTA.getRegion(base.getPosition());
					if (!vultureInMyBaseRegion || minePrepareLevel >= 2) { // ���� region���� ���� ��ġ����(�� �н�Ʈ ��ũ, �н�Ʈ ��Ŀ ���� ��� �ż�)
						boolean occupiedRegion = false;
						for (BaseLocation occupiedBase : occupiedBases) { // �ո��� ������ ���������� ������ ���� �ż���(��, �������̺�, ��� Ǫ���� ��� ���� �ż�)
							if (vultureRegion == BWTA.getRegion(occupiedBase.getPosition())) {
								occupiedRegion = true;
								break;
							}
						}
						if (!occupiedRegion || (!vultureInMyBaseRegion && minePrepareLevel >= 1)) {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition); // �׿ܿ��� �� ����
						} else {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerGoodPosition);
						}
					}
				}
			}
		}
		if (minePosition != null) { // �ż��� ������ �ִٸ� ����
			return true;
		}
		return false;
	}
	
	private boolean removeSpiderMine(Unit vulture) {
		return SpiderMineManger.Instance().removeMine(vulture);
	}

}
