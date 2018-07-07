
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/// �ش� Unit�� ID, UnitType, �Ҽ� Player, HitPoint, lastPosition, completed(�ǹ��� �ϼ��� ������) ���� �����صδ� �ڷᱸ��<br>
/// ���� ������ ��� �Ʊ� �þ� ���� ���� �ʾ� invisible ���°� �Ǿ��� �� ������ ��ȸ�Ҽ��� �������� �ľ��ߴ� ������ ���ǵǱ� ������ ���� �ڷᱸ���� �ʿ��մϴ�
public class UnitInfo {

	private int unitID;
	private int lastHealth;
	private int lastShields;
	private Player player;
	private Unit unit;
	private Position lastPosition;
	private UnitType type;
	private boolean completed;
	private int updateFrame;
	private int remainingBuildTime;

	public UnitInfo()
	{
		unitID = 0;
		lastHealth = 0;
		player = null;
		unit = null;
		lastPosition = Position.None;
		type = UnitType.None;
		completed = false;
		updateFrame = 0;
	}

	public UnitType getType() {
		return type;
	}

	public boolean isCompleted() {
		return completed || (remainingBuildTime < MyBotModule.Broodwar.getFrameCount() - updateFrame);
	}

	public Position getLastPosition() {
		return lastPosition;
	}

	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	public int getLastHealth() {
		return lastHealth;
	}

	public void setLastHealth(int lastHealth) {
		this.lastHealth = lastHealth;
	}

	public int getLastShields() {
		return lastShields;
	}

	public void setLastShields(int lastShields) {
		this.lastShields = lastShields;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setLastPosition(Position lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public void setRemainingBuildTime(int remainingBuildTime) {
		this.remainingBuildTime = remainingBuildTime;
	}
	
	public int getUpdateFrame() {
		return updateFrame;
	}

	public void setUpdateFrame(int updateFrame) {
		this.updateFrame = updateFrame;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitInfo)) return false;

        UnitInfo that = (UnitInfo) o;

        if (this.getUnitID() != that.getUnitID()) return false;

        return true;
    }

	@Override
	public String toString() {
		return "UnitInfo [unitID=" + unitID + ", lastHealth=" + lastHealth + ", lastShields=" + lastShields
				+ ", player=" + player + ", unit=" + unit + ", lastPosition=" + lastPosition + ", type=" + type
				+ ", completed=" + completed + ", updateFrame=" + updateFrame + "]";
	}

	
//		const bool operator == (BWAPI::Unit unit) const
//		{
//			return unitID == unit->getID();
//		}
//
//		const bool operator == (const UnitInfo & rhs) const
//		{
//			return (unitID == rhs.unitID);
//		}
//
//		const bool operator < (const UnitInfo & rhs) const
//		{
//			return (unitID < rhs.unitID);
//		}
};