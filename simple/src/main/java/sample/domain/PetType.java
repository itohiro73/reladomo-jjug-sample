package sample.domain;
import java.sql.Timestamp;
public class PetType extends PetTypeAbstract
{
	public static final int DOG = 0;
	public static final int CAT = 1;
	public static final int HAMSTER = 2;
	public static final int FROG = 3;

	public PetType()
	{
		super();
		// You must not modify this constructor. Mithra calls this internally.
		// You can call this constructor. You can also add new constructors.
	}
}
