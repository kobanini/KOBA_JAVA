
import java.util.Set;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/// �ǹ� �Ǽ� Construction Task �ڷᱸ��
public class ConstructionTask {

	/// �ǹ� �Ǽ� Construction Task �� ���� ����
	public enum ConstructionStatus { 
		Unassigned,				///< Construction �ϲ��� ������ �Ǿ��ִ� ����
		Assigned,				///< Construction �ϲ��� ���� �Ǿ�����, Construction �ϲ��� �Ǽ��� ���������� ���� ����
		UnderConstruction		///< Construction �ϲ��� ���� �Ǿ� �Ǽ� �۾��� �ϰ��ִ� ����
	};
	
	/// �ǹ��� Ÿ��
	private UnitType type;
	
	/// �ǹ��� �������� ��ȹ�� ��ġ<br>
	/// �ϲ��� �ǹ��� ������ ���� ���� �ش� ��ġ�� ��ֹ��� �ְ� �Ǵµ� ������ ����� �� ��ġ�� �߽����� �ٽ� �ǹ� ���� ��ġ�� Ž���ؼ� ���մϴ�
	private TilePosition desiredPosition;
	
	/// �ǹ��� ������ �Ǽ��Ǵ� ��ġ
	private TilePosition finalPosition;
	
	/// �ǹ� �Ǽ� Construction Task �� ���� ����
	private int status;
	
	/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ����
	private Unit constructionWorker;
	
	/// �ش� �ǹ��� �Ǽ� Construction �� �����ϴ� ����<br>
	/// buildingUnit ���� ó���� nullptr �� ���õǰ�, construction �� ���۵Ǿ� isBeingConstructed, underConstrunction ���°� �Ǿ�� ��μ� ���� ä������
	private Unit buildingUnit;

	/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ���ֿ��� build ����� �����Ͽ����� ����.<br>
	/// �ѹ��� �Ȱ��� Ÿ�Ͽ��� build ����� ���� �� �����Ƿ�, �ϴ� buildCommandGiven = false �� ���·� �ϲ��� �ش� Ÿ�� ��ġ�� �̵���Ų ��,<br> 
	/// �ϲ��� �ش� Ÿ�� ��ġ ��ó�� ���� buildCommand ���ø� �մϴ�
	private boolean buildCommandGiven;
	
	/// �ش� �ǹ��� �Ǽ� Construction Task �� ���� �ϲ� ���ֿ��� build ����� ������ �ð�
	private int lastBuildCommandGivenFrame;
	
	/// �ش� �ǹ��� �Ǽ� Construction Task �� �ֱٿ� �޾Ҵ� �ϲ� ������ ID<br>
	/// �ϲ� ������ Construction Task �� �޾����� ���� ������ ���ϴ� ������ ���, ���Ӱ� �ϲ� ������ �����ؼ� Construction Task �� �ο��ϴµ�,<br> 
	/// �Ź� �Ȱ��� �ϲ� ������ Construction Task �� ���� �ʰ� �ϱ� ���ؼ� ����
	private int lastConstructionWorkerID;
	
	/// Construction Task �� �Ǽ� �۾� �����ߴ°� ����
	private boolean underConstruction;

	/// true�ϰ�� �ƹ��͵� ������ �ʰ� �Ǽ� ��ġ�� desiredPosition �״�� ��� ��.
	private boolean forcedType;
	
	public ConstructionTask()
	{
		desiredPosition = TilePosition.None;
		finalPosition = TilePosition.None;
		type = UnitType.Unknown;
		buildingUnit = null;
		constructionWorker = null;
		lastBuildCommandGivenFrame = 0;
		lastConstructionWorkerID = 0;
		status = ConstructionStatus.Unassigned.ordinal();
		buildCommandGiven = false;
		underConstruction = false;
		forcedType = false;
	} 

	public ConstructionTask(UnitType t, TilePosition desiredPosition, boolean forcedType)
	{
		this.desiredPosition = desiredPosition;
		finalPosition = TilePosition.None;
		type = t;
		buildingUnit = null;
		constructionWorker = null;
		lastBuildCommandGivenFrame = 0;
		lastConstructionWorkerID = 0;
		status = ConstructionStatus.Unassigned.ordinal();
		buildCommandGiven = false;
		underConstruction = false;
		this.forcedType = forcedType;
	}

	public UnitType getType() {
		return type;
	}

	public Unit getConstructionWorker() {
		return constructionWorker;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public TilePosition getDesiredPosition() {
		return desiredPosition;
	}

	public TilePosition getFinalPosition() {
		return finalPosition;
	}

	public Unit getBuildingUnit() {
		return buildingUnit;
	}

	public int getLastConstructionWorkerID() {
		return lastConstructionWorkerID;
	}

	public void setFinalPosition(TilePosition finalPosition) {
		this.finalPosition = finalPosition;
	}

	public void setConstructionWorker(Unit constructionWorker) {
		this.constructionWorker = constructionWorker;
	}

	public void setLastConstructionWorkerID(int lastConstructionWorkerID) {
		this.lastConstructionWorkerID = lastConstructionWorkerID;
	}

	public void setBuildCommandGiven(boolean buildCommandGiven) {
		this.buildCommandGiven = buildCommandGiven;
	}

	public int getLastBuildCommandGivenFrame() {
		return lastBuildCommandGivenFrame;
	}

	public void setLastBuildCommandGivenFrame(int lastBuildCommandGivenFrame) {
		this.lastBuildCommandGivenFrame = lastBuildCommandGivenFrame;
	}

	public boolean isUnderConstruction() {
		return underConstruction;
	}

	public void setUnderConstruction(boolean underConstruction) {
		this.underConstruction = underConstruction;
	}
	
	public boolean isForcedType() {
		return forcedType;
	}

	public void setForcedType(boolean forcedType) {
		this.forcedType = forcedType;
	}

	public boolean isBuildCommandGiven() {
		return buildCommandGiven;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public void setDesiredPosition(TilePosition desiredPosition) {
		this.desiredPosition = desiredPosition;
	}

	public void setBuildingUnit(Unit buildingUnit) {
		this.buildingUnit = buildingUnit;
	}
		
	/// equals override  
	@Override
	public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConstructionTask)) return false;
	
    	/// buildings are equal if their worker unit or building unit are equal		
		ConstructionTask that = (ConstructionTask)obj;
		if (this.type != null) {
			if (this.type.equals(that.type)) {
				
				if (this.desiredPosition != null) {
					if (this.desiredPosition.equals(that.desiredPosition)) {

						if (this.buildingUnit != null) {
							if (this.buildingUnit.equals(that.buildingUnit)) {
								return true;
							}
						}
						else {
							if (that.buildingUnit == null) {
								return true;
							}
						}
						
						if (this.constructionWorker != null) {
							if (this.constructionWorker.equals(that.constructionWorker)) {
								return true;
							}
						}
						else {
							if (that.constructionWorker == null) {
								return true;
							}
						}
						
					}
				}
				
			}
		}
		
		return false;
	}
}