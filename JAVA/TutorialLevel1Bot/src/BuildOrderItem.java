
import java.util.Set;

import bwapi.Color;
import bwapi.TilePosition;
import bwapi.UnitType;

/// ���� ���
public class BuildOrderItem {

	public MetaType metaType;			///< the thing we want to 'build'
	public int priority;				///< the priority at which to place it in the queue
	public boolean blocking;			///< whether or not we block further items
	public TilePosition seedLocation; 	///< �Ǽ� ��ġ
	public int producerID;				///< producer unitID (�ǹ�ID, ����ID)
	public boolean forcedType;			///< true�̸� seedLocation�� �����Ѵ�� �����Ǽ�
	
	/// �Ǽ���ġ �ʾ� ���� ��å
	/// ���� ���� ���, ��� �� �� �߰�
	public enum SeedPositionStrategy { 
		MainBaseLocation,			///< �Ʊ� ���̽�
		MainBaseBackYard,			///< �Ʊ� ���̽� ����
		FirstChokePoint,			///< �Ʊ� ù��° ���
		FirstExpansionLocation,		///< �Ʊ� ù��° �ո���
		SecondChokePoint,			///< �Ʊ� �ι�° ���
		SeedPositionSpecified,		///< ���� ���� ��ġ
		NextExpansionPoint,    		///< ���� ��Ƽ ��ġ
		NextSupplePoint,    		///< ���� ���� ��ġ
		LastBuilingPoint,			///< ���� �ǹ� ��ġ
		getLastBuilingFinalLocation ///< ���� ���̻� ����
	};
	
	public SeedPositionStrategy seedLocationStrategy;	///< �Ǽ���ġ �ʾ� ���� ��å
	
	/// �Ǽ� ��ġ�� SeedPositionStrategy::MainBaseLocation �� ���� ã�´�
	/// @param metaType : ���� ��� Ÿ��
	/// @param priority : 0 = ���� ���� �켱����. ���ڰ� Ŭ���� �� ���� �켱����. ť�� �ִ� �����۵� �߿��� ���� ���� �켱������ ������ (�켱������ ������ ���� ť�� ���� ��)�� ���� ���� �����. 
	/// @param blocking : true = ���� �̰��� ������ �� ������, �̰� ���� �������������� ��ٸ�.  false = ���� �̰��� ������ �� �� ������ ������ ���� ���� ����.
	/// @param producerID : producerID �� �����ϸ� �ش� unit �� ���带 �����ϰ� ��
	public BuildOrderItem(MetaType metaType, int priority, boolean blocking, int producerID)
	{
		this.metaType = metaType;
		this.priority = priority;
		this.blocking = blocking;
		this.producerID = producerID;
		this.seedLocation = TilePosition.None;
		this.seedLocationStrategy = SeedPositionStrategy.MainBaseLocation;
		this.forcedType = false;
	}
	
	public BuildOrderItem(MetaType metaType, int priority, boolean blocking)
	{
		this(metaType, priority, blocking, -1);
	}

	public BuildOrderItem(MetaType metaType, int priority){
		this(metaType, priority, true, -1);
	}

	public BuildOrderItem(MetaType metaType){
		this(metaType, 0, true, -1);
	}

	/// �Ǽ� ��ġ�� seedLocation �������� ã�´�
	public BuildOrderItem(MetaType metaType, TilePosition seedLocation, int priority, boolean blocking, int producerID)
	{
		this.metaType = metaType;
		this.priority = priority;
		this.blocking = blocking;
		this.producerID = producerID;
		this.seedLocation = seedLocation;
		this.seedLocationStrategy = SeedPositionStrategy.SeedPositionSpecified;
		this.forcedType = false;
	}

	public BuildOrderItem(MetaType metaType, TilePosition seedLocation, int priority, boolean blocking, int producerID, boolean forcedType)
	{
		this.metaType = metaType;
		this.priority = priority;
		this.blocking = blocking;
		this.producerID = producerID;
		this.seedLocation = seedLocation;
		this.seedLocationStrategy = SeedPositionStrategy.SeedPositionSpecified;
		this.forcedType = forcedType;
	}
	
	public BuildOrderItem(MetaType metaType, TilePosition seedLocation){
		this(metaType, seedLocation, 0, true, -1);
	}

	public BuildOrderItem(MetaType metaType, TilePosition seedLocation, int priority){
		this(metaType, seedLocation, priority, true, -1);
	}

	/// �Ǽ� ��ġ�� seedPositionStrategy �� �̿��ؼ� ã�´�. ��ã�� ���, �ٸ� SeedPositionStrategy �� ��� ã�´�
	public BuildOrderItem(MetaType metaType, SeedPositionStrategy seedPositionStrategy, int priority, boolean blocking, int producerID)
	{
		this.metaType = metaType;
		this.priority = priority;
		this.blocking = blocking;
		this.producerID = producerID;
		this.seedLocation = TilePosition.None;
		this.seedLocationStrategy = seedPositionStrategy;
		this.forcedType = false;
	}
	
	public BuildOrderItem(MetaType metaType, SeedPositionStrategy seedPositionStrategy) {
		this(metaType, seedPositionStrategy, 0, true, -1);
	}

	public BuildOrderItem(MetaType metaType, SeedPositionStrategy seedPositionStrategy, int priority) {
		this(metaType, seedPositionStrategy, priority, true, -1);
	}
	
	/// equals override  
	@Override
	public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BuildOrderItem)) return false;
	
        BuildOrderItem that = (BuildOrderItem)obj;
		if (this.metaType != null && that.metaType != null) {
			if (this.metaType.equals(that)) 
			{
				if (this.priority == that.priority 
						&& this.blocking == that.blocking
						&& this.producerID == that.producerID) 
				{					
					if (this.seedLocation != null) {
						if (this.seedLocation.equals(that.seedLocation)){
							return true;
						}
					}
					else {
						if (that.seedLocation == null){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

}