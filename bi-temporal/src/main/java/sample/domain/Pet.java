package sample.domain;
import java.sql.Timestamp;
public class Pet extends PetAbstract
{
	public Pet(Timestamp businessDate
	, Timestamp processingDate
	)
	{
		super(businessDate
		,processingDate
		);
		// You must not modify this constructor. Mithra calls this internally.
		// You can call this constructor. You can also add new constructors.
	}

	public Pet(Timestamp businessDate)
	{
		super(businessDate);
	}

	public Pet(String name, int age, int petTypeId, Timestamp businessDate)
	{
		super(businessDate);
		this.setName(name);
		this.setAge(age);
		this.setPetTypeId(petTypeId);
	}
}
