

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;

public class MicroVessel extends MicroManager {
	
	// 유닛별 실행시 orderMap에서 unitId를 key로 하여 각 unit별 order를 사용한다.
	private static Map<Integer, SquadOrder> orderMap = new HashMap<>();
	
	// CombatManager.updateVesselSquad에서 명령을 지정한다. by insaneojw
	public static void setUnitOrder(Integer unitId, SquadOrder unitOrder) {
		orderMap.put(unitId, unitOrder);
	}
	public static void getUnitOrder(Integer unitId) {
		orderMap.get(unitId);
	}
	
	public static void removeInvalidUnitOrder() {
		List<Integer> removeKeys = new ArrayList<>();
		for (Integer unitId : orderMap.keySet()) {
			if (!CommandUtil.IsValidUnit(MyBotModule.Broodwar.getUnit(unitId))) {
				removeKeys.add(unitId);
			}
		}
		for (Integer unitId : removeKeys) {
			orderMap.remove(unitId);
		}
	}
	
	 
	private final static double sVesselCheckSpeed = UnitType.Terran_Science_Vessel.topSpeed()*8;
	private final static int sVesselCheckRadius = UnitType.Terran_Science_Vessel.sightRange();

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> sVessels = getUnits();
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setCooltimeAlwaysAttack(false);
		kitingOption.setUnitedKiting(false);
		kitingOption.setGoalPosition(order.getPosition());
		kitingOption.setFleeAngle(MicroSet.FleeAngle.WIDE_ANGLE);
		
		boolean unitedKiting = kitingOption.isUnitedKiting();
		Position goalPosition = kitingOption.getGoalPosition();
		Integer[] fleeAngle = kitingOption.getFleeAngle();
		Position originalPos = order.getPosition();
				
		for (Unit sVessel : sVessels) {
			if (!CommonUtils.executeUnitRotation(sVessel, LagObserver.groupsize())) {
				continue;
			}
			
			if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_MutalMany){
				//싸베 이리디 쓰기
				if(sVessel.getEnergy() >= 75){
					List<Unit> matrix_targets = null;
					matrix_targets = MapGrid.Instance().getUnitsNear(sVessel.getPosition(), sVesselCheckRadius+300, false, true, null);
					
					for (Unit target : matrix_targets) {
						if(target.getType() == UnitType.Zerg_Mutalisk || target.getHitPoints() > target.getType().maxHitPoints()*0.9){
							CommandUtil.useTechTarget(sVessel,TechType.Irradiate, target);
							return;
						}
	//					if(target.isUnderAttack() && target.getHitPoints() < target.getType().maxHitPoints()*0.7
	//							&& !target.isDefenseMatrixed() && target.getHitPoints() > target.getType().maxHitPoints()*0.15 ){
	//						CommandUtil.useTechTarget(sVessel,TechType.Defensive_Matrix, target);
	//						return;
	//					}
					}
				}
			}
			//싸베 디펜시브 매트릭스 쓰기
			else if(sVessel.getEnergy() >= 100){
				List<Unit> matrix_targets = null;
				matrix_targets = MapGrid.Instance().getUnitsNear(sVessel.getPosition(), sVesselCheckRadius+300, true, false, null);
				
				for (Unit target : matrix_targets) {
					if(target.getType() == UnitType.Terran_SCV || target.getType() == UnitType.Terran_Vulture_Spider_Mine ){
						continue;
					}
					if(target.isUnderAttack() && target.getHitPoints() < target.getType().maxHitPoints()*0.7
							&& !target.isDefenseMatrixed() && target.getHitPoints() > target.getType().maxHitPoints()*0.15 ){
						CommandUtil.useTechTarget(sVessel,TechType.Defensive_Matrix, target);
						return;
					}
				}
			}
			
