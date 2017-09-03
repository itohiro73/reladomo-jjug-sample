package sample.domain;

import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.finder.Operation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PersonTest
        extends AbstractReladomoTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonTest.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");;

    @Override
    protected String[] getTestDataFilenames()
    {
        return new String[] {"test_data.txt"}; // Look into this file to see the test data being used
    }

    @Test
    public void testBiTemporal()
    {
        Operation findTanakaOp = PersonFinder.lastName().eq("田中")
                .and(PersonFinder.businessDate().eq(parse("2017/01/01")));
        Person tanaka = PersonFinder.findOne(findTanakaOp);

        Assert.assertEquals(0, tanaka.getPets().size());

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            Pet chibi = new Pet(parse("2017/03/01"));
            chibi.setName("チビ");
            chibi.setAge(3);
            chibi.setPetTypeId(PetType.DOG);
            chibi.setOwner(tanaka);
            return chibi;
        });

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            Pet sakura = new Pet(parse("2017/05/01"));
            sakura.setName("サクラ");
            sakura.setAge(5);
            sakura.setPetTypeId(PetType.CAT);
            sakura.setOwner(tanaka);
            return sakura;
        });

        Operation findTanakaAsOf20170201 =
                PersonFinder.lastName().eq("田中")
                .and(PersonFinder.businessDate()
                        .eq(parse("2017/02/01")));
        Person tanakaAsOf20170201 =
                PersonFinder.findOne(findTanakaAsOf20170201);

        System.out.println(tanakaAsOf20170201.getPets().size()); //0

        Operation findTanakaAsOf20170302 =
                PersonFinder.lastName().eq("田中")
                .and(PersonFinder.businessDate()
                        .eq(parse("2017/03/02")));
        Person tanakaAsOf20170302 =
                PersonFinder.findOne(findTanakaAsOf20170302);

        System.out.println(
                tanakaAsOf20170302
                        .getPets()
                        .asGscList()
                        .collect(Pet::getName)); //[チビ]

        Operation findTanakaAsOf20170502 =
                PersonFinder.lastName().eq("田中")
                .and(PersonFinder.businessDate()
                        .eq(parse("2017/05/02")));
        Person tanakaAsOf20170502 =
                PersonFinder.findOne(findTanakaAsOf20170502);

        System.out.println(
                tanakaAsOf20170502
                        .getPets()
                        .asGscList()
                        .collect(Pet::getName)); //[チビ, サクラ]
    }

    private Timestamp parse(String dateTimeString)
    {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString+ " 00:00:00", DATE_TIME_FORMATTER);
        return Timestamp.valueOf(localDateTime);
    }
}
