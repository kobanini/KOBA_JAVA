

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class MicroWraith extends MicroManager {

	// 유닛별 실행시 orderMap에서 unitId를 key로 하여 각 unit별 order를 사용한다.
//	private static Map<Integer, SquadOrder> orderMap = new HashMap<>();
//	
//	// CombatManager.updateVesselSquad에서 명령을 지정한다. by insaneojw
//	public static void setUnitOrder(Integer unitId, SquadOrder unitOrder) {
//		orderMap.put(unitId, unitOrder);
//	}
//	public static void getUnitOrder(Integer unitId) {
//		orderMap.get(unitId);
//	}
//	
//	public static void removeInvalidUnitOrder() {
//		List<Integer> removeKeys = new ArrayList<>();
//		for (Integer unitId : orderMap.keySet()) {
//			if (!CommandUtil.IsValidUnit(MyBotModule.Broodwar.getUnit(unitId))) {
//				removeKeys.add(unitId);
//			}
//		}
//		for (Integer unitId : removeKeys) {
//			orderMap.remove(unitId);
//		}
//	}
	
	private final static double wraithCheckSpeed = UnitType.Terran_Wraith.topSpeed()*8;
	private final static int wraithCheckRadius = UnitType.Terran_Wraith.sightRange()+50;
//	private int attackFrame = 0;

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> wraiths = getUnits();
		List<Unit> wraithTargets = MicroUtils.filterTargets(targets, true);
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setCooltimeAlwaysAttack(false);
		kitingOption.setUnitedKiting(false);
		kitingOption.setGoalPosition(order.getPosition());
		kitingOption.setFleeAngle(MicroSet.FleeAngle.WIDE_ANGLE);
		
		boolean unitedKiting = kitingOption.isUnitedKiting();
		Position goalPosition = kitingOption.getGoalPosition();
		Integer[] fleeAngle = kitingOption.getFleeAngle();
				
		for (Unit wraith : wraiths) {
			if (!CommonUtils.executeUnitRotation(wraith, LagObserver.groupsize())) {
				continue;
			}

//			if (order.getType() == SquadOrderType.ATTACK && inUnityThereIsStrength(wraith)) {
//				continue;
//			}
			//wraith.
			
			/**
			 * 
			 * rangedUnit은 target으로부터 회피한다.
			 * 
			 * @param rangedUnit
			 * @param target
			 * @param moveDistPerSec : 초당 이동거리(pixel per 24frame). 해당 pixel만큼의 거리를 회피지점으로 선정한다. 찾지 못했을 경우 거리를 좁힌다.
			 * @param unitedKiting
			 * @param goalPosition
			 * @return
			 */
			//private static Position getFleePosition(Unit rangedUnit, Unit dangerous_target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
				
			Unit mostDangerousTarget = null;
			double mostDangercheck = -99999;
			UnitInfo mostDangerousBuildingTarget = null;
			double mostDangerBuildingcheck = -99999;
			
			List<Unit> dangerous_targets = null;
			dangerous_targets = MapGrid.Instance().getUnitsNear(wraith.getPosition(), wraithCheckRadius, false, true, UnitType.Terran_Missile_Turret);
			dangerous_targets.addAll(MapGrid.Instance().getUnitsNear(wraith.getPosition(), wraithCheckRadius, false, true, UnitType.Terran_Goliath));
			
			List<UnitInfo> dangerousBuildingTargets = null;
			dangerousBuildingTargets = InformationManager.Instance().getEnemyBuildingUnitsNear(wraith, wraithCheckRadius, true, false, true);
//			MapGrid.Instance().getUnitsNearForAir(sVessel.getPosition(), sVesselCheckRadius, false, true);
			if(dangerousBuildingTargets.size() > 0){
				for (UnitInfo target : dangerousBuildingTargets) {
					
					if(target.getType() != UnitType.Terran_Missile_Turret){
						continue;
					}
					
					double temp = target.getType().airWeapon().maxRange() - target.getLastPosition().getDistance(wraith.getPosition()); 
					if(temp > mostDangerBuildingcheck){
						mostDangerousBuildingTarget = target;
						mostDangerBuildingcheck = temp;
					}
				}
			}
			for (Unit target : dangerous_targets) {
//					boolean pass = false;
//					if(dangerousBuildingTargets != null){
//						for (UnitInfo foggedtarget : dangerousBuildingTargets) {
//							if(foggedtarget.getUnitID() == target.getID()){
//								pass = true;
//							}
//						}
//					}
//					if(pass){
//						continue;
//					}
				double temp = target.getType().airWeapon().maxRange() - target.getPosition().getDistance(wraith.getPosition()); 
				if(temp > mostDangercheck){
					mostDangerousTarget = target;
					mostDangercheck = temp;
				}
			}
			
//			double adjustment = 0;
			
//			if(mostDangerousBuildingTarget != null && mostDangerousTarget !=null){			
//				if(mostDangercheck < mostDangerBuildingcheck){
//					double DimT = mostDangerousBuildingTarget.getType().dimensionUp();
//					double DimB = mostDangerousBuildingTarget.getType().dimensionDown();
//					double DimL = mostDangerousBuildingTarget.getType().dimensionLeft();
//					double DimR = mostDangerousBuildingTarget.getType().dimensionRight();
//				
//					if(DimT < DimB){
//						DimT = DimB;
//					}
//					if(DimL < DimR){
//						DimL = DimR;
//					}
//					adjustment = Math.sqrt(DimT * DimT + DimL * DimL);
//					mostDangerousTarget = null;
//				}else{
//					mostDangerousBuildingTarget = null;
//					//mostDangerBuildingcheck = mostDangerousTarget;
//				}
//			}
			
			boolean fleeing = false;
			if(mostDangerousBuildingTarget != null){
				
				//MapGrid.Instance().getUnitsNearForAir(sVessel.getPosition(), sVesselCheckRadius, false, true);
//				double eee =  ((double)MyBotModule.Broodwar.enemy().weaponMaxRange(mostDangerousBuildingTarget.getType().airWeapon())
//						+ (double)wraithCheckSpeed +  (double)adjustment);
//				System.out.println("dis : " + eee);
//				System.out.println("Wraith ID: " + wraith.getID() + "In dangerous fogged "+ mostDangerousBuildingTarget.getType());
//				if(wraith.getDistance(mostDangerousBuildingTarget.getLastPosition()) 
//						<= MyBotModule.Broodwar.enemy().weaponMaxRange(mostDangerousBuildingTarget.getType().airWeapon())
//						+ wraithCheckSpeed +  adjustment)	{
				if(wraith.getDistance(mostDangerousBuildingTarget.getLastPosition()) > 350){
					Position fleePosition = getFleePosition(wraith, mostDangerousBuildingTarget.getLastPosition(), (int) wraithCheckSpeed, unitedKiting, goalPosition, fleeAngle);
					wraith.move(fleePosition);
					fleeing = true;
//					System.out.println("wraith current,  X:" + wraith.getX() + ", Y:" + wraith.getY());
//					System.out.println("flee fogged building,  X:" + fleePosition.getX() + ", Y:" + fleePosition.getY());
				}
			}
			
				
			if(fleeing == false){
				if(mostDangerousTarget != null){
//					double temp = 0;
//					if(mostDangerousTarget.getType() != UnitType.Terran_Missile_Turret){
//						temp = wraithCheckSpeed * 3;
//					}
//					
//					double eee =  (double) MyBotModule.Broodwar.enemy().weaponMaxRange(mostDangerousTarget.getType().airWeapon())
//							+ (double)wraithCheckSpeed + (double)temp;
//					System.out.println("dis2 : " + eee);
	//				System.out.println("Wraith ID: " + wraith.getID() + "mostDangerousTarget: "+ mostDangerousTarget.getType());
//					if(mostDangerousTarget.isInWeaponRange(wraith) || (wraith.getDistance(mostDangerousTarget) 
//							<= MyBotModule.Broodwar.enemy().weaponMaxRange(mostDangerousTarget.getType().airWeapon())
//							+ wraithCheckSpeed + temp)
//							){
//					if(mostDangerousTarget.getType() != UnitType.Terran_Missile_Turret || mostDangerousTarget.getType() != UnitType.Terran_Goliath){
						Position fleePosition = getFleePosition(wraith, mostDangerousTarget, (int) wraithCheckSpeed, unitedKiting, goalPosition, fleeAngle);
						wraith.move(fleePosition);
						fleeing = true;
//					}
				}
			}
			
			if(!fleeing){
				Unit target = getTarget(wraith, wraithTargets);
				if (target != null && !fleeing) {
					if (target.canAttack(wraith)) {
						System.out.println("attacking target: " + target.getType());
						MicroUtils.preciseKiting(wraith, wraith, kitingOption);
					} else {
						CommandUtil.attackUnit(wraith, target);
					}
//					
				}else {
					// if we're not near the order position, go there
					if (wraith.getDistance(order.getPosition()) > order.getRadius()) {
						CommandUtil.attackMove(wraith, order.getPosition());
					} else {
						if (wraith.isIdle()) {
							//Position randomPosition = MicroUtils.randomPosition(wraith.getPosition(), squadRange);
							CommandUtil.attackMove(wraith, order.getPosition());
						}
					}
				}
			} 
		}
		
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			if(target.getType() == UnitType.Terran_Missile_Turret || target.getType() == UnitType.Terran_Goliath || target.getType() == UnitType.Terran_Bunker){
				continue;
			}
			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			
			int distanceScore = 0; // 거리 점수
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
			
	        int totalScore = 0;
        	totalScore =  priorityScore + distanceScore;
	        
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
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

//		Integer[] FLEE_ANGLE = MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType()); // MicroData.FleeAngle에 저장된 유닛타입에 따른 회피 각 범위(골리앗 새끼들은 뚱뚱해서 각이 넓으면 지들끼리 낑김)
		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		
		while (safePosition == null && moveCalcSize < 1200) {//TODO 이거 회피지역 안나오면 무한 Loop 아닐까??? moveCalcsize 때문에 무한은 안 돌테지만... 사실상 moveCalcsize 는 늘어나야 찾을수 있을텐데
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
				
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				double risk = 0;
				risk = riskOfFleePositionAir(rangedUnit, movePosition, moveCalcSize, unitedKiting, fromUnit); // 회피지점에서의 예상위험도
				
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리
				// 회피지점은 유효하고, 걸어다닐 수 있어야 하고, 안전해야 하고 등등
				// 변경사항. 기존과 같이 전체를 확인하다가 만족스러운 곳이 있으면 회피 지점이 아닌 전체를 모두 확인하고 가장 좋은 자리를 가지고 간다.
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

	public static double riskOfFleePositionAir(Unit myunit, Position position, int radius, boolean united, boolean fromUnit) {
		double risk = 0;
		
		List<UnitInfo> dangerousBuildingTargets = null;
		dangerousBuildingTargets = InformationManager.Instance().getEnemyBuildingUnitsNear(position, wraithCheckRadius, true, false, true);
		
		List<Unit> dangerous_targets = null;
		dangerous_targets = MapGrid.Instance().getUnitsNear(position, wraithCheckRadius, false, true, UnitType.Terran_Missile_Turret);
		dangerous_targets.addAll(MapGrid.Instance().getUnitsNear(position, wraithCheckRadius, false, true, UnitType.Terran_Goliath));
			
		if(!fromUnit){
			for (UnitInfo enemyunits : dangerousBuildingTargets) {
				double adjustment =0;
				double DimT = enemyunits.getType().dimensionUp();
				double DimB = enemyunits.getType().dimensionDown();
				double DimL = enemyunits.getType().dimensionLeft();
				double DimR = enemyunits.getType().dimensionRight();
			
				if(DimT < DimB){
					DimT = DimB;
				}
				if(DimL < DimR){
					DimL = DimR;
				}
				adjustment = Math.sqrt(DimT * DimT + DimL * DimL);
				double inrange =  MyBotModule.Broodwar.enemy().weaponMaxRange(enemyunits.getType().airWeapon()) + adjustment;
				double additionalrange = wraithCheckSpeed;						
				double distance = position.getDistance(enemyunits.getLastPosition());
				
						
				if(enemyunits.getType() == UnitType.Terran_Missile_Turret){
					if(distance<inrange){
						risk += 100;
					}else{
						risk = (additionalrange - (distance - inrange))/additionalrange * 100;
					}
				}
			} 
		}else{
			for (Unit enemyunits : dangerous_targets) {
				
				double inrange =  MyBotModule.Broodwar.enemy().weaponMaxRange(enemyunits.getType().airWeapon());
				double additionalrange = wraithCheckSpeed;		
				double distance = enemyunits.getDistance(position);
				//if(enemyunits.getDistance(unit) <= inrange + additionalrange){
				if(enemyunits.getType() == UnitType.Terran_Missile_Turret
						|| enemyunits.getType() == UnitType.Terran_Goliath
						|| enemyunits.getType() == UnitType.Terran_Marine
						|| enemyunits.getType() == UnitType.Terran_Wraith
						|| enemyunits.getType() == UnitType.Terran_Marine
						|| (enemyunits.getType() == UnitType.Terran_Bunker && enemyunits.isLoaded())
						 ){
					if(distance<inrange){
						risk += 100;
					}else{
						risk = (additionalrange - (distance - inrange))/additionalrange * 100;
					}
				}
			}
		}
		return risk;
	}
}
