

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class VultureTravelManager {

	private Map<String, Integer> guerillaTimeMap = new HashMap<>();
	private Map<Integer, TravelSite> vultureSiteMap = new HashMap<>();
	
	public Map<Integer, TravelSite> getSquadSiteMap() {
		return vultureSiteMap;
	}
	
	private final List<TravelSite> travelSites = new ArrayList<>();
	
	private boolean initialized = false;

	private static VultureTravelManager instance = new VultureTravelManager();
	
	private VultureTravelManager() {}
	
	public static VultureTravelManager Instance() {
		return instance;
	}
	
	private static boolean timeToShiftDuty = false;
	
	public static boolean timeToShiftDuty() {
		if (VultureTravelManager.timeToShiftDuty) {
			VultureTravelManager.timeToShiftDuty = false;
			return true;
		}
		return false;
	}
	
	public void init() {
		List<BaseLocation> otherBases = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		
		if (!otherBases.isEmpty()) {
			travelSites.clear();
			for (BaseLocation base : otherBases) {
				travelSites.add(new TravelSite(base, 0, 0, 0));
			}
			initialized = true;
		}
	}
	
	public void update() {
		if (!initialized) {
			init();
			return;
		}
		
		// 1. ��ó �̵��� ����
		for (TravelSite travelSite : travelSites) {
			if (MyBotModule.Broodwar.isVisible(travelSite.baseLocation.getTilePosition())) {
				// 1. �þ߰� �������ٸ� visitFrame�� ��� ������Ʈ �Ѵ�.
				travelSite.visitFrame = MyBotModule.Broodwar.getFrameCount();
				
				// 2. �þ߰� ������ travelSite�� �湮������ �������� travelSite�� �����Ѵ�.
				Integer relatedVultureId = null;
				for (Integer id : vultureSiteMap.keySet()) {
					TravelSite site = vultureSiteMap.get(id);
					if (site.baseLocation.getPosition().equals(travelSite.baseLocation.getPosition())) {
						relatedVultureId = id; // �湮���� ������ ��
						break;
					}
				}
				
				if (relatedVultureId != null) {
//					System.out.println("change travel site");
					BaseLocation currentBase = vultureSiteMap.get(relatedVultureId).baseLocation;
					vultureSiteMap.remove(relatedVultureId);
					getBestTravelSite(relatedVultureId, currentBase); // travelSite ����(currentBase���� ����� ��)
				}
			}
		}

		// 2. �湮�� ����ð�
		Integer expiredVisitor = null;
		for (Integer vultureId : vultureSiteMap.keySet()) {
			TravelSite site = vultureSiteMap.get(vultureId);
			if (site.visitAssignedFrame < MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.CHECKER_INTERVAL_FRAME) {
				expiredVisitor = vultureId;
				break;
			}
		}
		if (expiredVisitor != null) {
			vultureSiteMap.remove(expiredVisitor);
		}

		// 3. �Ը��� �� ���� �ð� ����
		String ignoreExpiredSquad = null;
		for (String squadName : guerillaTimeMap.keySet()) {
			Integer startTime = guerillaTimeMap.get(squadName);
			if (startTime != null && startTime < MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.CHECKER_INTERVAL_FRAME) {
				ignoreExpiredSquad = squadName;
				break;
			}
		}
		if (ignoreExpiredSquad != null) {
			guerillaTimeMap.remove(ignoreExpiredSquad);
		}
		
		// 4. ��ó ��å ����(�� �ֿ� ����Ʈ �ż� ���μ�, checker��)
		if (CommonUtils.executeRotation(0, 48)) {
			int vultureCount = InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture);
			int mineCount = InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture_Spider_Mine);
			
			int mineNumPerPosition = vultureCount / 3 + 1;
			MicroSet.Vulture.spiderMineNumPerPosition = mineNumPerPosition;
			
			int bonumNumPerPosition = (mineCount - 10) / 8;
			if (bonumNumPerPosition > 0) {
				MicroSet.Vulture.spiderMineNumPerPosition += bonumNumPerPosition;
			}

			MicroSet.Vulture.spiderMineNumPerGoodPosition = vultureCount / 10 + 1;

			int checkerNum = vultureCount / 4; // 3��1 �����̴�.
			MicroSet.Vulture.maxNumChecker = checkerNum >= 3 ? 3 : checkerNum; // ������ó �ִ� 3��
//			System.out.println("vultureCount / maxNumChecker : " + vultureCount + " / " + MicroSet.Vulture.maxNumChecker);
		}
	}

	// ��ó�����尡 ������ base�� �����Ѵ�.
	// 1. �湮���� ���� ������ location���� �̵�
	// 2. �湮���� ���� ������ ��Ұ� �������̸�(�ƿ� ������ �ȵ�) �Ҵ���� ���� ������ location���� �̵�
	// 3. �湮 �� �Ҵ���� �����ϰ� ������ ��Ұ� �������̸�, ����� ������ ����. (currentBase���� ���� ����� ��, squadName�� �Է¹޴� ��� �� �ո��翡�� ���� ����� ��)
	public BaseLocation getBestTravelSite(Integer vultureId) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		return getBestTravelSite(vultureId, firstExpansion);
	}
	
	public BaseLocation getBestTravelSite(Integer vultureId, BaseLocation currentBase) {
		if (!initialized) {
			return null;
		}
		
		TravelSite site = vultureSiteMap.get(vultureId);
		if (site != null) {
			return site.baseLocation;
		}
		
		int longestVisitPassedFrame = MicroSet.Vulture.CHECKER_INTERVAL_FRAME;
		double shortestDistance = 999999.0;
		
		TravelSite bestTravelSite = null;
		
		int currentFrame = MyBotModule.Broodwar.getFrameCount();
		for (TravelSite travelSite : travelSites) {
			if (vultureSiteMap.values().contains(travelSite)) {
				continue;
			}
			
			int visitPassedFrame = currentFrame - travelSite.visitFrame;
			double distance = currentBase.getGroundDistance(travelSite.baseLocation);
			
			if (visitPassedFrame > longestVisitPassedFrame ||
					(visitPassedFrame == longestVisitPassedFrame && distance < shortestDistance)) {
				longestVisitPassedFrame = visitPassedFrame;
				shortestDistance = distance;
				bestTravelSite = travelSite;
			}
		}
		
		if (bestTravelSite != null) {
			vultureSiteMap.put(vultureId, bestTravelSite);
			bestTravelSite.visitAssignedFrame = MyBotModule.Broodwar.getFrameCount();
			return bestTravelSite.baseLocation;
		} else {
			vultureSiteMap.remove(vultureId);
			timeToShiftDuty = true;
			return null;
		}
	}
	
	// 1) �Ը��������� ������ �����ϴ� travelSites�̾�� �Ѵ�.
	// 2) �Ը��������� guerillaFrame��(Ÿ������ ������ frame) �����ð� �̻� ������� �Ѵ�.(�Ը���� ��ó�� ��� �Ҹ�Ǵ� ���� �����ϱ� ����)
	// 3) �ϲ��� ������ �켱������ ����. ������ ������ �켱������ ����.(Ư�� ���Ÿ���� ������)
	public BaseLocation getBestGuerillaSite(List<Unit> assignableVultures) {
		int vulturePower = CombatExpectation.getVulturePower(assignableVultures);
		int currFrame = MyBotModule.Broodwar.getFrameCount();
		
		int bestScore = 0;
		TravelSite bestTravelSite = null;
		
		for (TravelSite travelSite : travelSites) {
			if (assignableVultures.size() < MicroSet.Vulture.GEURILLA_FREE_VULTURE_COUNT && currFrame - travelSite.guerillaExamFrame < MicroSet.Vulture.GEURILLA_INTERVAL_FRAME) {
				continue;
			}
			 
			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(
					travelSite.baseLocation.getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS, true);
			if (enemiesInfo.isEmpty()) { // ������ �������� ����
				continue;
			}
			
			// �Ȱ����� �� ������ ������ �Ը��� Ÿ������ �������� Ȯ���Ѵ�.			
			int enemyPower = CombatExpectation.enemyPowerByUnitInfo(enemiesInfo, false);
			int score = CombatExpectation.guerillaScoreByUnitInfo(enemiesInfo);
			
			if (vulturePower > enemyPower && score > bestScore) {
				bestScore = score;
				bestTravelSite = travelSite;
			}
		}
		
		if (bestTravelSite != null) {
			bestTravelSite.guerillaExamFrame = currFrame;
			return bestTravelSite.baseLocation;
		} else {
			return null;
		}
	}
	
	public boolean guerillaIgnoreModeEnabled(String squadName) {
		return guerillaTimeMap.containsKey(squadName);
	}
	public void guerillaStart(String squadName) {
		guerillaTimeMap.put(squadName, MyBotModule.Broodwar.getFrameCount());
	}
}

class TravelSite {
	TravelSite(BaseLocation baseLocation, int visitFrame, int visitAssignedFrame, int guerillaExamFrame) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.visitAssignedFrame = visitAssignedFrame;
		this.guerillaExamFrame = guerillaExamFrame;
	}
	BaseLocation baseLocation;
	int visitFrame;
	int visitAssignedFrame;
	int guerillaExamFrame;

	@Override
	public String toString() {
		return "TravelSite [baseLocation=" + baseLocation.getPosition() + ", visitFrame=" + visitFrame + ", visitAssignedFrame="
				+ visitAssignedFrame + ", guerillaExamFrame=" + guerillaExamFrame + "]";
	}
}