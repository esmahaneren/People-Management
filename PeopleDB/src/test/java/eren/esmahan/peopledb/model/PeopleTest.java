package eren.esmahan.peopledb.model;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PeopleTest {

    @Test
    public void testForEquality()
    {
        People p1 = new People("p1","smith", ZonedDateTime.of(2000,2,2,3,23,15,14, ZoneId.of("+3")));
        People p2 = new People("p1","smith", ZonedDateTime.of(2000,2,2,3,23,15,14, ZoneId.of("+3")));
        assertThat(p1).isEqualTo(p2);

    }

    @Test
    public void testForInequality()
    {
        People p1 = new People("p1","smith", ZonedDateTime.of(2000,2,2,3,23,15,14, ZoneId.of("+3")));
        People p2 = new People("p2","smith", ZonedDateTime.of(2000,2,2,3,23,15,14, ZoneId.of("+3")));
        assertThat(p1).isNotEqualTo(p2);

    }

}