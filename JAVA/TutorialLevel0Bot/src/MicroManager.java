

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;

public abstract class MicroManager {
	private List<Unit> units = new ArrayList<>();
	private List<Unit> nearbyEnemies = new ArrayList<>();

	public List<Unit> getUnits() {
		return units;
	}
	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	public List<Unit> getNearbyEnemies() {
		return nearbyEnemies;
	}
	public void setNearbyEnemies(List<Unit> nearbyEnemies) {
		this.nearbyEnemies = nearbyEnemies;
	}

	protected SquadOrder order;
	protected Position squadCenter = null;
	protected int tankSize = 0;
	
	
	protected abstract void executeMicro(List<Unit> targets);
	
	public void setMicroInformation(SquadOrder inputOrder, List<Unit> nearbyEnemies) {
		order = inputOrder;
		if (units.isEmpty() || !order.isCombatOrder()) {
			return;
		}
		
		this.nearbyEnemies = nearbyEnemies;
	}
	
	public void execute() {
		if (units.isEmpty() || !order.isCombatOrder()) {
			return;
		}

		executeMicro(nearbyEnemies);
	}
	
	public void regroup(Position regroupPosition) {
//	    Position ourBasePosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
//	    int regroupDistanceFromBase = MapTools.Instance().getGroundDistance(regroupPosition, ourBasePosition);

		for (Unit unit : units) {
//	        int unitDistanceFromBase = MapTools.Instance().getGroundDistance(unit.getPosition(), ourBasePosition);

			// A unit in stay-home mode should stay home, not "regroup" away from home.
			// A unit whose retreat path is blocked by enemies should do something else, at least attack-move.
//			if (unitDistanceFromBase > regroupDistanceFromBase) {
//	            UnitUtils.move(unit, ourBasePosition); } else
//	        if (unit.getDistance(regroupPosition) > 96) {
//				CommandUtil.move(unit, regroupPosition);
//			} else {
//				CommandUtil.attackMove(unit, unit.getPosition());
//			}
	        CommandUtil.attackMove(unit, squadCenter);
		}
	}
	
	public boolean inTheSquad(Unit unit) { // TODO
		return true;
	}

	@Override
	public String toString() {
		return "MicroManager [units.size()=" + units.size() + ", nearbyEnemies=" + nearbyEnemies + ", order=" + order
				+ ", squadCenter=" + squadCenter + ", tankSize=" + tankSize + "]";
	}
	
}