			Unit invisibleEnemyUnit = null;
			int closestDistToVessel = 100000;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
				if (unit.isVisible() && unit.isDetected() && unit.getPosition().isValid()) {
					if(unit.getType() == UnitType.Protoss_Dark_Templar || unit.getType() == UnitType.Zerg_Lurker
							|| unit.getType() == UnitType.Terran_Wraith || unit.getType() == UnitType.Terran_Ghost || unit.getType() == UnitType.Protoss_Arbiter){
						int tempdist = unit.getDistance(sVessel);
												
						if(tempdist < closestDistToVessel){
							invisibleEnemyUnit = unit;
							closestDistToVessel = tempdist;
						}
					}
				}
			}
			
			if(invisibleEnemyUnit == null){
				closestDistToVessel = 100000;
				for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
					if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
						int tempdist = unit.getDistance(sVessel);
						if(tempdist < closestDistToVessel){
							invisibleEnemyUnit = unit;
							closestDistToVessel = tempdist;
						}
					}
				}
			}
			
			if(invisibleEnemyUnit != null){
				List<Unit> nearallies = MapGrid.Instance().getUnitsNear(sVessel.getPosition(), UnitType.Terran_Science_Vessel.sightRange(), true, false, null);
				
				if(nearallies.size() > 2){
					order.setPosition(invisibleEnemyUnit.getPosition());//움직이시오.
					if(invisibleEnemyUnit.getDistance(sVessel) < UnitType.Terran_Science_Vessel.sightRange()*2/3){
						order.setPosition(sVessel.getPosition());
					}
				}
			}

			Unit mostDangerousTarget = null;
			double mostDangercheck = -99999;
			
			List<Unit> dangerous_targets = null;
			dangerous_targets =  MapGrid.Instance().getUnitsNearForAir(sVessel.getPosition(), sVesselCheckRadius, false, true);
			
			
			for (Unit target : dangerous_targets) {
				
				double temp = target.getType().airWeapon().maxRange() - target.getPosition().getDistance(sVessel.getPosition()); 
				if(temp > mostDangercheck){
					mostDangerousTarget = target;
					mostDangercheck = temp;
				}
			}
			
			boolean fleeing = false;
			if(mostDangerousTarget != null){
				double temp = 0;
				if(!mostDangerousTarget.getType().isBuilding()){
					temp = sVesselCheckSpeed * 3;
				}
				//System.out.println("sVessel ID: " + sVessel.getID() + "mostDangerousTarget: "+ mostDangerousTarget.getType());
				if(mostDangerousTarget.isInWeaponRange(sVessel) || (sVessel.getDistance(mostDangerousTarget) 
						<= MyBotModule.Broodwar.enemy().weaponMaxRange(mostDangerousTarget.getType().airWeapon())
						+ sVesselCheckSpeed + temp)
						){
					//System.out.println("flee shown");
					//System.out.println("It's "+ mostDangerousTarget.getType() + " Danger and changing");
					Position fleePosition = getFleePosition(sVessel, mostDangerousTarget, (int) sVesselCheckSpeed, unitedKiting, goalPosition, fleeAngle);
					sVessel.move(fleePosition);
					fleeing = true;
					//System.out.println("sVessel current,  X:" + sVessel.getX() + ", Y:" + sVessel.getY());
					//System.out.println("flee shown unit,  X:" + fleePosition.getX() + ", Y:" + fleePosition.getY());
				}
			}
			
			if(!fleeing){
				if(originalPos.getDistance(sVessel.getPosition()) > 128){
					order.setPosition(originalPos);
				}
				CommandUtil.move(sVessel, order.getPosition());
			} 
		}
	}
	
	public static Position getFleePosition(Unit rangedUnit, Unit target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
		return getFleePosition(rangedUnit, target.getPosition(), moveDistPerSec, unitedKiting, goalPosition, fleeAngle, true);
	}
	
	public static Position getFleePosition(Unit rangedUnit, Position target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
		return getFleePosition(rangedUnit, target, moveDistPerSec, unitedKiting, goalPosition, fleeAngle, false);
	}
	public static Position getFleePosition(Unit rangedUnit, Position target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle, boolean fromUnit) {
		int reverseX = rangedUnit.getPosition().getX() - target.getX(); // 타겟과 반대로 가는 x양
		int reverseY = rangedUnit.getPosition().getY() - target.getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null;
		double minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;

		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		
		while (safePosition == null && moveCalcSize < 1200) {//TODO 이거 회피지역 안나오면 무한 Loop 아닐까??? moveCalcsize 때문에 무한은 안 돌테지만... 사실상 moveCalcsize 는 늘어나야 찾을수 있을텐데
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
				
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				//Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				if(rangedUnit.getID() == 102){
				//System.out.print("fleeVector: " + fleeVector.toString());
				//System.out.print("movePosition: " + movePosition.toString());
				//System.out.print("middlePosition: " + middlePosition.toString());
				}
				
				double risk = 0;
				risk = riskOfFleePositionAir(rangedUnit, movePosition, moveCalcSize, unitedKiting, fromUnit); // 회피지점에서의 예상위험도
				
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리

				if (risk < 100 	&& (risk < minimumRisk || ((risk == minimumRisk) && distanceToGoal < minimumDistanceToGoal))) {
				
					//System.out.print("selected: " + i + "  ");
					safePosition =  movePosition;
					//System.out.println("setting position : " +safePosition.getX() + ","+safePosition.getY());
					minimumRisk = risk;
					minimumDistanceToGoal = distanceToGoal;
				}
				fleeRadianAdjust = MicroUtils.rotate(fleeRadian, FLEE_ANGLE[i]); // 각도변경
		    }
			if (safePosition == null) { // 회피지역이 없을 경우 1) 회피거리 짧게 잡고 다시 조회 
		    	moveCalcSize = moveCalcSize * 2; //TODO 보정 로직이 moveCalcsize 늘려야 하는거 같은데..또한 늘렸을시에 무한 loop 조심
			}
		}
		if (safePosition == null) { // 회피지역이 없을 경우 3) 목표지점으로 간다. 이 경우는 거의 없다.
			safePosition = goalPosition;
		}
		
	    return safePosition;
	}
	/**
	 * 회피지점의 위험도
	 * 
	 * @param unitType
	 * @param position
	 * @param radius
	 * @param united
	 * @return
	 */

	public static double riskOfFleePositionAir(Unit myunit, Position position, int radius, boolean united, boolean fromUnit) {
		double risk = 0;
		
		List<Unit> dangerous_targets = null;
		dangerous_targets = MapGrid.Instance().getUnitsNear(position, sVesselCheckRadius, false, true, UnitType.Terran_Missile_Turret);
		dangerous_targets.addAll(MapGrid.Instance().getUnitsNear(position, sVesselCheckRadius, false, true, UnitType.Terran_Goliath));
			
			for (Unit enemyunits : dangerous_targets) {
				
				double inrange =  MyBotModule.Broodwar.enemy().weaponMaxRange(enemyunits.getType().airWeapon());
				double additionalrange = sVesselCheckSpeed;		
				double distance = enemyunits.getDistance(position);
				if(enemyunits.getType().airWeapon() != WeaponType.None){
					if(distance<inrange){
						risk += 100;
					}else{
						risk = (additionalrange - (distance - inrange))/additionalrange * 100;
					}
				}
			}
		return risk;
	}
}
