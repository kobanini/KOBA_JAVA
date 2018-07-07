

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;

public class SquadData {
	
	private Map<String, Squad> squads = new HashMap<>();
	
	public void update() {
//		LagTest lag = LagTest.startTest();
//		lag.setDuration(10);
		for (String squadName : squads.keySet()) {
			squads.get(squadName).update();
//			lag.estimate(squadName + ", " + squads.get(squadName).getUnitSet().size());
		}
		
//		if (CommonUtils.executeRotation(0, 168)){
//			System.out.println("[SQUAD INFO]");
//			for (String squadName : squads.keySet()) {
//				Squad sq = squads.get(squadName);
//				if (sq.getUnitSet().size() > 0) {
//					System.out.println(sq + " -> v: " + sq.microVulture.getUnits().size() + ", t: " + sq.microTank.getUnits().size() + ", g: " + sq.microGoliath.getUnits().size() + ", m: " + sq.microMarine.getUnits().size());
//				}
//			}
//		}
	}
	
	public void putSquad(Squad squad) {
		squads.put(squad.getName(), squad);
	}

	public Squad getSquad(String squadName) {
		return squads.get(squadName);
	}

	public Squad removeSquad(String squadName) {
		return squads.remove(squadName);
	}

	public List<Squad> getSquadList(String squadName) {
		List<Squad> squadList = new ArrayList<>();
		for (Squad squad : squads.values()) {
			if (squad.getName().startsWith(squadName)) {
				squadList.add(squad);
			}
		}
		return squadList;
	}

	public Squad getUnitSquad(Unit unit) {
		for (String sqaudName : squads.keySet()) {
			Squad squad = squads.get(sqaudName);
			if (MicroUtils.isUnitContainedInUnitSet(unit, squad.getUnitSet())) {
				return squad;
			}
	    }
		return null;
	}
	
	public boolean canAssignUnitToSquad(Unit unit, Squad squad) {
	    Squad unitSqaud = getUnitSquad(unit);
	    return unitSqaud == null || unitSqaud.getPriority() < squad.getPriority();
	}
	public void assignWorkerToSquad(Unit unit, Squad squad) {
		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
	}
	
	public void assignUnitToSquad(Unit unit, Squad squad) {
		if (!canAssignUnitToSquad(unit, squad)) {
//			MyBotModule.Broodwar.sendText("We shouldn't be re-assigning this unit!");
			return;
		}
		
		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
	}

//	public void printSquadInfo() {
//		for (String squadName : squads.keySet()) {
//			Squad squad = squads.get(squadName);
//			System.out.println("[" + squad.getName() + "] SIZE: " + squad.getUnitSet().size() + "\nORDER: " + squad.getOrder());
//		}
//		System.out.println();
//	}

	@Override
	public String toString() {
		return "SquadData [squads=" + squads + "]";
	}
	
}
