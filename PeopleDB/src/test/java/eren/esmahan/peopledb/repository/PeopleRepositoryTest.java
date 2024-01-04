package eren.esmahan.peopledb.repository;

import eren.esmahan.peopledb.model.Address;
import eren.esmahan.peopledb.model.People;
import eren.esmahan.peopledb.model.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTest {


    private Connection connection;
    private PeopleRepository repo;


    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:C://Users//Esmahan EREN//Downloads//peopletest");
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }


    // think like;
    // try { // open connection } catch(Exception e) { handle exception } finally { cleanup or recover}


    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void canSaveOnePeople() throws SQLException {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        People savedPerson = repo.save(john);
        assertThat(savedPerson.getId()).isGreaterThan(0);

    }


    @Test
    public void canSaveTwoPeople() {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        People jane = new People("Jane", "Smith", ZonedDateTime.of(2001, 2, 1, 1, 5, 10, 13, ZoneId.of("-6")));
        People savedPerson1 = repo.save(john);
        People savedPerson2 = repo.save(jane);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());

    }

    @Test
    public void canSaveWithHomeAddress() throws SQLException {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        Address address = new Address(null,"123 Baele St.","Apt. 1A","Wala Wala", "WA","90210","United States"," Fulton County", Region.WEST);
        john.setHomeAddress(address);

        People savedPerson = repo.save(john);
        assertThat(savedPerson.getHomeAddress().get().id()).isGreaterThan(0);

    }
    @Test
    public void canSaveWithBizAddress() throws SQLException {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        Address address = new Address(null,"123 Baele St.","Apt. 1A","Wala Wala", "WA","90210","United States"," Fulton County", Region.WEST);
        john.setBusinessAddress(address);

        People savedPerson = repo.save(john);
        assertThat(savedPerson.getBusinessAddress().get().id()).isGreaterThan(0);

    }



    @Test
    public void canFindPersonByIdWithHomeAddress() throws SQLException {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        Address address = new Address(null,"123 Baele St.","Apt. 1A","Wala Wala", "WA","90210","United States"," Fulton County", Region.WEST);
        john.setHomeAddress(address);

        People savedPerson = repo.save(john);
        People foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getHomeAddress().get().state()).isEqualTo("WA");


    }
    @Test
    public void canFindPersonByIdWithBusinessAddress() throws SQLException {
        People john = new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6")));
        Address address = new Address(null,"123 Baele St.","Apt. 1A","Wala Wala", "WA","90210","United States"," Fulton County", Region.WEST);
        john.setBusinessAddress(address);

        People savedPerson = repo.save(john);
        People foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getBusinessAddress().get().state()).isEqualTo("WA");


    }



    @Test
    public void canFindPersonById() {
        People savedPerson = repo.save(new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        People foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson).isEqualTo(savedPerson);
    }



    @Test
    @Disabled
    public void canFindAll() {
        repo.save(new People("John1", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John2", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John3", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John4", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John5", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John6", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        repo.save(new People("John7", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        List<People> people = repo.findAll();
        assertThat(people.size()).isGreaterThanOrEqualTo(10);


    }

    @Test
    public void testPersonIdNotFound() {
        Optional<People> foundPerson = repo.findById(-2L);
        assertThat(foundPerson).isEmpty();

    }


    @Test
    public void canDelete() {
        People savedPerson = repo.save(new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 1);

    }

    @Test
    public void canDeleteMultiplePeople() {
        People p1 = repo.save(new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        People p2 = repo.save(new People("Jane", "Smith", ZonedDateTime.of(2001, 2, 1, 1, 5, 10, 13, ZoneId.of("-6"))));
        long startCount = repo.count();
        repo.delete(p1, p2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 2);

    }

    @Test
    public void canUpdate() {
        People savedPerson = repo.save(new People("John", "Smith", ZonedDateTime.of(2000, 12, 12, 11, 50, 20, 13, ZoneId.of("-6"))));
        People p1 = repo.findById(savedPerson.getId()).get();

        savedPerson.setSalary(new BigDecimal(13000));
        repo.update(savedPerson);
        People p2 = repo.findById(savedPerson.getId()).get();

        assertThat(p1.getSalary()).isNotEqualTo(p2.getSalary());
    }

    @Test
    @Disabled
    public void loadData() throws IOException, SQLException {
        Files.lines(Path.of("C://Users//Esmahan EREN//Downloads//Hr5m.csv"))
                .skip(1)
                .map(l -> l.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH));
                    LocalDateTime dtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    People person = new People(a[2], a[4], zdtob);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;

                } )
                .forEach(repo::save);
        connection.commit();

    }
    }


