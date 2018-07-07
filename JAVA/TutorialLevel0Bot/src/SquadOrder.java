

import bwapi.Position;
import bwapi.Unit;

public class SquadOrder {
	
	private SquadOrderType type;
	private Position position;
	private int radius;
	private String status;
	private Unit unitleader;
	
	public SquadOrder(SquadOrderType type, Position position, int radius, String status, Unit unitleader) {
		this.type = type;
		this.position = position;
		this.radius = radius;
		this.status = status;
		this.unitleader = unitleader; 
	}
	
	public SquadOrder(SquadOrderType type, Position position, int radius, String status) {
		this.type = type;
		this.position = position;
		this.radius = radius;
		this.status = status;
		this.unitleader = null; 
	}
	
	public SquadOrderType getType() {
		return type;
	}
	public void setType(SquadOrderType type) {
		this.type = type;
	}
	public Unit getUnitLeader() {
		return unitleader;
	}
	public void setUnitLeader(Unit unitleader) {
		this.unitleader = unitleader;
	}
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isCombatOrder() {
		return type != SquadOrderType.NONE && type != SquadOrderType.IDLE;
	}

	@Override
	public String toString() {
		return "SquadOrder [type=" + type + ", position=" + position + ", radius=" + radius + ", status=" + status + "]";
	}
}

enum SquadOrderType {
	NONE, IDLE, ATTACK, DEFEND, HOLD, WATCH, CHECK, GUERILLA
}